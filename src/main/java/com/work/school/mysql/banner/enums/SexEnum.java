package com.work.school.mysql.banner.enums;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 年级枚举 后期做成配置
 */
public enum SexEnum {
    /**
     * 班级枚举
     */
    MALE(1, "m","男"),
    FEMALE(0, "f","女");

    private Integer num;
    private String code;
    private String desc;

    public Integer getNum() {
        return num;
    }

    public String getCode() {
        return code;
    }


    public String getDesc() {
        return desc;
    }


    public static SexEnum getSexEnum(Integer num) {
        if (num == null) {
            return null;
        }
        for (SexEnum e : SexEnum.values()) {
            if (e.getNum().equals(num)) {
                return e;
            }
        }
        return null;
    }



    SexEnum(Integer num, String code, String desc) {
        this.num = num;
        this.code = code;
        this.desc = desc;
    }
}
