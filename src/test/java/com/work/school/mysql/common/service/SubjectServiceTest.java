package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 9:25 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SubjectServiceTest {

    @Resource
    private SubjectService subjectService;

    @Test
    public void listAllSubjectByGradeTest(){
        var subjectDOList = subjectService.listAllSubjectByGrade(1);
        Assert.assertNotNull(subjectDOList);
    }

    @Test
    public void computerMaxSubjectWeight() {
        Integer workDay = 1;
        Integer classNum = 1;
        Integer time = 7;
        Integer maxClassNum = 7;
        var passFlag = true;
        var subjectDOList = subjectService.listAllSubjectByGrade(1);
        List<SubjectWeightDTO> subjectWeightDTOList = new ArrayList<>();
        for (SubjectDO x:subjectDOList){
            SubjectWeightDTO subjectWeightDTO = new SubjectWeightDTO();
            BeanUtils.copyProperties(x,subjectWeightDTO);
            subjectWeightDTOList.add(subjectWeightDTO);
        }

        var cattyResult = subjectService.computerMaxSubjectWeight(classNum,subjectWeightDTOList,passFlag);
        Assert.assertTrue(cattyResult.isSuccess());

        workDay = 5;
        time = 5;
        cattyResult = subjectService.computerMaxSubjectWeight(classNum,subjectWeightDTOList,passFlag);
        Assert.assertTrue(cattyResult.isSuccess());

        workDay = 5;
        time = 7;
        subjectWeightDTOList.forEach(x->{
            if (x.getId() == 9){
                x.setFrequency(0);
            }
        });
        cattyResult = subjectService.computerMaxSubjectWeight(classNum,subjectWeightDTOList,passFlag);
        Assert.assertTrue(cattyResult.isSuccess());

        subjectWeightDTOList.forEach(x->{
            if (x.getId() == 9){
                x.setFrequency(1);
            }
        });
        workDay = 1;
        time = 2;
        cattyResult = subjectService.computerMaxSubjectWeight(classNum,subjectWeightDTOList,passFlag);
        Assert.assertTrue(cattyResult.isSuccess());

        workDay = 2;
        time = 6;
        cattyResult = subjectService.computerMaxSubjectWeight(classNum,subjectWeightDTOList,passFlag);
        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void listSubjectTypeMapTest(){
        List<SubjectDO> subjectDOList = subjectService.listAllSubjectByGrade(1);
        var listSubjectTypeMapResult = subjectService.listSubjectTypeMap(subjectDOList);
        Assert.assertTrue(listSubjectTypeMapResult.isSuccess());
    }

}