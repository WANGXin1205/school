package com.work.school.mysql.timetable.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:47 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TimeTableServiceTest {

    @Resource
    private TimeTableService timeTableService;

    @Test
    public void planTimeTable() {
        Integer grade = 1;
        var cattyResult = timeTableService.planTimeTable(grade);
        Assert.assertTrue(cattyResult.isSuccess());
    }

}