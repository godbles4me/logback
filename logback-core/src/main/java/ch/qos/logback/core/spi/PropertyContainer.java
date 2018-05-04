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

import java.util.Map;

/**
 * 属性容器
 * @author Daniel Lea
 */
public interface PropertyContainer {

    /**
     * 获取属性值
     * @param key 属性键
     * @return 属性值
     */
    String getProperty(final String key);

    /**
     * 获取属性K-V拷贝
     * @return
     */
    Map<String, String> getCopyOfPropertyMap();
}
