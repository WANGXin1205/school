package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.banner.enums.WorkDayEnum;
import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.SchoolCommonService;
import com.work.school.mysql.common.service.SubjectService;
import com.work.school.mysql.common.service.TeacherService;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.service.dto.JudgeClassRightDTO;
import com.work.school.mysql.timetable.service.dto.TimeTableDTO;
import com.work.school.mysql.timetable.service.dto.TimeTableKeyDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class TimeTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableService.class);
    /**
     * 停机条件
     */
    private static final Integer STOP_WEIGHT = -100;

    @Resource
    private SubjectService subjectService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private SchoolCommonService schoolCommonService;

    /**
     * 排课接口
     *
     * @param grade
     * @return
     */
    public CattyResult<List<TimeTableDTO>> planTimeTable(Integer grade) {
        CattyResult<List<TimeTableDTO>> cattyResult = new CattyResult<>();

        // 初始数据赋值
        CattyResult<SchoolGradeDefaultDTO> preparePlanTimeTableResult = this.preparePlanTimeTable(grade);
        if (!preparePlanTimeTableResult.isSuccess()) {
            LOGGER.warn(preparePlanTimeTableResult.getMessage());
            cattyResult.setMessage(preparePlanTimeTableResult.getMessage());
            return cattyResult;
        }
        var schoolGradeDefaultDTO = preparePlanTimeTableResult.getData();

        // 计算权重，并且赋值
        CattyResult<HashMap<TimeTableKeyDTO, Integer>> computerWeightResult = this.computerWeight(schoolGradeDefaultDTO);
        if (!computerWeightResult.isSuccess()) {
            LOGGER.warn(computerWeightResult.getMessage());
            cattyResult.setMessage(computerWeightResult.getMessage());
            return cattyResult;
        }
        HashMap<TimeTableKeyDTO, Integer> timeTableMap = computerWeightResult.getData();


        // 结果赋值
        var allSubjectMap = schoolGradeDefaultDTO.getAllSubjectMap();
        List<TimeTableDTO> timeTableDTOList = new ArrayList<>();
        for (int x = 1; x <= SchoolGradeDefaultDTO.getWorkDay(); x++) {

            for (ClassInfoDO y : schoolGradeDefaultDTO.getAllClassInfoList()) {
                HashMap<TimeTableKeyDTO, String> timeTableShowMap = new HashMap<>(16);
                TimeTableDTO timeTableDTO = new TimeTableDTO();

                for (int z = 1; z <= SchoolGradeDefaultDTO.getTime(); z++) {
                    TimeTableKeyDTO timeTableKeyDTO = new TimeTableKeyDTO();
                    timeTableKeyDTO.setWorkDay(x);
                    timeTableKeyDTO.setClassNum(y.getClassNum());
                    timeTableKeyDTO.setTime(z);
                    var subjectId = timeTableMap.get(timeTableKeyDTO);
                    SubjectDO subjectDO = allSubjectMap.get(subjectId);
                    timeTableShowMap.put(timeTableKeyDTO, subjectDO.getName());

                    timeTableDTO.setWorkDay(x);
                    timeTableDTO.setWorkDayDesc(WorkDayEnum.getDesc(x));
                    timeTableDTO.setClassNum(y.getClassNum());
                    timeTableDTO.setClassName(y.getClassName());
                    timeTableDTO.setTimeTableShowMap(timeTableShowMap);
                }
                timeTableDTOList.add(timeTableDTO);
            }

        }

        cattyResult.setData(timeTableDTOList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }


    /**
     * 准备默认学校配置
     *
     * @param grade
     * @return
     */
    private CattyResult<SchoolGradeDefaultDTO> preparePlanTimeTable(Integer grade) {
        CattyResult<SchoolGradeDefaultDTO> cattyResult = new CattyResult<>();

        var schoolDefaultDTO = schoolCommonService.getSchoolDefaultDTO(grade);

        cattyResult.setData(schoolDefaultDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 计算权重并且赋值
     *
     * @param schoolGradeDefaultDTO
     * @return
     */
    private CattyResult<HashMap<TimeTableKeyDTO, Integer>> computerWeight(SchoolGradeDefaultDTO schoolGradeDefaultDTO) {
        CattyResult<HashMap<TimeTableKeyDTO, Integer>> cattyResult = new CattyResult<>();
        HashMap<TimeTableKeyDTO, Integer> timeTableMap = new HashMap<>(16);

        Integer workDay = SchoolGradeDefaultDTO.getWorkDay();
        Integer classSize = schoolGradeDefaultDTO.getClassSize();
        var time = SchoolGradeDefaultDTO.getTime();
        var allSubjectWeightList = schoolGradeDefaultDTO.getAllSubjectWeightList();
        var classSubjectTeacherMap = schoolGradeDefaultDTO.getClassSubjectTeacherMap();
        var classSubjectTeachingNumMap = schoolGradeDefaultDTO.getClassSubjectTeachingNumMap();
        var teacherFreeMap = schoolGradeDefaultDTO.getAllTeacherFreeMap();

        // 权重赋值
        for (int x = 1; x <= workDay; x++) {

            for (int y = 1; y <= classSize; y++) {
                for (int z = 1; z <= time; z++) {

                    InitSubjectWeightDTO initSubjectWeightDTO = this.packInitSubjectWeightDTO(x, y, z,
                            allSubjectWeightList, timeTableMap, classSubjectTeachingNumMap);

                    CattyResult<List<SubjectWeightDTO>> initSubjectWeightResult = subjectService.initSubjectWeight(initSubjectWeightDTO);
                    if (!initSubjectWeightResult.isSuccess()) {
                        cattyResult.setMessage(initSubjectWeightResult.getMessage());
                        return cattyResult;
                    }
                    List<SubjectWeightDTO> subjectWeightDTOList = initSubjectWeightResult.getData();

                    boolean fitFlag = true;
                    boolean passFlag = true;
                    while (fitFlag) {

                        CattyResult<SubjectWeightDTO> computerMaxSubjectWeightResult
                                = subjectService.computerMaxSubjectWeight(y, subjectWeightDTOList, passFlag);
                        if (!computerMaxSubjectWeightResult.isSuccess()) {
                            LOGGER.warn(computerMaxSubjectWeightResult.getMessage());
                            cattyResult.setMessage(computerMaxSubjectWeightResult.getMessage());
                            return cattyResult;
                        }
                        SubjectWeightDTO maxSubjectWeightDTO = computerMaxSubjectWeightResult.getData();

                        // 查询该科目是否合适
                        JudgeClassRightDTO judgeTeacherDTO = this.packJudgeClassRightDTO(x, y, z,
                                maxSubjectWeightDTO, classSubjectTeacherMap, teacherFreeMap, timeTableMap);
                        CattyResult<Boolean> judgeTeacherIsFreeResult = teacherService.judgeTeacherIsFree(judgeTeacherDTO);
                        if (!judgeTeacherIsFreeResult.isSuccess()) {
                            cattyResult.setMessage(judgeTeacherIsFreeResult.getMessage());
                            return cattyResult;
                        }
                        boolean teacherFreeFlag = judgeTeacherIsFreeResult.getData();

                        // 如果合适，赋值
                        if (teacherFreeFlag) {
                            fitFlag = false;
                            passFlag = true;

                            // 给课程表里面加这节课
                            TimeTableKeyDTO timeTableKeyDTO = new TimeTableKeyDTO();
                            timeTableKeyDTO.setWorkDay(x);
                            timeTableKeyDTO.setClassNum(y);
                            timeTableKeyDTO.setTime(z);
                            timeTableMap.put(timeTableKeyDTO, maxSubjectWeightDTO.getId());

                            ClassSubjectKeyDTO classSubjectKeyDTO = new ClassSubjectKeyDTO();
                            classSubjectKeyDTO.setClassNum(y);
                            classSubjectKeyDTO.setSubjectId(maxSubjectWeightDTO.getId());
                            var teacherId = classSubjectTeacherMap.get(classSubjectKeyDTO);

                            TeacherFreeKeyDTO teacherFreeKeyDTO = new TeacherFreeKeyDTO();
                            teacherFreeKeyDTO.setWorkDay(x);
                            teacherFreeKeyDTO.setTeacherId(teacherId);
                            var timeList = teacherFreeMap.get(teacherFreeKeyDTO);
                            if (CollectionUtils.isEmpty(timeList)) {
                                timeList = new ArrayList<>();
                            }
                            timeList.add(z);
                            teacherFreeMap.put(teacherFreeKeyDTO, timeList);

                            // 赋值的班级课程次数要减少，并且权重清零
                            subjectWeightDTOList = subjectService.clearSubjectWeightDTOList(maxSubjectWeightDTO, subjectWeightDTOList);
                        }
                        // 如果不合适，passFlag变为假
                        if (!teacherFreeFlag) {
                            passFlag = false;
                            if (maxSubjectWeightDTO.getWeight() < STOP_WEIGHT) {
                                cattyResult.setMessage("在此权重下，没有得到局部最优解");
                                return cattyResult;
                            }
                        }
                    }

                }
            }

        }

        cattyResult.setData(timeTableMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 装配课程是否合适的参数
     *
     * @param workDay
     * @param classNum
     * @param time
     * @param subjectWeightDTO
     * @param classSubjectTeacherMap
     * @param teacherFreeMap
     * @param timeTableMap
     * @return
     */
    private JudgeClassRightDTO packJudgeClassRightDTO(Integer workDay, Integer classNum, Integer time,
                                                      SubjectWeightDTO subjectWeightDTO,
                                                      Map<ClassSubjectKeyDTO, Integer> classSubjectTeacherMap,
                                                      Map<TeacherFreeKeyDTO, List<Integer>> teacherFreeMap,
                                                      HashMap<TimeTableKeyDTO, Integer> timeTableMap) {
        JudgeClassRightDTO judgeTeacherDTO = new JudgeClassRightDTO();
        judgeTeacherDTO.setWorkDay(workDay);
        judgeTeacherDTO.setClassNum(classNum);
        judgeTeacherDTO.setTime(time);
        judgeTeacherDTO.setSubjectWeightDTO(subjectWeightDTO);
        judgeTeacherDTO.setClassSubjectTeacherMap(classSubjectTeacherMap);
        judgeTeacherDTO.setTeacherFreeMap(teacherFreeMap);
        judgeTeacherDTO.setTimeTableMap(timeTableMap);

        return judgeTeacherDTO;
    }

    /**
     * 组装 初始化 权重数据
     *
     * @param workDay
     * @param classNum
     * @param time
     * @param subjectWeightDTOList
     * @param timeTableMap
     * @param classSubjectTeachingNumMap
     * @return
     */
    private InitSubjectWeightDTO packInitSubjectWeightDTO(Integer workDay, Integer classNum, Integer time,
                                                          List<SubjectWeightDTO> subjectWeightDTOList,
                                                          HashMap<TimeTableKeyDTO, Integer> timeTableMap,
                                                          Map<ClassSubjectKeyDTO, Integer> classSubjectTeachingNumMap) {
        InitSubjectWeightDTO initSubjectWeightDTO = new InitSubjectWeightDTO();
        initSubjectWeightDTO.setWorkDay(workDay);
        initSubjectWeightDTO.setClassNum(classNum);
        initSubjectWeightDTO.setTime(time);
        initSubjectWeightDTO.setSubjectWeightDTOList(subjectWeightDTOList);
        initSubjectWeightDTO.setTimeTableMap(timeTableMap);
        initSubjectWeightDTO.setClassSubjectTeachingNumMap(classSubjectTeachingNumMap);

        return initSubjectWeightDTO;
    }
}
