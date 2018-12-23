package com.work.school.common.utils.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类
 *
 * <br>createDate：2013-11-07
 * <br>updateDate：2014-04-16
 * @version 1.2.3
 * @author shikunhuang
 */
public class DateUtils {

    private static final Log logger = LogFactory.getLog(DateUtils.class);

    private static DateFormat[] dateFormats;

    static {
        dateFormats = new DateFormat[5];
        dateFormats[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        dateFormats[1] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormats[2] = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormats[3] = new SimpleDateFormat("yyyy-MM-dd");
        dateFormats[4] = new SimpleDateFormat("yyyy-MM");
    }

    /**
     *
     * 类名:		NfsqDateFormatUtils
     * 描述:		日期格式化format
     * @author 	yuexintong
     *
     */
    public class NfsqDateFormatUtils {

        public static final String ISO_DATETIME_FORMAT     = "yyyy-MM-dd HH:mm:ss";

        public static final String ISO_DATE_FORMAT         = "yyyy-MM-dd";

        public static final String ISO_DATETIME_SSS_FORMAT = "yyyy-MM-dd HH:mm:ss SSS";

        public static final String ISO_DATE_MM_DD_FORMAT   = "MM-dd";

        public static final String ISO_DATE_YY_MM_FORMAT   = "yyyy-MM";

        public static final String ISO_DATE_YYMM_FORMAT   = "yyyyMM";

        public static final String ISO_DATE_YYMMDD_FORMAT = "yyyyMMdd";
    }

    // -------------------------------------------------------------------------
    // 字符串转换为日期对象：
    // -------------------------------------------------------------------------

    /**
     * 字符串转换为Date对象
     * @param source			字符串
     * @param format			日期格式
     * @return					为空，或转换失败时皆返回null
     */
    public static Date parse(String source, String format) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(format)) {
            return null;
        }

        try {
            return new SimpleDateFormat(format).parse(source);
        } catch (Exception e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyy-MM-dd") 
     * @param source			字符串
     * @return					source为空，或转换失败时皆返回null
     */
    public static Date parseDate(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        try {
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_FORMAT).parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyy-MM")
     * @param source			字符串
     * @return					source为空，或转换失败时皆返回null
     */
    public static Date parseDateMonth(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        try {
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_YY_MM_FORMAT).parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyyMM")
     * @param source			字符串
     * @return					source为空，或转换失败时皆返回null
     */
    public static Date parseDateMonthInt(Integer source) {
        if (source == null) {
            return null;
        }

        try {
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_YYMM_FORMAT).parse(String.valueOf(source));
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyy-MM-dd") 
     * @param source            字符串
     * @return                  source为空，或转换失败时皆返回null
     */
    public static Date parseSqlDate(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        try {
            source = source.substring(0, 4) + "-" + source.substring(4, 6) + "-" + source.substring(6, 8);
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_FORMAT).parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }
    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATETIME_FORMAT "yyyy-MM-dd HH:mm:ss")
     * @param source			字符串
     * @return					source为空，或转换失败时皆返回null
     */
    public static Date parseDateTime(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        try {
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATETIME_FORMAT).parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    /**
     * 字符串转换为Date对象(字符串格式：NfsqDateFormatUtils.ISO_DATETIME_SSS_FORMAT "yyyy-MM-dd HH:mm:ss SSS")
     * @param source			字符串
     * @return					source为空，或转换失败时皆返回null
     */
    public static Date parseDateMiTime(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        try {
            return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATETIME_SSS_FORMAT).parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换为Date对象时异常", e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // 日期对象转换为字符串：
    // -------------------------------------------------------------------------

    /**
     * Date对象转换为字符串
     * @param date				Date
     * @param format			日期格式器
     * @return					为空，或转换失败时皆返回null
     */
    public static String format(Date date, SimpleDateFormat format) {
        if (null == date || format == null) {
            return null;
        }

        return format.format(date);
    }

    /**
     * Date对象转换为字符串
     * @param date				Date
     * @param format			日期格式
     * @return					为空，或转换失败时皆返回null
     */
    public static String format(Date date, String format) {
        if (null == date || StringUtils.isBlank(format)) {
            return null;
        }

        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyy-MM-dd")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static String formatDate(Date date) {
        if (null == date) {
            return "";
        }

        return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_FORMAT).format(date);
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATE_YYMMDD_FORMAT "yyyyMMdd")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static String formatDateToYYMMDD(Date date) {
        if (null == date) {
            return "";
        }

        return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_YYMMDD_FORMAT).format(date);
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATE_FORMAT "yyyy-MM-dd")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static Integer formatDateMonthInt(Date date) {
        if (null == date) {
            return null;
        }

        return Integer.parseInt(new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_YYMM_FORMAT).format(date));
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATE_MM_DD_FORMAT "MM-dd")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static String formatDate2(Date date) {
        if (null == date) {
            return "";
        }

        return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATE_MM_DD_FORMAT).format(date);
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATETIME_FORMAT "yyyy-MM-dd HH:mm:ss")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static String formatDateTime(Date date) {
        if (null == date) {
            return "";
        }

        return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATETIME_FORMAT).format(date);
    }

