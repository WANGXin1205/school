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
    BEST(1, "文明之星"),
    TEAM(2, "路队之星"),
    SPORT(3, "体育之星"),
    HEALTH(4, "卫生之星");


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
