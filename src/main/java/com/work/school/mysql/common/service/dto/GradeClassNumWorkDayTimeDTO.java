package com.work.school.mysql.common.service.dto;

/**
 * @Author : Growlithe
 * @Date : 2019/9/24 11:58 PM
 * @Description
 */
public class GradeClassNumWorkDayTimeDTO {

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
    public String toString() {
        return "GradeClassNumWorkDayTimeDTO{" +
                "grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                ", time=" + time +
                '}';
    }
}
