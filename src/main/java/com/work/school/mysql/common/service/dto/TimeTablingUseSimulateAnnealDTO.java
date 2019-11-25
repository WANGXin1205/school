package com.work.school.mysql.common.service.dto;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeTablingUseSimulateAnnealDTO implements Serializable {
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
     * 获取科目年级班级对应教师的map
     */
    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap;
    /**
     * 特殊课程上课时间map
     */
    private HashMap<String, String> specialSubjectTimeMap;

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

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherMap() {
        return subjectGradeClassTeacherMap;
    }

    public void setSubjectGradeClassTeacherMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        this.subjectGradeClassTeacherMap = subjectGradeClassTeacherMap;
    }

    public HashMap<String, String> getSpecialSubjectTimeMap() {
        return specialSubjectTimeMap;
    }

    public void setSpecialSubjectTimeMap(HashMap<String, String> specialSubjectTimeMap) {
        this.specialSubjectTimeMap = specialSubjectTimeMap;
    }

    @Override
    public String toString() {
        return "TimeTablingUseSimulateAnnealDTO{" +
                "allGradeClassInfo=" + allGradeClassInfo +
                ", gradeClassCountMap=" + gradeClassCountMap +
                ", allSubject=" + allSubject +
                ", allSubjectNameMap=" + allSubjectNameMap +
                ", gradeSubjectMap=" + gradeSubjectMap +
                ", subjectGradeClassTeacherMap=" + subjectGradeClassTeacherMap +
                ", specialSubjectTimeMap=" + specialSubjectTimeMap +
                '}';
    }
}
