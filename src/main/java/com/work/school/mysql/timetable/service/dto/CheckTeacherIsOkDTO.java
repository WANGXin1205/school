package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.SubjectGradeClassDTO;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/9/23 6:14 PM
 * @Description
 */
public class CheckTeacherIsOkDTO implements Serializable {
    /**
     * 需要检验的时间
     */
    private Integer time;

    private Integer grade;

    private Integer classNum;

    private Integer workDay;

    private SubjectWeightDTO subjectMaxWeightDTO;

    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;

    private HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

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

    public SubjectWeightDTO getSubjectMaxWeightDTO() {
        return subjectMaxWeightDTO;
    }

    public void setSubjectMaxWeightDTO(SubjectWeightDTO subjectMaxWeightDTO) {
        this.subjectMaxWeightDTO = subjectMaxWeightDTO;
    }

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherMap() {
        return subjectGradeClassTeacherMap;
    }

    public void setSubjectGradeClassTeacherMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        this.subjectGradeClassTeacherMap = subjectGradeClassTeacherMap;
    }

    public HashMap<Integer, HashMap<Integer, List<Integer>>> getTeacherTeachingMap() {
        return teacherTeachingMap;
    }

    public void setTeacherTeachingMap(HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        this.teacherTeachingMap = teacherTeachingMap;
    }

    @Override
    public String toString() {
        return "CheckTeacherIsOkDTO{" +
                "time=" + time +
                ", grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                ", subjectMaxWeightDTO=" + subjectMaxWeightDTO +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                ", teacherTeachingMap=" + teacherTeachingMap +
                '}';
    }
}
