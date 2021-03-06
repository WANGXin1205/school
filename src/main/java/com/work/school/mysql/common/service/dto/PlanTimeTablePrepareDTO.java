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
public class PlanTimeTablePrepareDTO implements Serializable {
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
     * 所有科目对应名称map
     */
    private Map<Integer, String> allSubjectNameMap;
    /**
     * 所有课程按照grade的map
     */
    private Map<Integer, List<SubjectDTO>> gradeSubjectMap;
    /**
     * 所有课程按照grade，classNum,workDay，Subject count 的Map
     */
    private HashMap<Integer,HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>>> gradeClassNumWorkDaySubjectCountMap;
    /**
     * 所有上课的教师
     */
    private List<TeacherDO> allWorkTeacher;
    /**
     * 教师获取所有教师上课时间map
     */
    private HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap;
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
    private HashMap<Integer,List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap;
    /**
     * 年级班级下对应课程权重的map
     */
    private HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap;
    /**
     * 小课教室最大容量
     */
    private HashMap<Integer,Integer> classroomMaxCapacityMap;
    /**
     * 初始化特殊教室使用情况
     */
    private HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>> classRoomUsedCountMap;
    /**
     * 课程表map
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer,HashMap<Integer, Integer>>>> timeTableMap;

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

    public Map<Integer, String> getAllSubjectNameMap() {
        return allSubjectNameMap;
    }

    public void setAllSubjectNameMap(Map<Integer, String> allSubjectNameMap) {
        this.allSubjectNameMap = allSubjectNameMap;
    }

    public Map<Integer, List<SubjectDTO>> getGradeSubjectMap() {
        return gradeSubjectMap;
    }

    public void setGradeSubjectMap(Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        this.gradeSubjectMap = gradeSubjectMap;
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

    public HashMap<Integer, HashMap<Integer, List<Integer>>> getTeacherTeachingMap() {
        return teacherTeachingMap;
    }

    public void setTeacherTeachingMap(HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        this.teacherTeachingMap = teacherTeachingMap;
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

    public HashMap<Integer, Integer> getClassroomMaxCapacityMap() {
        return classroomMaxCapacityMap;
    }

    public void setClassroomMaxCapacityMap(HashMap<Integer, Integer> classroomMaxCapacityMap) {
        this.classroomMaxCapacityMap = classroomMaxCapacityMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getClassRoomUsedCountMap() {
        return classRoomUsedCountMap;
    }

    public void setClassRoomUsedCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> classRoomUsedCountMap) {
        this.classRoomUsedCountMap = classRoomUsedCountMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    @Override
    public String toString() {
        return "PlanTimeTablePrepareDTO{" +
                "allGradeClassInfo=" + allGradeClassInfo +
                ", gradeClassCountMap=" + gradeClassCountMap +
                ", allSubject=" + allSubject +
                ", allSubjectNameMap=" + allSubjectNameMap +
                ", gradeSubjectMap=" + gradeSubjectMap +
                ", gradeClassNumWorkDaySubjectCountMap=" + gradeClassNumWorkDaySubjectCountMap +
                ", allWorkTeacher=" + allWorkTeacher +
                ", teacherTeachingMap=" + teacherTeachingMap +
                ", allSubjectTeacherGradeClassDTO=" + allSubjectTeacherGradeClassDTO +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                ", subjectGradeClassTeacherCountMap=" + subjectGradeClassTeacherCountMap +
                ", teacherSubjectListMap=" + teacherSubjectListMap +
                ", gradeClassSubjectWeightMap=" + gradeClassSubjectWeightMap +
                ", classroomMaxCapacityMap=" + classroomMaxCapacityMap +
                ", classRoomUsedCountMap=" + classRoomUsedCountMap +
                ", timeTableMap=" + timeTableMap +
                '}';
    }
}
