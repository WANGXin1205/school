package com.work.school.common.utils.business;

import com.work.school.common.utils.common.DateUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.banner.enums.SchoolTermEnum;

import java.util.Date;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 10:20 PM
 * @Description
 */
public class SchoolTermUtils {

    /**
     * 获取当前时间对应的学期
     * @return
     */
    public static Integer getSchoolTerm(){
        Integer schoolTerm = null;
        Date now = DateUtils.clearTime(DateUtils.getNow());
        for (SchoolTermEnum x: SchoolTermEnum.values()){
            if (StringUtils.isNotEmpty(x.getStart()) && StringUtils.isNotEmpty(x.getEnd())){
                Date start = DateUtils.clearTime(DateUtils.parseDate(x.getStart()));
                Date end = DateUtils.clearTime(DateUtils.parseDate(x.getEnd()));

                if (start.before(now) && now.before(end)){
                    schoolTerm = x.getCode();
                }

            }
        }

        return schoolTerm;
    }
}
