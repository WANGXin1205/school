package com.work.school.mysql.common.service.dto;

import java.math.BigInteger;

/**
 * @Author : Growlithe
 * @Date : 2019/9/24 11:58 PM
 * @Description
 */
public final class GeneticDefaultValueDTO {
    /**
     * 遗传算法次数
     */
    public static final int GENE_TIMES = 200;
    /**
     * 基因开始和结束索引
     */
    public static final String GENE_INDEX = "gene";
    public static final int GENE_BEGIN_INDEX = 0;
    public static final int GENE_END_INDEX = 13;
    /**
     * 固定的索引位置
     */
    public static final String FIXED_INDEX = "fixed";
    public static final int FIXED_BEGIN_INDEX = 0;
    public static final int FIXED_END_INDEX = 2;
    /**
     * 年级的索引位置
     */
    public static final String GRADE_INDEX = "grade";
    public static final int GRADE_BEGIN_INDEX = 2;
    public static final int GRADE_END_INDEX = 4;
    /**
     * 班级的索引位置
     */
    public static final String CLASS_NO_INDEX = "classNo";
    public static final int CLASS_NO_BEGIN_INDEX = 4;
    public static final int CLASS_NO_END_INDEX = 6;
    /**
     * 年级和班级索引位置
     */
    public static final String GRADE_CLASS_INDEX = "gradeClassNo";
    public static final int GRADE_CLASS_BEGIN_INDEX = 2;
    public static final int GRADE_CLASS_END_INDEX = 6;
    /**
     * 课程编号的索引位置
     */
    public static final String SUBJECT_ID_INDEX = "subjectId";
    public static final int SUBJECT_ID_BEGIN_INDEX = 6;
    public static final int SUBJECT_ID_END_INDEX = 8;
    /**
     * 课程节次的索引位置
     */
    public static final String SUBJECT_FREQUENCY_INDEX = "subjectFrequency";
    public static final int SUBJECT_FREQUENCY_BEGIN_INDEX = 8;
    public static final int SUBJECT_FREQUENCY_END_INDEX = 10;
    /**
     * 课程属性的索引位置
     */
    public static final String SUBJECT_TYPE_INDEX = "subjectType";
    public static final int SUBJECT_TYPE_BEGIN_INDEX = 10;
    public static final int SUBJECT_TYPE_END_INDEX = 11;
    /**
     * 教师编号的索引位置
     */
    public static final String TEACHER_ID_INDEX = "teacherId";
    public static final int TEACHER_ID_BEGIN_INDEX = 11;
    public static final int TEACHER_ID_END_INDEX = 13;
    /**
     * 开课时间的索引位置
     */
    public static final String CLASS_TIME_INDEX = "classTime";
    public static final int CLASS_TIME_BEGIN_INDEX = 13;

    /**
     * 班队会时间
     */
    public static final String CLASS_MEETING_TIME_KEY = "1001";
    public static final String CLASS_MEETING_TIME_VALUE = "07";
    /**
     * 书法课时间
     */
    public static final String WRITING_TIME_KEY = "1401";
    public static final String WRITING_TIME_VALUE = "21";
    /**
     * 校本课程时间
     */
    public static final String SCHOOL_BASED_TIME_ONE_KEY = "0901";
    public static final String SCHOOL_BASED_TIME_ONE_VALUE = "32";
    public static final String SCHOOL_BASED_TIME_TWO_KEY = "0902";
    public static final String SCHOOL_BASED_TIME_TWO_VALUE = "33";
    public static final String SCHOOL_BASED_TIME_THREE_KEY = "0903";
    public static final String SCHOOL_BASED_TIME_THREE_VALUE = "34";
    public static final String SCHOOL_BASED_TIME_FOUR_KEY = "0904";
    public static final String SCHOOL_BASED_TIME_FOUR_VALUE = "35";

    /**
     * 课程固定与否
     */
    public static final String UN_FIXED = "00";
    public static final String FIXED = "01";

    /**
     * 设置年级的标准格式长度
     */
    public static final int GRADE_STANDARD_LENGTH = 2;
    /**
     * 设置班级的标准格式长度
     */
    public static final int CLASS_STANDARD_LENGTH = 2;
    /**
     * 设置科目的标准格式长度
     */
    public static final int SUBJECT_ID_STANDARD_LENGTH = 2;
    /**
     * 设置科目次数的标准格式长度
     */
    public static final int SUBJECT_FREQUENCY_STANDARD_LENGTH = 2;
    /**
     * 设置教师id的标准格式长度
     */
    public static final int TEACHER_ID_STANDARD_LENGTH = 2;
    /**
     * 设置没有教师id的标准格式
     */
    public static final String NO_TEACHER_ID_STANDARD = "00";
    /**
     * 开课时间长度
     */
    public static final int CLASS_TIME_STANDARD_LENGTH = 2;
    /**
     * 开课时间默认为00
     */
    public static final String CLASS_TIME_STANDARD = "00";
    /**
     * 最小的节次
     */
    public static final int MIN_TIME = 1;
    /**
     * 最大的节次
     */
    public static final int MAX_TIME = 35;
}
