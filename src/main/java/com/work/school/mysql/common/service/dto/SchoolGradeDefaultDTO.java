package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.timetable.service.dto.TimeTableDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/3/16 11:43 AM
 * @Description 获取学校某个年级下的默认信息
 */
public class SchoolGradeDefaultDTO implements Serializable {

    /**
     * 一周的工作天数
     */
    private static final Integer workDay = 5;
    /**
     * 每天上课节数
     */
    private static final Integer time = 7;
    /**
     * 某年级下所有的班级信息
     */
    private List<ClassInfoDO> allClassInfoList;
    /**
     * 某年级下所有的科目信息
     */
    private List<SubjectDO> allSubjectList;
    /**
     * 某年级下所有的教师信息
     */
    private List<TeacherDO> allTeacherList;
    /**
     * 某年级下班级数目的关系
     */
    private Integer classSize;
    /**
     * 某年级下班级id 和 班级的map
     */
    private Map<Integer,ClassInfoDO> allClassMap;
    /**
     * 某年级下科目id和 科目的map
     */
    private Map<Integer,SubjectDO> allSubjectMap;
    /**
     * 某年级下所有教师id 和教师map
     */
    private Map<Integer, TeacherDO> allTeacherMap;
    /**
     * 某年级下所有教师id 和教师上课节数map
     */
    private Map<TeacherFreeKeyDTO,List<Integer>> allTeacherFreeMap;
    /**
     * 某年级下所有课程权重表
     */
    private List<SubjectWeightDTO> allSubjectWeightList;
    /**
     * 某个班某个课程是和老师对应关系 的map
     */
    private Map<ClassSubjectKeyDTO,Integer> classSubjectTeacherMap;
    /**
     * 获取某个课程某个班的老师 所带班级数目 的map
     */
    private Map<ClassSubjectKeyDTO,Integer> classSubjectTeachingNumMap;

    public static Integer getWorkDay() {
        return workDay;
    }

    public static Integer getTime() {
        return time;
    }

    public List<ClassInfoDO> getAllClassInfoList() {
        return allClassInfoList;
    }

    public void setAllClassInfoList(List<ClassInfoDO> allClassInfoList) {
        this.allClassInfoList = allClassInfoList;
    }

    public List<SubjectDO> getAllSubjectList() {
        return allSubjectList;
    }

    public void setAllSubjectList(List<SubjectDO> allSubjectList) {
        this.allSubjectList = allSubjectList;
    }

    public List<TeacherDO> getAllTeacherList() {
        return allTeacherList;
    }

    public void setAllTeacherList(List<TeacherDO> allTeacherList) {
        this.allTeacherList = allTeacherList;
    }

    public Integer getClassSize() {
        return classSize;
    }

    public void setClassSize(Integer classSize) {
        this.classSize = classSize;
    }

    public Map<Integer, ClassInfoDO> getAllClassMap() {
        return allClassMap;
    }

    public void setAllClassMap(Map<Integer, ClassInfoDO> allClassMap) {
        this.allClassMap = allClassMap;
    }

    public Map<Integer, SubjectDO> getAllSubjectMap() {
        return allSubjectMap;
    }

    public void setAllSubjectMap(Map<Integer, SubjectDO> allSubjectMap) {
        this.allSubjectMap = allSubjectMap;
    }

    public Map<Integer, TeacherDO> getAllTeacherMap() {
        return allTeacherMap;
    }

    public void setAllTeacherMap(Map<Integer, TeacherDO> allTeacherMap) {
        this.allTeacherMap = allTeacherMap;
    }

    public Map<TeacherFreeKeyDTO, List<Integer>> getAllTeacherFreeMap() {
        return allTeacherFreeMap;
    }

    public void setAllTeacherFreeMap(Map<TeacherFreeKeyDTO, List<Integer>> allTeacherFreeMap) {
        this.allTeacherFreeMap = allTeacherFreeMap;
    }

    public List<SubjectWeightDTO> getAllSubjectWeightList() {
        return allSubjectWeightList;
    }

    public void setAllSubjectWeightList(List<SubjectWeightDTO> allSubjectWeightList) {
        this.allSubjectWeightList = allSubjectWeightList;
    }

    public Map<ClassSubjectKeyDTO, Integer> getClassSubjectTeacherMap() {
        return classSubjectTeacherMap;
    }

    public void setClassSubjectTeacherMap(Map<ClassSubjectKeyDTO, Integer> classSubjectTeacherMap) {
        this.classSubjectTeacherMap = classSubjectTeacherMap;
    }

    public Map<ClassSubjectKeyDTO, Integer> getClassSubjectTeachingNumMap() {
        return classSubjectTeachingNumMap;
    }

    public void setClassSubjectTeachingNumMap(Map<ClassSubjectKeyDTO, Integer> classSubjectTeachingNumMap) {
        this.classSubjectTeachingNumMap = classSubjectTeachingNumMap;
    }

    @Override
    public String toString() {
        return "SchoolGradeDefaultDTO{" +
                "allClassInfoList=" + allClassInfoList +
                ", allSubjectList=" + allSubjectList +
                ", allTeacherList=" + allTeacherList +
                ", classSize=" + classSize +
                ", allClassMap=" + allClassMap +
                ", allSubjectMap=" + allSubjectMap +
                ", allTeacherMap=" + allTeacherMap +
                ", allTeacherFreeMap=" + allTeacherFreeMap +
                ", allSubjectWeightList=" + allSubjectWeightList +
                ", classSubjectTeacherMap=" + classSubjectTeacherMap +
                ", classSubjectTeachingNumMap=" + classSubjectTeachingNumMap +
                '}';
    }
}
