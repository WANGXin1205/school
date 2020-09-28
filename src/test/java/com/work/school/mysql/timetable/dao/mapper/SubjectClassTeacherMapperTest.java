package com.work.school.mysql.timetable.dao.mapper;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.mapper.ClassInfoMapper;
import com.work.school.mysql.common.dao.mapper.SubjectDetailsMapper;
import com.work.school.mysql.common.dao.mapper.SubjectMapper;
import com.work.school.mysql.common.dao.mapper.TeacherMapper;
import com.work.school.mysql.common.service.dto.SubjectDTO;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubjectClassTeacherMapperTest {

    @Resource
    private SubjectClassTeacherMapper subjectClassTeacherMapper;
    @Resource
    private ClassInfoMapper classInfoMapper;
    @Resource
    private SubjectDetailsMapper subjectDetailsMapper;

    @Test
    public void saveBatchWithoutTeacherTest() {
        var allClassDO = classInfoMapper.listAllClass();
        var allSubjectDTO = subjectDetailsMapper.listAllSubjectDTO();

        List<SubjectClassTeacherDO> subjectClassTeacherDOList = new ArrayList<>();
        for (ClassInfoDO x:allClassDO){
            List<SubjectDTO> subjectDTOS = allSubjectDTO.stream().filter(y->y.getGrade().equals(x.getGrade())).collect(Collectors.toList());
            for (SubjectDTO subjectDTO:subjectDTOS){
                SubjectClassTeacherDO subjectClassTeacherDO = new SubjectClassTeacherDO();
                subjectClassTeacherDO.setGrade(x.getGrade());
                subjectClassTeacherDO.setClassNum(x.getClassNum());
                subjectClassTeacherDO.setSubjectId(subjectDTO.getSubjectId());
                subjectClassTeacherDO.setFrequency(subjectDTO.getFrequency());
                subjectClassTeacherDO.setCreateBy("WANGXin");

                subjectClassTeacherDOList.add(subjectClassTeacherDO);
            }
        }

//        subjectClassTeacherMapper.saveBatchWithoutTeacher(subjectClassTeacherDOList);
        Assert.assertNotNull(subjectClassTeacherDOList);
    }

    @Test
    public void listSubjectClassTeacherByGradeTest() {
        Integer grade = 1;
        var subjectClassTeacherDOList = subjectClassTeacherMapper.listSubjectClassTeacherByGrade(grade);
        Assert.assertNotNull(subjectClassTeacherDOList);

        grade = null;
        subjectClassTeacherDOList = subjectClassTeacherMapper.listSubjectClassTeacherByGrade(grade);
        Assert.assertNotNull(subjectClassTeacherDOList);
    }
}