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
package ch.qos.logback.core.util;

import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Daniel Lea
 */
public class TimeUtil {

    public static long computeStartOfNextSecond(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.SECOND, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextMinute(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MINUTE, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextHour(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextDay(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextWeek(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime().getTime();
    }

    public static long computeStartOfNextMonth(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime().getTime();
    }


    // since JDK 1.8


    public static Date currentTime() {
        return new Date();
    }

    private static String formatLocalDate(final LocalDate date, final String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatCurrentLocalDate(final String pattern) {
        return StringUtils.isBlank(pattern) ? null : formatLocalDate(LocalDate.now(), pattern);
    }

    private static String formatLocalTime(final LocalTime date, final String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatCurrentLocalTime(final String pattern) {
        return StringUtils.isBlank(pattern) ? null : formatLocalTime(LocalTime.now(), pattern);
    }

    private static String formatLocalDateTime(final LocalDateTime dateTime, final String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatCurrentLocalDateTime(final String pattern) {
        return StringUtils.isBlank(pattern) ? null : formatLocalDateTime(LocalDateTime.now(), pattern);
    }

    public static String formatSpecifiedTimestamp2Date(final long timestamp, final String pattern) {
        return StringUtils.isBlank(pattern) ? null : new SimpleDateFormat(pattern).format(new Date(timestamp));
    }

}
