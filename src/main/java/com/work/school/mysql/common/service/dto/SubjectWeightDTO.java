package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

public class SubjectWeightDTO implements Serializable {
    /**
     * 科目id
     */
    private Integer subjectId;
    /**
     * 科目名称
     */
    private String name;
    /**
     * 科目类型
     */
    private Integer type;

    private Integer weight;

    private Integer frequency;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "SubjectWeightDTO{" +
                "subjectId=" + subjectId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", weight=" + weight +
                ", frequency=" + frequency +
                '}';
    }
}