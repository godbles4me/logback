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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * This class serves the super-class of all converters in logback. It extends
 * {@link DynamicConverter}.
 *
 * 创建自定义格式转换符必须继承{@link ClassicConverter}
 * @author Ceki Gulcu
 * @author Daniel Lea
 */
public abstract class ClassicConverter extends DynamicConverter<ILoggingEvent> {
}
