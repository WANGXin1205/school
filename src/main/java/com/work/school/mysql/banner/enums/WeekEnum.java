package com.work.school.mysql.banner.enums;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 周次枚举 后期做成配置
 */
public enum WeekEnum {
    /**
     * 班级枚举
     */
    ONE(1, "第一周"),
    TWO(2, "第二周"),
    THREE(3, "第三周"),
    FOUR(4, "第四周"),
    FIVE(5, "第五周"),
    SIX(6, "第六周"),
    SEVEN(7, "第七周"),
    EIGHT(8, "第八周"),
    NINE(9, "第九周"),
    TEN(10, "第十周"),
    ELEVEN(11, "第十一周"),
    TWELVE(12, "第十二周"),
    THIRTEEN(13, "第十三周"),
    FOURTEEN(14, "第十四周"),
    FIFTEEN(15, "第十五周"),
    SIXTEEN(16, "第十六周"),
    SEVENTEEN(17, "第十七周"),
    EIGHTEEN(18, "第十八周"),
    NINETEEN(19, "第十九周"),
    TWENTY(20, "第二十周");


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
        for (WeekEnum e : WeekEnum.values()) {
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
        for (WeekEnum e : WeekEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getCode();
            }
        }
        return null;
    }

    WeekEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
