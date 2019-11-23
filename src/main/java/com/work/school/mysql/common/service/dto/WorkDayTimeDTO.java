package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

public class WorkDayTimeDTO implements Serializable {
    private Integer workDay;

    private Integer time;

    public Integer getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Integer workDay) {
        this.workDay = workDay;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "WorkDayTimeDTO{" +
                "workDay=" + workDay +
                ", time=" + time +
                '}';
    }
}
