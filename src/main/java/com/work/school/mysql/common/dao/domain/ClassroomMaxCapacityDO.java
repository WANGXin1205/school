package com.work.school.mysql.common.dao.domain;

import java.util.Date;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 10:11 AM
 * @Description 课程信息
 */
public class ClassroomMaxCapacityDO {

    private Integer id;

    private Integer subjectId;

    private Integer maxCapacity;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ClassroomMaxCapacityDO{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", maxCapacity=" + maxCapacity +
                ", status=" + status +
                '}';
    }
}