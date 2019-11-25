package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class PrepareService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareService.class);

    @Resource
    private ClassInfoService classInfoService;
    @Resource
    private TeacherSubjectService teacherSubjectService;
    @Resource
    private SubjectService subjectService;
    @Resource
    private ClassroomMaxCapacityService classroomMaxCapacityService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private CheckingService checkingService;

    /**
     * 模拟退火算法准备学校默认配置
     *
     * @return
     */
    public CattyResult<TimeTablingUseSimulateAnnealDTO> prepareTimeTablingUseSimulateAnneal() {
        CattyResult<TimeTablingUseSimulateAnnealDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();
        var specialSubjectTimeMap = this.getSpecialSubjectTime();

        TimeTablingUseSimulateAnnealDTO timeTablingUseSimulateAnnealDTO = new TimeTablingUseSimulateAnnealDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseSimulateAnnealDTO);
        timeTablingUseSimulateAnnealDTO.setSpecialSubjectTimeMap(specialSubjectTimeMap);

        cattyResult.setData(timeTablingUseSimulateAnnealDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 遗传算法准备学校默认配置
     *
     * @return
     */
    public CattyResult<TimeTablingUseGeneticDTO> prepareTimeTablingUseGenetic() {
        CattyResult<TimeTablingUseGeneticDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();
        var specialSubjectTimeMap = this.getSpecialSubjectTime();

        TimeTablingUseGeneticDTO timeTablingUseGeneticDTO = new TimeTablingUseGeneticDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseGeneticDTO);
        timeTablingUseGeneticDTO.setSpecialSubjectTimeMap(specialSubjectTimeMap);

        cattyResult.setData(timeTablingUseGeneticDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 获得特殊课程的上课时间 id+次数
     *
     * @return
     */
    private HashMap<String, String> getSpecialSubjectTime() {
        HashMap<String, String> specialSubjectTimeMap = new HashMap<>();
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.CLASS_MEETING_TIME_KEY,GeneticDefaultValueDTO.CLASS_MEETING_TIME_VALUE);
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.WRITING_TIME_KEY,GeneticDefaultValueDTO.WRITING_TIME_VALUE);
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.SCHOOL_BASED_TIME_ONE_KEY,GeneticDefaultValueDTO.SCHOOL_BASED_TIME_ONE_VALUE);
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.SCHOOL_BASED_TIME_TWO_KEY,GeneticDefaultValueDTO.SCHOOL_BASED_TIME_TWO_VALUE);
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.SCHOOL_BASED_TIME_THREE_KEY,GeneticDefaultValueDTO.SCHOOL_BASED_TIME_THREE_VALUE);
        specialSubjectTimeMap.put(GeneticDefaultValueDTO.SCHOOL_BASED_TIME_FOUR_KEY,GeneticDefaultValueDTO.SCHOOL_BASED_TIME_FOUR_VALUE);

        return specialSubjectTimeMap;
    }

    /**
     * 回溯算法准备学校默认配置
     *
     * @return
     */
    public CattyResult<TimeTablingUseBacktrackingDTO> prepareTimeTablingUseBacktracking() {
        CattyResult<TimeTablingUseBacktrackingDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();

        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = new TimeTablingUseBacktrackingDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseBacktrackingDTO);

        cattyResult.setData(timeTablingUseBacktrackingDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 动态权重回溯算法准备默认学校配置
     *
     * @return
     */
    public CattyResult<TimeTablingUseDynamicWeightsAndBacktrackingDTO> prepareTimeTablingUseDynamicWeightsAndBacktracking() {
        CattyResult<TimeTablingUseDynamicWeightsAndBacktrackingDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();

        TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO = new TimeTablingUseDynamicWeightsAndBacktrackingDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseDynamicWeightsAndBacktrackingDTO);

        cattyResult.setSuccess(true);
        cattyResult.setData(timeTablingUseDynamicWeightsAndBacktrackingDTO);
        return cattyResult;
    }

    /**
     * 准备默认学校配置
     *
     * @return
     */
    public PrepareTimeTablingDTO prepareTimeTabling() {

        var allGradeClassInfo = classInfoService.listAllClass();
        var gradeClassCountMap = classInfoService.getGradeClassCountMap(allGradeClassInfo);

        var allSubjects = subjectService.listAllSubject();
        var allSubjectNameMap = subjectService.getAllSubjectNameMap(allSubjects);
        var allSubjectDTO = subjectService.listAllSubjectDTO();
        var gradeSubjectMap = subjectService.getGradeSubjectMap(allSubjectDTO);

        var allSubjectTeacherGradeClassDTO = teacherSubjectService.listAllSubjectTeacherGradeClassDTO();
        var subjectGradeClassTeacherMap = teacherSubjectService.getSubjectGradeClassTeacherMap(allSubjectTeacherGradeClassDTO);
        var subjectGradeClassTeacherCountMap = teacherSubjectService.getSubjectGradeClassTeacherCountMap(allSubjectTeacherGradeClassDTO);
        var teacherSubjectListMap = teacherSubjectService.getTeacherSubjectListMap(allSubjectTeacherGradeClassDTO);

        var allWorkTeacher = teacherService.listAllWorkTeacher();

        var classroomMaxCapacityMap = classroomMaxCapacityService.getClassroomMaxCapacityMap();
        var classRoomUsedCountMap = subjectService.initClassRoomUsedCountMap(classroomMaxCapacityMap);

        // 准备求解的中间变量的默认值
        HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap = this.getGradeClassNumSubjectWeightMap(gradeClassCountMap, gradeSubjectMap);
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap = this.getTimeTableMap(gradeClassSubjectWeightMap);
        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap = this.getTeacherTeachingMap(allWorkTeacher);
        var gradeClassNumWorkDaySubjectCountMap = this.getGradeClassNumWorkDaySubjectCountMap(gradeClassCountMap, allSubjects);
        var orderGradeClassNumWorkDayTimeMap = this.getOrderGradeClassNumWorkDayTimeMap(gradeClassCountMap);
        HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassSubjectFrequencyMap
                = this.getGradeClassSubjectFrequencyMap(gradeSubjectMap, gradeClassCountMap);
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>>
                gradeClassNumWorkDayTimeSubjectIdCanUseMap = this.getGradeClassNumWorkDayTimeSubjectIdCanUseMap(gradeSubjectMap, gradeClassCountMap);
        HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap = new HashMap<>();
        for (Integer grade : gradeSubjectMap.keySet()) {
            var subjectDTOList = gradeSubjectMap.get(grade);
            var subjectDTOMap = (HashMap<Integer, SubjectDTO>) subjectDTOList.stream().collect(Collectors.toMap(SubjectDTO::getSubjectId, Function.identity()));
            gradeSubjectDTOMap.put(grade, subjectDTOMap);
        }

        // 检查年级信息无误
        CattyResult checkGradeResult = checkingService.checkGrade(gradeClassCountMap, gradeSubjectMap);
        if (!checkGradeResult.isSuccess()) {
            throw new TransactionException(checkGradeResult.getMessage());
        }

        // 检查教师和上课教师信息无误
        CattyResult checkTeacherResult = checkingService.checkTeacher(allWorkTeacher, teacherTeachingMap);
        if (!checkTeacherResult.isSuccess()) {
            throw new TransactionException(checkTeacherResult.getMessage());
        }
        // 检查问题有解
        var checkTimeTableSolutionResult = checkingService.checkTimeTableSolution(gradeSubjectMap, allSubjectNameMap, subjectGradeClassTeacherMap);
        if (!checkTimeTableSolutionResult.isSuccess()) {
            throw new TransactionException(checkTimeTableSolutionResult.getMessage());
        }

        // 组装planTimeTablePrepareDTO
        PrepareTimeTablingDTO prepareTimeTablingDTO = new PrepareTimeTablingDTO();
        prepareTimeTablingDTO.setAllGradeClassInfo(allGradeClassInfo);
        prepareTimeTablingDTO.setGradeClassCountMap(gradeClassCountMap);
        prepareTimeTablingDTO.setAllSubject(allSubjects);
        prepareTimeTablingDTO.setAllSubjectNameMap(allSubjectNameMap);
        prepareTimeTablingDTO.setGradeSubjectMap(gradeSubjectMap);
        prepareTimeTablingDTO.setGradeClassNumWorkDaySubjectCountMap(gradeClassNumWorkDaySubjectCountMap);
        prepareTimeTablingDTO.setClassroomMaxCapacityMap(classroomMaxCapacityMap);
        prepareTimeTablingDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
        prepareTimeTablingDTO.setAllWorkTeacher(allWorkTeacher);
        prepareTimeTablingDTO.setTeacherTeachingMap(teacherTeachingMap);
        prepareTimeTablingDTO.setAllSubjectTeacherGradeClassDTO(allSubjectTeacherGradeClassDTO);
        prepareTimeTablingDTO.setSubjectGradeClassTeacherMap(subjectGradeClassTeacherMap);
        prepareTimeTablingDTO.setSubjectGradeClassTeacherCountMap(subjectGradeClassTeacherCountMap);
        prepareTimeTablingDTO.setTeacherSubjectListMap(teacherSubjectListMap);
        prepareTimeTablingDTO.setGradeSubjectDTOMap(gradeSubjectDTOMap);
        prepareTimeTablingDTO.setGradeClassSubjectWeightMap(gradeClassSubjectWeightMap);
        prepareTimeTablingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassSubjectFrequencyMap);
        prepareTimeTablingDTO.setGradeClassNumWorkDayTimeSubjectIdCanUseMap(gradeClassNumWorkDayTimeSubjectIdCanUseMap);
        prepareTimeTablingDTO.setOrderGradeClassNumWorkDayTimeMap(orderGradeClassNumWorkDayTimeMap);
        prepareTimeTablingDTO.setTimeTableMap(timeTableMap);

        return prepareTimeTablingDTO;
    }

    /**
     * 获取年级班级科目和科目使用次数的map
     *
     * @param gradeSubjectMap
     * @param gradeClassCountMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getGradeClassSubjectFrequencyMap(Map<Integer, List<SubjectDTO>> gradeSubjectMap,
                                                                                                           HashMap<Integer, Integer> gradeClassCountMap) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassSubjectFrequencyMap = new HashMap<>();
        for (Integer grade : gradeSubjectMap.keySet()) {
            HashMap<Integer, HashMap<Integer, Integer>> classSubjectFrequencyMap = new HashMap<>();
            Integer classCount = gradeClassCountMap.get(grade);
            for (int classNum = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); classNum <= classCount; classNum++) {
                HashMap<Integer, Integer> subjectFrequencyMap = new HashMap<>();
                for (SubjectDTO subjectDTO : gradeSubjectMap.get(grade)) {
                    subjectFrequencyMap.put(subjectDTO.getSubjectId(), subjectDTO.getFrequency());
                }
                classSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
            }
            gradeClassSubjectFrequencyMap.put(grade, classSubjectFrequencyMap);
        }

        return gradeClassSubjectFrequencyMap;
    }

    /**
     * 获取排课节点对应表
     *
     * @param gradeClassCountMap
     * @return
     */
    private HashMap<Integer, GradeClassNumWorkDayTimeDTO> getOrderGradeClassNumWorkDayTimeMap(HashMap<Integer, Integer> gradeClassCountMap) {
        HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap = new HashMap<>();
        int count = SchoolTimeTableDefaultValueDTO.getStartCount();
        for (Integer grade : gradeClassCountMap.keySet()) {
            for (int classNum = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); classNum <= gradeClassCountMap.get(grade); classNum++) {
                for (int workDay = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex(); workDay <= SchoolTimeTableDefaultValueDTO.getWorkDay(); workDay++) {
                    for (int time = SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex(); time <= SchoolTimeTableDefaultValueDTO.getClassTime(); time++) {
                        GradeClassNumWorkDayTimeDTO gradeClassNumWorkDayTimeDTO = new GradeClassNumWorkDayTimeDTO();
                        gradeClassNumWorkDayTimeDTO.setGrade(grade);
                        gradeClassNumWorkDayTimeDTO.setClassNum(classNum);
                        gradeClassNumWorkDayTimeDTO.setWorkDay(workDay);
                        gradeClassNumWorkDayTimeDTO.setTime(time);
                        orderGradeClassNumWorkDayTimeMap.put(count, gradeClassNumWorkDayTimeDTO);

                        count = count + SubjectDefaultValueDTO.getOneCount();
                    }
                }
            }
        }
        return orderGradeClassNumWorkDayTimeMap;
    }


    /**
     * 获取科目使用的Map
     *
     * @param gradeSubjectMap
     * @param gradeClassCountMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> getGradeClassNumWorkDayTimeSubjectIdCanUseMap(Map<Integer, List<SubjectDTO>> gradeSubjectMap,
                                                                                                                                                            HashMap<Integer, Integer> gradeClassCountMap) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> subjectIdCanUseMap = new HashMap<>();

        for (int grade = SchoolTimeTableDefaultValueDTO.getStartGradeIndex(); grade <= gradeSubjectMap.keySet().size(); grade++) {

            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>> classNumWorkDayTimeSubjectCanUseMap = new HashMap<>();
            Integer classCount = gradeClassCountMap.get(grade);
            for (int classNum = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); classNum <= classCount; classNum++) {

                HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>> workDayTimeSubjectCanUseMap = new HashMap<>();
                for (int worDay = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex(); worDay <= SchoolTimeTableDefaultValueDTO.getWorkDay(); worDay++) {

                    HashMap<Integer, HashMap<Integer, Boolean>> timeSubjectCanUseMap = new HashMap<>();
                    for (int time = SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex(); time <= SchoolTimeTableDefaultValueDTO.getClassTime(); time++) {

                        HashMap<Integer, Boolean> subjectCanUseMap = new HashMap<>();
                        for (SubjectDTO subjectDTO : gradeSubjectMap.get(grade)) {
                            subjectCanUseMap.put(subjectDTO.getSubjectId(), true);
                        }
                        timeSubjectCanUseMap.put(time, subjectCanUseMap);
                    }
                    workDayTimeSubjectCanUseMap.put(worDay, timeSubjectCanUseMap);
                }
                classNumWorkDayTimeSubjectCanUseMap.put(classNum, workDayTimeSubjectCanUseMap);
            }
            subjectIdCanUseMap.put(grade, classNumWorkDayTimeSubjectCanUseMap);
        }

        return subjectIdCanUseMap;
    }

    /**
     * 获取每个年级每个班的课程权重
     *
     * @param gradeClassCountMap
     * @param allGradeSubjectMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> getGradeClassNumSubjectWeightMap(HashMap<Integer, Integer> gradeClassCountMap,
                                                                                                        HashMap<Integer, List<SubjectDTO>> allGradeSubjectMap) {
        HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap = new HashMap<>();

        for (Integer x : gradeClassCountMap.keySet()) {
            // 获取年级下班级数目
            Integer count = gradeClassCountMap.get(x);
            // 获取年级下所有科目
            List<SubjectDTO> allSubjectDTO = allGradeSubjectMap.get(x);

            HashMap<Integer, List<SubjectWeightDTO>> classSubjectWeightMap = new HashMap<>();
            for (int y = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); y <= count; y++) {

                // 将科目变为带权重的科目
                var allSubjectWeight = subjectService.copyAllSubjectToAllSubjectWeight(allSubjectDTO);

                classSubjectWeightMap.put(y, allSubjectWeight);
            }

            gradeClassSubjectWeightMap.put(x, classSubjectWeightMap);
        }

        return gradeClassSubjectWeightMap;
    }

    /**
     * 课程表
     *
     * @param gradeClassSubjectWeightMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getTimeTableMap(HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap = new HashMap<>();

        for (Integer x : gradeClassSubjectWeightMap.keySet()) {
            var classSubjectWeightMap = gradeClassSubjectWeightMap.get(x);

            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> classWorkDayTimeSubjectIdMap = new HashMap<>();
            for (Integer y : classSubjectWeightMap.keySet()) {

                HashMap<Integer, HashMap<Integer, Integer>> workDayTimeSubjectIdMap = new HashMap<>();
                for (int i = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex(); i <= SchoolTimeTableDefaultValueDTO.getWorkDay(); i++) {

                    HashMap<Integer, Integer> timeSubjectIdMap = new HashMap<>();
                    for (int j = SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex(); j <= SchoolTimeTableDefaultValueDTO.getClassTime(); j++) {
                        timeSubjectIdMap.put(j, null);
                    }
                    workDayTimeSubjectIdMap.put(i, timeSubjectIdMap);
                }
                classWorkDayTimeSubjectIdMap.put(y, workDayTimeSubjectIdMap);
            }
            timeTableMap.put(x, classWorkDayTimeSubjectIdMap);
        }

        return timeTableMap;
    }

    /**
     * 获取所有教师上课时间map
     *
     * @param allWorkTeacher
     * @return HashMap<teacherId HashMap < workDay List < time>>>
     */
    private HashMap<Integer, HashMap<Integer, List<Integer>>> getTeacherTeachingMap(List<TeacherDO> allWorkTeacher) {
        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap = new HashMap<>();

        for (TeacherDO x : allWorkTeacher) {
            HashMap<Integer, List<Integer>> teachingMap = new HashMap<>();
            for (int i = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex(); i <= SchoolTimeTableDefaultValueDTO.getWorkDay(); i++) {
                teachingMap.put(i, new ArrayList<>());
            }
            teacherTeachingMap.put(x.getId(), teachingMap);
        }

        return teacherTeachingMap;
    }

    /**
     * 所有课程按照grade，workDay，Subject count 的Map
     *
     * @param gradeClassCountMap
     * @param allSubjects
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> getGradeClassNumWorkDaySubjectCountMap(HashMap<Integer, Integer> gradeClassCountMap,
                                                                                                                                   List<SubjectDO> allSubjects) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorkDaySubjectCountMap = new HashMap<>();
        for (Integer grade : gradeClassCountMap.keySet()) {

            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> classNumWorkDaySubjectCountMap = new HashMap<>();
            for (Integer classNum = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); classNum <= gradeClassCountMap.get(grade); classNum++) {

                HashMap<Integer, HashMap<Integer, Integer>> workDaySubjectCountMap = new HashMap<>();
                for (Integer workDay = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex()
                     ; workDay <= SchoolTimeTableDefaultValueDTO.getWorkDay(); workDay++) {

                    HashMap<Integer, Integer> subjectCountMap = new HashMap<>();
                    for (SubjectDO subjectDO : allSubjects) {
                        subjectCountMap.put(subjectDO.getId(), SubjectWeightDefaultValueDTO.getZeroFrequency());
                    }
                    workDaySubjectCountMap.put(workDay, subjectCountMap);
                }
                classNumWorkDaySubjectCountMap.put(classNum, workDaySubjectCountMap);
            }
            gradeClassNumWorkDaySubjectCountMap.put(grade, classNumWorkDaySubjectCountMap);
        }

        return gradeClassNumWorkDaySubjectCountMap;
    }

}