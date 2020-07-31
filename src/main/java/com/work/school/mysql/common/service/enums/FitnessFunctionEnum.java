package com.work.school.mysql.common.service.enums;


import com.github.pagehelper.StringUtil;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:18 PM
 * @Description
 */
public enum FitnessFunctionEnum {
    HARD_SATISFIED(1,"硬约束条件"),
    SOFT_SATISFIED(2,"软约束条件");


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
