package com.work.school.mysql.timetable.service.dto;

import com.work.school.mysql.common.service.dto.GradeClassNumWorkDayTimeDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class TimeTableConstraintDTO implements Serializable {
    /**
     * 序列
     */
    private Integer order;
    /**
     * 约束order
     */
    private Integer orderConstraint;
    /**
     * 约束课程
     */
    private Integer subjectIdConstraint;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOrderConstraint() {
        return orderConstraint;
    }

    public void setOrderConstraint(Integer orderConstraint) {
        this.orderConstraint = orderConstraint;
    }

    public Integer getSubjectIdConstraint() {
        return subjectIdConstraint;
    }

    public void setSubjectIdConstraint(Integer subjectIdConstraint) {
        this.subjectIdConstraint = subjectIdConstraint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeTableConstraintDTO that = (TimeTableConstraintDTO) o;
        return Objects.equals(order, that.order) &&
                Objects.equals(orderConstraint, that.orderConstraint) &&
                Objects.equals(subjectIdConstraint, that.subjectIdConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, orderConstraint, subjectIdConstraint);
    }

    @Override
    public String toString() {
        return "TimeTableConstraintDTO{" +
                "order=" + order +
                ", orderConstraint=" + orderConstraint +
                ", subjectIdConstraint=" + subjectIdConstraint +
                '}';
    }
}
