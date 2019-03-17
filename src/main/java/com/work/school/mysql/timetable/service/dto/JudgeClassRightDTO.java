package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.ClassSubjectKeyDTO;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;
import com.work.school.mysql.common.service.dto.TeacherFreeKeyDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 9:44 PM
 * @Description
 */
public class JudgeClassRightDTO implements Serializable {

    private Integer workDay;

    private Integer classNum;

    private SubjectWeightDTO subjectWeightDTO;

    private Integer time;

    private Map<TeacherFreeKeyDTO, List<Integer>> teacherFreeMap;

    private Map<ClassSubjectKeyDTO, Integer> classSubjectTeacherMap;

    private HashMap<TimeTableKeyDTO, Integer> timeTableMap;

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

    public SubjectWeightDTO getSubjectWeightDTO() {
        return subjectWeightDTO;
    }

    public void setSubjectWeightDTO(SubjectWeightDTO subjectWeightDTO) {
        this.subjectWeightDTO = subjectWeightDTO;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Map<TeacherFreeKeyDTO, List<Integer>> getTeacherFreeMap() {
        return teacherFreeMap;
    }

    public void setTeacherFreeMap(Map<TeacherFreeKeyDTO, List<Integer>> teacherFreeMap) {
        this.teacherFreeMap = teacherFreeMap;
    }

    public Map<ClassSubjectKeyDTO, Integer> getClassSubjectTeacherMap() {
        return classSubjectTeacherMap;
    }

    public void setClassSubjectTeacherMap(Map<ClassSubjectKeyDTO, Integer> classSubjectTeacherMap) {
        this.classSubjectTeacherMap = classSubjectTeacherMap;
    }

    public HashMap<TimeTableKeyDTO, Integer> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<TimeTableKeyDTO, Integer> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    @Override
    public String toString() {
        return "JudgeClassRightDTO{" +
                "workDay=" + workDay +
                ", classNum=" + classNum +
                ", subjectWeightDTO=" + subjectWeightDTO +
                ", time=" + time +
                ", teacherFreeMap=" + teacherFreeMap +
                ", classSubjectTeacherMap=" + classSubjectTeacherMap +
                ", timeTableMap=" + timeTableMap +
                '}';
    }
}
