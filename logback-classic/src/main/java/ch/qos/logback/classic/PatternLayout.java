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
package ch.qos.logback.classic;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.pattern.*;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.color.*;
import ch.qos.logback.core.pattern.parser.Parser;

/**
 * 使用模式字符串实现灵活的布局配置. 该类用于格式化日志事件{@link ILoggingEvent},
 * 并返回一个字符串. 格式化结果依赖于conversion pattern(默认模式).
 *
 * @author Daniel Lea
 */
public class PatternLayout extends PatternLayoutBase<ILoggingEvent> {

    /** 默认转换器 */
    public static final Map<String, String> defaultConverterMap = new HashMap<>();
    /** 头部前缀 */
    public static final String HEADER_PREFIX = "#logback.classic pattern: ";

    static {
        defaultConverterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        // 日志日期
        defaultConverterMap.put("d", DateConverter.class.getName());
        defaultConverterMap.put("date", DateConverter.class.getName());

        // 日志相对时间
        defaultConverterMap.put("r", RelativeTimeConverter.class.getName());
        defaultConverterMap.put("relative", RelativeTimeConverter.class.getName());

        // 日志级别
        defaultConverterMap.put("level", LevelConverter.class.getName());
        defaultConverterMap.put("le", LevelConverter.class.getName());
        defaultConverterMap.put("p", LevelConverter.class.getName());

        // 当前线程
        defaultConverterMap.put("t", ThreadConverter.class.getName());
        defaultConverterMap.put("thread", ThreadConverter.class.getName());

        // 日志器名称
        defaultConverterMap.put("lo", LoggerConverter.class.getName());
        defaultConverterMap.put("logger", LoggerConverter.class.getName());
        defaultConverterMap.put("c", LoggerConverter.class.getName());

        // 日志消息
        defaultConverterMap.put("m", MessageConverter.class.getName());
        defaultConverterMap.put("msg", MessageConverter.class.getName());
        defaultConverterMap.put("message", MessageConverter.class.getName());

        // 日志调用者类信息
        defaultConverterMap.put("C", ClassOfCallerConverter.class.getName());
        defaultConverterMap.put("class", ClassOfCallerConverter.class.getName());

        // 日志调用者方法信息
        defaultConverterMap.put("M", MethodOfCallerConverter.class.getName());
        defaultConverterMap.put("method", MethodOfCallerConverter.class.getName());

        // 日志调用者代码行信息
        defaultConverterMap.put("L", LineOfCallerConverter.class.getName());
        defaultConverterMap.put("line", LineOfCallerConverter.class.getName());

        // 日志调用者文件信息
        defaultConverterMap.put("F", FileOfCallerConverter.class.getName());
        defaultConverterMap.put("file", FileOfCallerConverter.class.getName());

        defaultConverterMap.put("X", MDCConverter.class.getName());
        defaultConverterMap.put("mdc", MDCConverter.class.getName());


        defaultConverterMap.put("ex", ThrowableProxyConverter.class.getName());
        defaultConverterMap.put("exception", ThrowableProxyConverter.class.getName());
        defaultConverterMap.put("rEx", RootCauseFirstThrowableProxyConverter.class.getName());
        defaultConverterMap.put("rootException", RootCauseFirstThrowableProxyConverter.class.getName());
        defaultConverterMap.put("throwable", ThrowableProxyConverter.class.getName());

        defaultConverterMap.put("xEx", ExtendedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("xException", ExtendedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("xThrowable", ExtendedThrowableProxyConverter.class.getName());

        defaultConverterMap.put("nopex", NopThrowableInformationConverter.class.getName());
        defaultConverterMap.put("nopexception", NopThrowableInformationConverter.class.getName());

        //
        defaultConverterMap.put("cn", ContextNameConverter.class.getName());
        defaultConverterMap.put("contextName", ContextNameConverter.class.getName());

        // 调用者数据
        defaultConverterMap.put("caller", CallerDataConverter.class.getName());

        // 标记
        defaultConverterMap.put("marker", MarkerConverter.class.getName());

        // 属性
        defaultConverterMap.put("property", PropertyConverter.class.getName());

        // 行分隔符
        defaultConverterMap.put("n", LineSeparatorConverter.class.getName());

        // 颜色以及高亮
        defaultConverterMap.put("black", BlackCompositeConverter.class.getName());
        defaultConverterMap.put("red", RedCompositeConverter.class.getName());
        defaultConverterMap.put("green", GreenCompositeConverter.class.getName());
        defaultConverterMap.put("yellow", YellowCompositeConverter.class.getName());
        defaultConverterMap.put("blue", BlueCompositeConverter.class.getName());
        defaultConverterMap.put("magenta", MagentaCompositeConverter.class.getName());
        defaultConverterMap.put("cyan", CyanCompositeConverter.class.getName());
        defaultConverterMap.put("white", WhiteCompositeConverter.class.getName());
        defaultConverterMap.put("gray", GrayCompositeConverter.class.getName());
        defaultConverterMap.put("boldRed", BoldRedCompositeConverter.class.getName());
        defaultConverterMap.put("boldGreen", BoldGreenCompositeConverter.class.getName());
        defaultConverterMap.put("boldYellow", BoldYellowCompositeConverter.class.getName());
        defaultConverterMap.put("boldBlue", BoldBlueCompositeConverter.class.getName());
        defaultConverterMap.put("boldMagenta", BoldMagentaCompositeConverter.class.getName());
        defaultConverterMap.put("boldCyan", BoldCyanCompositeConverter.class.getName());
        defaultConverterMap.put("boldWhite", BoldWhiteCompositeConverter.class.getName());
        defaultConverterMap.put("highlight", HighlightingCompositeConverter.class.getName());

        // 本地序列号
        defaultConverterMap.put("lsn", LocalSequenceNumberConverter.class.getName());

    }

    public PatternLayout() {
        this.postCompileProcessor = new EnsureExceptionHandling();
    }

    public Map<String, String> getDefaultConverterMap() {
        return defaultConverterMap;
    }

    public String doLayout(ILoggingEvent event) {
        if (!isStarted()) {
            return CoreConstants.EMPTY_STRING;
        }
        return writeLoopOnConverters(event);
    }

    @Override
    protected String getPresentationHeaderPrefix() {
        return HEADER_PREFIX;
    }
}
