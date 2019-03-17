package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 10:11 AM
 * @Description
 */
public class ClassSubjectKeyDTO implements Serializable {

    private Integer classNum;

    private Integer subjectId;

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassSubjectKeyDTO that = (ClassSubjectKeyDTO) o;
        return Objects.equals(classNum, that.classNum) &&
                Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classNum, subjectId);
    }

    @Override
    public String toString() {
        return "ClassSubjectKeyDTO{" +
                "classNum=" + classNum +
                ", subjectId=" + subjectId +
                '}';
    }
}
