package com.work.school.mysql.exam.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/28 8:22 PM
 * @Description
 */
public class ExamResultExcelDTO implements Serializable {
    /**
     * 年级
     */
    private Integer grade;
    /**
     * 班级名称
     */
    private String classNum;
    /**
     * 考试科目
     */
    private String subjectName;
    /**
     * 学生成绩
     */
    private List<ExamResultExcelMainDTO> examResultExcelMainDTOList;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getClassNum() {
        return classNum;
    }

    public void setClassNum(String classNum) {
        this.classNum = classNum;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<ExamResultExcelMainDTO> getExamResultExcelMainDTOList() {
        return examResultExcelMainDTOList;
    }

    public void setExamResultExcelMainDTOList(List<ExamResultExcelMainDTO> examResultExcelMainDTOList) {
        this.examResultExcelMainDTOList = examResultExcelMainDTOList;
    }

    @Override
    public String toString() {
        return "ExamResultExcelDTO{" +
                "grade=" + grade +
                ", classNum='" + classNum + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", examResultExcelMainDTOList=" + examResultExcelMainDTOList +
                '}';
    }
}
