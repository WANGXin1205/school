package com.work.school.mysql.timetable.service.dto;

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
public class CheckAllCompleteIsOkDTO implements Serializable {
    /**
     * 需要检验的时间
     */
    private Integer time;

    private Integer grade;

    private Integer classNum;

    private Integer workDay;

    private SubjectWeightDTO subjectMaxWeightDTO;

    /**
     * 所有课程按照grade的map
     */
    private HashMap<Integer,HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap;

    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;

    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap;
    /**
     * 初始化特殊教室使用情况 order subjectId workDay time count
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap;

    private HashMap<Integer,Integer> classroomMaxCapacity;

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

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getOrderClassRoomUsedCountMap() {
        return orderClassRoomUsedCountMap;
    }

    public void setOrderClassRoomUsedCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap) {
        this.orderClassRoomUsedCountMap = orderClassRoomUsedCountMap;
    }

    public HashMap<Integer, Integer> getClassroomMaxCapacity() {
        return classroomMaxCapacity;
    }

    public void setClassroomMaxCapacity(HashMap<Integer, Integer> classroomMaxCapacity) {
        this.classroomMaxCapacity = classroomMaxCapacity;
    }

    @Override
    public String toString() {
        return "CheckAllCompleteIsOkDTO{" +
                "time=" + time +
                ", grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                ", subjectMaxWeightDTO=" + subjectMaxWeightDTO +
                ", gradeSubjectDTOMap=" + gradeSubjectDTOMap +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                ", timeTableMap=" + timeTableMap +
                ", orderClassRoomUsedCountMap=" + orderClassRoomUsedCountMap +
                ", classroomMaxCapacity=" + classroomMaxCapacity +
                '}';
    }
}
