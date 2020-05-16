package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.timetable.service.dto.TimeTableConstraintDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/9/22 2:29 AM
 * @Description
 */
public class TimeTablingUseFCDWBacktrackingDTO implements Serializable {
    /**
     * 查询所有年级下的班级
     */
    private List<ClassInfoDO> allGradeClassInfo;
    /**
     * 获取所有年级下属班级数目
     */
    private HashMap<Integer, Integer> gradeClassCountMap;
    /**
     * 所有的课程
     */
    private List<SubjectDO> allSubject;
    /**
     * 所有课程按照grade的map
     */
    private Map<Integer, List<SubjectDTO>> gradeSubjectMap;
    /**
     * 课程Map
     */
    private HashMap<Integer,HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap;
    /**
     * 所有科目对应名称map
     */
    private Map<Integer, String> allSubjectNameMap;
    /**
     * 所有课程按照grade，classNum,workDay，Subject count 的Map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorkDaySubjectCountMap;
    /**
     * 所有上课的教师
     */
    private List<TeacherDO> allWorkTeacher;
    /**
     * 查询所有科目，教师，年级，班级之间的关系
     */
    private List<SubjectTeacherGradeClassDTO> allSubjectTeacherGradeClassDTO;
    /**
     * 获取科目年级班级对应教师的map
     */
    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;
    /**
     * 获取科目年级班级对应教师所带班级数目的map
     */
    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap;
    /**
     * 教师所带年级班级和科目map
     */
    private HashMap<Integer, List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap;
    /**
     * 年级班级下对应课程权重的map
     */
    private HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap;
    /**
     * 年级班级下对应课程次数的map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap;
    /**
     * 小课教室最大容量
     */
    private HashMap<Integer, Integer> classroomMaxCapacityMap;
    /**
     * 教师获取所有教师上课时间map order teacherId workday time 中间变量
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> orderTeacherWorkDayTimeMap;
    /**
     * 初始化特殊教室使用情况 order subjectId workDay time count
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap;
    /**
     * 获取科目使用的Map
     */
    HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap;
    /**
     * 排序关系
     */
    private HashMap<Integer,GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap;
    /**
     * 排序关系2
     */
    private HashMap<GradeClassNumWorkDayTimeDTO,Integer> gradeClassNumWorkDayTimeOrderMap;
    /**
     * 约束条件
     */
    private List<TimeTableConstraintDTO> timeTableConstraintDTOList;
    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap;
    /**
     * 课程表 order
     */
    private HashMap<Integer,Integer> orderSubjectIdMap;

    public List<ClassInfoDO> getAllGradeClassInfo() {
        return allGradeClassInfo;
    }

    public void setAllGradeClassInfo(List<ClassInfoDO> allGradeClassInfo) {
        this.allGradeClassInfo = allGradeClassInfo;
    }

    public HashMap<Integer, Integer> getGradeClassCountMap() {
        return gradeClassCountMap;
    }

    public void setGradeClassCountMap(HashMap<Integer, Integer> gradeClassCountMap) {
        this.gradeClassCountMap = gradeClassCountMap;
    }

    public List<SubjectDO> getAllSubject() {
        return allSubject;
    }

    public void setAllSubject(List<SubjectDO> allSubject) {
        this.allSubject = allSubject;
    }

    public Map<Integer, List<SubjectDTO>> getGradeSubjectMap() {
        return gradeSubjectMap;
    }

    public void setGradeSubjectMap(Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        this.gradeSubjectMap = gradeSubjectMap;
    }

    public HashMap<Integer, HashMap<Integer, SubjectDTO>> getGradeSubjectDTOMap() {
        return gradeSubjectDTOMap;
    }

    public void setGradeSubjectDTOMap(HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap) {
        this.gradeSubjectDTOMap = gradeSubjectDTOMap;
    }

    public Map<Integer, String> getAllSubjectNameMap() {
        return allSubjectNameMap;
    }

    public void setAllSubjectNameMap(Map<Integer, String> allSubjectNameMap) {
        this.allSubjectNameMap = allSubjectNameMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getGradeClassNumWorkDaySubjectCountMap() {
        return gradeClassNumWorkDaySubjectCountMap;
    }

    public void setGradeClassNumWorkDaySubjectCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorkDaySubjectCountMap) {
        this.gradeClassNumWorkDaySubjectCountMap = gradeClassNumWorkDaySubjectCountMap;
    }

    public List<TeacherDO> getAllWorkTeacher() {
        return allWorkTeacher;
    }

    public void setAllWorkTeacher(List<TeacherDO> allWorkTeacher) {
        this.allWorkTeacher = allWorkTeacher;
    }

