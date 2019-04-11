package com.work.school.mysql.exam.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/4/1 5:54 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExamSeatingScheduleServiceTest {

    @Resource
    private ExamSeatingScheduleService examSeatingScheduleService;

    @Test
    public void scheduleExamSeatingTest() {
        Integer grade = 1;
        var examNum = 7;
        var cattyResult = examSeatingScheduleService.scheduleExamSeating(grade,examNum);
        Assert.assertTrue(cattyResult.isSuccess());
    }
}