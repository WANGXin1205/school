package com.work.school.mysql.exam.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/4/9 9:16 AM
 * @Description
 */
public class ExamSeatingScheduleDTO implements Serializable {
    /**
     * 考场号
     */
    private Integer examNum;
    /**
     * 学生信息
     */
    private List<StudentDTO> studentDTOList;

    public Integer getExamNum() {
        return examNum;
    }

    public void setExamNum(Integer examNum) {
        this.examNum = examNum;
    }

    public List<StudentDTO> getStudentDTOList() {
        return studentDTOList;
    }

    public void setStudentDTOList(List<StudentDTO> studentDTOList) {
        this.studentDTOList = studentDTOList;
    }

    @Override
    public String toString() {
        return "ExamSeatingScheduleDTO{" +
                "examNum=" + examNum +
                ", studentDTOList=" + studentDTOList +
                '}';
    }
}
