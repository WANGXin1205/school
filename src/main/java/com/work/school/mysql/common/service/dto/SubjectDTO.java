package com.work.school.mysql.common.service.dto;

import java.util.Date;

/**
 * @Author : Growlithe
 * @Date : 2019/9/24 11:58 PM
 * @Description
 */
public class SubjectDTO {

    private Integer subjectId;

    private String name;

    private Integer grade;

    private Integer frequency;

    private Integer type;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SubjectDTO{" +
                "subjectId=" + subjectId +
                ", name='" + name + '\'' +
                ", grade=" + grade +
                ", frequency=" + frequency +
                ", type=" + type +
                '}';
    }
}
