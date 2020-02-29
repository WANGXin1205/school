package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.GradeClassNumWorkDayTimeDTO;

import java.io.Serializable;
import java.util.HashMap;

public class RollbackDTO implements Serializable {
    /**
     * 序列
     */
    private Integer order;
    /**
     * 获取科目使用的Map
     */
    private HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap;
    /**
     * 排序关系
     */
    private HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 年级班级下对应课程次数的map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap;
    /**
     * 初始化特殊教室使用情况 order subjectId workDay time count
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap;
    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public HashMap<Integer, HashMap<Integer, Boolean>> getOrderSubjectIdCanUseMap() {
        return orderSubjectIdCanUseMap;
    }

    public void setOrderSubjectIdCanUseMap(HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        this.orderSubjectIdCanUseMap = orderSubjectIdCanUseMap;
    }

    public HashMap<Integer, GradeClassNumWorkDayTimeDTO> getOrderGradeClassNumWorkDayTimeMap() {
        return orderGradeClassNumWorkDayTimeMap;
    }

    public void setOrderGradeClassNumWorkDayTimeMap(HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap) {
        this.orderGradeClassNumWorkDayTimeMap = orderGradeClassNumWorkDayTimeMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getGradeClassNumSubjectFrequencyMap() {
        return gradeClassNumSubjectFrequencyMap;
    }

    public void setGradeClassNumSubjectFrequencyMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        this.gradeClassNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getOrderClassRoomUsedCountMap() {
        return orderClassRoomUsedCountMap;
    }

    public void setOrderClassRoomUsedCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap) {
        this.orderClassRoomUsedCountMap = orderClassRoomUsedCountMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    @Override
    public String toString() {
        return "RollbackDTO{" +
                "order=" + order +
                ", orderSubjectIdCanUseMap=" + orderSubjectIdCanUseMap +
                ", orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumSubjectFrequencyMap=" + gradeClassNumSubjectFrequencyMap +
                ", orderClassRoomUsedCountMap=" + orderClassRoomUsedCountMap +
                ", timeTableMap=" + timeTableMap +
                '}';
    }
}
