package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.enums.BacktrackingTypeEnum;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.service.dto.SubjectConstraintDTO;
import com.work.school.mysql.timetable.service.dto.TeacherConstraintDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author : Growlithe
 * @Date : 2019/9/22 2:29 AM
 * @Description
 */
public class PrepareDTO implements Serializable {
    /**
     * 科目、班级、教师
     */
    private List<SubjectClassTeacherDO> subjectClassTeacherDOList;
    /**
     * 所有科目名称map
     */
    private Map<Integer, SubjectDO> allSubjectMap;
    /**
     * 特殊教室最大容量
     */
    private HashMap<Integer, Integer> classroomMaxCapacityMap;
    /**
     * 教师上课时间列表
     */
    private HashMap<Integer,List<Integer>> teacherOrderListMap;
    /**
     * 课程约束条件
     */
    private List<SubjectConstraintDTO> subjectConstraintDTOList;
    /**
     * 教师约束条件
     */
    private List<TeacherConstraintDTO> teacherConstraintDTOList;
    /**
     * 节点可使用科目表
     */
    private HashMap<Integer,HashMap<Integer, Boolean>> orderSubjectIdCanUseMap;
    /**
     * 排序关系
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 排序关系
     */
    private HashMap<GradeClassNumWorkDayTimeDTO,Integer> gradeClassNumWorkDayTimeOrderMap;
    /**
     * 课程表 order subjectId
     */
    private TreeMap<Integer, Integer> timeTableMap;
    /**
     * 排课方法
     */
    private BacktrackingTypeEnum backtrackingTypeEnum;

    public List<SubjectClassTeacherDO> getSubjectClassTeacherDOList() {
        return subjectClassTeacherDOList;
    }

    public void setSubjectClassTeacherDOList(List<SubjectClassTeacherDO> subjectClassTeacherDOList) {
        this.subjectClassTeacherDOList = subjectClassTeacherDOList;
    }

    public Map<Integer, SubjectDO> getAllSubjectMap() {
        return allSubjectMap;
    }

    public void setAllSubjectMap(Map<Integer, SubjectDO> allSubjectMap) {
        this.allSubjectMap = allSubjectMap;
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

    public List<SubjectConstraintDTO> getSubjectConstraintDTOList() {
        return subjectConstraintDTOList;
    }

    public void setSubjectConstraintDTOList(List<SubjectConstraintDTO> subjectConstraintDTOList) {
        this.subjectConstraintDTOList = subjectConstraintDTOList;
    }

    public List<TeacherConstraintDTO> getTeacherConstraintDTOList() {
        return teacherConstraintDTOList;
    }

    public void setTeacherConstraintDTOList(List<TeacherConstraintDTO> teacherConstraintDTOList) {
        this.teacherConstraintDTOList = teacherConstraintDTOList;
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

    public HashMap<GradeClassNumWorkDayTimeDTO, Integer> getGradeClassNumWorkDayTimeOrderMap() {
        return gradeClassNumWorkDayTimeOrderMap;
    }

    public void setGradeClassNumWorkDayTimeOrderMap(HashMap<GradeClassNumWorkDayTimeDTO, Integer> gradeClassNumWorkDayTimeOrderMap) {
        this.gradeClassNumWorkDayTimeOrderMap = gradeClassNumWorkDayTimeOrderMap;
    }

    public TreeMap<Integer, Integer> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(TreeMap<Integer, Integer> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public BacktrackingTypeEnum getBacktrackingTypeEnum() {
        return backtrackingTypeEnum;
    }

    public void setBacktrackingTypeEnum(BacktrackingTypeEnum backtrackingTypeEnum) {
        this.backtrackingTypeEnum = backtrackingTypeEnum;
    }

    @Override
    public String toString() {
        return "PrepareDTO{" +
                "subjectClassTeacherDOList=" + subjectClassTeacherDOList +
                ", allSubjectMap=" + allSubjectMap +
                ", classroomMaxCapacityMap=" + classroomMaxCapacityMap +
                ", teacherOrderListMap=" + teacherOrderListMap +
                ", subjectConstraintDTOList=" + subjectConstraintDTOList +
                ", teacherConstraintDTOList=" + teacherConstraintDTOList +
                ", orderSubjectIdCanUseMap=" + orderSubjectIdCanUseMap +
                ", orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumWorkDayTimeOrderMap=" + gradeClassNumWorkDayTimeOrderMap +
                ", timeTableMap=" + timeTableMap +
                ", backtrackingTypeEnum=" + backtrackingTypeEnum +
                '}';
    }
}
