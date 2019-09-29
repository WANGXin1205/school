package com.work.school.mysql.common.service;

import com.google.common.collect.Lists;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;
import com.work.school.mysql.timetable.service.TimeTableService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Comparator;

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


}