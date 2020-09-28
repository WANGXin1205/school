package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/9/18 9:24 AM
 * @Description
 */
public class GradeClassWorkDayDTO implements Serializable {

    private Integer grade;

    private Integer classNum;

    private Integer workDay;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeClassWorkDayDTO that = (GradeClassWorkDayDTO) o;
        return Objects.equals(grade, that.grade) &&
                Objects.equals(classNum, that.classNum) &&
                Objects.equals(workDay, that.workDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grade, classNum, workDay);
    }

    @Override
    public String toString() {
        return "GradeClassWorkDayDTO{" +
                "grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                '}';
    }
}
