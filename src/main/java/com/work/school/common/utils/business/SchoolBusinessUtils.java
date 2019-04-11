package com.work.school.common.utils.business;

import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.common.DateUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.banner.enums.ClassEnum;
import com.work.school.mysql.banner.enums.GradeEnum;
import com.work.school.mysql.banner.enums.SchoolTermEnum;

import java.util.Date;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 10:20 PM
 * @Description
 */
public class SchoolBusinessUtils {

    /**
     * 默认开始索引
     */
    public static final Integer DEFAULT_INDEX = 0;

    public static final Integer SPILT_MONTH = 7;

    /**
     * 获取当前时间对应的学期
     *
     * @return
     */
    public static Integer getSchoolTerm() {
        Integer schoolTerm = null;
        Date now = DateUtils.clearTime(DateUtils.getNow());
        for (SchoolTermEnum x : SchoolTermEnum.values()) {
            if (StringUtils.isNotEmpty(x.getStart()) && StringUtils.isNotEmpty(x.getEnd())) {
                Date start = DateUtils.clearTime(DateUtils.parseDate(x.getStart()));
                Date end = DateUtils.clearTime(DateUtils.parseDate(x.getEnd()));

                if (start.before(now) && now.before(end)) {
                    schoolTerm = x.getCode();
                }

            }
        }

        return schoolTerm;
    }

    /**
     * 根据年级获取年级
     *
     * @param classEnum
     * @return
     */
    public static GradeEnum getGradeByClassEnum(ClassEnum classEnum) {
        if (classEnum == null) {
            return null;
        }
        String[] gradeInfo = classEnum.getDesc().split("\\(");
        String grade = gradeInfo[DEFAULT_INDEX];
        GradeEnum gradeEnum = GradeEnum.getGradeEnum(grade);
        if (gradeEnum == null) {
            throw new TransactionException("未知的年级");
        }

        return gradeEnum;
    }

    /**
     * 根据年份活的年级
     *
     * @param year
     * @return
     */
    public static GradeEnum getGradeByYear(Integer year) {
        var now = DateUtils.getNow();
        var thisYear = DateUtils.getYear(now);
        var month = DateUtils.getMonth(now);
        Integer separatedYear = null;
        if (month > SPILT_MONTH){
            separatedYear = thisYear - year + 1;
        }
        if (month <= SPILT_MONTH){
            separatedYear = thisYear - year;
        }

        var gradeEnum =  GradeEnum.getGradeEnum(separatedYear);
        if (gradeEnum == null){
            throw new TransactionException("该年级已经毕业");
        }
        return gradeEnum;
    }
}
