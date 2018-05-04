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

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * 在0.9.19版本以前,Appender会委托Layout实现具体任务:
 * 1. 转换输入事件(event)为字符串;
 * 2. 将字节数组输出到指定目的地.
 *
 * 但在0.9.19版本之后,Layout仅具有转换输入事件为字符串功能,
 * 不再具有写文件功能.
 *
 * @param <E>
 */
public interface Layout<E> extends ContextAware, LifeCycle {

    /**
     * Transform an event (of type Object) and return it as a String after 
     * appropriate formatting.
     * 
     * <p>Taking in an object and returning a String is the least sophisticated
     * way of formatting events. However, it is remarkably CPU-effective.
     * </p>
     * 
     * @param event The event to format
     * @return the event formatted as a String
     */
    String doLayout(E event);

    /**
     * Return the file header for this layout. The returned value may be null.
     * @return The header.
     */
    String getFileHeader();

    /**
     * Return the header of the logging event formatting. The returned value
     * may be null.
     * 
     * @return The header.
     */
    String getPresentationHeader();

    /**
     * Return the footer of the logging event formatting. The returned value
     * may be null.
     * 
     * @return The footer.
     */

    String getPresentationFooter();

    /**
     * Return the file footer for this layout. The returned value may be null.
     * @return The footer.
     */
    String getFileFooter();

    /**
     * Returns the content type as appropriate for the implementation.
     *  
     * @return
     */
    String getContentType();

}
