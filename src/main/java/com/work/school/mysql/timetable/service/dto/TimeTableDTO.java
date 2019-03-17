package com.work.school.mysql.timetable.service.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:30 PM
 * @Description
 */
public class TimeTableDTO implements Serializable {

    private Integer workDay;

    private String workDayDesc;

    private Integer classNum;

    private String className;

    private HashMap<TimeTableKeyDTO,String> timeTableShowMap;

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    public String getWorkDayDesc() {
        return workDayDesc;
    }

    public void setWorkDayDesc(String workDayDesc) {
        this.workDayDesc = workDayDesc;
    }

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public HashMap<TimeTableKeyDTO, String> getTimeTableShowMap() {
        return timeTableShowMap;
    }

    public void setTimeTableShowMap(HashMap<TimeTableKeyDTO, String> timeTableShowMap) {
        this.timeTableShowMap = timeTableShowMap;
    }

    @Override
    public String toString() {
        return "TimeTableDTO{" +
                "workDay=" + workDay +
                ", workDayDesc='" + workDayDesc + '\'' +
                ", classNum=" + classNum +
                ", className='" + className + '\'' +
                ",  timeTableShowMap=" + timeTableShowMap +
                '}';
    }
}
