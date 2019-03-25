package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.dao.mapper.TeacherMapper;
import com.work.school.mysql.common.service.dto.ClassSubjectKeyDTO;
import com.work.school.mysql.common.service.dto.TeacherFreeKeyDTO;
import com.work.school.mysql.timetable.service.dto.JudgeClassRightDTO;
import com.work.school.mysql.timetable.service.dto.TimeTableKeyDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.security.auth.Subject;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 7:30 PM
 * @Description
 */
@Service
public class TeacherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherService.class);

    /**
     * 小课类型
     */
    private static final Integer OTHER_SUBJECT_TYPE = 2;
    /**
     * 特殊的课程分类 3
     */
    private static final Integer SPECIAL_CLASS_TYPE = 3;
    /**
     * 教师一天上课节数不能超过4节课
     */
    private static final Integer SUP_TIME = 4;

    @Resource
    private TeacherMapper teacherMapper;


    /**
     * 判断教师是否空闲
     *
     * @param judgeTeacherDTO
     * @return
     */
    public CattyResult<Boolean> judgeIsFree(JudgeClassRightDTO judgeTeacherDTO) {
        CattyResult<Boolean> cattyResult = new CattyResult<>();
        boolean freeFlag = true;

        // 特殊课程的判断
        Integer subjectType = judgeTeacherDTO.getSubjectWeightDTO().getType();
        if (SPECIAL_CLASS_TYPE.equals(subjectType)) {
            cattyResult.setData(freeFlag);
            cattyResult.setSuccess(true);
            return cattyResult;
        }

        var workDay = judgeTeacherDTO.getWorkDay();
        var classNum = judgeTeacherDTO.getClassNum();
        Integer time = judgeTeacherDTO.getTime();
        var subjectWeightDTO = judgeTeacherDTO.getSubjectWeightDTO();
        var subjectId = subjectWeightDTO.getId();
        var classSubjectTeacherMap = judgeTeacherDTO.getClassSubjectTeacherMap();
        var teacherFreeMap = judgeTeacherDTO.getTeacherFreeMap();
        var timeTableMap = judgeTeacherDTO.getTimeTableMap();

        ClassSubjectKeyDTO classSubjectKeyDTO = new ClassSubjectKeyDTO();
        classSubjectKeyDTO.setClassNum(classNum);
        classSubjectKeyDTO.setSubjectId(subjectId);
        var teacherId = classSubjectTeacherMap.get(classSubjectKeyDTO);

        TeacherFreeKeyDTO teacherFreeKeyDTO = new TeacherFreeKeyDTO();
        teacherFreeKeyDTO.setWorkDay(workDay);
        teacherFreeKeyDTO.setTeacherId(teacherId);

        List<Integer> timeList = teacherFreeMap.get(teacherFreeKeyDTO);
        if (CollectionUtils.isNotEmpty(timeList)) {
            boolean timeFlag = timeList.contains(time) || timeList.size() > SUP_TIME;
            if (timeFlag) {
                freeFlag = false;
            }
        }

        for (TimeTableKeyDTO x:timeTableMap.keySet()){
            var useSubjectId = timeTableMap.get(x);

            // 同一天同一个班小课程不允许上两节
            boolean otherSubjectRepeatFlag = workDay.equals(x.getWorkDay()) && classNum.equals(x.getClassNum())
                    && OTHER_SUBJECT_TYPE.equals(subjectWeightDTO.getType()) && useSubjectId.equals(subjectId);
            if (otherSubjectRepeatFlag){
                freeFlag = false;
            }
        }

        cattyResult.setData(freeFlag);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 科目id 与 上课教师id 对应关系
     *
     * @param teacherDOList
     * @return
     */
    public CattyResult<Map<Integer, List<Integer>>> listAllSubjectTeacherMap(List<TeacherDO> teacherDOList) {
        CattyResult<Map<Integer, List<Integer>>> cattyResult = new CattyResult<>();

        if (CollectionUtils.isEmpty(teacherDOList)) {
            cattyResult.setMessage("未查询到任何上课教师");
            return cattyResult;
        }

        Map<Integer, List<Integer>> subjectTeacherMap = teacherDOList.stream().collect(groupingBy(TeacherDO::getSubjectId,
                Collectors.mapping(TeacherDO::getId, Collectors.toList())));

        cattyResult.setData(subjectTeacherMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 教师id 与 groupId 对应关系
     *
     * @param teacherDOList
     * @return
     */
    public CattyResult<Map<Integer, Integer>> listAllTeacherGroupMap(List<TeacherDO> teacherDOList) {
        CattyResult<Map<Integer, Integer>> cattyResult = new CattyResult<>();

        if (CollectionUtils.isEmpty(teacherDOList)) {
            cattyResult.setMessage("未查询到任何上课教师");
            return cattyResult;
        }

        Map<Integer, Integer> teacherGroupMap = teacherDOList.stream()
                .collect(Collectors.toMap(TeacherDO::getId, TeacherDO::getTeacherGroupId));

        cattyResult.setData(teacherGroupMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 查询某个年级下所有上课教师
     *
     * @param grade
     * @return
     */
    public List<TeacherDO> listAllTeachingTeacherByGrade(Integer grade) {
        return teacherMapper.listAllTeachingTeacherByGrade(grade);
    }

}
