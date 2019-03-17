package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 7:23 PM
 * @Description
 */
public class TeacherFreeKeyDTO implements Serializable {
    /**
     * 教师id
     */
    private Integer teacherId;
    /**
     * 第几天
     */
    private Integer workDay;

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeacherFreeKeyDTO that = (TeacherFreeKeyDTO) o;
        return Objects.equals(teacherId, that.teacherId) &&
                Objects.equals(workDay, that.workDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, workDay);
    }

    @Override
    public String toString() {
        return "TeacherFreeKeyDTO{" +
                "teacherId=" + teacherId +
                ", workDay=" + workDay +
                '}';
    }
}
