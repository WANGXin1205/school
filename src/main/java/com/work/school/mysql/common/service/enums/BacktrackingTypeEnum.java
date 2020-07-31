package com.work.school.mysql.common.service.enums;


/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:18 PM
 * @Description
 */
public enum BacktrackingTypeEnum {
    BA(1,"回溯算法"),
    DW_BA(2,"动态权重-回溯算法"),
    FC_BA(3,"前行检测-回溯算法"),
    FC_DW_BA(4,"前行检测动态权重-回溯算法");

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