    public List<SubjectTeacherGradeClassDTO> getAllSubjectTeacherGradeClassDTO() {
        return allSubjectTeacherGradeClassDTO;
    }

    public void setAllSubjectTeacherGradeClassDTO(List<SubjectTeacherGradeClassDTO> allSubjectTeacherGradeClassDTO) {
        this.allSubjectTeacherGradeClassDTO = allSubjectTeacherGradeClassDTO;
    }

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherMap() {
        return subjectGradeClassTeacherMap;
    }

    public void setSubjectGradeClassTeacherMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        this.subjectGradeClassTeacherMap = subjectGradeClassTeacherMap;
    }

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherCountMap() {
        return subjectGradeClassTeacherCountMap;
    }

    public void setSubjectGradeClassTeacherCountMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap) {
        this.subjectGradeClassTeacherCountMap = subjectGradeClassTeacherCountMap;
    }

    public HashMap<Integer, List<SubjectTeacherGradeClassDTO>> getTeacherSubjectListMap() {
        return teacherSubjectListMap;
    }

    public void setTeacherSubjectListMap(HashMap<Integer, List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap) {
        this.teacherSubjectListMap = teacherSubjectListMap;
    }

    public HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> getGradeClassSubjectWeightMap() {
        return gradeClassSubjectWeightMap;
    }

    public void setGradeClassSubjectWeightMap(HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap) {
        this.gradeClassSubjectWeightMap = gradeClassSubjectWeightMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getGradeClassNumSubjectFrequencyMap() {
        return gradeClassNumSubjectFrequencyMap;
    }

    public void setGradeClassNumSubjectFrequencyMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        this.gradeClassNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap;
    }

    public HashMap<Integer, Integer> getClassroomMaxCapacityMap() {
        return classroomMaxCapacityMap;
    }

    public void setClassroomMaxCapacityMap(HashMap<Integer, Integer> classroomMaxCapacityMap) {
        this.classroomMaxCapacityMap = classroomMaxCapacityMap;
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

    public List<TimeTableConstraintDTO> getTimeTableConstraintDTOList() {
        return timeTableConstraintDTOList;
    }

    public void setTimeTableConstraintDTOList(List<TimeTableConstraintDTO> timeTableConstraintDTOList) {
        this.timeTableConstraintDTOList = timeTableConstraintDTOList;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public HashMap<Integer, Integer> getOrderSubjectIdMap() {
        return orderSubjectIdMap;
    }

    public void setOrderSubjectIdMap(HashMap<Integer, Integer> orderSubjectIdMap) {
        this.orderSubjectIdMap = orderSubjectIdMap;
    }

    @Override
    public String toString() {
        return "TimeTablingUseFCDWBacktrackingDTO{" +
                "allGradeClassInfo=" + allGradeClassInfo +
                ", gradeClassCountMap=" + gradeClassCountMap +
                ", allSubject=" + allSubject +
                ", gradeSubjectMap=" + gradeSubjectMap +
                ", gradeSubjectDTOMap=" + gradeSubjectDTOMap +
                ", allSubjectNameMap=" + allSubjectNameMap +
                ", gradeClassNumWorkDaySubjectCountMap=" + gradeClassNumWorkDaySubjectCountMap +
                ", allWorkTeacher=" + allWorkTeacher +
                ", allSubjectTeacherGradeClassDTO=" + allSubjectTeacherGradeClassDTO +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                ", subjectGradeClassTeacherCountMap=" + subjectGradeClassTeacherCountMap +
                ", teacherSubjectListMap=" + teacherSubjectListMap +
                ", gradeClassSubjectWeightMap=" + gradeClassSubjectWeightMap +
                ", gradeClassNumSubjectFrequencyMap=" + gradeClassNumSubjectFrequencyMap +
                ", classroomMaxCapacityMap=" + classroomMaxCapacityMap +
                ", orderTeacherWorkDayTimeMap=" + orderTeacherWorkDayTimeMap +
                ", orderClassRoomUsedCountMap=" + orderClassRoomUsedCountMap +
                ", orderSubjectIdCanUseMap=" + orderSubjectIdCanUseMap +
                ", orderGradeClassNumWorkDayTimeMap=" + orderGradeClassNumWorkDayTimeMap +
                ", gradeClassNumWorkDayTimeOrderMap=" + gradeClassNumWorkDayTimeOrderMap +
                ", timeTableConstraintDTOList=" + timeTableConstraintDTOList +
                ", timeTableMap=" + timeTableMap +
                ", orderSubjectIdMap=" + orderSubjectIdMap +
                '}';
    }

}
