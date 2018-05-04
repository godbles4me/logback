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

import static ch.qos.logback.core.CoreConstants.CODES_URL;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * OutputStreamAppender appends events to a {@link OutputStream}. This class
 * provides basic services that other appenders build upon.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class OutputStreamAppender<E> extends UnsynchronizedAppenderBase<E> {

    /**
     * It is the encoder which is ultimately responsible for writing the event to
     * an {@link OutputStream}.
     */
    protected Encoder<E> encoder;

    /**
     * All synchronization in this class is done via the lock object.
     */
    protected final ReentrantLock lock = new ReentrantLock(false);

    /** IO密集型操作对象不应频繁创建, 应实现对象复用. */
    private volatile OutputStream outputStream;

    // 是否立即刷新缓冲区(可配置)
    boolean immediateFlush = true;

    /**
    * The underlying output stream used by this appender.
    * 
    * @return
    */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Checks that requires parameters are set and if everything is in order,
     * activates this appender.
     */
    @Override
    public void start() {
        int errors = 0;
        if (this.encoder == null) {
            addStatus(new ErrorStatus("No encoder set for the appender named \"" + name + "\".", this));
            errors++;
        }

        if (this.outputStream == null) {
            addStatus(new ErrorStatus("No output stream set for the appender named \"" + name + "\".", this));
            errors++;
        }
        // only error free appenders should be activated
        if (errors == 0) {
            super.start();
        }
    }

    /**
     * 设置编码器的布局
     * @param layout
     */
    public void setLayout(Layout<E> layout) {
        addWarn("This appender no longer admits a layout as a sub-component, set an encoder instead.");
        addWarn("To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.");
        addWarn("See also " + CODES_URL + "#layoutInsteadOfEncoder for details");
        // 布局包装编码器
        LayoutWrappingEncoder<E> lwe = new LayoutWrappingEncoder<E>();
        // 设置布局
        lwe.setLayout(layout);
        // 设置编码器上下文
        lwe.setContext(context);

        this.encoder = lwe;
    }

    @Override
    protected void append(E eventObject) {
        // 如果appender未激活,直接返回
        if (!isStarted()) {
            return;
        }

        // 实际执行append逻辑
        subAppend(eventObject);
    }

    /**
     * Stop this appender instance. The underlying stream or writer is also
     * closed.
     * 
     * <p>
     * Stopped appenders cannot be reused.
     */
    @Override
    public void stop() {
        lock.lock();
        try {
            // 关闭输出流
            closeOutputStream();
            // 暂停appender实例
            super.stop();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Close the underlying {@link OutputStream}.
     */
    protected void closeOutputStream() {
        if (this.outputStream != null) {
            try {
                // before closing we have to output out layout's footer
                encoderClose();
                this.outputStream.close();
                this.outputStream = null;
            } catch (IOException e) {
                addStatus(new ErrorStatus("Could not close output stream for OutputStreamAppender.", this, e));
            }
        }
    }


    void encoderClose() {
        if (encoder != null && this.outputStream != null) {
            try {
                byte[] footer = encoder.footerBytes();
                writeBytes(footer);
            } catch (IOException ioe) {
                this.started = false;
                addStatus(new ErrorStatus("Failed to write footer for appender named [" + name + "].", this, ioe));
            }
        }
    }

    /**
     * <p>
     * Sets the @link OutputStream} where the log output will go. The specified
     * <code>OutputStream</code> must be opened by the user and be writable. The
     * <code>OutputStream</code> will be closed when the appender instance is
     * closed.
     * 
     * @param outputStream
     *          An already opened OutputStream.
     */
    public void setOutputStream(OutputStream outputStream) {
        lock.lock();
        try {
            // close any previously opened output stream
            closeOutputStream();
            this.outputStream = outputStream;
            if (encoder == null) {
                addWarn("Encoder has not been set. Cannot invoke its init method.");
                return;
            }

            encoderInit();
        } finally {
            lock.unlock();
        }
    }

    void encoderInit() {
        if (encoder != null && this.outputStream != null) {
            try {
                byte[] header = encoder.headerBytes();
                writeBytes(header);
            } catch (IOException ioe) {
                this.started = false;
                addStatus(new ErrorStatus("Failed to initialize encoder for appender named [" + name + "].", this, ioe));
            }
        }
    }

    /**
     * 写编码格式化后的日志数据.
     * @param event
     * @throws IOException
     */
    protected void writeOut(E event) throws IOException {
        byte[] byteArray = this.encoder.encode(event);
        writeBytes(byteArray);
    }

    /**
     * 写日志字节数据.
     * @param byteArray
     * @throws IOException
     */
    private void writeBytes(byte[] byteArray) throws IOException {
        if(byteArray == null || byteArray.length == 0) {
            return;
        }
        
        lock.lock();
        try {
            // 日志写
            this.outputStream.write(byteArray);
            // 是否需要立即刷新缓冲区
            if (immediateFlush) {
                this.outputStream.flush();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 实际日志写.
     * 大多数WriterAppender的子类需要重写该方法.
     *
     * @since 0.9.0
     */
    protected void subAppend(E event) {
        if (!isStarted()) {
            return;
        }
        try {
            // this step avoids LBCLASSIC-139
            if (event instanceof DeferredProcessingAware) {
                ((DeferredProcessingAware) event).prepareForDeferredProcessing();
            }
            // the synchronization prevents the OutputStream from being closed while we
            // are writing. It also prevents multiple threads from entering the same
            // converter. Converters assume that they are in a synchronized block.
            // lock.lock();

            byte[] byteArray = this.encoder.encode(event);
            writeBytes(byteArray);

        } catch (IOException ioe) {
            // as soon as an exception occurs, move to non-started state
            // and add a single ErrorStatus to the SM.
            this.started = false;
            addStatus(new ErrorStatus("IO failure in appender", this, ioe));
        }
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

}
