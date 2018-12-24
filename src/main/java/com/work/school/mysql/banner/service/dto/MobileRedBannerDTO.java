package com.work.school.mysql.banner.service.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author : Growlithe
 * @Date : 2018/12/24 9:29 AM
 * @Description
 */
public class MobileRedBannerDTO implements Serializable {
    /**
     * id 主键
     */
    private Long id;
    /**
     * 班级id
     */
    private Integer classId;
    /**
     * 班级名称
     */
    private String className;
    /**
     * 学期记号
     */
    private Integer schoolTerm;
    /**
     * 学期描述
     */
    private String schoolTermDesc;
    /**
     * 周
     */
    private Integer week;
    /**
     * 流动红旗类型 1 文明 2 路队 3 体育 4 卫生
     */
    private Integer redBannerType;
    /**
     * 流动红旗类型描述
     */
    private String redBannerTypeDesc;
    /**
     * 数据状态 1 有效 0 失效
     */
    private Integer status;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getSchoolTerm() {
        return schoolTerm;
    }

    public void setSchoolTerm(Integer schoolTerm) {
        this.schoolTerm = schoolTerm;
    }

    public String getSchoolTermDesc() {
        return schoolTermDesc;
    }

    public void setSchoolTermDesc(String schoolTermDesc) {
        this.schoolTermDesc = schoolTermDesc;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public Integer getRedBannerType() {
        return redBannerType;
    }

    public void setRedBannerType(Integer redBannerType) {
        this.redBannerType = redBannerType;
    }

    public String getRedBannerTypeDesc() {
        return redBannerTypeDesc;
    }

    public void setRedBannerTypeDesc(String redBannerTypeDesc) {
        this.redBannerTypeDesc = redBannerTypeDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "MobileRedBannerDTO{" +
                "id=" + id +
                ", classId=" + classId +
                ", className='" + className + '\'' +
                ", schoolTerm=" + schoolTerm +
                ", schoolTermDesc='" + schoolTermDesc + '\'' +
                ", week=" + week +
                ", redBannerType=" + redBannerType +
                ", redBannerTypeDesc='" + redBannerTypeDesc + '\'' +
                ", status=" + status +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", updateBy='" + updateBy + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
