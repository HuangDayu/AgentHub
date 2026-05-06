package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;

@AgentTools(name = "DateTimeTools", description = "日期时间工具，提供日期时间获取、格式化、计算、时区转换等日期时间操作功能", defaultEnable = false)
public class DateTimeTools {

    @Tool(name = "datetime_now", description = "Get current date and time")
    public String getNow() {
        return LocalDateTime.now().toString();
    }

    @Tool(name = "datetime_now_formatted", description = "Get current datetime formatted")
    public String getNowFormatted(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    @Tool(name = "datetime_today", description = "Get today's date")
    public String getToday() {
        return LocalDate.now().toString();
    }

    @Tool(name = "datetime_time_now", description = "Get current time")
    public String getTimeNow() {
        return LocalTime.now().toString();
    }

    @Tool(name = "datetime_timestamp", description = "Get current timestamp in millis")
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Tool(name = "datetime_timestamp_seconds", description = "Get current timestamp in seconds")
    public long getTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    @Tool(name = "datetime_parse", description = "Parse date string to standard format")
    public String parseDate(String dateStr) {
        return LocalDate.parse(dateStr).toString();
    }

    @Tool(name = "datetime_add_days", description = "Add days to date")
    public String addDays(String dateStr, long days) {
        return LocalDate.parse(dateStr).plusDays(days).toString();
    }

    @Tool(name = "datetime_add_months", description = "Add months to date")
    public String addMonths(String dateStr, long months) {
        return LocalDate.parse(dateStr).plusMonths(months).toString();
    }

    @Tool(name = "datetime_add_years", description = "Add years to date")
    public String addYears(String dateStr, long years) {
        return LocalDate.parse(dateStr).plusYears(years).toString();
    }

    @Tool(name = "datetime_days_between", description = "Calculate days between two dates")
    public long daysBetween(String start, String end) {
        return ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.parse(end));
    }

    @Tool(name = "datetime_months_between", description = "Calculate months between two dates")
    public long monthsBetween(String start, String end) {
        return ChronoUnit.MONTHS.between(LocalDate.parse(start), LocalDate.parse(end));
    }

    @Tool(name = "datetime_years_between", description = "Calculate years between two dates")
    public long yearsBetween(String start, String end) {
        return ChronoUnit.YEARS.between(LocalDate.parse(start), LocalDate.parse(end));
    }

    @Tool(name = "datetime_day_of_week", description = "Get day of week for date")
    public String getDayOfWeek(String dateStr) {
        return LocalDate.parse(dateStr).getDayOfWeek().toString();
    }

    @Tool(name = "datetime_day_of_year", description = "Get day of year for date")
    public int getDayOfYear(String dateStr) {
        return LocalDate.parse(dateStr).getDayOfYear();
    }

    @Tool(name = "datetime_is_leap_year", description = "Check if year is leap year")
    public boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    @Tool(name = "datetime_week_of_year", description = "Get week number of year")
    public int getWeekOfYear(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    @Tool(name = "datetime_in_timezone", description = "Get current time in timezone")
    public String getTimeInTimezone(String zoneId) {
        return ZonedDateTime.now(ZoneId.of(zoneId)).toString();
    }

    @Tool(name = "datetime_convert_timezone", description = "Convert datetime to timezone")
    public String convertTimezone(String datetime, String fromZone, String toZone) {
        ZonedDateTime zdt = ZonedDateTime.parse(datetime).withZoneSameInstant(ZoneId.of(toZone));
        return zdt.toString();
    }

    @Tool(name = "datetime_format", description = "Format datetime with pattern")
    public String format(String datetime, String pattern) {
        LocalDateTime ldt = LocalDateTime.parse(datetime);
        return ldt.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Tool(name = "datetime_from_timestamp", description = "Convert timestamp to datetime")
    public String fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString();
    }
}
