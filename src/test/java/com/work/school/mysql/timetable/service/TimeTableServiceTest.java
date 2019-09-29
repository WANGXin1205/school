package com.work.school.mysql.timetable.service;

import com.work.school.common.utils.common.DateUtils;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.TeacherService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public void planTimeTableTest(){
        var cattyResult = timeTableService.planTimeTable();
        Assert.assertTrue(cattyResult.isSuccess());
    }


}