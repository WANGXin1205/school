package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.service.dto.SubjectConstraintDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AddSubjectConstraintDTO implements Serializable {
    /**
     * 顺序
     */
    private Integer order;
    /**
     * 选择的科目
     */
    private Integer subjectId;
    /**
     * 约束条件列表
     */
    private List<SubjectConstraintDTO> subjectConstraintDTOList;
    /**
     * 科目map
     */
    private Map<Integer, SubjectDO> allSubjectMap;
    /**
     * 科目班级教师关系
     */
    private List<SubjectClassTeacherDO> subjectClassTeacherDOList;
    /**
     * 教师上课时间列表
     */
    private HashMap<Integer,List<Integer>> teacherOrderListMap;
    /**
     * 排序关系
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 排序关系
     */
    private HashMap<GradeClassNumWorkDayTimeDTO,Integer> gradeClassNumWorkDayTimeOrderMap;
    /**
     * 课程表
     */
    private TreeMap<Integer,Integer> timetableMap;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public List<SubjectConstraintDTO> getSubjectConstraintDTOList() {
        return subjectConstraintDTOList;
    }

    public void setSubjectConstraintDTOList(List<SubjectConstraintDTO> subjectConstraintDTOList) {
        this.subjectConstraintDTOList = subjectConstraintDTOList;
    }

    public Map<Integer, SubjectDO> getAllSubjectMap() {
        return allSubjectMap;
    }

    public void setAllSubjectMap(Map<Integer, SubjectDO> allSubjectMap) {
        this.allSubjectMap = allSubjectMap;
    }

    public List<SubjectClassTeacherDO> getSubjectClassTeacherDOList() {
        return subjectClassTeacherDOList;
    }

    public void setSubjectClassTeacherDOList(List<SubjectClassTeacherDO> subjectClassTeacherDOList) {
        this.subjectClassTeacherDOList = subjectClassTeacherDOList;
    }

    public HashMap<Integer, List<Integer>> getTeacherOrderListMap() {
        return teacherOrderListMap;
    }

    public void setTeacherOrderListMap(HashMap<Integer, List<Integer>> teacherOrderListMap) {
        this.teacherOrderListMap = teacherOrderListMap;
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

    public TreeMap<Integer, Integer> getTimetableMap() {
        return timetableMap;
    }

    public void setTimetableMap(TreeMap<Integer, Integer> timetableMap) {
        this.timetableMap = timetableMap;
    }

    @Override
    public String toString() {
        return "AddSubjectConstraintDTO{" +
                "order=" + order +
                ", subjectId=" + subjectId +
                ", subjectConstraintDTOList=" + subjectConstraintDTOList +
                ", allSubjectMap=" + allSubjectMap +
                ", subjectClassTeacherDOList=" + subjectClassTeacherDOList +
                ", teacherOrderListMap=" + teacherOrderListMap +
                ", orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumWorkDayTimeOrderMap=" + gradeClassNumWorkDayTimeOrderMap +
                ", timetableMap=" + timetableMap +
                '}';
    }
}
