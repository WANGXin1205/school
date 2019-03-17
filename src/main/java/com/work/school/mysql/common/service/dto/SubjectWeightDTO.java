package com.work.school.mysql.common.service.dto;

import java.io.Serializable;
import java.util.Date;

public class SubjectWeightDTO implements Serializable {

    private Integer id;
    /**
     * 科目类型
     */
    private String name;
    /**
     * 班级
     */
    private Integer classNum;

    private Integer weight;

    private Integer type;

    private Integer frequency;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClassNum() {
        return classNum;
    }

    public void setClassNum(Integer classNum) {
        this.classNum = classNum;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
                "id=" + id +
                ", name='" + name + '\'' +
                ", classNum=" + classNum +
                ", weight=" + weight +
                ", type=" + type +
                ", frequency=" + frequency +
                '}';
    }
}