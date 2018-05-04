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
package org.slf4j.impl;

import ch.qos.logback.core.status.StatusUtil;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * 
 * The binding of {@link LoggerFactory} class with an actual instance of
 * {@link ILoggerFactory} is performed using information returned by this class.
 *
 * 静态日志绑定. 使用slf4时需要人工选择日志实现包.
 * common-logging采用动态查找机制,在运行时搜寻所需日志实现包.
 * 
 * @author Ceki G&uuml;lc&uuml;</a>
 * @author Daniel Lea
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    /**
     * Declare the version of the SLF4J API this implementation is compiled
     * against. The value of this field is usually modified with each release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.16"; // !final

    final static String NULL_CS_URL = CoreConstants.CODES_URL + "#null_CS";

    /** 单例对象 */
    private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    private static Object KEY = new Object();

    /**
     *
     */
    static {
        SINGLETON.init();
    }

    private boolean initialized = false;
    private LoggerContext defaultLoggerContext = new LoggerContext();
    private final ContextSelectorStaticBinder contextSelectorBinder = ContextSelectorStaticBinder.getSingleton();

    private StaticLoggerBinder() {
        defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Package access for testing purposes.
     */
    static void reset() {
        SINGLETON = new StaticLoggerBinder();
        SINGLETON.init();
    }

    /**
     * Package access for testing purposes.
     */
    void init() {
        try {
            try {
                // 上下文初始化
                new ContextInitializer(defaultLoggerContext).autoConfig();
            } catch (JoranException je) {
                Util.report("Failed to auto configure default logger context", je);
            }
            // logback-292
            if (!StatusUtil.contextHasStatusListener(defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(defaultLoggerContext);
            }
            contextSelectorBinder.init(defaultLoggerContext, KEY);
            initialized = true;
        } catch (Exception t) { // see LOGBACK-1159
            Util.report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
        }
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        // 如果未初始化,返回默认日志器上下文
        if (!initialized) {
            return defaultLoggerContext;
        }

        if (null == contextSelectorBinder.getContextSelector()) {
            throw new IllegalStateException("contextSelector cannot be null. See also " + NULL_CS_URL);
        }
        return contextSelectorBinder.getContextSelector().getLoggerContext();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return contextSelectorBinder.getClass().getName();
    }

}
