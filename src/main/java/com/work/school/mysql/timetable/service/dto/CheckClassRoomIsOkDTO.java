package com.work.school.mysql.timetable.service.dto;

import java.util.HashMap;

/**
 * @Author : Growlithe
 * @Date : 2019/9/25 4:54 PM
 * @Description
 */
public class CheckClassRoomIsOkDTO {

    private Integer subjectId;

    private Integer workDay;

    private Integer time;

    private HashMap<Integer, Integer> classroomMaxCapacity;
    /**
     * 初始化特殊教室使用情况 order subjectId workDay time count
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

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

    public HashMap<Integer, Integer> getClassroomMaxCapacity() {
        return classroomMaxCapacity;
    }

    public void setClassroomMaxCapacity(HashMap<Integer, Integer> classroomMaxCapacity) {
        this.classroomMaxCapacity = classroomMaxCapacity;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getOrderClassRoomUsedCountMap() {
        return orderClassRoomUsedCountMap;
    }

    public void setOrderClassRoomUsedCountMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap) {
        this.orderClassRoomUsedCountMap = orderClassRoomUsedCountMap;
    }

    @Override
    public String toString() {
        return "CheckClassRoomIsOkDTO{" +
                "subjectId=" + subjectId +
                ", workDay=" + workDay +
                ", time=" + time +
                ", classroomMaxCapacity=" + classroomMaxCapacity +
                ", orderClassRoomUsedCountMap=" + orderClassRoomUsedCountMap +
                '}';
    }
}
