package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.timetable.service.dto.TimeTableKeyDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/3/11 10:29 AM
 * @Description
 */
public class InitSubjectWeightDTO {

    private Integer workDay;

    private Integer classNum;

    private Integer time;

    private List<SubjectWeightDTO> subjectWeightDTOList;

    private HashMap<TimeTableKeyDTO,Integer> timeTableMap;

    private Map<ClassSubjectKeyDTO, Integer> classSubjectTeachingNumMap;

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

    public List<SubjectWeightDTO> getSubjectWeightDTOList() {
        return subjectWeightDTOList;
    }

    public void setSubjectWeightDTOList(List<SubjectWeightDTO> subjectWeightDTOList) {
        this.subjectWeightDTOList = subjectWeightDTOList;
    }

    public HashMap<TimeTableKeyDTO, Integer> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<TimeTableKeyDTO, Integer> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public Map<ClassSubjectKeyDTO, Integer> getClassSubjectTeachingNumMap() {
        return classSubjectTeachingNumMap;
    }

    public void setClassSubjectTeachingNumMap(Map<ClassSubjectKeyDTO, Integer> classSubjectTeachingNumMap) {
        this.classSubjectTeachingNumMap = classSubjectTeachingNumMap;
    }

    @Override
    public String toString() {
        return "InitSubjectWeightDTO{" +
                "workDay=" + workDay +
                ", classNum=" + classNum +
                ", time=" + time +
                ", subjectWeightDTOList=" + subjectWeightDTOList +
                ", timeTableMap=" + timeTableMap +
                ", classSubjectTeachingNumMap=" + classSubjectTeachingNumMap +
                '}';
    }
}
