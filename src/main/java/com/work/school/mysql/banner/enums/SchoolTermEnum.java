package com.work.school.mysql.banner.enums;

/**
 * @Author : Growlithe
 * @Date : 2018/5/19 11:36
 * @Description 学期枚举 后期做成配置
 */
public enum SchoolTermEnum {
    /**
     * 班级枚举
     */
    ONE_ONE(1, "2018-2019学年第一学期","2018-09-27","2019-01-13"),
    ONE_TWO(2, "2018-2019学年第二学期","",""),
    TWO_ONE(3, "2019-2020学年第一学期","",""),
    TWO_TWO(4, "2019-2020学年第二学期","","");


    private Integer code;
    private String desc;
    private String start;
    private String end;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public static String getDesc(Integer code) {
        if (code == null) {
            return null;
        }
        for (SchoolTermEnum e : SchoolTermEnum.values()) {
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
        for (SchoolTermEnum e : SchoolTermEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getCode();
            }
        }
        return null;
    }

    SchoolTermEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    SchoolTermEnum(Integer code, String desc, String start, String end) {
        this.code = code;
        this.desc = desc;
        this.start = start;
        this.end = end;
    }
}
