package com.work.school.mysql.common.service.dto;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/9/23 8:51 AM
 * @Description
 */
public class ComputerSubjectWeightDTO implements Serializable {
    /**
     * 年级
     */
    private Integer grade;
    /**
     * 班级
     */
    private Integer classNum;
    /**
     * 工作日
     */
    private Integer workDay;
    /**
     * 节次
     */
    private Integer time;
    /**
     * 获取对应的上课课程
     */
    private List<SubjectWeightDTO> subjectWeightDTOList;
    /**
     * 教师获取所有教师上课时间map
     */
    private HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap;
    /**
     * 教师所带年级班级和科目map
     */
    private HashMap<Integer,List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap;
    /**
     * 每个年级每个班级每天科目次数统计列表
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorDaySubjectCountMap;
    /**
     * 课表
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap;
    /**
     * 某个科目对应教师所带班级数目的map
     */
    private HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
    }

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public List<SubjectWeightDTO> getSubjectWeightDTOList() {
        return subjectWeightDTOList;
    }

    public void setSubjectWeightDTOList(List<SubjectWeightDTO> subjectWeightDTOList) {
        this.subjectWeightDTOList = subjectWeightDTOList;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap() {
        return timeTableMap;
    }

    public void setTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        this.timeTableMap = timeTableMap;
    }

    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherCountMap() {
        return subjectGradeClassTeacherCountMap;
    }

    public void setSubjectGradeClassTeacherCountMap(HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap) {
        this.subjectGradeClassTeacherCountMap = subjectGradeClassTeacherCountMap;
    }

    public HashMap<Integer, HashMap<Integer, List<Integer>>> getTeacherTeachingMap() {
        return teacherTeachingMap;
    }

    public void setTeacherTeachingMap(HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        this.teacherTeachingMap = teacherTeachingMap;
    }

    public HashMap<Integer, List<SubjectTeacherGradeClassDTO>> getTeacherSubjectListMap() {
        return teacherSubjectListMap;
    }

    public void setTeacherSubjectListMap(HashMap<Integer, List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap) {
        this.teacherSubjectListMap = teacherSubjectListMap;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getGradeClassNumWorDaySubjectCountMap() {
        return gradeClassNumWorDaySubjectCountMap;
    }

    public void setGradeClassNumWorDaySubjectCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorDaySubjectCountMap) {
        this.gradeClassNumWorDaySubjectCountMap = gradeClassNumWorDaySubjectCountMap;
    }

    @Override
    public String toString() {
        return "ComputerSubjectWeightDTO{" +
                "grade=" + grade +
                ", classNum=" + classNum +
                ", workDay=" + workDay +
                ", time=" + time +
                ", subjectWeightDTOList=" + subjectWeightDTOList +
                ", teacherTeachingMap=" + teacherTeachingMap +
                ", teacherSubjectListMap=" + teacherSubjectListMap +
                ", gradeClassNumWorDaySubjectCountMap=" + gradeClassNumWorDaySubjectCountMap +
                ", timeTableMap=" + timeTableMap +
                ", subjectGradeClassTeacherCountMap=" + subjectGradeClassTeacherCountMap +
                '}';
    }
}
