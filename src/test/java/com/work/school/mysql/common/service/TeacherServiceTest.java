package com.work.school.mysql.common.service;

import com.work.school.mysql.timetable.service.TimeTableService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 8:10 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherServiceTest {

    @Resource
    private TeacherService teacherService;

    @Test
    public void listAllTeacherGroupMapTest(){
        var teacherDOList = teacherService.listAllTeachingTeacherByGrade(1);
        var cattyResult = teacherService.listAllTeacherGroupMap(teacherDOList);
        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void listAllSubjectTeacherMapTest(){
        var teacherDOList = teacherService.listAllTeachingTeacherByGrade(1);
        var cattyResult = teacherService.listAllSubjectTeacherMap(teacherDOList);
        Assert.assertTrue(cattyResult.isSuccess());
    }

}