    /**
     * Date对象转换为字符串(格式：NfsqDateFormatUtils.ISO_DATETIME_SSS_FORMAT "yyyy-MM-dd HH:mm:ss SSS")
     * @param date				Date
     * @return					date为空时皆返回""
     */
    public static String formatDateTimeMi(Date date) {
        if (null == date) {
            return "";
        }

        return new SimpleDateFormat(NfsqDateFormatUtils.ISO_DATETIME_SSS_FORMAT).format(date);
    }

    // -------------------------------------------------------------------------
    // 天 的开始时间和结束时间：
    // -------------------------------------------------------------------------

    /**
     * 天的开始时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date dayBegin(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        dayBegin(calendar);

        return calendar.getTime();
    }

    /**
     * 天的开始时间
     * @param calendar			Calendar
     * @return					calendar为空时皆返回null
     */
    private static Date dayBegin(Calendar calendar) {
        if (null == calendar) {
            return null;
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 天的结束时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date dayEnd(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        dayEnd(calendar);

        return calendar.getTime();
    }

    /**
     * 天的结束时间
     * @param calendar			Calendar
     * @return					calendar为空时皆返回null
     */
    private static Date dayEnd(Calendar calendar) {
        if (null == calendar) {
            return null;
        }

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 获取当前月份的第一天的日期
     * @return
     */
    public static Date firstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        //设置从当前月份一号
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

        return calendar.getTime();
    }

    /**
     * 获取指定日期当前月份的第一天的日期
     * @return
     */
    public static Date firstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //设置从当前月份一号
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

        return calendar.getTime();
    }

    /**
     * 获取当前月份的最后一天的日期
     * @return
     */
    public static Date lastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     *  获取指定月份的最后一天的日期
     * @param date
     * @return
     */
    public static Date lastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取当天日期
     * @return
     */
    public static Date today() {
        Calendar calendar = Calendar.getInstance();

        return calendar.getTime();
    }

    /**
     * 获取当前日期
     * @return 当前日期对象
     */
    public static Date getNow(){
        return Calendar.getInstance().getTime();
    }

    /**
     * 按日期生成Calendar
     * @param date 日期
     * @return Calendar
     */
    public static Calendar getCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    /**
     * 获取年份
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.YEAR);
    }

    /**
     * 获取月份
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.MONTH) + 1;
    }
    /**
     * 获取月份
     * @param date 日期
     * @return 月份
     */
    public static int getMonthAndYear(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.YEAR)*100+c.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日(月中的第几天)
     * @param date 日期
     * @return 日
     */
    public static int getDay(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取小时
     * @param date 日期
     * @return 小时
     */
    public static int getHour(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.HOUR);
    }

    /**
     * 获取分钟
     * @param date 日期
     * @return 分钟
     */
    public static int getMinute(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.MINUTE);
    }

