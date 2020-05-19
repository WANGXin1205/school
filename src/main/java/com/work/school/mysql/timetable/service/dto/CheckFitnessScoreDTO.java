package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.SubjectDTO;
import com.work.school.mysql.common.service.dto.SubjectGradeClassDTO;

import java.io.Serializable;
import java.util.HashMap;

public class CheckFitnessScoreDTO implements Serializable {
    /**
     * 获取所有年级下属班级数目
     */
    private HashMap<Integer, Integer> gradeClassCountMap;
    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap;
    /**
     * 年级和课程关系
     */
    private HashMap<Integer,HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap;
    /**
     * 课程上限
     */
    private HashMap<Integer,Integer> classroomMaxCapacity;
    /**
     * 获取科目年级班级对应教师的map
     */
    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;

    public HashMap<Integer, Integer> getGradeClassCountMap() {
        return gradeClassCountMap;
    }

    public void setGradeClassCountMap(HashMap<Integer, Integer> gradeClassCountMap) {
        this.gradeClassCountMap = gradeClassCountMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public HashMap<Integer, HashMap<Integer, SubjectDTO>> getGradeSubjectDTOMap() {
        return gradeSubjectDTOMap;
    }

    public void setGradeSubjectDTOMap(HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap) {
        this.gradeSubjectDTOMap = gradeSubjectDTOMap;
    }

    public HashMap<Integer, Integer> getClassroomMaxCapacity() {
        return classroomMaxCapacity;
    }

    public void setClassroomMaxCapacity(HashMap<Integer, Integer> classroomMaxCapacity) {
        this.classroomMaxCapacity = classroomMaxCapacity;
    }

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherMap() {
        return subjectGradeClassTeacherMap;
    }

    public void setSubjectGradeClassTeacherMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        this.subjectGradeClassTeacherMap = subjectGradeClassTeacherMap;
    }

    @Override
    public String toString() {
        return "CheckFitnessScoreDTO{" +
                "gradeClassCountMap=" + gradeClassCountMap +
                ", timeTableMap=" + timeTableMap +
                ", gradeSubjectDTOMap=" + gradeSubjectDTOMap +
                ", classroomMaxCapacity=" + classroomMaxCapacity +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                '}';
    }
}
