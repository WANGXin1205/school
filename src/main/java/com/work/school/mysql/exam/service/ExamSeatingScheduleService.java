package com.work.school.mysql.exam.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.StudentDO;
import com.work.school.mysql.common.dao.mapper.ClassInfoMapper;
import com.work.school.mysql.common.dao.mapper.StudentMapper;
import com.work.school.mysql.common.service.ClassInfoService;
import com.work.school.mysql.common.service.StudentService;
import com.work.school.mysql.exam.service.dto.StudentDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/4/1 5:18 PM
 * @Description
 */
@Service
public class ExamSeatingScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamSeatingScheduleService.class);

    @Resource
    private ClassInfoService classInfoService;
    @Resource
    private StudentService studentService;


    /**
     * 根据grade分配考场
     *
     * @param grade
     * @return
     */
    public CattyResult<Map<Integer, List<StudentDO>>> scheduleExamSeating(Integer grade,Integer examNum) {
        CattyResult<Map<Integer, List<StudentDO>>> cattyResult = new CattyResult<>();

        // 根据年级查询所有学生
        var allStudentList = studentService.listStudentByGrade(grade);
        if (CollectionUtils.isEmpty(allStudentList)){
            cattyResult.setMessage("根据年级未查询到学生信息");
            return cattyResult;
        }

        List<Integer> classIdList = new ArrayList<>();
        List<StudentDTO> studentDTOList = new ArrayList<>();
        for (StudentDO x:allStudentList){
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setClassId(x.getClassNum());
            studentDTO.setStudentClassId(x.getStudentClassId());
            studentDTO.setName(x.getStudentName());
            studentDTO.setUsed(false);
            studentDTOList.add(studentDTO);

            if (!classIdList.contains(x.getClassNum())){
                classIdList.add(x.getClassNum());
            }
        }
        var studentListSize = studentDTOList.size();
        var surplusStudentNum = studentListSize % examNum;


        cattyResult.setSuccess(true);
        return cattyResult;
    }

}
