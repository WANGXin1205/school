package com.work.school.mysql.exam.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author : Growlithe
 * @Date : 2019/3/28 8:24 PM
 * @Description
 */
public class ExamResultExcelMainDTO implements Serializable {
    /**
     * 学生班级id
     */
    private Integer studentClassId;
    /**
     * 学生姓名
     */
    private String studentName;
    /**
     * 学生分数
     */
    private BigDecimal score;

    public Integer getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(Integer studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ExamResultExcelMainDTO{" +
                "studentClassId=" + studentClassId +
                ", studentName='" + studentName + '\'' +
                ", score=" + score +
                '}';
    }
}
