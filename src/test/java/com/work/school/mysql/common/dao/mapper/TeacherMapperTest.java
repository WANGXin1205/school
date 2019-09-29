package com.work.school.mysql.common.dao.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 8:52 AM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherMapperTest {

    @Resource
    private TeacherMapper teacherMapper;

    @Test
    public void listAllTeacher() {
        var allTeacherList = teacherMapper.listAll();
        Assert.assertNotNull(allTeacherList);
    }

}