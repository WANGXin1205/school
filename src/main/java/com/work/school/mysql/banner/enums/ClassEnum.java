package com.work.school.mysql.banner.enums;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 班级枚举 后期做成配置
 */
public enum ClassEnum {
    /**
     * 班级枚举
     */
    ONE_ONE(1, "一年级(1)班"),
    ONE_TWO(2, "一年级(2)班"),
    ONE_THREE(3, "一年级(3)班"),
    ONE_FOUR(4, "一年级(4)班"),
    ONE_FIVE(5, "一年级(5)班"),
    ONE_SIX(6, "一年级(6)班"),
    ONE_SEVEN(7, "一年级(7)班");


    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Integer code) {
        if (code == null) {
            return null;
        }
        for (ClassEnum e : ClassEnum.values()) {
            if (e.getCode().equals(code)) {
                return e.getDesc();
            }
        }
        return null;
    }

    public static Integer getCode(String desc) {
        if (desc == null) {
            return null;
        }
        for (ClassEnum e : ClassEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getCode();
            }
        }
        return null;
    }

    ClassEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