    /**
     * 获取秒
     * @param date 日期
     * @return 秒
     */
    public static int getSecond(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.SECOND);
    }

    /**
     * 获取毫秒
     * @param date 日期
     * @return 秒
     */
    public static int getMilliSecond(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.MILLISECOND);
    }

    /**
     * 获取星期几
     * @param date 日期
     * @return 星期几
     */
    public static int getDayOfWeek(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取一年中的第几天
     * @param date 日期
     * @return 一年中的第几天
     */
    public static int getDayOfYear(Date date){
        Calendar c = getCalendar(date);
        return c.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 清空日期的时分秒
     * @param date 要处理的日期
     * @return 清空时分秒后的日期
     */
    public static Date clearTime(Date date){
        Calendar c = getCalendar(date);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        clearSecond(date);

        return c.getTime();
    }

    /**
     * 清除秒
     * @param date
     * @return
     */
    public static Date clearSecond(Date date){
        Calendar c = getCalendar(date);

        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 为日期增加年份
     * @param date 日期
     * @param count 要增加的年份数量
     * @return 日期
     */
    public static Date addYears(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.YEAR, count);

        return c.getTime();
    }

    /**
     * 为日期增加月份
     * @param date 日期
     * @param count 要增加的月份数量
     * @return 日期
     */
    public static Date addMonths(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.MONTH, count);

        return c.getTime();
    }



    /**
     * 为日期增加天
     * @param date 日期
     * @param count 要增加的天数量
     * @return 日期
     */
    public static Date addDays(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.DATE, count);

        return c.getTime();
    }

    /**
     * 为日期增加小时
     * @param date 日期
     * @param count 要增加的小时数量
     * @return 日期
     */
    public static Date addHours(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.HOUR, count);

        return c.getTime();
    }

    /**
     * 为日期增加分钟
     * @param date 日期
     * @param count 要增加的分钟数量
     * @return 日期
     */
    public static Date addMinutes(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.MINUTE, count);

        return c.getTime();
    }

    /**
     * 为日期增加秒
     * @param date 日期
     * @param count 要增加的秒数量
     * @return 日期
     */
    public static Date addSeconds(Date date, int count){
        Calendar c = getCalendar(date);
        c.add(Calendar.SECOND, count);

        return c.getTime();
    }

    /**
     * 获取指定年月日时分秒的日期对象
     * @param year 年
     * @param month 月
     * @return 日期
     */
    public static Date newDate(int year, int month){
        return newDate(year, month, 1);
    }

    /**
     * 获取指定年月日时分秒的日期对象
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 日期
     */
    public static Date newDate(int year, int month, int day){
        return newDate(year, month, day, 0);
    }

    /**
     * 获取指定年月日时分秒的日期对象
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @return 日期
     */
    public static Date newDate(int year, int month, int day, int hour){
        return newDate(year, month, day, hour, 0);
    }

    /**
     * 获取指定年月日时分秒的日期对象
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param minute 分
     * @return 日期
     */
    public static Date newDate(int year, int month, int day, int hour, int minute){
        return newDate(year, month, day, hour, minute, 0);
    }

    /**
     * 获取指定年月日时分秒的日期对象
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param minute 分
     * @param second 秒
     * @return 日期
     */
    public static Date newDate(int year, int month, int day, int hour, int minute, int second){
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, second);
        return c.getTime();
    }

    /**
     * 获取某个时间的上周同日时间
     * @param date Date
     * @return date为空时皆返回null
     */
    public static Date theDayLastWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (null != date) {
            calendar.setTime(date);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime();
    }

    // -------------------------------------------------------------------------
    // 月/年 的开始时间和结束时间：
    // -------------------------------------------------------------------------

    /**
     * 月的开始时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date monthBegin(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        dayBegin(calendar);

        return calendar.getTime();
    }

    /**
     * 月的结束时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date monthEnd(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        dayEnd(calendar);

        return calendar.getTime();
    }

    /**
     * 年的开始时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date yearBegin(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
        dayBegin(calendar);

        return calendar.getTime();
    }

    /**
     * 年的结束时间
     * @param date				Date
     * @return					date为空时皆返回null
     */
    public static Date yearEnd(Date date) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        dayEnd(calendar);

        return calendar.getTime();
    }

    // -------------------------------------------------------------------------
    // 根据参数组成一个日期对象：
    // -------------------------------------------------------------------------

    /**
     * 根据参数组成一个日期对象，参数："年/月/日/时/分/秒/毫秒"
     * <br> 说明：<font color="red">如值为-1则使用当前的</font>
     * @param year				年
     * @param month				月
     * @param day				日
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @param milliSecond		毫秒
     * @return
     */
    public static Date set(int year, int month, int day, int hour, int minute, int second, int milliSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (year != -1)
            calendar.set(Calendar.YEAR, year);
        if (month != -1)
            calendar.set(Calendar.MONTH, month);
        if (day != -1)
            calendar.set(Calendar.DATE, day);
        if (hour != -1)
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        if (minute != -1)
            calendar.set(Calendar.MINUTE, minute);
        if (second != -1)
            calendar.set(Calendar.SECOND, second);
        if (milliSecond != -1)
            calendar.set(Calendar.MILLISECOND, milliSecond);

        return calendar.getTime();
    }

    /**
     * 根据参数组成一个日期对象，参数："年/月/日"
     * <br> 说明：<font color="red">如值为-1则使用当前的</font>
     * @param year				年
     * @param month				月
     * @param day				日
     * @return
     */
    public static Date setDate(int year, int month, int day) {
        return set(year, month, day, -1, -1, -1, -1);
    }

    /**
     * 根据参数组成一个日期对象，参数："时/分/秒"
     * <br> 说明：<font color="red">如值为-1则使用当前的</font>
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @return
     */
    public static Date setTime(int hour, int minute, int second) {
        return set(-1, -1, -1, hour, minute, second, -1);
    }

    /**
     * 根据参数组成一个日期对象，参数："时/分/秒/毫秒"
     * <br> 说明：<font color="red">如值为-1则使用当前的</font>
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @param milliSecond		毫秒
     * @return
     */
    public static Date setTime(int hour, int minute, int second, int milliSecond) {
        return set(-1, -1, -1, hour, minute, second, milliSecond);
    }

    // -------------------------------------------------------------------------
    // 修改指定时间的值：
    // -------------------------------------------------------------------------

    /**
     * 更改指定时间的"年/月/日/时/分/秒/毫秒"
     * <br> 说明：<font color="red">如值为-1则不进行修改</font>
     * @param date				原始时间
     * @param year				年
     * @param month				月
     * @param day				日
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @param milliSecond		毫秒
     * @return
     */
    public static Date update(Date date, int year, int month, int day, int hour, int minute, int second, int milliSecond) {
        if (null == date) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (year != -1)
            calendar.set(Calendar.YEAR, year);
        if (month != -1)
            calendar.set(Calendar.MONTH, month);
        if (day != -1)
            calendar.set(Calendar.DATE, day);
        if (hour != -1)
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        if (minute != -1)
            calendar.set(Calendar.MINUTE, minute);
        if (second != -1)
            calendar.set(Calendar.SECOND, second);
        if (milliSecond != -1)
            calendar.set(Calendar.MILLISECOND, milliSecond);

        return calendar.getTime();
    }

    /**
     * 更改指定时间的"年/月/日"
     * <br> 说明：<font color="red">如值为-1则不进行修改</font>
     * @param date				原始时间
     * @param year				年
     * @param month				月
     * @param day				日
     * @return
     */
    public static Date updateDate(Date date, int year, int month, int day) {
        return update(date, year, month, day, -1, -1, -1, -1);
    }

    /**
     * 更改指定时间的"时/分/秒"
     * <br> 说明：<font color="red">如值为-1则不进行修改</font>
     * @param date				原始时间
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @return
     */
    public static Date updateTime(Date date, int hour, int minute, int second) {
        return update(date, -1, -1, -1, hour, minute, second, -1);
    }

    /**
     * 更改指定时间的"时/分/秒/毫秒"
     * <br> 说明：<font color="red">如值为-1则不进行修改</font>
     * @param date				原始时间
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @param milliSecond		毫秒
     * @return
     */
    public static Date updateTime(Date date, int hour, int minute, int second, int milliSecond) {
        return update(date, -1, -1, -1, hour, minute, second, milliSecond);
    }

    // -------------------------------------------------------------------------
    // 指定时间的加、减：
    // -------------------------------------------------------------------------

    /**
     * 在指定时间的基础上增/减天数（说明：正数为增、负数为减）
     * @param oldDate			原始时间
     * @param day				天数
     * @return
     */
    public static Date addDay(Date oldDate, int day) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);

        calendar.add(Calendar.DATE, day);

        return calendar.getTime();
    }

    /**
     * 在指定时间的基础上增/减"年/月/日"（说明：正数为增、负数为减）
     * @param oldDate			原始时间
     * @param year				年
     * @param month				月
     * @param day				日
     * @return
     */
    public static Date addDate(Date oldDate, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);

        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);

        return calendar.getTime();
    }

    /**
     * 在指定时间的基础上增/减"年/月/日/时/分/秒"（说明：正数为增、负数为减）
     * @param oldDate			原始时间
     * @param year				年
     * @param month				月
     * @param day				日
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @return
     */
    public static Date addDate(Date oldDate, int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);

        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);

        return calendar.getTime();
    }

    // -------------------------------------------------------------------------
    // 根据指定值得到时间对象：
    // -------------------------------------------------------------------------

    /**
     * 得到指定"年/月/日"的时间对象(时/分/秒为当前的)
     * @param year				年
     * @param month				月
     * @param day				日
     * @return
     */
    public static Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    /**
     * 得到指定"年/月/日/时/分/秒"的时间对象
     * @param year				年
     * @param month				月
     * @param day				日
     * @param hour				时
     * @param minute			分
     * @param second			秒
     * @return
     */
    public static Date getDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar.getTime();
    }

    // -------------------------------------------------------------------------
    // 获取指定时间的年/月/日：
    // -------------------------------------------------------------------------

    /**
     * 获取指定时间的年
     * @param date
     * @return					date为空时皆返回0
     */
    public static int year(Date date) {
        if (null == date) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取指定时间的月
     * @param date
     * @return					date为空时皆返回0
     */
    public static int month(Date date) {
        if (null == date) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取指定时间的日
     * @param date
     * @return					date为空时皆返回0
     */
    public static int day(Date date) {
        if (null == date) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    // -------------------------------------------------------------------------
    // 日期比较：
    // -------------------------------------------------------------------------

    /**
     * 日期比较，得出其天数
     * <br> 注意：这里不检查两值为空情况
     * @param beginDate			开始时间
     * @param endDate			结束时间
     * @return					可能会返回负值，两值相差不足一天时返回0
     */
    public static int compareDate(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    // -------------------------------------------------------------------------
    // 其它：
    // -------------------------------------------------------------------------

    /**
     * 检测日期格式字符串是否正确
     * @param dateStr			日期格式字符串(可为"yyyyMMdd"或"yyyy-MM-dd")
     * @return					正确返回true；格式不正确、长度不对等皆返回false
     */
    public static boolean checkDateStr(String dateStr) {
        // 是否为空、长度、是否全为数字的检测：
        if (isBlank(dateStr)) {
            return false;
        } else {
            dateStr = dateStr.replaceAll("-", "");
            if (dateStr.length() != 8 || !dateStr.matches("\\d{8}")) {
                return false;
            }
        }

        int year = Integer.parseInt(dateStr.substring(0, 4));
        int month = Integer.parseInt(dateStr.substring(4, 6));
        int day = Integer.parseInt(dateStr.substring(6, 8));

        // 二月份最大天数：
        int febMaxDay = isLeapYear(year) ? 29 : 28;
        // 每月最大天数集：
        int[] days = new int[] { 31, febMaxDay, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        for (int i = 0; i < 12; i++) {
            if (month == i + 1) {
                if (1 <= day && day <= days[i]) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 判断是否是闰年
     * <br> 规则：四年一闰，百年不闰，四百年再闰
     * <br> 说明：仅考虑3200年以前(其实3200等不是闰年)
     * @param year				指定年
     * @return					返回true:是、false:不是
     */
    public static boolean isLeapYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否为空
     * <br> 说明：为了使本工具类不与字符串工具类有关联，对于字符串为空的情况简单的做下处理
     * @param str				目标字符串
     * @return					为空时将返回true
     */
    private static boolean isBlank(String str) {
        if (null == str || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前时间是否是执行时间
     * @param nowTime
     * @param executeTime
     * @return
     */
    public static boolean isSameMonth(Date nowTime,Date executeTime){
        if(null == nowTime || null == executeTime){
            return false;
        }
        if(DateUtils.getMonth(nowTime) == DateUtils.getMonth(executeTime)){
            return true;
        }
        return false;
    }

    /**
     * 描述:   获取当前时间所在的非自然年的年份
     * <br> 说明：非按自然年获取时间，自定义每年的起始时间和结束时间分别为12月1日和11月30日</br>
     *
     * @param date
     * @return
     */
    public static int getCurrentSeasonYear(Date date) {
        int year = DateUtils.year(date);
        if (12 == DateUtils.month(date)) {
            year = year + 1;
        }
        return year;
    }

    /**
     * 描述:获取当前时间的月份
     * @return int
     */
    public static int getCurrentMonth(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        return month;
    }

    /**
     * 描述:   获取时间段中的月份列表
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Integer> getMonthsBetweenDate(Date startDate, Date endDate) {
        if (null == startDate || null == endDate) {
            return null;
        }
        List<Integer> monthList = new ArrayList<Integer>();
        Calendar calendar = Calendar.getInstance();
        startDate = setDate(year(startDate), month(startDate) - 1, day(startDate));
        calendar.setTime(startDate);
        while (compareDate(calendar.getTime(), endDate) >= 0) {
            monthList.add(month(calendar.getTime()));
            calendar.add(Calendar.MONTH, 1);
        }
        return monthList;
    }

    /**
     * 描述: 获取时间段中的月份列表
     * @param startDate
     * @param endDate
     * @param format  日期格式
     * @return
     */
    public static List<Integer> getMonthsBetweenDate(Date startDate, Date endDate, String format) {
        if (null == startDate || null == endDate) {
            return null;
        }
        List<Integer> monthList = new ArrayList<Integer>();
        Calendar calendar = Calendar.getInstance();
        startDate = setDate(year(startDate), month(startDate) - 1, day(startDate));
        calendar.setTime(startDate);
        while (compareDate(calendar.getTime(), endDate) >= 0) {
            String times  = new SimpleDateFormat(format).format(calendar.getTime());
            monthList.add(Integer.valueOf(times));
            calendar.add(Calendar.MONTH, 1);
        }
        return monthList;
    }

    /**
     * 将字符串解析成日期格式
     * @param text 字符串
     * @param ignoreException 是否忽略异常
     * @return 日期
     * @throws Exception
     */
    public static Date parse(String text, boolean ignoreException) throws Exception{
        //依次解析,成功就跳出
        Date dt = null;
        int len = dateFormats.length;
        boolean success = false;
        ParseException parseException = null;
        for(int index = 0; index < len; index++){
            try {
                dt = dateFormats[index].parse(text);
                success = true;
                break;
            }catch (ParseException e){
                if(!ignoreException){
                    throw e;
                }
            }
        }
        if(!success){
            throw new IllegalArgumentException("无法解析日期: " + parseException.getMessage(), parseException);
        }else{
            return dt;
        }
    }

    /**
     * 计算两个日期间的非负时间差
     * @param date1
     * @param date2
     * @return
     */
    public static Long dateDifference(Date date1, Date date2){
        Long defaultOneMin = 60000L;
        Long fromDate = DateUtils.clearSecond(date1).getTime();
        Long toDate = DateUtils.clearSecond(date2).getTime();
        Long midTime = fromDate - toDate;
        Long time = Math.abs(midTime)/defaultOneMin;

        return time;
    }
}
