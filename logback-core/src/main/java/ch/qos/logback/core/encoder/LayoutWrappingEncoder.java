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
package ch.qos.logback.core.encoder;

import java.nio.charset.Charset;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.OutputStreamAppender;

/**
 * LayoutWrappingEncoder包装器用户版本兼容.
 * 在0.9.19版本以后, Encoder兼有了Layout的
 * 部分功能,而只将日志事件格式转化工作交给了
 * Layout.为了兼容之前版本的代码,需要将之前Layout
 * 的部分功能包装为Encoder.
 *
 * @author Daniel Lea
 */
public class LayoutWrappingEncoder<E> extends EncoderBase<E> {

    // 包装0.9.19版本之前的layout
    protected Layout<E> layout;

    // null使用系统默认字符集
    private Charset charset;

    Appender<?> parent;

    // 如果immediateFlush为false,可以提高5倍日志吞吐量
    Boolean immediateFlush = null;

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Sets the immediateFlush option. The default value for immediateFlush is 'true'. If set to true,
     * the doEncode() method will immediately flush the underlying OutputStream. Although immediate flushing
     * is safer, it also significantly degrades logging throughput.
     *
     * @since 1.0.3
     */
    public void setImmediateFlush(boolean immediateFlush) {
        addWarn("As of version 1.2.0 \"immediateFlush\" property should be set within the enclosing Appender.");
        addWarn("Please move \"immediateFlush\" property into the enclosing appender.");
        this.immediateFlush = immediateFlush;
    }

    @Override
    public byte[] headerBytes() {
        if (layout == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        // 添加文件头部
        appendIfNotNull(sb, layout.getFileHeader());
        // 添加表达式头部
        appendIfNotNull(sb, layout.getPresentationHeader());
        if (sb.length() > 0) {
            // If at least one of file header or presentation header were not
            // null, then append a line separator.
            // This should be useful in most cases and should not hurt.
            sb.append(CoreConstants.LINE_SEPARATOR);
        }

        return convertToBytes(sb.toString());
    }

    @Override
    public byte[] footerBytes() {
        if (layout == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, layout.getPresentationFooter());
        appendIfNotNull(sb, layout.getFileFooter());
        return convertToBytes(sb.toString());
    }

    /**
     * 转换字符串为字节数组
     * @param s 字符串
     * @return
     */
    private byte[] convertToBytes(String s) {
        if (charset == null) {
            return s.getBytes();
        } else {
            return s.getBytes(charset);
        }
    }

    /**
     * encode具体实现Encoder两部分功能.
     * @param event
     * @return
     */
    @Override
    public byte[] encode(E event) {
        // 借用layout转换事件为字符串功能
        String txt = layout.doLayout(event);
        // 字符串转字节数组
        return convertToBytes(txt);
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void start() {
        if (immediateFlush != null) {
            if (parent instanceof OutputStreamAppender) {
                addWarn("Setting the \"immediateFlush\" property of the enclosing appender to " + immediateFlush);
                @SuppressWarnings("unchecked")
                OutputStreamAppender<E> parentOutputStreamAppender = (OutputStreamAppender<E>) parent;
                parentOutputStreamAppender.setImmediateFlush(immediateFlush);
            } else {
                addError("Could not set the \"immediateFlush\" property of the enclosing appender.");
            }
        }
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    /**
     * 字符串非空则拼接
     * @param sb
     * @param s
     */
    private void appendIfNotNull(StringBuilder sb, String s) {
        if (null != s) {
            sb.append(s);
        }
    }

    /**
     * This method allows RollingPolicy implementations to be aware of their
     * containing appender.
     * 
     * @param appender
     */
    public void setParent(Appender<?> parent) {
        this.parent = parent;
    }
}
