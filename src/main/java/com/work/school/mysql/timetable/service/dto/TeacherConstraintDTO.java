package com.work.school.mysql.timetable.service.dto;

import java.io.Serializable;
import java.util.Objects;

public class TeacherConstraintDTO implements Serializable {
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
    private Integer teacherConstraint;

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

    public Integer getTeacherConstraint() {
        return teacherConstraint;
    }

    public void setTeacherConstraint(Integer teacherConstraint) {
        this.teacherConstraint = teacherConstraint;
    }

    @Override
    public String toString() {
        return "teacherConstraintDTO{" +
                "order=" + order +
                ", orderConstraint=" + orderConstraint +
                ", teacherConstraint=" + teacherConstraint +
                '}';
    }
}
