package com.work.school.mysql.banner.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 工作日枚举
 */
public enum WorkDayEnum {
    /**
     * 工作日枚举
     */
    Monday(1,"周一"),
    Tuesday(2,"周二"),
    Wednesday(3,"周三"),
    Thursday(4,"周四"),
    Friday(5,"周五");


    private Integer num;
    private String desc;

    public Integer getNum() {
        return num;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Integer code) {
        if (code == null) {
            return null;
        }
        for (WorkDayEnum e : WorkDayEnum.values()) {
            if (e.getNum().equals(code)) {
                return e.getDesc();
            }
        }
        return null;
    }

    public static Integer getNum(String desc) {
        if (desc == null) {
            return null;
        }
        for (WorkDayEnum e : WorkDayEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getNum();
            }
        }
        return null;
    }

    public static List<Integer> listAllNum() {
        List<Integer> allNumList = new ArrayList<>();
        for (WorkDayEnum e : WorkDayEnum.values()) {
            allNumList.add(e.num);
        }
        return allNumList;
    }

    WorkDayEnum(Integer num, String desc) {
        this.num = num;
        this.desc = desc;
    }
}
