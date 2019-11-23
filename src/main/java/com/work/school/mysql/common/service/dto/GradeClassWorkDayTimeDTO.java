package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/9/18 9:24 AM
 * @Description
 */
public class GradeClassWorkDayTimeDTO implements Serializable {

    private Integer grade;

    private Integer classNum;

    private Integer workDay;

    private Integer time;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
    }

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeClassWorkDayTimeDTO that = (GradeClassWorkDayTimeDTO) o;
        return Objects.equals(grade, that.grade) &&
                Objects.equals(classNum, that.classNum) &&
                Objects.equals(workDay, that.workDay) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grade, classNum, workDay, time);
    }

    @Override
    public String toString() {
        return "GradeClassWorkDayTimeDTO{" +
                "grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                ", time=" + time +
                '}';
    }
}
