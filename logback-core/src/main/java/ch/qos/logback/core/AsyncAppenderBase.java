/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.util.InterruptUtil;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This appender and derived classes, log events asynchronously.  In order to avoid loss of logging events, this
 * appender should be closed. It is the user's  responsibility to close appenders, typically at the end of the
 * application lifecycle.
 * <p/>
 * This appender buffers events in a {@link BlockingQueue}. {@link Worker} thread created by this appender takes
 * events from the head of the queue, and dispatches them to the single appender attached to this appender.
 * <p/>
 * <p>Please refer to the <a href="http://logback.qos.ch/manual/appenders.html#AsyncAppender">logback manual</a> for
 * further information about this appender.</p>
 *
 * @param <E>
 * @author Ceki G&uuml;lc&uuml;
 * @author Torsten Juergeleit
 * @since 1.0.4
 */
public class AsyncAppenderBase<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();

    /** 默认队列缓存大小 256MB */
    public static final int             DEFAULT_QUEUE_SIZE          = 256;
    /** 默认最大刷盘时间 1S    */
    public static final int             DEFAULT_MAX_FLUSH_TIME      = 1000;

    // 阻塞队列
    BlockingQueue<E> blockingQueue;
    // 队列长度
    int queueSize = DEFAULT_QUEUE_SIZE;

    // appender计数器
    int appenderCount = 0;

    private static final int            UNDEFINED                   = -1;
    // 丢弃消息阈值, 默认未定义
    private int                         discardingThreshold         = UNDEFINED;
    private boolean                     neverBlock                  = false;

    // 日志拉取线程
    Worker worker = new Worker();

    /**
     * The default maximum queue flush time allowed during appender stop. If the 
     * worker takes longer than this time it will exit, discarding any remaining 
     * items in the queue
     */
    // 默认在appender停止后,默认最大队列刷盘时间. 工作线程超时则退出,并丢弃队列中该消息
    private int                         maxFlushTime                = DEFAULT_MAX_FLUSH_TIME;

    /**
     * Is the eventObject passed as parameter discardable? The base class's implementation of this method always returns
     * 'false' but sub-classes may (and do) override this method.
     * <p/>
     * <p>Note that only if the buffer is nearly full are events discarded. Otherwise, when the buffer is "not full"
     * all events are logged.
     *
     * @param eventObject
     * @return - true if the event can be discarded, false otherwise
     */
    protected boolean isDiscardable(E eventObject) {
        return false;
    }

    /**
     * 日志入队操作前预处理.
     * 基本类不进行预处理,由子类覆写该方法提供具体预处理实现.
     *
     * @param eventObject 事件对象
     */
    protected void preprocess(E eventObject) {
    }

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }
        if (appenderCount == 0) {
            addError("No attached appenders found.");
            return;
        }
        if (queueSize < 1) {
            addError("Invalid queue size [" + queueSize + "]");
            return;
        }
        blockingQueue = new ArrayBlockingQueue<E>(queueSize);

        // 消息丢弃阈值: 未定义时, 默认为队列长度只剩20%时丢弃
        if (discardingThreshold == UNDEFINED) {
            discardingThreshold = queueSize / 5;
        }
        addInfo("Setting discardingThreshold to " + discardingThreshold);

        // 后台日志拉取工作线程
        worker.setDaemon(true);
        worker.setName("AsyncAppender-Worker-" + getName());
        // make sure this instance is marked as "started" before staring the worker Thread
        super.start();
        worker.start();
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        // mark this appender as stopped so that Worker can also processPriorToRemoval if it is invoking
        // aii.appendLoopOnAppenders
        // and sub-appenders consume the interruption
        super.stop();


        // 中断可被子appender恢复
        worker.interrupt();

        InterruptUtil interruptUtil = new InterruptUtil(context);

        try {
            interruptUtil.maskInterruptFlag();

            worker.join(maxFlushTime);

            // check to see if the thread ended and if not add a warning message
            if (worker.isAlive()) {
                addWarn("Max queue flush timeout (" + maxFlushTime + " ms) exceeded. Approximately " + blockingQueue.size()
                                + " queued events were possibly discarded.");
            } else {
                addInfo("Queue flush finished successfully within timeout.");
            }

        } catch (InterruptedException e) {
            int remaining = blockingQueue.size();
            addError("Failed to join worker thread. " + remaining + " queued events may be discarded.", e);
        } finally {
            interruptUtil.unmaskInterruptFlag();
        }
    }





    @Override
    protected void append(E eventObject) {
        if (isQueueBelowDiscardingThreshold() && isDiscardable(eventObject)) {
            return;
        }
        // 预处理事件对象
        preprocess(eventObject);
        // 入队操作,
        put(eventObject);
    }

    private boolean isQueueBelowDiscardingThreshold() {
        return (blockingQueue.remainingCapacity() < discardingThreshold);
    }

    /**
     * 日志进入阻塞队列,操作可能中断.
     */
    private void put(E eventObject) {
        if (neverBlock) {
            blockingQueue.offer(eventObject);
        } else {
            putUninterruptibly(eventObject);
        }
    }

    /**
     * 日志非中断进入阻塞队列
     */
    private void putUninterruptibly(E eventObject) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    blockingQueue.put(eventObject);
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            // 如果入队发生中断异常,则当前日志操作线程发生中断.
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    public int getMaxFlushTime() {
        return maxFlushTime;
    }

    public void setMaxFlushTime(int maxFlushTime) {
        this.maxFlushTime = maxFlushTime;
    }

    /**
     * Returns the number of elements currently in the blocking queue.
     *
     * @return number of elements currently in the queue.
     */
    public int getNumberOfElementsInQueue() {
        return blockingQueue.size();
    }

    public void setNeverBlock(boolean neverBlock) {
        this.neverBlock = neverBlock;
    }

    public boolean isNeverBlock() {
        return neverBlock;
    }

    /**
     * The remaining capacity available in the blocking queue.
     *
     * @return the remaining capacity
     * @see {@link java.util.concurrent.BlockingQueue#remainingCapacity()}
     */
    public int getRemainingCapacity() {
        return blockingQueue.remainingCapacity();
    }

    /**
     * 添加新appender
     */
    @Override
    public void addAppender(Appender<E> newAppender) {
        if (appenderCount == 0) {
            appenderCount++;
            addInfo("Attaching appender named [" + newAppender.getName() + "] to AsyncAppender.");
            aai.addAppender(newAppender);
        } else {
            addWarn("One and only one appender may be attached to AsyncAppender.");
            addWarn("Ignoring additional appender named [" + newAppender.getName() + "]");
        }
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> eAppender) {
        return aai.isAttached(eAppender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> eAppender) {
        return aai.detachAppender(eAppender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }


    /**
     * 日志刷盘工作线程.
     * 从阻塞队列中拉取日志信息,然后异步刷入磁盘.
     */
    class Worker extends Thread {

        @Override
        public void run() {
            AsyncAppenderBase<E> parent = AsyncAppenderBase.this;
            AppenderAttachableImpl<E> aai = parent.aai;

            // loop while the parent is started
            while (parent.isStarted()) {
                try {
                    E e = parent.blockingQueue.take();
                    aai.appendLoopOnAppenders(e);
                } catch (InterruptedException ie) {
                    break;
                }
            }

            addInfo("Worker thread will flush remaining events before exiting. ");

            for (E e : parent.blockingQueue) {
                aai.appendLoopOnAppenders(e);
                parent.blockingQueue.remove(e);
            }

            aai.detachAndStopAllAppenders();
        }
    }
}
