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
 * @Date : 2019/4/1 5:43 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentMapperTest {

    @Resource
    private StudentMapper studentMapper;

    @Test
    public void listStudentByGradeTest() {
        Integer grade = 1;
        var studentList = studentMapper.listStudentByGrade(grade);
        Assert.assertNotNull(studentList);
    }

    @Test
    public void listStudentByGradeClassTest() {
        Integer grade = 1;
        Integer classId = 1;
        var studentList = studentMapper.listStudentByGradeClass(grade, classId);
        Assert.assertNotNull(studentList);
    }
}