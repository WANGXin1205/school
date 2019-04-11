package com.work.school.mysql.exam.service.dto;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2019/4/9 8:21 AM
 * @Description
 */
public class StudentDTO implements Serializable {
    /**
     * 班级
     */
    private Integer classId;
    /**
     * 学生班级序号
     */
    private Integer studentClassId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 是否使用过
     */
    private Boolean isUsed;

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(Integer studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

    @Override
    public String toString() {
        return "StudentDTO{" +
                "classId=" + classId +
                ", studentClassId=" + studentClassId +
                ", name='" + name + '\'' +
                ", isUsed=" + isUsed +
                '}';
    }
}
