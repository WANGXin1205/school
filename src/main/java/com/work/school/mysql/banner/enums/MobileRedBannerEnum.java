package com.work.school.mysql.banner.enums;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 流动红旗枚举 后期做成配置
 */
public enum MobileRedBannerEnum {
    /**
     * 班级枚举
     */
    BEST(1, "榜样"),
    SING(2, "百灵"),
    SPORT(3, "健将"),
    BOOK(4, "书虫");


    private final Integer code;
    private final String desc;

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
        for (MobileRedBannerEnum e : MobileRedBannerEnum.values()) {
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
        for (MobileRedBannerEnum e : MobileRedBannerEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getCode();
            }
        }
        return null;
    }

    MobileRedBannerEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
