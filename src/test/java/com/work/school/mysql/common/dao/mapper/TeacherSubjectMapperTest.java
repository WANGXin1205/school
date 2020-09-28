package com.work.school.mysql.common.dao.mapper;

import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.service.SubjectClassTeacherService;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private SubjectClassTeacherService subjectClassTeacherService;

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

    @Test
    public void saveBatchTest(){
        Integer grade = null;
        var subjectClassTeacherDOList = subjectClassTeacherService.listSubjectClassTeacherByGrade(grade);
        List<TeacherSubjectDO> teacherSubjectDOList = new ArrayList<>();
        subjectClassTeacherDOList = subjectClassTeacherDOList.stream().filter(x->x.getTeacherId() != null).collect(Collectors.toList());
        for (SubjectClassTeacherDO subjectClassTeacherDO:subjectClassTeacherDOList){
            TeacherSubjectDO teacherSubjectDO = new TeacherSubjectDO();
            teacherSubjectDO.setSubjectId(subjectClassTeacherDO.getSubjectId());
            teacherSubjectDO.setTeacherId(subjectClassTeacherDO.getTeacherId());
            teacherSubjectDO.setCreateBy("WANGXin");
            if (!teacherSubjectDOList.contains(teacherSubjectDO)){
                teacherSubjectDOList.add(teacherSubjectDO);
            }
        }

        Assert.assertNotNull(teacherSubjectDOList);

        teacherSubjectMapper.saveBatch(teacherSubjectDOList);
    }
}