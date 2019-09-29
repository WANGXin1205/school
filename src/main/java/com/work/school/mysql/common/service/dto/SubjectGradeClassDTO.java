package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author : Growlithe
 * @Date : 2019/9/4 9:53 AM
 * @Description
 */
public class SubjectGradeClassDTO implements Serializable {

    private Integer subjectId;

    private Integer grade;

    private Integer classNum;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectGradeClassDTO that = (SubjectGradeClassDTO) o;
        return Objects.equals(subjectId, that.subjectId) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(classNum, that.classNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectId, grade, classNum);
    }

    @Override
    public String toString() {
        return "SubjectTeacherGradeClassDTO{" +
                "subjectId=" + subjectId +
                ", grade=" + grade +
                ", classNum=" + classNum +
                '}';
    }

}
