package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.dto.SubjectDTO;
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
public class CheckCompleteUseBacktrackingDTO implements Serializable {
    /**
     * 需要检验的时间
     */
    private Integer time;

    private Integer grade;

    private Integer classNum;

    private Integer workDay;

    private Integer subjectId;
    /**
     * 课程列表
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap;

    private HashMap<Integer,HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap;

    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;

    private HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap;
    /**
     * 初始化特殊教室使用情况
     */
    private HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>> classroomUsedCountMap;

    private HashMap<Integer,Integer> classroomMaxCapacity;

    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer,HashMap<Integer, Integer>>>> timeTableMap;

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

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public HashMap<Integer, HashMap<Integer, SubjectDTO>> getGradeSubjectDTOMap() {
        return gradeSubjectDTOMap;
    }

    public void setGradeSubjectDTOMap(HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap) {
        this.gradeSubjectDTOMap = gradeSubjectDTOMap;
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

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getClassroomUsedCountMap() {
        return classroomUsedCountMap;
    }

    public void setClassroomUsedCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> classroomUsedCountMap) {
        this.classroomUsedCountMap = classroomUsedCountMap;
    }

    public HashMap<Integer, Integer> getClassroomMaxCapacity() {
        return classroomMaxCapacity;
    }

    public void setClassroomMaxCapacity(HashMap<Integer, Integer> classroomMaxCapacity) {
        this.classroomMaxCapacity = classroomMaxCapacity;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getGradeClassNumSubjectFrequencyMap() {
        return gradeClassNumSubjectFrequencyMap;
    }

    public void setGradeClassNumSubjectFrequencyMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        this.gradeClassNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }


}
