package com.work.school.mysql.common.service.enums;


import com.github.pagehelper.StringUtil;

/**
 * Created by WANGXin on 2017/3/15.
 */
public enum FitnessFunctionEnum {
    HARD_SATISFIED(1,"直到满足所有硬约束条件"),
    MORE_SATISFIED(2,"尽可能满足条件，有迭代停止");


    private Integer num;
    private String desc;

    public Integer getNum() {
        return num;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Integer num) {
        if (num == null) {
            return null;
        }
        for (FitnessFunctionEnum e : FitnessFunctionEnum.values()) {
            if (e.getNum().equals(num)) {
                return e.getDesc();
            }
        }
        return null;
    }

    public static Integer getNum(String desc) {
        if (desc == null) {
            return null;
        }
        for (FitnessFunctionEnum e : FitnessFunctionEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getNum();
            }
        }
        return null;
    }

    FitnessFunctionEnum(Integer num, String desc) {
        this.num = num;
        this.desc = desc;
    }
}
