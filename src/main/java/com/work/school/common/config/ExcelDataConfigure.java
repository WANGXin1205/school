package com.work.school.common.config;

import java.math.BigDecimal;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 9:40 PM
 * @Description
 */
public class ExcelDataConfigure {
    /**
     * 默认工作表
     */
    public static Integer DEFAULT_SHEET = 0;
    /**
     * 默认开始行
     */
    public static Integer DEFAULT_BEGIN_ROW_INDEX = 0;

    /**
     * 转换为Excel行数所需要的值
     */
    public static Integer DEFAULT_ADD_ROW = 2;

    /**
     * 默认创建人 更新人
     */
    public static String GROWLITHE = "WANGXin";


    /**
     * 流动红旗汇总表配置
     */
    public static String[] RED_BANNER_DATA_NAME = {"周", "文明之星", "路队之星", "体育之星", "卫生之星"};

    /**
     * 流动红旗excel默认值
     */
    public static Integer RED_BANNER_WEEK_INDEX = 0;
    public static Integer RED_BANNER_BEST_INDEX = 1;
    public static Integer RED_BANNER_TEAM_INDEX = 2;
    public static Integer RED_BANNER_SPORT_INDEX = 3;
    public static Integer RED_BANNER_HEALTH_INDEX = 4;


    /**
     * 考试成绩单 班级 和 科目 提取 行
     */
    public static Integer EXAM_RESULT_CLASS_AND_SUBJECT_ROW = 0;
    /**
     * 考试成绩单 标题开始行
     */
    public static Integer EXAM_RESULT_TITLE_ROW = 1;
    /**
     * 考试成绩单 第1行格式
     */
    public static String[] EXAM_RESULT_TITLE_NAME = {"序号", "姓名", "成绩", "序号", "姓名", "成绩"};
    /**
     * 考试成绩单 数据开始行
     */
    public static Integer EXAM_RESULT_DATA_START_ROW = 1;
    /**
     * 考试成绩excel 默认值
     */
    public static Integer EXAM_RESULT_CLASS_INDEX = 0;
    public static Integer EXAM_RESULT_SUBJECT_INDEX = 3;
    public static String EXAM_RESULT_SPILT_SIGN = "：";
    public static String GRADE_CLASS_SPILT_SIGN = "年级";
    public static Integer GRADE_SPILT_INDEX = 0;
    public static Integer GRADE_CLASS_INDEX = 1;
    public static Integer EXAM_RESULT_CLASS_AND_SUBJECT_SPILT_INDEX = 1;
    public static Integer EXAM_RESULT_STUDENT_CLASS_ID_FIRST_INDEX = 0;
    public static Integer EXAM_RESULT_NAME_FIRST_INDEX = 1;
    public static Integer EXAM_RESULT_SCORE_FIRST_INDEX = 2;
    public static Integer EXAM_RESULT_STUDENT_CLASS_ID_SEC_INDEX = 3;
    public static Integer EXAM_RESULT_NAME_SEC_INDEX = 4;
    public static Integer EXAM_RESULT_SCORE_SEC_INDEX = 5;
    public static Integer SPECIAL_SPILT_GRADE = 2;

    /**
     * 优秀分数
     */
    public static BigDecimal EXCELLENT_SUB_SCORE = new BigDecimal("89");


    /**
     * 及格分数
     */
    public static BigDecimal LOW_PASS_SCORE = new BigDecimal("69");
    public static BigDecimal HIGH_PASS_SCORE = new BigDecimal("59");
    /**
     * sup 上确界 sub 下确界
     */
    public static BigDecimal LOW_GOOD_SUP_SCORE = new BigDecimal("89");
    public static BigDecimal LOW_MIDDLE_SUP_SCORE = new BigDecimal("79");

    public static BigDecimal HIGH_GOOD_SUP_SCORE = new BigDecimal("89");
    public static BigDecimal HIGH_MIDDLE_SUP_SCORE = new BigDecimal("74");

    /**
     * 学生信息
     */
    public static String[] STUDENT_DATA_NAME = {"序号", "姓名", "性别"};
    public static String STUDENT_GRADE_CLASS_SPILT_SIGN = "级";
    public static String STUDENT_CLASS_SPILT_SIGN = "班";
    /**
     * 学生年级信息行数
     */
    public static Integer STUDENT_GRADE_CLASS_ROW = 0;
    public static Integer STUDENT_GRADE_CLASS_BEGIN_COL = 0;
    public static Integer STUDENT_GRADE_COL = 0;
    public static Integer STUDENT_CLASS_INFO_COL = 0;
    public static Integer STUDENT_CLASS_COL = 1;
    public static Integer STUDENT_TITLE_ROW = 1;
    public static Integer STUDENT_TITLE_INDEX_COL = 0;
    public static Integer STUDENT_TITLE_NAME_COL = 1;
    public static Integer STUDENT_TITLE_SEX_COL = 2;
    public static Integer STUDENT_BEGIN_ROW = 2;
    public static Integer STUDENT_CLASS_ID_COL = 0;
    public static Integer STUDENT_NAME_COL = 1;
    public static Integer STUDENT_SEX_COL = 2;

}
