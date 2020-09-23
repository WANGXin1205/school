package com.work.school.mysql.banner.service.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 21:38
 * @Description 流动红旗DTO
 */
public class MobileRedBannerDTO implements Serializable {
    /**
     * 班级名称
     */
    private String gradeClassName;
    /**
     * 流动红旗类型 1 文明 2 路队 3 体育 4 卫生
     */
    private HashMap<String,Integer> redBannerCount;

    public String getGradeClassName() {
        return gradeClassName;
    }

    public void setGradeClassName(String gradeClassName) {
        this.gradeClassName = gradeClassName;
    }

    public HashMap<String, Integer> getRedBannerCount() {
        return redBannerCount;
    }

    public void setRedBannerCount(HashMap<String, Integer> redBannerCount) {
        this.redBannerCount = redBannerCount;
    }

    @Override
    public String toString() {
        return "MobileRedBannerDTO{" +
                "gradeClassName='" + gradeClassName + '\'' +
                ", redBannerCount=" + redBannerCount +
                '}';
    }
}