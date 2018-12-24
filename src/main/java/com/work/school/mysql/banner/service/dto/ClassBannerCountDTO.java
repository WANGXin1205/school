package com.work.school.mysql.banner.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author : Growlithe
 * @Date : 2018/12/24 9:36 AM
 * @Description
 */
public class ClassBannerCountDTO implements Serializable {
    /**
     * 班级id
     */
    private Integer classId;
    /**
     * 班级名称
     */
    private String className;
    /**
     * 文明之星次数
     */
    private Integer bestBannerCount;
    /**
     * 其他之星次数
     */
    private Integer otherBannerCount;
    /**
     * 分数
     */
    private BigDecimal score;
    /**
     * 流动红旗描述
     */
    private String bannerDesc;

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getBestBannerCount() {
        return bestBannerCount;
    }

    public void setBestBannerCount(Integer bestBannerCount) {
        this.bestBannerCount = bestBannerCount;
    }

    public Integer getOtherBannerCount() {
        return otherBannerCount;
    }

    public void setOtherBannerCount(Integer otherBannerCount) {
        this.otherBannerCount = otherBannerCount;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getBannerDesc() {
        return bannerDesc;
    }

    public void setBannerDesc(String bannerDesc) {
        this.bannerDesc = bannerDesc;
    }

    @Override
    public String toString() {
        return "ClassBannerCountDTO{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", bestBannerCount=" + bestBannerCount +
                ", otherBannerCount=" + otherBannerCount +
                ", score=" + score +
                ", bannerDesc='" + bannerDesc + '\'' +
                '}';
    }
}
