package com.work.school.common.utils.common;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/4/1 7:17 PM
 * @Description
 */
public class MathsUtilsTest {

    @Test
    public void getMatchScore() {
        Long start = DateUtils.getNow().getTime();
        String[] strings = {"74","67.1","69","70","70"};
        MathsUtils.getMatchScore(strings);
        Long end = DateUtils.getNow().getTime();
        System.out.println("共用时间: " + (end - start) +"微秒");
    }

}