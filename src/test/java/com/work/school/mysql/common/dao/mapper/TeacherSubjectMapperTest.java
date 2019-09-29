package com.work.school.mysql.common.dao.mapper;

import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/9/3 12:18 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherSubjectMapperTest {

    @Resource
    private TeacherSubjectMapper teacherSubjectMapper;

    @Test
    public void listAllTeacherSubject() {
        List<TeacherSubjectDO> teacherSubjectDOList = teacherSubjectMapper.listAllTeacherSubject();
        Assert.assertNotNull(teacherSubjectDOList);
    }

    @Test
    public void listAllSubjectTeacherGradeClassTest(){
        var allSubjectTeacherGradeClass = teacherSubjectMapper.listAllSubjectTeacherGradeClass();
        Assert.assertNotNull(allSubjectTeacherGradeClass);
    }
}