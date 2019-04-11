package com.work.school.mysql.banner.enums;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 年级枚举 后期做成配置
 */
public enum GradeEnum {
    /**
     * 班级枚举
     */
    ONE(1, "一年级"),
    TWO(2, "二年级"),
    THREE(3, "三年级"),
    FOUR(4, "四年级"),
    FIVE(5, "五年级"),
    SIX(6, "六年级");


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
        for (GradeEnum e : GradeEnum.values()) {
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
        for (GradeEnum e : GradeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getCode();
            }
        }
        return null;
    }

    public static GradeEnum getGradeEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (GradeEnum e : GradeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static GradeEnum getGradeEnum(String desc) {
        if (desc == null) {
            return null;
        }
        for (GradeEnum e : GradeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e;
            }
        }
        return null;
    }

    GradeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
