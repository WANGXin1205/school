package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CheckMatchDTO implements Serializable {
    /**
     * 选择的科目
     */
    private Integer subjectId;
    /**
     * 顺序
     */
    private Integer order;
    /**
     * 科目信息
     */
    private Map<Integer, SubjectDO> allSubjectMap;
    /**
     * 年级 班级 日期 节次
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 排序关系
     */
    private HashMap<GradeClassNumWorkDayTimeDTO,Integer> gradeClassNumWorkDayTimeOrderMap;
    /**
     * 科目班级教师关系
     */
    private List<SubjectClassTeacherDO> subjectClassTeacherDOList;
    /**
     * 特殊教室最大容量
     */
    private HashMap<Integer, Integer> classroomMaxCapacityMap;
    /**
     * 教师上课时间列表
     */
    private HashMap<Integer,List<Integer>> teacherOrderListMap;
    /**
     * 课程表
     */
    private TreeMap<Integer,Integer> timetableMap;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Map<Integer, SubjectDO> getAllSubjectMap() {
        return allSubjectMap;
    }

    public void setAllSubjectMap(Map<Integer, SubjectDO> allSubjectMap) {
        this.allSubjectMap = allSubjectMap;
    }

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

    public List<SubjectClassTeacherDO> getSubjectClassTeacherDOList() {
        return subjectClassTeacherDOList;
    }

    public void setSubjectClassTeacherDOList(List<SubjectClassTeacherDO> subjectClassTeacherDOList) {
        this.subjectClassTeacherDOList = subjectClassTeacherDOList;
    }

    public HashMap<Integer, Integer> getClassroomMaxCapacityMap() {
        return classroomMaxCapacityMap;
    }

    public void setClassroomMaxCapacityMap(HashMap<Integer, Integer> classroomMaxCapacityMap) {
        this.classroomMaxCapacityMap = classroomMaxCapacityMap;
    }

    public HashMap<Integer, List<Integer>> getTeacherOrderListMap() {
        return teacherOrderListMap;
    }

    public void setTeacherOrderListMap(HashMap<Integer, List<Integer>> teacherOrderListMap) {
        this.teacherOrderListMap = teacherOrderListMap;
    }

    public TreeMap<Integer, Integer> getTimetableMap() {
        return timetableMap;
    }

    public void setTimetableMap(TreeMap<Integer, Integer> timetableMap) {
        this.timetableMap = timetableMap;
    }

    @Override
    public String toString() {
        return "CheckMatchDTO{" +
                "subjectId=" + subjectId +
                ", order=" + order +
                ", allSubjectMap=" + allSubjectMap +
                ", orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumWorkDayTimeOrderMap=" + gradeClassNumWorkDayTimeOrderMap +
                ", subjectClassTeacherDOList=" + subjectClassTeacherDOList +
                ", classroomMaxCapacityMap=" + classroomMaxCapacityMap +
                ", teacherOrderListMap=" + teacherOrderListMap +
                ", timetableMap=" + timetableMap +
                '}';
    }
}
