package com.work.school.mysql.common.dao.mapper;

import com.work.school.mysql.common.dao.domain.TeacherGradeClassDO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/9/3 3:28 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherGradeClassMapperTest {

    @Resource
    private TeacherGradeClassMapper teacherGradeClassMapper;

    @Test
    public void listAllTeacherGradeClass() {
        var teacherGradeClassDOList = teacherGradeClassMapper.listAllTeacherGradeClass();
        Assert.assertNotNull(teacherGradeClassDOList);
    }

    @Test
    public void listTeacherGradeClassByGradeTest() {
        Integer grade = 1;
        var teacherGradeClassDOList = teacherGradeClassMapper.listTeacherGradeClassByGrade(grade);
        Assert.assertNotNull(teacherGradeClassDOList);
    }
}