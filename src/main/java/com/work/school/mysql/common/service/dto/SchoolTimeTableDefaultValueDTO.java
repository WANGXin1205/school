package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2019/9/5 7:42 AM
 * @Description
 */
public final class SchoolTimeTableDefaultValueDTO implements Serializable{
    /**
     * 一周的工作天数
     */
    private static final Integer WORK_DAY = 5;
    /**
     * 一周的工作开始索引
     */
    private static final Integer START_WORK_DAY_INDEX = 1;
    /**
     * 每天上课节数
     */
    private static final Integer CLASS_TIME = 7;
    /**
     * 每天的上课开始索引
     */
    private static final Integer START_CLASS_TIME_INDEX = 1;
    /**
     * 一周的课时量
     */
    private static final Integer TOTAL_FREQUENCY = WORK_DAY * CLASS_TIME;
    /**
     * 周一
     */
    private static final Integer MONDAY_NUM = 1;
    /**
     * 周五
     */
    private static final Integer FRIDAY_NUM = 5;
    /**
     * 早上第一节课
     */
    private static final Integer MORNING_FIR_TIME = 1;
    /**
     * 早上第二节课
     */
    private static final Integer MORNING_SEC_TIME = 2;
    /**
     * 早上最后一节课
     */
    private static final Integer MORNING_LAST_TIME = 3;
    /**
     * 上一节课
     */
    private static final Integer PAST_TIME = 1;
    /**
     * 班会课时间
     */
    private static final Integer CLASS_MEETING_TIME = 7;
    /**
     * 校本课程时间
     */
    private static final Integer[] SCHOOL_BASED_TIME = {4, 5, 6, 7};
    /**
     * 分配班级开始索引
     */
    private static final Integer START_CLASS_INDEX = 1;
    /**
     * 语文
     */
    private static final Integer SUBJECT_CHINESE_ID = 1;
    /**
     * 数学
     */
    private static final Integer SUBJECT_MATHS_ID = 2;
    /**
     * 班队会课 编号
     */
    private static final Integer SUBJECT_CLASS_MEETING_ID = 10;
    /**
     * 校本课程课 编号
     */
    private static final Integer SUBJECT_SCHOOL_BASED_ID = 9;
    /**
     * 主课类型
     */
    private static final Integer MAIN_SUBJECT_TYPE = 1;
    /**
     * 小课类型
     */
    private static final Integer OTHER_SUBJECT_TYPE = 2;
    /**
     * 占用资源小课程类型
     */
    private static final Integer OTHER_NEED_AREA_SUBJECT_TYPE = 3;
    /**
     * 固定课程类型
     */
    private static final Integer SPECIAL_SUBJECT_TYPE = 4;
    /**
     * 科目列表只有一个的索引
     */
    private static final Integer SUBJECT_WEIGHT_LIST_ONE_INDEX = 1;
    /**
     * 每个班级每天最多上2节主课
     */
    private static final Integer SUBJECT_CONTINUE_TIME = 2;
    /**
     * 学生的连堂课时间段
     */
    private static final Integer[][] STUDENT_CONTINUE_TIME = {{1, 2}, {4, 5}, {5, 6}};
    private static final Integer STUDENT_CONTINUE_TIME_LAST_INDEX = 1;
    /**
     * 教师的连堂课时间段
     */
    private static final Integer[][] TEACHER_CONTINUE_TIME = {{4, 5, 6}};
    private static final Integer TEACHER_CONTINUE_TIME_LAST_INDEX = 2;
    private static final Integer TEACHER_CONTINUE_TIME_MAX_SIZE = 3;
    private static final Integer TEACHER_TIME_MIN_OVER_SIZE = 4;
    /**
     * 初始化使用教室的数量
     */
    private static final Integer INIT_CLASSROOM_USED_COUNT = 0;

    public static Integer getWorkDay() {
        return WORK_DAY;
    }

    public static Integer getStartWorkDayIndex() {
        return START_WORK_DAY_INDEX;
    }

    public static Integer getClassTime() {
        return CLASS_TIME;
    }

    public static Integer getStartClassTimeIndex() {
        return START_CLASS_TIME_INDEX;
    }

    public static Integer getTotalFrequency() {
        return TOTAL_FREQUENCY;
    }

    public static Integer getMondayNum() {
        return MONDAY_NUM;
    }

    public static Integer getFridayNum() {
        return FRIDAY_NUM;
    }

    public static Integer getMorningFirTime() {
        return MORNING_FIR_TIME;
    }

    public static Integer getMorningSecTime() {
        return MORNING_SEC_TIME;
    }

    public static Integer getMorningLastTime() {
        return MORNING_LAST_TIME;
    }

    public static Integer getPastTime() {
        return PAST_TIME;
    }

    public static Integer getClassMeetingTime() {
        return CLASS_MEETING_TIME;
    }

    public static Integer[] getSchoolBasedTime() {
        return SCHOOL_BASED_TIME;
    }

    public static Integer getStartClassIndex() {
        return START_CLASS_INDEX;
    }

    public static Integer getSubjectChineseId() {
        return SUBJECT_CHINESE_ID;
    }

    public static Integer getSubjectMathsId() {
        return SUBJECT_MATHS_ID;
    }

    public static Integer getSubjectClassMeetingId() {
        return SUBJECT_CLASS_MEETING_ID;
    }

    public static Integer getSubjectSchoolBasedId() {
        return SUBJECT_SCHOOL_BASED_ID;
    }

    public static Integer getMainSubjectType() {
        return MAIN_SUBJECT_TYPE;
    }

    public static Integer getOtherSubjectType() {
        return OTHER_SUBJECT_TYPE;
    }

    public static Integer getOtherNeedAreaSubjectType() {
        return OTHER_NEED_AREA_SUBJECT_TYPE;
    }

    public static Integer getSpecialSubjectType() {
        return SPECIAL_SUBJECT_TYPE;
    }

    public static Integer getSubjectWeightListOneIndex() {
        return SUBJECT_WEIGHT_LIST_ONE_INDEX;
    }

    public static Integer[][] getStudentContinueTime() {
        return STUDENT_CONTINUE_TIME;
    }

    public static Integer getStudentContinueTimeLastIndex() {
        return STUDENT_CONTINUE_TIME_LAST_INDEX;
    }

    public static Integer[][] getTeacherContinueTime() {
        return TEACHER_CONTINUE_TIME;
    }

    public static Integer getTeacherContinueTimeLastIndex() {
        return TEACHER_CONTINUE_TIME_LAST_INDEX;
    }

    public static Integer getTeacherContinueTimeMaxSize() {
        return TEACHER_CONTINUE_TIME_MAX_SIZE;
    }

    public static Integer getTeacherTimeMinOverSize() {
        return TEACHER_TIME_MIN_OVER_SIZE;
    }

    public static Integer getInitClassroomUsedCount() {
        return INIT_CLASSROOM_USED_COUNT;
    }

    public static Integer getSubjectContinueTime() {
        return SUBJECT_CONTINUE_TIME;
    }

    @Override
    public String toString() {
        return "SchoolTimeTableDefaultValueDTO{}";
    }
}