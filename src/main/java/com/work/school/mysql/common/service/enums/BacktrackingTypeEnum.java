package com.work.school.mysql.common.service.enums;


/**
 * Created by WANGXin on 2017/3/15.
 */
public enum BacktrackingTypeEnum {
    BA(1,"回溯算法"),
    DY_BA(2,"动态权重——回溯算法");

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
        for (BacktrackingTypeEnum e : BacktrackingTypeEnum.values()) {
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
        for (BacktrackingTypeEnum e : BacktrackingTypeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getNum();
            }
        }
        return null;
    }

    BacktrackingTypeEnum(Integer num, String desc) {
        this.num = num;
        this.desc = desc;
    }
}
