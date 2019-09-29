package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2019/9/4 9:53 AM
 * @Description
 */
public class SubjectTeacherGradeClassDTO implements Serializable {

    private Integer subjectId;

    private Integer teacherId;

    private Integer grade;

    private Integer classNum;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

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

    @Override
    public String toString() {
        return "SubjectTeacherGradeClassDTO{" +
                "subjectId=" + subjectId +
                ", teacherId=" + teacherId +
                ", grade=" + grade +
                ", classNum=" + classNum +
                '}';
    }

}
