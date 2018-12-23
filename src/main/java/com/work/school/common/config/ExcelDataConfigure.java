package com.work.school.common.config;

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
    public static String[] RED_BANNER_DATA_NAME = {"周","文明之星","路队之星","体育之星","卫生之星"};

    /**
     * 流动红旗excel默认值
     */
    public static Integer RED_BANNER_WEEK_INDEX = 0;
    public static Integer RED_BANNER_BEST_INDEX = 1;
    public static Integer RED_BANNER_TEAM_INDEX = 2;
    public static Integer RED_BANNER_SPORT_INDEX = 3;
    public static Integer RED_BANNER_HEALTH_INDEX = 4;

}
