package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.GradeClassNumWorkDayTimeDTO;
import com.work.school.mysql.common.service.dto.SubjectDTO;
import com.work.school.mysql.common.service.dto.SubjectGradeClassDTO;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Author : Growlithe
 * @Date : 2019/9/23 6:14 PM
 * @Description
 */
public class CheckCompleteDTO implements Serializable {
    /**
     * 科目
     */
    private SubjectDTO subjectDTO;
    /**
     * 次序
     */
    private Integer order;
    /**
     * 排序关系
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 获取科目使用的Map
     */
    private HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap;
    /**
     * 课程列表
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap;

    private HashMap<Integer,HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap;

    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;

    /**
     * 教师获取所有教师上课时间map order teacherId workday time 中间变量
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> orderTeacherWorkDayTimeMap;
    /**
     * 初始化特殊教室使用情况 order subjectId workDay time count
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap;

    private HashMap<Integer,Integer> classroomMaxCapacity;
    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer,HashMap<Integer, Integer>>>> timeTableMap;

    public SubjectDTO getSubjectDTO() {
        return subjectDTO;
    }

    public void setSubjectDTO(SubjectDTO subjectDTO) {
        this.subjectDTO = subjectDTO;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public HashMap<Integer, GradeClassNumWorkDayTimeDTO> getOrderGradeClassNumWorkDayTimeMap() {
        return orderGradeClassNumWorkDayTimeMap;
    }

    public void setOrderGradeClassNumWorkDayTimeMap(HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap) {
        this.orderGradeClassNumWorkDayTimeMap = orderGradeClassNumWorkDayTimeMap;
    }

    public HashMap<Integer, HashMap<Integer, Boolean>> getOrderSubjectIdCanUseMap() {
        return orderSubjectIdCanUseMap;
    }

    public void setOrderSubjectIdCanUseMap(HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        this.orderSubjectIdCanUseMap = orderSubjectIdCanUseMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getGradeClassNumSubjectFrequencyMap() {
        return gradeClassNumSubjectFrequencyMap;
    }

    public void setGradeClassNumSubjectFrequencyMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        this.gradeClassNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap;
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

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getOrderTeacherWorkDayTimeMap() {
        return orderTeacherWorkDayTimeMap;
    }

    public void setOrderTeacherWorkDayTimeMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> orderTeacherWorkDayTimeMap) {
        this.orderTeacherWorkDayTimeMap = orderTeacherWorkDayTimeMap;
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

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }
}
