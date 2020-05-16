package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.GradeClassNumWorkDayTimeDTO;

import java.io.Serializable;
import java.util.HashMap;

public class OrderDTO implements Serializable {

    private HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    private HashMap<GradeClassNumWorkDayTimeDTO,Integer> gradeClassNumWorkDayTimeOrderMap;

    public HashMap<Integer, GradeClassNumWorkDayTimeDTO> getOrderGradeClassNumWorkDayTimeMap() {
        return orderGradeClassNumWorkDayTimeMap;
    }

    public void setOrderGradeClassNumWorkDayTimeMap(HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap) {
        this.orderGradeClassNumWorkDayTimeMap = orderGradeClassNumWorkDayTimeMap;
    }

    public HashMap<GradeClassNumWorkDayTimeDTO, Integer> getGradeClassNumWorkDayTimeOrderMap() {
        return gradeClassNumWorkDayTimeOrderMap;
    }

    public void setGradeClassNumWorkDayTimeOrderMap(HashMap<GradeClassNumWorkDayTimeDTO, Integer> gradeClassNumWorkDayTimeOrderMap) {
        this.gradeClassNumWorkDayTimeOrderMap = gradeClassNumWorkDayTimeOrderMap;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumWorkDayTimeOrderMap=" + gradeClassNumWorkDayTimeOrderMap +
                '}';
    }
}
