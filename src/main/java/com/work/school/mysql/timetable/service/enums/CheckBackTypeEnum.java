package com.work.school.mysql.timetable.service.enums;


/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:18 PM
 * @Description
 */
public enum CheckBackTypeEnum {
    GOOD(1,"良好"),
    BAD_NOW(2,"不好当前节点"),
    BAD_NEXT(3,"不好未来节点");

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
        for (CheckBackTypeEnum e : CheckBackTypeEnum.values()) {
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
        for (CheckBackTypeEnum e : CheckBackTypeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e.getNum();
            }
        }
        return null;
    }

    CheckBackTypeEnum(Integer num, String desc) {
        this.num = num;
        this.desc = desc;
    }
}
