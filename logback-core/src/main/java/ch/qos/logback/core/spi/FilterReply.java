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
package ch.qos.logback.core.spi;

/**
 *
 * This enum represents the possible replies that a filtering component
 * in logback can return. It is used by implementations of both 
 * {@link ch.qos.logback.core.filter.Filter} and
 * {@link ch.qos.logback.classic.turbo.TurboFilter} abstract classes.
 * 
 * Based on the order that the FilterReply values are declared,
 * FilterReply.ACCEPT.compareTo(FilterReply.DENY) will return 
 * a positive value.
 *
 * 过滤器响应
 *
 * @author S&eacute;bastien Pennec
 * @author Daniel Lea
 */
public enum FilterReply {
    // 直接抛弃事件,不再执行过滤器链后续逻辑
    DENY,
    // 继续执行滤器链中的后续过滤器,即继续记录后续事件
    NEUTRAL,
    // 立即处理事件,不再执行过滤器链后续逻辑
    ACCEPT,
    ;
}
