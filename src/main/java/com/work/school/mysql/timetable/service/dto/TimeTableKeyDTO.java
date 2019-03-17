package com.work.school.mysql.timetable.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 8:20 AM
 * @Description
 */
public class TimeTableKeyDTO implements Serializable {

    private Integer workDay;

    private Integer classNum;

    private Integer time;

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
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
        TimeTableKeyDTO that = (TimeTableKeyDTO) o;
        return Objects.equals(workDay, that.workDay) &&
                Objects.equals(classNum, that.classNum) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workDay, classNum, time);
    }

    @Override
    public String toString() {
        return "TimeTableKeyDTO{" +
                "workDay=" + workDay +
                ", classNum=" + classNum +
                ", time=" + time +
                '}';
    }
}
