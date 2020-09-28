package com.work.school.mysql.common.dao.mapper;

import com.work.school.mysql.common.dao.domain.TeacherGradeClassDO;
import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;
import com.work.school.mysql.common.service.TeacherSubjectService;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.service.SubjectClassTeacherService;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private SubjectClassTeacherService subjectClassTeacherService;

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

    @Test
    public void saveBatchTest(){
        Integer grade = null;
        var subjectClassTeacherDOList = subjectClassTeacherService.listSubjectClassTeacherByGrade(grade);
        subjectClassTeacherDOList = subjectClassTeacherDOList.stream().filter(x->x.getTeacherId() != null).collect(Collectors.toList());
        List<TeacherGradeClassDO> teacherGradeClassDOList = new ArrayList<>();
        for (SubjectClassTeacherDO subjectClassTeacherDO:subjectClassTeacherDOList){
            TeacherGradeClassDO teacherGradeClassDO = new TeacherGradeClassDO();
            teacherGradeClassDO.setGrade(subjectClassTeacherDO.getGrade());
            teacherGradeClassDO.setClassNum(subjectClassTeacherDO.getClassNum());
            teacherGradeClassDO.setTeacherId(subjectClassTeacherDO.getTeacherId());
            teacherGradeClassDO.setCreateBy("WANGXin");
            if (!teacherGradeClassDOList.contains(teacherGradeClassDO)){
                teacherGradeClassDOList.add(teacherGradeClassDO);
            }
        }

        Assert.assertNotNull(teacherGradeClassDOList);

        teacherGradeClassMapper.saveBatch(teacherGradeClassDOList);
    }
}