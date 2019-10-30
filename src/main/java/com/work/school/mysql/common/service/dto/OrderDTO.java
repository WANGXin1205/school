package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/9/22 2:29 AM
 * @Description
 */
public class OrderDTO implements Serializable {

    /**
     * 排序关系
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 排序关系
     */
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
