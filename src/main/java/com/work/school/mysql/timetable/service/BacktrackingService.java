package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.BacktrackingTypeEnum;
import com.work.school.mysql.timetable.service.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class BacktrackingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktrackingService.class);

    private static final int ONLY_ONE_COUNT = 1;
    private static final int ADD_SCORE = 1;
    private static final int BIG_SCORE = 10;
    /**
     * 接受概率
     */
    private static final BigDecimal ACCEPT_PRO = new BigDecimal("0.1");

    @Resource
    private SubjectService subjectService;
    @Resource
    private GeneticService geneticService;

    /**
     * 回溯算法核心
     *
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> backtracking(TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO, BacktrackingTypeEnum backtrackingTypeEnum) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();

        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        List<String> messageList = new ArrayList<>();
        for (int order = SchoolTimeTableDefaultValueDTO.getStartOrder(); order <= orderGradeClassNumWorkDayTimeMap.keySet().size(); order++) {
            var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
            var grade = gradeClassNumWorkDayTimeDTO.getGrade();
            var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
            var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            var time = gradeClassNumWorkDayTimeDTO.getTime();

            while (timeTablingUseBacktrackingDTO.getTimeTableMap().get(grade).get(classNum).get(workDay).get(time) == null) {
                // 获取某个年级某个班的课程和使用次数
                var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
                var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);

                // 获取课程使用表
                var subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, timeTablingUseBacktrackingDTO.getOrderSubjectIdCanUseMap());

                // 根据上课次数和时间点判断是否能够选择的课程
                this.updateSubjectIdCanUseMap(subjectIdCanUseMap, subjectFrequencyMap, workDay, time);

                // 检查是否有回溯课程
                var backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                if (backFlag) {
                    var rollbackDTO = this.getRollbackDTO(order, timeTablingUseBacktrackingDTO);
                    this.rollback(rollbackDTO);
                    this.getTimeTablingUseBacktrackingDTO(rollbackDTO, timeTablingUseBacktrackingDTO);
                    order = rollbackDTO.getOrder();
                    gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
                    grade = gradeClassNumWorkDayTimeDTO.getGrade();
                    classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
                    workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                    time = gradeClassNumWorkDayTimeDTO.getTime();
                }
                if (!backFlag) {
                    // 选择一个课程
                    Integer chooseSubjectId = null;
                    if (BacktrackingTypeEnum.BA.equals(backtrackingTypeEnum)) {
                        chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                    }
                    if (BacktrackingTypeEnum.DW_BA.equals(backtrackingTypeEnum)) {
                        chooseSubjectId = this.getMaxWeightSubjectId(order, timeTablingUseBacktrackingDTO);
                    }

                    // 更新课程使用状态
                    subjectIdCanUseMap.put(chooseSubjectId, false);

                    var checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseBacktrackingDTO);
                    var fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                    String message = this.packMessage(fitnessScoreDTO);
                    messageList.add(message);

                    // 检查是否满足排课需求
                    var checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, chooseSubjectId, timeTablingUseBacktrackingDTO);
                    boolean completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                    if (completeFlag) {
                        this.updateAllStatus(order, chooseSubjectId, timeTablingUseBacktrackingDTO);
                        if (order == orderGradeClassNumWorkDayTimeMap.keySet().size()) {
                            checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseBacktrackingDTO);
                            fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                            message = this.packMessage(fitnessScoreDTO);
                            messageList.add(message);
                        }
                    }
                    // 如果不满足排课需求，就需要回溯
                    while (!completeFlag) {
                        // 判断这一层的回溯点是否都已经使用，如果没有使用完毕，不需要回溯，选择下一个课程
                        subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, timeTablingUseBacktrackingDTO.getOrderSubjectIdCanUseMap());
                        backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                        if (!backFlag) {
                            // 回溯点不清零，记录该点的排课课程，下次不再选择这么课程
                            chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                            // 更新课程使用状态
                            subjectIdCanUseMap.put(chooseSubjectId, false);

                            checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseBacktrackingDTO);
                            fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                            message = this.packMessage(fitnessScoreDTO);
                            messageList.add(message);

                            // 检查是否满足排课需求
                            checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, chooseSubjectId, timeTablingUseBacktrackingDTO);
                            completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                            if (completeFlag) {
                                this.updateAllStatus(order, chooseSubjectId, timeTablingUseBacktrackingDTO);
                                if (order == orderGradeClassNumWorkDayTimeMap.keySet().size()) {
                                    checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseBacktrackingDTO);
                                    fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                                    message = this.packMessage(fitnessScoreDTO);
                                    messageList.add(message);
                                }
                            }
                        }

                        // 如果课程使用完毕，则找上一层的回溯点
                        if (backFlag) {
                            var rollbackDTO = this.getRollbackDTO(order, timeTablingUseBacktrackingDTO);
                            this.rollback(rollbackDTO);
                            this.getTimeTablingUseBacktrackingDTO(rollbackDTO, timeTablingUseBacktrackingDTO);
                            order = rollbackDTO.getOrder();
                            gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
                            grade = gradeClassNumWorkDayTimeDTO.getGrade();
                            classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
                            workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                            time = gradeClassNumWorkDayTimeDTO.getTime();
                            completeFlag = true;
                        }

                    }
                }

            }
        }

        long start = System.currentTimeMillis();
        geneticService.markToTXT(String.valueOf(start), messageList);

        cattyResult.setData(timeTablingUseBacktrackingDTO.getTimeTableMap());
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 组装适应度函数类
     *
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private CheckFitnessScoreDTO packFitnessScore(TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        CheckFitnessScoreDTO checkFitnessScoreDTO = new CheckFitnessScoreDTO();
        checkFitnessScoreDTO.setGradeClassCountMap(timeTablingUseFCDWBacktrackingDTO.getGradeClassCountMap());
        checkFitnessScoreDTO.setTimeTableMap(timeTablingUseFCDWBacktrackingDTO.getTimeTableMap());
        checkFitnessScoreDTO.setGradeSubjectDTOMap(timeTablingUseFCDWBacktrackingDTO.getGradeSubjectDTOMap());
        checkFitnessScoreDTO.setSubjectGradeClassTeacherMap(timeTablingUseFCDWBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkFitnessScoreDTO.setClassroomMaxCapacity(timeTablingUseFCDWBacktrackingDTO.getClassroomMaxCapacityMap());

        return checkFitnessScoreDTO;
    }

    /**
     * 组装适应度函数类
     *
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private CheckFitnessScoreDTO packFitnessScore(TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        CheckFitnessScoreDTO checkFitnessScoreDTO = new CheckFitnessScoreDTO();
        checkFitnessScoreDTO.setGradeClassCountMap(timeTablingUseBacktrackingDTO.getGradeClassCountMap());
        checkFitnessScoreDTO.setTimeTableMap(timeTablingUseBacktrackingDTO.getTimeTableMap());
        checkFitnessScoreDTO.setGradeSubjectDTOMap(timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap());
        checkFitnessScoreDTO.setSubjectGradeClassTeacherMap(timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkFitnessScoreDTO.setClassroomMaxCapacity(timeTablingUseBacktrackingDTO.getClassroomMaxCapacityMap());

        return checkFitnessScoreDTO;
    }

    /**
     * 计算适应度函数评分
     *
     * @param checkFitnessScoreDTO
     * @return
     */
    private FitnessScoreDTO computerFitnessScore(CheckFitnessScoreDTO checkFitnessScoreDTO) {
        var timeTableMap = checkFitnessScoreDTO.getTimeTableMap();
        var gradeClassCountMap = checkFitnessScoreDTO.getGradeClassCountMap();
        // 计算总共的班级数量
        int totalClassNum = 0;
        for (Integer grade : gradeClassCountMap.keySet()) {
            var classNumCount = gradeClassCountMap.get(grade);
            totalClassNum = totalClassNum + classNumCount;
        }
        var subjectGradeClassTeacherMap = checkFitnessScoreDTO.getSubjectGradeClassTeacherMap();
        var gradeSubjectDTOMap = checkFitnessScoreDTO.getGradeSubjectDTOMap();

        var allSize = totalClassNum * SchoolTimeTableDefaultValueDTO.getWorkDay() * SchoolTimeTableDefaultValueDTO.getClassTime();
        HashMap<String, Integer> everyTimeHaveClassMap = new HashMap<>();
        HashMap<String, Integer> oneTimeOneClassMoreSubjectMap = new HashMap<>();
        HashMap<Integer, HashMap<Integer, List<Integer>>> oneTimeOneTeacherMoreClassMap = new HashMap<>();
        HashMap<Integer, List<String>> fixedSubjectTimeMap = new HashMap<>();
        HashMap<String, List<Integer>> oneClassMoreOtherSubjectMap = new HashMap<>();
        HashMap<Integer, HashMap<String, List<String>>> otherNeedAreaSubjectIdCountMap = new HashMap<>();
        HashMap<String, Integer> mainSubjectCountMap = new HashMap<>();
        HashMap<String, List<Integer>> continueSubjectMap = new HashMap<>();
        HashMap<String, List<Integer>> goTimesHashMap = new HashMap<>();
        HashMap<String, List<Integer>> sportTimesHashMap = new HashMap<>();
        for (Integer grade : timeTableMap.keySet()) {
            var gradeStandard = geneticService.getStandard(grade.toString(), GeneticDefaultValueDTO.GRADE_STANDARD_LENGTH);
            var classNumWorkDayTimeSubjectMap = timeTableMap.get(grade);
            for (Integer classNum : classNumWorkDayTimeSubjectMap.keySet()) {
                var classNumStandard = geneticService.getStandard(classNum.toString(), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);
                var workDayTimeSubjectMap = classNumWorkDayTimeSubjectMap.get(classNum);
                for (Integer workDay : workDayTimeSubjectMap.keySet()) {
                    var workDayStandard = geneticService.getStandard(workDay.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                    var timeSubjectMap = workDayTimeSubjectMap.get(workDay);
                    for (Integer time : timeSubjectMap.keySet()) {

                        var timeStandard = geneticService.getStandard(time.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                        var subjectId = timeSubjectMap.get(time);
                        // 任何时刻都要有人上课
                        var gradeClassWorkDay = gradeStandard.concat(classNumStandard).concat(workDayStandard);
                        String gradeClassNumWorkDayTime = gradeClassWorkDay.concat(timeStandard);
                        everyTimeHaveClassMap.put(gradeClassNumWorkDayTime, subjectId);

                        // 同一时间一个班级上了多节课
                        oneTimeOneClassMoreSubjectMap.put(gradeClassNumWorkDayTime, subjectId);

                        // 同一时刻一个教师上了多节课
                        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
                        subjectGradeClassDTO.setSubjectId(subjectId);
                        subjectGradeClassDTO.setGrade(grade);
                        subjectGradeClassDTO.setClassNum(classNum);
                        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
                        if (teacherId != null) {
                            var workDayTimeMap = oneTimeOneTeacherMoreClassMap.get(teacherId);
                            List<Integer> timeList;
                            if (workDayTimeMap == null) {
                                timeList = new ArrayList<>();
                                timeList.add(time);
                                workDayTimeMap = new HashMap<>();
                            } else {
                                timeList = workDayTimeMap.get(workDay);
                                if (CollectionUtils.isEmpty(timeList)) {
                                    timeList = new ArrayList<>();
                                }
                                timeList.add(time);
                            }
                            workDayTimeMap.put(workDay, timeList);
                            oneTimeOneTeacherMoreClassMap.put(teacherId, workDayTimeMap);
                        }

                        // 固定时间上固定的课程
                        var allGradeSubjectMap = gradeSubjectDTOMap.get(grade);
                        var subjectDTO = allGradeSubjectMap.get(subjectId);
                        if (subjectId != null) {
                            var subjectIdStandard = geneticService.getStandard(subjectId.toString(), GeneticDefaultValueDTO.SUBJECT_ID_STANDARD_LENGTH);

                            // 固定时间上固定的课程
                            if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
                                var workDayTimeList = fixedSubjectTimeMap.get(subjectId);
                                if (CollectionUtils.isEmpty(workDayTimeList)) {
                                    workDayTimeList = new ArrayList<>();
                                }
                                String workDayTime = workDayStandard.concat(timeStandard);
                                workDayTimeList.add(workDayTime);
                                fixedSubjectTimeMap.put(subjectId, workDayTimeList);
                            }

                            // 小课一天在一个班只上一节小课
                            if (subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getOtherSubjectType())
                                    || subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType())) {
                                String key = subjectIdStandard.concat(gradeStandard).concat(classNumStandard).concat(workDayStandard);
                                var timeList = oneClassMoreOtherSubjectMap.get(key);
                                if (CollectionUtils.isEmpty(timeList)) {
                                    timeList = new ArrayList<>();
                                }
                                timeList.add(time);
                                oneClassMoreOtherSubjectMap.put(key, timeList);
                            }

                            // 功能部室不能超过最大班级数
                            if (subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType())) {
                                var workDayTimeGradeClassNumMap = otherNeedAreaSubjectIdCountMap.get(subjectId);
                                if (workDayTimeGradeClassNumMap == null) {
                                    workDayTimeGradeClassNumMap = new HashMap<>();
                                }
                                var workTimeStandard = workDayStandard.concat(timeStandard);
                                var gradeClassNumStandard = gradeStandard.concat(classNumStandard);
                                var gradeClassNumList = workDayTimeGradeClassNumMap.get(workDayStandard);
                                if (CollectionUtils.isEmpty(gradeClassNumList)) {
                                    gradeClassNumList = new ArrayList<>();
                                }
                                gradeClassNumList.add(gradeClassNumStandard);
                                workDayTimeGradeClassNumMap.put(workTimeStandard, gradeClassNumList);
                                otherNeedAreaSubjectIdCountMap.put(subjectId, workDayTimeGradeClassNumMap);
                            }

                            // 学生不能上连堂课
                            if (subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getMainSubjectType())) {
                                var key = gradeClassWorkDay.concat(subjectIdStandard);
                                var timeList = continueSubjectMap.get(key);
                                if (CollectionUtils.isEmpty(timeList)) {
                                    timeList = new ArrayList<>();
                                }
                                timeList.add(time);
                                continueSubjectMap.put(key, timeList);
                            }

                            // 第1，2节课是语文，数学课
                            if (time < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
                                mainSubjectCountMap.put(gradeClassNumWorkDayTime, subjectId);
                            }

                            // 围棋课不在下午
                            String gradeClassWorkDaySubject = gradeClassWorkDay.concat(subjectIdStandard);
                            if (SchoolTimeTableDefaultValueDTO.getSubjectGoId().equals(subjectId)) {
                                var goTimeList = goTimesHashMap.get(gradeClassWorkDaySubject);
                                if (CollectionUtils.isEmpty(goTimeList)) {
                                    goTimeList = new ArrayList<>();
                                }
                                goTimeList.add(time);
                                goTimesHashMap.put(gradeClassWorkDaySubject, goTimeList);
                            }

                            // 第3、5、6，7节不是体育课
                            // 体育课尽量在上午最后一节(第3节)，或者下午最后两节(第6,7节)
                            if (SchoolTimeTableDefaultValueDTO.getSubjectSportId().equals(subjectId)) {
                                var sportTimeList = sportTimesHashMap.get(gradeClassWorkDaySubject);
                                if (CollectionUtils.isEmpty(sportTimeList)) {
                                    sportTimeList = new ArrayList<>();
                                }
                                sportTimeList.add(time);
                                sportTimesHashMap.put(gradeClassWorkDaySubject, sportTimeList);
                            }
                        }

                    }
                }
            }
        }
        // 先计算硬约束冲突评分
        // 1.任何时刻都要有人上课，null为冲突
        int everyTimeHaveSubjectCount = 0;
        for (String gradeClassNumWorkDayTime : everyTimeHaveClassMap.keySet()) {
            var subjectId = everyTimeHaveClassMap.get(gradeClassNumWorkDayTime);
            if (subjectId == null) {
                everyTimeHaveSubjectCount = everyTimeHaveSubjectCount + ONLY_ONE_COUNT;
            }
        }

        // 2.同一时间一个班级上了多节课
        int oneTimeOneClassMoreSubjectCount = allSize - oneTimeOneClassMoreSubjectMap.size();

        // 3.同一时间一个教师上了多个班级的课程
        int oneTimeOneTeacherMoreClassCount = 0;
        for (Integer teacherId : oneTimeOneTeacherMoreClassMap.keySet()) {
            var workDayTimeListMap = oneTimeOneTeacherMoreClassMap.get(teacherId);
            for (Integer workDay : workDayTimeListMap.keySet()) {
                var timeList = workDayTimeListMap.get(workDay);
                var distinctTimeList = timeList.stream().distinct().collect(Collectors.toList());
                oneTimeOneTeacherMoreClassCount = oneTimeOneTeacherMoreClassCount + timeList.size() - distinctTimeList.size();
            }
        }

        // 4.固定时间上固定的课程
        int fixedSubjectIdCount = 0;
        for (Integer subjectId : fixedSubjectTimeMap.keySet()) {
            var workDayTimeList = fixedSubjectTimeMap.get(subjectId);
            if (SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectId)) {
                var mondayStandard = geneticService.getStandard(SchoolTimeTableDefaultValueDTO.getMondayNum().toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                var lastTimeStandard = geneticService.getStandard(SchoolTimeTableDefaultValueDTO.getClassMeetingTime().toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                var matchTime = mondayStandard.concat(lastTimeStandard);
                for (String workDayTime : workDayTimeList) {
                    if (!workDayTime.equals(matchTime)) {
                        fixedSubjectIdCount = fixedSubjectIdCount + ONLY_ONE_COUNT;
                    }
                }
            }
            if (SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectId)) {
                var wedStandard = geneticService.getStandard(SchoolTimeTableDefaultValueDTO.getWednesdayNum().toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                var lastTimeStandard = geneticService.getStandard(SchoolTimeTableDefaultValueDTO.getWritingTime().toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                var matchTime = wedStandard.concat(lastTimeStandard);
                for (String workDayTime : workDayTimeList) {
                    if (!workDayTime.equals(matchTime)) {
                        fixedSubjectIdCount = fixedSubjectIdCount + ONLY_ONE_COUNT;
                    }
                }
            }
            if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
                var friStandard = geneticService.getStandard(SchoolTimeTableDefaultValueDTO.getFridayNum().toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                List<String> matchTimeList = new ArrayList<>();
                for (Integer time : SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()) {
                    var timeStandard = geneticService.getStandard(time.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                    var matchTime = friStandard.concat(timeStandard);
                    matchTimeList.add(matchTime);
                }

                for (String workDayTime : workDayTimeList) {
                    if (!matchTimeList.contains(workDayTime)) {
                        fixedSubjectIdCount = fixedSubjectIdCount + ONLY_ONE_COUNT;
                    }
                }
            }

        }

        // 5.小课一天只上一节小课
        int oneClassMoreOtherSubject = 0;
        for (String key : oneClassMoreOtherSubjectMap.keySet()) {
            var timeList = oneClassMoreOtherSubjectMap.get(key);
            oneClassMoreOtherSubject = oneClassMoreOtherSubject + timeList.size() - ONLY_ONE_COUNT;
        }

        // 6.功能部室不能超过最大班级数
        int needAreaSubjectCount = 0;
        for (Integer subjectId : otherNeedAreaSubjectIdCountMap.keySet()) {
            var classroomMaxCapacityMap = checkFitnessScoreDTO.getClassroomMaxCapacity();
            var maxCount = classroomMaxCapacityMap.get(subjectId);
            var workDayTimeGradeClassMap = otherNeedAreaSubjectIdCountMap.get(subjectId);
            for (String workDayTime : workDayTimeGradeClassMap.keySet()) {
                var gradeClassNumList = workDayTimeGradeClassMap.get(workDayTime);
                if (maxCount < gradeClassNumList.size()) {
                    needAreaSubjectCount = needAreaSubjectCount + ONLY_ONE_COUNT;
                }
            }
        }

        int hardScore = everyTimeHaveSubjectCount + oneTimeOneClassMoreSubjectCount
                + oneTimeOneTeacherMoreClassCount + fixedSubjectIdCount
                + oneClassMoreOtherSubject + needAreaSubjectCount;
        hardScore = hardScore * BIG_SCORE;

        // 再计算软约束评分
        // 1.教师一天不能上超过4节课
        int teacherOutMaxTimeCount = 0;
        for (Integer teacherId : oneTimeOneTeacherMoreClassMap.keySet()) {
            var workDayTimeListMap = oneTimeOneTeacherMoreClassMap.get(teacherId);
            for (Integer workDay : workDayTimeListMap.keySet()) {
                var timeList = workDayTimeListMap.get(workDay);
                if (timeList.size() > SchoolTimeTableDefaultValueDTO.getTeacherContinueTimeMaxSize()) {
                    teacherOutMaxTimeCount = teacherOutMaxTimeCount + ONLY_ONE_COUNT;
                }
            }
        }

        // 2.第1，2节课必须是语文，数学课
        int noMainSubjectCount = 0;
        for (String gradeClassWorkDayTime : mainSubjectCountMap.keySet()) {
            var subjectId = mainSubjectCountMap.get(gradeClassWorkDayTime);
            if (subjectId == null) {
                noMainSubjectCount = noMainSubjectCount + ONLY_ONE_COUNT;
            }
            if (subjectId != null) {
                var mainSubjectFlag = SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(subjectId)
                        || SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(subjectId);
                if (!mainSubjectFlag) {
                    noMainSubjectCount = noMainSubjectCount + ONLY_ONE_COUNT;
                }
            }
        }

        // 3.体育课最好在第3、5、6，7节
        int sportNoFinalCount = 0;
        for (String key : sportTimesHashMap.keySet()) {
            var times = sportTimesHashMap.get(key);
            for (Integer time : times) {
                if (time < SchoolTimeTableDefaultValueDTO.getAfternoonSecTime() && !time.equals(SchoolTimeTableDefaultValueDTO.getMorningLastTime())) {
                    sportNoFinalCount = sportNoFinalCount + ONLY_ONE_COUNT;
                }
            }
        }

        // 4.围棋课最好在下午
        int goTimeNoAfternoonCount = 0;
        for (String key : goTimesHashMap.keySet()) {
            var times = goTimesHashMap.get(key);
            for (Integer time : times) {
                if (time < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
                    goTimeNoAfternoonCount = goTimeNoAfternoonCount + ONLY_ONE_COUNT;
                }
            }
        }

        // 5.学生不能上连堂课
        int studentContinueSameClassCount = 0;
        for (String gradeClassWorkDaySubject : continueSubjectMap.keySet()) {
            var timeList = continueSubjectMap.get(gradeClassWorkDaySubject);
            Collections.sort(timeList);
            for (int i = 0; i < timeList.size() - 1; i++) {
                for (int j = i + 1; j < timeList.size(); j++) {
                    var continueFlag = timeList.get(i) + 1 == timeList.get(j) && !timeList.get(i).equals(SchoolTimeTableDefaultValueDTO.getMorningLastTime());
                    if (continueFlag) {
                        studentContinueSameClassCount = studentContinueSameClassCount + ONLY_ONE_COUNT;
                    }
                }
            }
        }

        int noBestTimeBestClassCount = noMainSubjectCount + sportNoFinalCount;
        int softScore = (teacherOutMaxTimeCount + noBestTimeBestClassCount + goTimeNoAfternoonCount + studentContinueSameClassCount) * ADD_SCORE;

        // 最后计算一共的得分
        int score = hardScore + softScore;

        FitnessScoreDTO fitnessScoreDTO = new FitnessScoreDTO();
        fitnessScoreDTO.setHardScore(hardScore);
        fitnessScoreDTO.setSoftScore(softScore);
        fitnessScoreDTO.setScore(score);
        fitnessScoreDTO.setEveryTimeHaveSubjectCount(everyTimeHaveSubjectCount);
        fitnessScoreDTO.setOneTimeOneClassMoreSubjectCount(oneTimeOneClassMoreSubjectCount);
        fitnessScoreDTO.setOneTimeOneTeacherMoreClassCount(oneTimeOneTeacherMoreClassCount);
        fitnessScoreDTO.setFixedSubjectIdCount(fixedSubjectIdCount);
        fitnessScoreDTO.setOneClassMoreOtherSubject(oneClassMoreOtherSubject);
        fitnessScoreDTO.setNeedAreaSubjectCount(needAreaSubjectCount);
        fitnessScoreDTO.setTeacherOutMaxTimeCount(teacherOutMaxTimeCount);
        fitnessScoreDTO.setNoBestTimeBestSubjectCount(noBestTimeBestClassCount);
        fitnessScoreDTO.setStudentContinueSameClassCount(studentContinueSameClassCount);
        fitnessScoreDTO.setNoMainSubjectCount(noMainSubjectCount);
        fitnessScoreDTO.setSportNoFinalClassCount(sportNoFinalCount);
        fitnessScoreDTO.setGoTimeNoAfternoonCount(goTimeNoAfternoonCount);

        return fitnessScoreDTO;
    }

    /**
     * 前行检查和回溯算法核心
     *
     * @param timeTablingUseFCDWBacktrackingDTO
     * @param backtrackingTypeEnum
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> forwardCheckDynamicWeightBacktracking(TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO, BacktrackingTypeEnum backtrackingTypeEnum) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();
        var orderSubjectIdMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdMap();
        List<String> messageList = new ArrayList<>();
        for (int order = SchoolTimeTableDefaultValueDTO.getStartOrder(); order <= orderSubjectIdMap.keySet().size(); order++) {

            while (orderSubjectIdMap.get(order) == null) {

                // 获取课程使用表
                var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();
                var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getTimeTableConstraintDTOList();
                this.getOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap, timeTableConstraintDTOList);

                var subjectIdCanUseMap = getSubjectIdCanUseMap(order, timeTablingUseFCDWBacktrackingDTO);

                var backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                // 检查是否有回溯课程
                if (backFlag) {
                    var rollbackInFCDWDTO = this.getRollbackDTO(order, timeTablingUseFCDWBacktrackingDTO);
                    this.rollback(rollbackInFCDWDTO);
                    this.getTimeTablingUseFCDWBacktrackingDTO(rollbackInFCDWDTO, timeTablingUseFCDWBacktrackingDTO);
                    order = rollbackInFCDWDTO.getOrder();
                    this.clearConstraint(order, timeTablingUseFCDWBacktrackingDTO);
                }
                if (!backFlag) {
                    subjectIdCanUseMap = getSubjectIdCanUseMap(order, timeTablingUseFCDWBacktrackingDTO);
                    Integer chooseSubjectId = null;
                    if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_BA)) {
                        // 选择一个课程
                        chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                    }
                    if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_DW_BA)) {
                        chooseSubjectId = this.getMaxWeightSubjectId(order, timeTablingUseFCDWBacktrackingDTO);
                    }
                    this.listConstraint(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);

                    var checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                    var fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                    String message = this.packMessage(fitnessScoreDTO);
                    messageList.add(message);

                    // 检查是否满足排课需求
                    var checkCompleteDTO = this.packCheckCompleteDTO(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                    boolean completeFlag = this.checkAllComplete(checkCompleteDTO);
                    if (completeFlag) {
                        // 更新所有状态
                        this.updateAllStatus(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                        if (order == orderSubjectIdMap.keySet().size()) {
                            checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                            fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                            message = this.packMessage(fitnessScoreDTO);
                            messageList.add(message);
                        }
                    }
                    // 如果不满足排课需求，就需要回溯
                    while (!completeFlag) {
                        this.clearConstraint(order, timeTablingUseFCDWBacktrackingDTO);
                        orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();
                        timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getTimeTableConstraintDTOList();
                        this.getOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap, timeTableConstraintDTOList);

                        // 判断这一层的回溯点是否都已经使用，如果没有使用完毕，不需要回溯，选择下一个课程
                        backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                        if (!backFlag) {
                            subjectIdCanUseMap = getSubjectIdCanUseMap(order, timeTablingUseFCDWBacktrackingDTO);
                            // 回溯点不清零，记录该点的排课课程，下次不再选择这么课程
                            chooseSubjectId = null;
                            if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_BA)) {
                                // 选择一个课程
                                chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                            }
                            if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_DW_BA)) {
                                chooseSubjectId = this.getMaxWeightSubjectId(order, timeTablingUseFCDWBacktrackingDTO);
                            }

                            this.listConstraint(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);

                            checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                            fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                            message = this.packMessage(fitnessScoreDTO);
                            messageList.add(message);

                            checkCompleteDTO = this.packCheckCompleteDTO(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                            completeFlag = this.checkAllComplete(checkCompleteDTO);
                            if (completeFlag) {
                                this.updateAllStatus(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                                if (order == orderSubjectIdMap.keySet().size()) {
                                    checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                                    fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                                    message = this.packMessage(fitnessScoreDTO);
                                    messageList.add(message);
                                }
                            }
                        }
                        // 如果课程使用完毕，则找上一层的回溯点
                        if (backFlag) {
                            var rollbackInFCDWDTO = this.getRollbackDTO(order, timeTablingUseFCDWBacktrackingDTO);
                            this.rollback(rollbackInFCDWDTO);
                            this.getTimeTablingUseFCDWBacktrackingDTO(rollbackInFCDWDTO, timeTablingUseFCDWBacktrackingDTO);
                            order = rollbackInFCDWDTO.getOrder();
                            this.clearConstraint(order, timeTablingUseFCDWBacktrackingDTO);
                            completeFlag = true;
                        }
                    }

                }
            }
        }

        long start = System.currentTimeMillis();
        geneticService.markToTXT(String.valueOf(start), messageList);
        cattyResult.setData(timeTablingUseFCDWBacktrackingDTO.getTimeTableMap());
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 组装信息
     *
     * @param fitnessScoreDTO
     * @return
     */
    private String packMessage(FitnessScoreDTO fitnessScoreDTO) {
        return fitnessScoreDTO.getScore()
                + " " + fitnessScoreDTO.getHardScore()
                + " " + fitnessScoreDTO.getEveryTimeHaveSubjectCount()
                + " " + fitnessScoreDTO.getOneTimeOneClassMoreSubjectCount()
                + " " + fitnessScoreDTO.getOneTimeOneTeacherMoreClassCount()
                + " " + fitnessScoreDTO.getFixedSubjectIdCount()
                + " " + fitnessScoreDTO.getOneClassMoreOtherSubject()
                + " " + fitnessScoreDTO.getNeedAreaSubjectCount()
                + " " + fitnessScoreDTO.getSoftScore()
                + " " + fitnessScoreDTO.getTeacherOutMaxTimeCount()
                + " " + fitnessScoreDTO.getNoBestTimeBestSubjectCount()
                + " " + fitnessScoreDTO.getStudentContinueSameClassCount()
                + " " + fitnessScoreDTO.getNoMainSubjectCount()
                + " " + fitnessScoreDTO.getSportNoFinalClassCount()
                + " " + fitnessScoreDTO.getGoTimeNoAfternoonCount();
    }

    /**
     * 获取强制约束
     *
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    public TimeTablingUseFCDWBacktrackingDTO getDefaultConstraint(TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getTimeTableConstraintDTOList();

        for (Integer key : orderGradeClassNumWorkDayTimeMap.keySet()) {
            var specialGradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(key);
            var specialWork = specialGradeClassNumWorkDayTimeDTO.getWorkDay();
            var specialTime = specialGradeClassNumWorkDayTimeDTO.getTime();
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getMondayNum()) && specialTime.equals(SchoolTimeTableDefaultValueDTO.getClassMeetingTime()))) {
                TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                timeTableConstraintDTO.setOrder(0);
                timeTableConstraintDTO.setOrderConstraint(key);
                timeTableConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId());
                timeTableConstraintDTOList.add(timeTableConstraintDTO);
            }
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getWednesdayNum()) && specialTime.equals(SchoolTimeTableDefaultValueDTO.getWritingTime()))) {
                TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                timeTableConstraintDTO.setOrder(0);
                timeTableConstraintDTO.setOrderConstraint(key);
                timeTableConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getWritingId());
                timeTableConstraintDTOList.add(timeTableConstraintDTO);
            }
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getFridayNum()) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(specialTime))) {
                TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                timeTableConstraintDTO.setOrder(0);
                timeTableConstraintDTO.setOrderConstraint(key);
                timeTableConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId());
                timeTableConstraintDTOList.add(timeTableConstraintDTO);
            }

            var subjectDOList = timeTablingUseFCDWBacktrackingDTO.getAllSubject();
            for (SubjectDO subjectDO : subjectDOList) {
                boolean unClassMeetingFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getMondayNum())
                        && specialTime.equals(SchoolTimeTableDefaultValueDTO.getClassMeetingTime())
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId());
                if (unClassMeetingFlag) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(0);
                    timeTableConstraintDTO.setOrderConstraint(key);
                    timeTableConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }

                boolean unWritingFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getWednesdayNum())
                        && specialTime.equals(SchoolTimeTableDefaultValueDTO.getWritingTime())
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getWritingId());
                if (unWritingFlag) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(0);
                    timeTableConstraintDTO.setOrderConstraint(key);
                    timeTableConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }

                boolean unSchoolBaseFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getFridayNum())
                        && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(specialTime)
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId());
                if (unSchoolBaseFlag) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(0);
                    timeTableConstraintDTO.setOrderConstraint(key);
                    timeTableConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }
            }

        }

        // 先约束，再给orderSubjectIdCanUseMap 赋值
        timeTableConstraintDTOList = timeTableConstraintDTOList.stream().distinct().collect(Collectors.toList());
        for (TimeTableConstraintDTO timeTableConstraintDTO : timeTableConstraintDTOList) {
            var subjectCanUseMap = orderSubjectIdCanUseMap.get(timeTableConstraintDTO.getOrderConstraint());
            subjectCanUseMap.put(timeTableConstraintDTO.getSubjectIdConstraint(), false);
            orderSubjectIdCanUseMap.put(timeTableConstraintDTO.getOrderConstraint(), subjectCanUseMap);
            timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);
        }

        timeTablingUseFCDWBacktrackingDTO.setTimeTableConstraintDTOList(timeTableConstraintDTOList);
        return timeTablingUseFCDWBacktrackingDTO;
    }

    /**
     * 获取约束
     *
     * @param order
     * @param timeTablingUseFCDWBacktrackingDTO
     * @param subjectId
     * @return
     */
    private TimeTablingUseFCDWBacktrackingDTO listConstraint(Integer order, Integer subjectId,
                                                             TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {

        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getTimeTableConstraintDTOList();

        var gradeClassNumSubjectFrequencyMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();

        // 课程约束
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();

        var gradeClassNumWorkDayTimeOrderMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumWorkDayTimeOrderMap();
        var matchKeyList = gradeClassNumWorkDayTimeOrderMap.keySet().stream().filter(x -> x.getGrade().equals(grade) && x.getClassNum().equals(classNum)).collect(Collectors.toList());

        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        for (GradeClassNumWorkDayTimeDTO key : matchKeyList) {
            var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
            for (Integer subject : subjectFrequencyMap.keySet()) {
                Integer frequency = subjectFrequencyMap.get(subjectId);
                if (subjectId.equals(subject) && frequency - 1 == 0 && matchOrder > order) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(order);
                    timeTableConstraintDTO.setOrderConstraint(matchOrder);
                    timeTableConstraintDTO.setSubjectIdConstraint(subject);
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }
            }
        }

        // 教师约束
        var subjectGradeClassTeacherMap = timeTablingUseFCDWBacktrackingDTO.getSubjectGradeClassTeacherMap();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
        subjectGradeClassDTO.setSubjectId(subjectId);
        subjectGradeClassDTO.setGrade(grade);
        subjectGradeClassDTO.setClassNum(classNum);
        // 首先根据 subjectId 年级班级查询上课的教师
        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
        // 如果有教师，那么查询该教师上课的年级班级
        if (teacherId != null) {
            // 将同样日期、同样时间 该教师上课年级和班级 的subjectId 设置为false
            var teacherSubjectListMap = timeTablingUseFCDWBacktrackingDTO.getTeacherSubjectListMap();
            var teachingSubjectList = teacherSubjectListMap.get(teacherId);
            gradeClassNumWorkDayTimeOrderMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumWorkDayTimeOrderMap();

            for (SubjectTeacherGradeClassDTO x : teachingSubjectList) {
                var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                var time = gradeClassNumWorkDayTimeDTO.getTime();

                GradeClassNumWorkDayTimeDTO constraintDTO = new GradeClassNumWorkDayTimeDTO();
                constraintDTO.setGrade(x.getGrade());
                constraintDTO.setClassNum(x.getClassNum());
                constraintDTO.setWorkDay(workDay);
                constraintDTO.setTime(time);

                var orderConstraint = gradeClassNumWorkDayTimeOrderMap.get(constraintDTO);
                if (orderConstraint > order) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(order);
                    timeTableConstraintDTO.setOrderConstraint(orderConstraint);
                    timeTableConstraintDTO.setSubjectIdConstraint(x.getSubjectId());
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }
            }
        }

        // 小课程约束
        var gradeSubjectDTOMap = timeTablingUseFCDWBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(subjectId);
        var isSmallType = subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getOtherSubjectType())
                || subjectDTO.getType().equals(SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType());
        if (isSmallType) {
            for (Integer key : orderGradeClassNumWorkDayTimeMap.keySet()) {
                var gradeClassNumWorkDayTime = orderGradeClassNumWorkDayTimeMap.get(key);
                var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                boolean matchFlag = gradeClassNumWorkDayTime.getGrade().equals(grade)
                        && gradeClassNumWorkDayTime.getClassNum().equals(classNum)
                        && gradeClassNumWorkDayTime.getWorkDay().equals(workDay)
                        && key > order;
                if (matchFlag) {
                    TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
                    timeTableConstraintDTO.setOrder(order);
                    timeTableConstraintDTO.setOrderConstraint(key);
                    timeTableConstraintDTO.setSubjectIdConstraint(subjectId);
                    timeTableConstraintDTOList.add(timeTableConstraintDTO);
                }
            }
        }

        // 选择课程约束
        TimeTableConstraintDTO timeTableConstraintDTO = new TimeTableConstraintDTO();
        timeTableConstraintDTO.setOrder(order);
        timeTableConstraintDTO.setOrderConstraint(order);
        timeTableConstraintDTO.setSubjectIdConstraint(subjectId);
        timeTableConstraintDTOList.add(timeTableConstraintDTO);

        timeTableConstraintDTOList = timeTableConstraintDTOList.stream().distinct().collect(Collectors.toList());

        timeTablingUseFCDWBacktrackingDTO.setTimeTableConstraintDTOList(timeTableConstraintDTOList);
        return timeTablingUseFCDWBacktrackingDTO;
    }

    /**
     * 清理约束
     *
     * @param order
     * @param timeTablingUseFCDWBacktrackingDTO
     */
    private TimeTablingUseFCDWBacktrackingDTO clearConstraint(Integer order, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        // 回溯
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getTimeTableConstraintDTOList();
        var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();

        List<TimeTableConstraintDTO> newTimeTableConstraintDTOList = new ArrayList<>();
        for (TimeTableConstraintDTO timeTableConstraintDTO : timeTableConstraintDTOList) {
            if (timeTableConstraintDTO.getOrder() > order && timeTableConstraintDTO.getOrderConstraint() > order) {
                var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(timeTableConstraintDTO.getOrderConstraint());
                subjectIdCanUseMap.put(timeTableConstraintDTO.getSubjectIdConstraint(), true);
                orderSubjectIdCanUseMap.put(timeTableConstraintDTO.getOrderConstraint(), subjectIdCanUseMap);
                timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);
            }
            if ((timeTableConstraintDTO.getOrder().equals(order) && timeTableConstraintDTO.getOrderConstraint().equals(order))
                    || timeTableConstraintDTO.getOrder() < order) {
                newTimeTableConstraintDTOList.add(timeTableConstraintDTO);
            }
        }

        timeTablingUseFCDWBacktrackingDTO.setTimeTableConstraintDTOList(newTimeTableConstraintDTOList);

        return timeTablingUseFCDWBacktrackingDTO;
    }


    /**
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer order, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();
        var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(order);
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var gradeClassNumSubjectFrequencyMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getClassNum());
        for (Integer key : subjectIdCanUseMap.keySet()) {
            var frequency = subjectFrequencyMap.get(key);
            if (frequency.equals(0)) {
                subjectIdCanUseMap.put(key, false);
            }
        }
        return subjectIdCanUseMap;
    }

    /**
     * 约束变为选课
     *
     * @param orderSubjectIdCanUseMap
     * @param timeTableConstraintDTOList
     * @return
     */
    private HashMap<Integer, HashMap<Integer, Boolean>> getOrderSubjectIdCanUseMap(HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap,
                                                                                   List<TimeTableConstraintDTO> timeTableConstraintDTOList) {
        for (TimeTableConstraintDTO timeTableConstraintDTO : timeTableConstraintDTOList) {
            var subjectCanUseMap = orderSubjectIdCanUseMap.get(timeTableConstraintDTO.getOrderConstraint());
            subjectCanUseMap.put(timeTableConstraintDTO.getSubjectIdConstraint(), false);
            orderSubjectIdCanUseMap.put(timeTableConstraintDTO.getOrderConstraint(), subjectCanUseMap);
        }

        return orderSubjectIdCanUseMap;
    }

    /**
     * 获取RollBackDTO
     *
     * @param order
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private RollbackDTO getRollbackDTO(Integer order, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        RollbackDTO rollbackDTO = new RollbackDTO();
        rollbackDTO.setOrder(order);
        BeanUtils.copyProperties(timeTablingUseBacktrackingDTO, rollbackDTO);
        return rollbackDTO;
    }

    private RollbackInFCDWDTO getRollbackDTO(Integer order, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        RollbackInFCDWDTO rollbackInFCDWDTO = new RollbackInFCDWDTO();
        rollbackInFCDWDTO.setOrder(order);
        BeanUtils.copyProperties(timeTablingUseFCDWBacktrackingDTO, rollbackInFCDWDTO);
        return rollbackInFCDWDTO;
    }

    /**
     * 获取 timeTablingUseBacktrackingDTO
     *
     * @param rollbackDTO
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private TimeTablingUseBacktrackingDTO getTimeTablingUseBacktrackingDTO(RollbackDTO rollbackDTO, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        BeanUtils.copyProperties(rollbackDTO, timeTablingUseBacktrackingDTO);
        return timeTablingUseBacktrackingDTO;
    }

    /**
     * 获取 TimeTablingUseFCDWBacktrackingDTO
     *
     * @param rollbackInFCDWDTO
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private TimeTablingUseFCDWBacktrackingDTO getTimeTablingUseFCDWBacktrackingDTO(RollbackInFCDWDTO rollbackInFCDWDTO, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        BeanUtils.copyProperties(rollbackInFCDWDTO, timeTablingUseFCDWBacktrackingDTO);
        return timeTablingUseFCDWBacktrackingDTO;
    }

    /**
     * 回溯数据状态
     *
     * @param rollbackInFCDWDTO
     * @return
     */
    private void rollback(RollbackInFCDWDTO rollbackInFCDWDTO) {

        RollbackDTO rollbackDTO = new RollbackDTO();
        BeanUtils.copyProperties(rollbackInFCDWDTO, rollbackDTO);
        this.rollback(rollbackDTO);
        BeanUtils.copyProperties(rollbackDTO, rollbackInFCDWDTO);
        var order = rollbackInFCDWDTO.getOrder();
        var orderSubjectIdMap = rollbackInFCDWDTO.getOrderSubjectIdMap();
        for (Integer key : orderSubjectIdMap.keySet()) {
            if (key >= order) {
                orderSubjectIdMap.put(key, null);
            }
        }
    }

    /**
     * 回溯数据状态
     *
     * @param rollbackDTO
     * @return
     */
    private void rollback(RollbackDTO rollbackDTO) {
        Integer order = rollbackDTO.getOrder();
        if (SchoolTimeTableDefaultValueDTO.getStartOrder().equals(order)) {
            throw new TransactionException("不能回溯");
        }

        var orderSubjectIdCanUseMap = rollbackDTO.getOrderSubjectIdCanUseMap();

        // 确定回溯点
        Integer endOrder = order;
        boolean flag;
        HashMap<Integer, Boolean> subjectIdCanUseMap;
        do {
            order = order - SchoolTimeTableDefaultValueDTO.getSTEP();
            subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, orderSubjectIdCanUseMap);
            flag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        } while (flag);

        var orderGradeClassNumWorkDayTimeMap = rollbackDTO.getOrderGradeClassNumWorkDayTimeMap();

        // 回溯
        for (int clearOrder = order; clearOrder <= endOrder; clearOrder++) {
            var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
            var grade = gradeClassNumWorkDayTimeDTO.getGrade();
            var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
            var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            var time = gradeClassNumWorkDayTimeDTO.getTime();

            // 后面的回溯记录表也要清空,回溯点不清空可使用课程
            if (clearOrder != order) {
                this.rollbackSubjectIdCanUseMap(clearOrder, orderSubjectIdCanUseMap);
            }

            // 还课次数
            this.rollbackSubjectFrequency(clearOrder, rollbackDTO);

            // 教室状态回溯
            this.rollbackClassroom(clearOrder, rollbackDTO.getOrderClassRoomUsedCountMap());
            // 课表也回溯
            this.rollbackTimeTableMap(grade, classNum, workDay, time, rollbackDTO.getTimeTableMap());

        }

        rollbackDTO.setOrder(order);
    }


    /**
     * 获取可用的课程列表
     *
     * @param order
     * @param orderSubjectIdCanUseMap
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer order, HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        return orderSubjectIdCanUseMap.get(order);
    }

    /**
     * 回溯可使用课程
     *
     * @param order
     * @param orderSubjectIdCanUseMap
     */
    private void rollbackSubjectIdCanUseMap(Integer order, HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        var subjectIdCanUsedMap = orderSubjectIdCanUseMap.get(order);
        subjectIdCanUsedMap.replaceAll((key, value) -> true);
    }


    /**
     * 归还所有教室
     *
     * @param order
     * @param orderClassRoomUsedCountMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> rollbackClassroom(Integer order, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> orderClassRoomUsedCountMap) {
        var classRoomUsedCountMap = orderClassRoomUsedCountMap.get(order);
        if (classRoomUsedCountMap == null || classRoomUsedCountMap.isEmpty()) {
            return orderClassRoomUsedCountMap;
        } else {
            for (Integer subjectId : classRoomUsedCountMap.keySet()) {
                var workDayTimeCountMap = classRoomUsedCountMap.get(subjectId);
                for (Integer workDay : workDayTimeCountMap.keySet()) {
                    var timeCountMap = workDayTimeCountMap.get(workDay);
                    for (Integer time : timeCountMap.keySet()) {
                        timeCountMap.put(time, timeCountMap.get(time) - SchoolTimeTableDefaultValueDTO.getSTEP());
                    }
                    workDayTimeCountMap.put(workDay, timeCountMap);
                }
                classRoomUsedCountMap.put(subjectId, workDayTimeCountMap);
            }
            orderClassRoomUsedCountMap.put(order, classRoomUsedCountMap);
        }

        return orderClassRoomUsedCountMap;
    }


    /**
     * 还课
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param rollbackDTO
     */
    private RollbackDTO rollbackSubjectFrequency(Integer grade, Integer classNum, Integer workDay, Integer time, RollbackDTO rollbackDTO) {
        var subjectId = this.getSubjectIdFromTimeTableMap(grade, classNum, workDay, time, rollbackDTO.getTimeTableMap());
        if (subjectId == null) {
            return rollbackDTO;
        }

        return this.rollbackSubjectFrequencyCore(grade, classNum, subjectId, rollbackDTO);
    }

    /**
     * 还课
     *
     * @param subjectId
     * @param rollbackDTO
     * @return
     */
    private RollbackDTO rollbackSubjectFrequency(Integer subjectId, Integer order, RollbackDTO rollbackDTO) {
        var orderGradeClassNumWorkDayTimeMap = rollbackDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var gradeClassNumSubjectFrequencyMap = rollbackDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getClassNum());
        var frequency = subjectFrequencyMap.get(subjectId);
        subjectFrequencyMap.put(subjectId, frequency + 1);
        classNumSubjectFrequencyMap.put(gradeClassNumWorkDayTimeDTO.getClassNum(), subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(gradeClassNumWorkDayTimeDTO.getGrade(), classNumSubjectFrequencyMap);
        rollbackDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
        return rollbackDTO;
    }

    /**
     * 还课
     *
     * @param order
     * @param rollbackDTO
     * @return
     */
    private RollbackDTO rollbackSubjectFrequency(Integer order, RollbackDTO rollbackDTO) {
        var orderGradeClassNumWorkDayTimeMap = rollbackDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var timeTableMap = rollbackDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectMap = timeTableMap.get(grade);
        var workDayTimeSubjectMap = classNumWorkDayTimeSubjectMap.get(classNum);
        var timeSubjectMap = workDayTimeSubjectMap.get(workDay);
        var subjectId = timeSubjectMap.get(time);
        if (subjectId != null) {
            var gradeClassNumSubjectFrequencyMap = rollbackDTO.getGradeClassNumSubjectFrequencyMap();
            var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
            var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
            var frequency = subjectFrequencyMap.get(subjectId);
            subjectFrequencyMap.put(subjectId, frequency + 1);
            classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
            gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
            rollbackDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
            return rollbackDTO;
        }

        return rollbackDTO;
    }

    /**
     * 归还年级班级下对应课程次数
     *
     * @param grade
     * @param classNum
     * @param subjectId
     * @param rollbackDTO
     * @return
     */
    private RollbackDTO rollbackSubjectFrequencyCore(Integer grade, Integer classNum, Integer subjectId, RollbackDTO rollbackDTO) {
        var gradeClassNumSubjectFrequencyMap = rollbackDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        var frequency = subjectFrequencyMap.get(subjectId);
        subjectFrequencyMap.put(subjectId, frequency + SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
        rollbackDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
        return rollbackDTO;
    }

    /**
     * 从课表中获取课程id
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTableMap
     * @return
     */
    private Integer getSubjectIdFromTimeTableMap(Integer grade, Integer classNum, Integer workDay, Integer time, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        return timeSubjectIdMap.get(time);
    }

    /**
     * 回溯课程表
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTableMap
     */
    private void rollbackTimeTableMap(Integer grade, Integer classNum, Integer workDay, Integer time, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        timeSubjectIdMap.put(time, null);
        workDayTimeSubjectIdMap.put(workDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
    }

    /**
     * 更新一系列状态
     *
     * @param order
     * @param firstCanUseSubjectId
     * @param timeTablingUseFCDWBacktrackingDTO
     */
    private void updateAllStatus(Integer order, Integer firstCanUseSubjectId, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var subjectGradeClassTeacherMap = timeTablingUseFCDWBacktrackingDTO.getSubjectGradeClassTeacherMap();
        var orderTeacherWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderTeacherWorkDayTimeMap();
        var gradeSubjectDTOMap = timeTablingUseFCDWBacktrackingDTO.getGradeSubjectDTOMap();
        var orderClassRoomUsedCountMap = timeTablingUseFCDWBacktrackingDTO.getOrderClassRoomUsedCountMap();
        var timeTableMap = timeTablingUseFCDWBacktrackingDTO.getTimeTableMap();

        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(firstCanUseSubjectId);

        // 课程中的次数减少1次
        this.updateFrequency(firstCanUseSubjectId, grade, classNum, timeTablingUseFCDWBacktrackingDTO);

        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            // 更新教师状态
            var teacherId = this.getTeacherId(firstCanUseSubjectId, grade, classNum, subjectGradeClassTeacherMap);
            HashMap<Integer, Integer> worDayTimeMap = new HashMap<>();
            worDayTimeMap.put(workDay, time);
            HashMap<Integer, HashMap<Integer, Integer>> teacherWorkDayTimeMap = new HashMap<>();
            teacherWorkDayTimeMap.put(teacherId, worDayTimeMap);
            orderTeacherWorkDayTimeMap.put(order, teacherWorkDayTimeMap);

            // 如果使用教室 更新教室状态
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
                HashMap<Integer, Integer> timeCountMap = new HashMap<>();
                timeCountMap.put(time, SchoolTimeTableDefaultValueDTO.getSTEP());
                HashMap<Integer, HashMap<Integer, Integer>> workDayTimeCountMap = new HashMap<>();
                workDayTimeCountMap.put(workDay, timeCountMap);
                HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> subjectWorkDayTimeCountMap = new HashMap<>();
                subjectWorkDayTimeCountMap.put(firstCanUseSubjectId, workDayTimeCountMap);
                orderClassRoomUsedCountMap.put(order, subjectWorkDayTimeCountMap);
            }

        }

        // 给order subjectId 赋值
        var orderSubjectIdMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdMap();
        orderSubjectIdMap.put(order, firstCanUseSubjectId);
        timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdMap(orderSubjectIdMap);

        // 如果满足要求 给timeTableMap 赋值
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        timeSubjectIdMap.put(time, firstCanUseSubjectId);
        workDayTimeSubjectIdMap.put(workDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseFCDWBacktrackingDTO.setTimeTableMap(timeTableMap);
    }

    /**
     * 更新一系列状态
     *
     * @param order
     * @param firstCanUseSubjectId
     * @param timeTablingUseBacktrackingDTO
     */
    private void updateAllStatus(Integer order, Integer firstCanUseSubjectId, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var subjectGradeClassTeacherMap = timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherMap();
        var orderTeacherWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderTeacherWorkDayTimeMap();
        var gradeSubjectDTOMap = timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap();
        var orderClassRoomUsedCountMap = timeTablingUseBacktrackingDTO.getOrderClassRoomUsedCountMap();
        var timeTableMap = timeTablingUseBacktrackingDTO.getTimeTableMap();

        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(firstCanUseSubjectId);

        // 课程中的次数减少1次
        this.updateFrequency(firstCanUseSubjectId, grade, classNum, timeTablingUseBacktrackingDTO);

        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            // 更新教师状态
            var teacherId = this.getTeacherId(firstCanUseSubjectId, grade, classNum, subjectGradeClassTeacherMap);
            HashMap<Integer, Integer> worDayTimeMap = new HashMap<>();
            worDayTimeMap.put(workDay, time);
            HashMap<Integer, HashMap<Integer, Integer>> teacherWorkDayTimeMap = new HashMap<>();
            teacherWorkDayTimeMap.put(teacherId, worDayTimeMap);
            orderTeacherWorkDayTimeMap.put(order, teacherWorkDayTimeMap);

            // 如果使用教室 更新教室状态
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
                HashMap<Integer, Integer> timeCountMap = new HashMap<>();
                timeCountMap.put(time, SchoolTimeTableDefaultValueDTO.getSTEP());
                HashMap<Integer, HashMap<Integer, Integer>> workDayTimeCountMap = new HashMap<>();
                workDayTimeCountMap.put(workDay, timeCountMap);
                HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> subjectWorkDayTimeCountMap = new HashMap<>();
                subjectWorkDayTimeCountMap.put(firstCanUseSubjectId, workDayTimeCountMap);
                orderClassRoomUsedCountMap.put(order, subjectWorkDayTimeCountMap);
            }

        }

        // 如果满足要求 给timeTableMap 赋值
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        timeSubjectIdMap.put(time, firstCanUseSubjectId);
        workDayTimeSubjectIdMap.put(workDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseBacktrackingDTO.setTimeTableMap(timeTableMap);
    }

    /**
     * 获取教师id
     *
     * @param subjectId
     * @param grade
     * @param classNum
     * @param subjectGradeClassTeacherMap
     * @return
     */
    private Integer getTeacherId(Integer subjectId, Integer grade, Integer classNum, HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
        subjectGradeClassDTO.setSubjectId(subjectId);
        subjectGradeClassDTO.setGrade(grade);
        subjectGradeClassDTO.setClassNum(classNum);
        return subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
    }

    /**
     * 更新所选课程的次数
     *
     * @param subjectId
     * @param grade
     * @param classNum
     * @param timeTablingUseFCDWBacktrackingDTO
     */
    private void updateFrequency(Integer subjectId, Integer grade, Integer classNum, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var gradeClassNumSubjectFrequencyMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        var frequency = subjectFrequencyMap.get(subjectId);
        subjectFrequencyMap.put(subjectId, frequency - SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
        timeTablingUseFCDWBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
    }

    /**
     * 更新所选课程的次数
     *
     * @param subjectId
     * @param grade
     * @param classNum
     * @param timeTablingUseBacktrackingDTO
     */
    private void updateFrequency(Integer subjectId, Integer grade, Integer classNum, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        var frequency = subjectFrequencyMap.get(subjectId);
        subjectFrequencyMap.put(subjectId, frequency - SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
        timeTablingUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
    }

    /**
     * 获取第一个可以使用的课程
     *
     * @param subjectIdCanUseMap
     * @return
     */
    private Integer getFirstCanUseSubjectIdInSubjectIdCanUseMap(HashMap<Integer, Boolean> subjectIdCanUseMap) {
        for (Integer x : subjectIdCanUseMap.keySet()) {
            if (subjectIdCanUseMap.get(x).equals(Boolean.TRUE)) {
                return x;
            }
        }
        return null;
    }

    /**
     * 获取最大权重的课程
     *
     * @param order
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private Integer getMaxWeightSubjectId(Integer order, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTime = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTime.getGrade();
        var classNum = gradeClassNumWorkDayTime.getClassNum();
        var workDay = gradeClassNumWorkDayTime.getWorkDay();
        var time = gradeClassNumWorkDayTime.getTime();

        var gradeClassSubjectWeightMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassSubjectWeightMap();

        // 筛选出某个年级下某个班级要赋值的课程,并且初始化所有课程的权重
        var subjectWeightDTOList = this.listSubjectWeightDTO(grade, classNum, gradeClassSubjectWeightMap);

        // 组装computerSubjectWeightDTO
        var computerSubjectWeightDTO = this.packComputerSubjectWeightDTO(grade, classNum, workDay, time, subjectWeightDTOList, timeTablingUseFCDWBacktrackingDTO);

        // 计算权重
        var maxOrder = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdMap().size();
        subjectService.computerSubjectWeightDTO(order, maxOrder, computerSubjectWeightDTO);

        // 返回最大权重的课程
        var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();
        var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(order);
        return subjectWeightDTOList.stream().filter(x -> x.getFrequency() > SubjectWeightDefaultValueDTO.getZeroFrequency())
                .filter(x -> subjectIdCanUseMap.get(x.getSubjectId()).equals(true))
                .max(Comparator.comparing(SubjectWeightDTO::getWeight))
                .map(SubjectWeightDTO::getSubjectId).get();
    }

    /**
     * 获取最大权重的课程
     *
     * @param order
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private Integer getMaxWeightSubjectId(Integer order, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTime = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTime.getGrade();
        var classNum = gradeClassNumWorkDayTime.getClassNum();
        var workDay = gradeClassNumWorkDayTime.getWorkDay();
        var time = gradeClassNumWorkDayTime.getTime();

        var gradeClassSubjectWeightMap = timeTablingUseBacktrackingDTO.getGradeClassSubjectWeightMap();

        // 筛选出某个年级下某个班级要赋值的课程,并且初始化所有课程的权重
        var subjectWeightDTOList = this.listSubjectWeightDTO(grade, classNum, gradeClassSubjectWeightMap);

        // 组装computerSubjectWeightDTO
        var computerSubjectWeightDTO = this.packComputerSubjectWeightDTO(grade, classNum, workDay, time, subjectWeightDTOList, timeTablingUseBacktrackingDTO);

        // 计算权重
        subjectService.computerSubjectWeightDTO(computerSubjectWeightDTO);

        // 返回最大权重的课程
        var orderSubjectIdCanUseMap = timeTablingUseBacktrackingDTO.getOrderSubjectIdCanUseMap();
        var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(order);
        return subjectWeightDTOList.stream().filter(x -> x.getFrequency() > SubjectWeightDefaultValueDTO.getZeroFrequency())
                .filter(x -> subjectIdCanUseMap.get(x.getSubjectId()).equals(true))
                .max(Comparator.comparing(SubjectWeightDTO::getWeight))
                .map(SubjectWeightDTO::getSubjectId).get();
    }

    /**
     * 筛选出需要某年级某班级需要排课的课程
     *
     * @param grade
     * @param classNum
     * @param gradeClassSubjectWeightMap
     * @return
     */
    private List<SubjectWeightDTO> listSubjectWeightDTO(Integer grade,
                                                        Integer classNum,
                                                        HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap) {
        var classSubjectWeightMap = gradeClassSubjectWeightMap.get(grade);
        var subjectWeightList = classSubjectWeightMap.get(classNum);
        subjectWeightList.stream().filter(x -> x.getFrequency() > SubjectWeightDefaultValueDTO.getZeroFrequency())
                .forEach(x -> x.setWeight(x.getFrequency() * (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType() - x.getType())));
        return subjectWeightList;
    }

    /**
     * 组装ComputerSubjectWeightDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectWeightDTOList
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private ComputerSubjectWeightDTO packComputerSubjectWeightDTO(Integer grade,
                                                                  Integer classNum,
                                                                  Integer workDay,
                                                                  Integer time,
                                                                  List<SubjectWeightDTO> subjectWeightDTOList,
                                                                  TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        ComputerSubjectWeightDTO computerSubjectWeightDTO = new ComputerSubjectWeightDTO();

        computerSubjectWeightDTO.setGrade(grade);
        computerSubjectWeightDTO.setClassNum(classNum);
        computerSubjectWeightDTO.setWorkDay(workDay);
        computerSubjectWeightDTO.setTime(time);
        computerSubjectWeightDTO.setSubjectWeightDTOList(subjectWeightDTOList);
        computerSubjectWeightDTO.setSubjectGradeClassTeacherCountMap(timeTablingUseFCDWBacktrackingDTO.getSubjectGradeClassTeacherCountMap());
        computerSubjectWeightDTO.setOrderTeacherWorkDayTimeMap(timeTablingUseFCDWBacktrackingDTO.getOrderTeacherWorkDayTimeMap());
        computerSubjectWeightDTO.setTeacherSubjectListMap(timeTablingUseFCDWBacktrackingDTO.getTeacherSubjectListMap());
        computerSubjectWeightDTO.setGradeClassNumWorDaySubjectCountMap(timeTablingUseFCDWBacktrackingDTO.getGradeClassNumWorkDaySubjectCountMap());
        computerSubjectWeightDTO.setTimeTableMap(timeTablingUseFCDWBacktrackingDTO.getTimeTableMap());
        computerSubjectWeightDTO.setClassroomMaxCapacityMap(timeTablingUseFCDWBacktrackingDTO.getClassroomMaxCapacityMap());

        return computerSubjectWeightDTO;
    }

    /**
     * 组装ComputerSubjectWeightDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectWeightDTOList
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private ComputerSubjectWeightDTO packComputerSubjectWeightDTO(Integer grade,
                                                                  Integer classNum,
                                                                  Integer workDay,
                                                                  Integer time,
                                                                  List<SubjectWeightDTO> subjectWeightDTOList,
                                                                  TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        ComputerSubjectWeightDTO computerSubjectWeightDTO = new ComputerSubjectWeightDTO();

        computerSubjectWeightDTO.setGrade(grade);
        computerSubjectWeightDTO.setClassNum(classNum);
        computerSubjectWeightDTO.setWorkDay(workDay);
        computerSubjectWeightDTO.setTime(time);
        computerSubjectWeightDTO.setSubjectWeightDTOList(subjectWeightDTOList);
        computerSubjectWeightDTO.setSubjectGradeClassTeacherCountMap(timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherCountMap());
        computerSubjectWeightDTO.setOrderTeacherWorkDayTimeMap(timeTablingUseBacktrackingDTO.getOrderTeacherWorkDayTimeMap());
        computerSubjectWeightDTO.setTeacherSubjectListMap(timeTablingUseBacktrackingDTO.getTeacherSubjectListMap());
        computerSubjectWeightDTO.setGradeClassNumWorDaySubjectCountMap(timeTablingUseBacktrackingDTO.getGradeClassNumWorkDaySubjectCountMap());
        computerSubjectWeightDTO.setTimeTableMap(timeTablingUseBacktrackingDTO.getTimeTableMap());
        computerSubjectWeightDTO.setClassroomMaxCapacityMap(timeTablingUseBacktrackingDTO.getClassroomMaxCapacityMap());

        return computerSubjectWeightDTO;
    }

    /**
     * 根据上课次数和时间点判断是否能够选择的课程
     *
     * @param subjectIdCanUseMap
     * @param subjectFrequencyMap
     * @param worDay
     * @param time
     * @return
     */
    private void updateSubjectIdCanUseMap(HashMap<Integer, Boolean> subjectIdCanUseMap,
                                          HashMap<Integer, Integer> subjectFrequencyMap,
                                          Integer worDay, Integer time) {
        for (Integer subjectId : subjectFrequencyMap.keySet()) {
            Integer count = subjectFrequencyMap.get(subjectId);
            if (SubjectDefaultValueDTO.getZeroFrequency().equals(count)) {
                subjectIdCanUseMap.put(subjectId, false);
            }

            for (Integer x : subjectIdCanUseMap.keySet()) {
                // 班会时间
                boolean classMeetingTimeFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(worDay) && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(time);
                if (classMeetingTimeFlag) {
                    if (!SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(x)) {
                        subjectIdCanUseMap.put(x, false);
                    }
                }
                if (!classMeetingTimeFlag) {
                    subjectIdCanUseMap.put(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId(), false);
                }
                // 书法课时间
                boolean writingTimeFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(worDay) && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
                if (writingTimeFlag) {
                    if (!SchoolTimeTableDefaultValueDTO.getWritingId().equals(x)) {
                        subjectIdCanUseMap.put(x, false);
                    }
                }
                if (!writingTimeFlag) {
                    subjectIdCanUseMap.put(SchoolTimeTableDefaultValueDTO.getWritingId(), false);
                }
                // 校本课程时间
                boolean schoolBasedTimeFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(worDay) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
                if (schoolBasedTimeFlag) {
                    if (!SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(x)) {
                        subjectIdCanUseMap.put(x, false);
                    }
                }
                if (!schoolBasedTimeFlag) {
                    subjectIdCanUseMap.put(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId(), false);
                }

            }
        }

    }

    /**
     * TimeTableMap转换为TimeTableNameMap
     *
     * @param timeTableMap
     * @return
     */
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>
    convertTimeTableMapToTimeTableNameMap(Map<Integer, String> allSubjectNameMap,
                                          HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>> timeTableNameMap = new HashMap<>();
        // 年级
        for (Integer grade : timeTableMap.keySet()) {
            var classWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
            HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> classWorkDayTimeSubjectNameMap = new HashMap<>();
            // 班
            for (Integer classNum : classWorkDayTimeSubjectIdMap.keySet()) {
                var workDayTimeSubjectIdMap = classWorkDayTimeSubjectIdMap.get(classNum);
                HashMap<Integer, HashMap<Integer, String>> workDayTimeSubjectNameMap = new HashMap<>();
                // 工作日
                for (Integer workDay : workDayTimeSubjectIdMap.keySet()) {
                    var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
                    HashMap<Integer, String> timeSubjectNameMap = new HashMap<>();
                    // 节次
                    for (Integer time : timeSubjectIdMap.keySet()) {
                        var subjectId = timeSubjectIdMap.get(time);
                        var subjectName = allSubjectNameMap.get(subjectId);
                        timeSubjectNameMap.put(time, subjectName);
                    }
                    workDayTimeSubjectNameMap.put(workDay, timeSubjectNameMap);
                }
                classWorkDayTimeSubjectNameMap.put(classNum, workDayTimeSubjectNameMap);
            }
            timeTableNameMap.put(grade, classWorkDayTimeSubjectNameMap);
        }

        return timeTableNameMap;
    }

    /**
     * 组装CheckAllCompleteIsOkDTO
     *
     * @param order
     * @param subjectId
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private CheckCompleteDTO packCheckCompleteDTO(Integer order, Integer subjectId,
                                                  TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var gradeSubjectDTOMap = timeTablingUseFCDWBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var subjectDTO = subjectDTOMap.get(subjectId);

        CheckCompleteDTO checkCompleteDTO = new CheckCompleteDTO();
        checkCompleteDTO.setOrder(order);
        checkCompleteDTO.setOrderGradeClassNumWorkDayTimeMap(timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap());
        checkCompleteDTO.setOrderSubjectIdCanUseMap(timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap());
        checkCompleteDTO.setSubjectDTO(subjectDTO);
        checkCompleteDTO.setGradeClassNumSubjectFrequencyMap(timeTablingUseFCDWBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        checkCompleteDTO.setTimeTableMap(timeTablingUseFCDWBacktrackingDTO.getTimeTableMap());
        checkCompleteDTO.setGradeSubjectDTOMap(timeTablingUseFCDWBacktrackingDTO.getGradeSubjectDTOMap());
        checkCompleteDTO.setSubjectGradeClassTeacherMap(timeTablingUseFCDWBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkCompleteDTO.setOrderTeacherWorkDayTimeMap(timeTablingUseFCDWBacktrackingDTO.getOrderTeacherWorkDayTimeMap());
        checkCompleteDTO.setClassroomMaxCapacity(timeTablingUseFCDWBacktrackingDTO.getClassroomMaxCapacityMap());
        checkCompleteDTO.setOrderClassRoomUsedCountMap(timeTablingUseFCDWBacktrackingDTO.getOrderClassRoomUsedCountMap());

        return checkCompleteDTO;
    }

    /**
     * 组装CheckAllCompleteIsOkDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectId
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private CheckCompleteUseBacktrackingDTO packCheckCompleteUseBacktrackingDTO(Integer grade,
                                                                                Integer classNum,
                                                                                Integer workDay,
                                                                                Integer time,
                                                                                Integer subjectId,
                                                                                TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var gradeSubjectDTOMap = timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(subjectId);

        CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO = new CheckCompleteUseBacktrackingDTO();
        checkCompleteUseBacktrackingDTO.setSubjectDTO(subjectDTO);
        checkCompleteUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        checkCompleteUseBacktrackingDTO.setTimeTableMap(timeTablingUseBacktrackingDTO.getTimeTableMap());
        checkCompleteUseBacktrackingDTO.setGradeSubjectDTOMap(timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap());
        checkCompleteUseBacktrackingDTO.setSubjectGradeClassTeacherMap(timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkCompleteUseBacktrackingDTO.setOrderTeacherWorkDayTimeMap(timeTablingUseBacktrackingDTO.getOrderTeacherWorkDayTimeMap());
        checkCompleteUseBacktrackingDTO.setClassroomMaxCapacity(timeTablingUseBacktrackingDTO.getClassroomMaxCapacityMap());
        checkCompleteUseBacktrackingDTO.setOrderClassRoomUsedCountMap(timeTablingUseBacktrackingDTO.getOrderClassRoomUsedCountMap());
        checkCompleteUseBacktrackingDTO.setGrade(grade);
        checkCompleteUseBacktrackingDTO.setClassNum(classNum);
        checkCompleteUseBacktrackingDTO.setWorkDay(workDay);
        checkCompleteUseBacktrackingDTO.setTime(time);

        return checkCompleteUseBacktrackingDTO;
    }

    /**
     * 检查所有是否满足排课条件
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkAllComplete(CheckCompleteDTO checkCompleteDTO) {
        // 保证每天有主课上
        var everyDayHaveMainFlag = this.checkEveryDayHaveMainIsOk(checkCompleteDTO);
        if (!everyDayHaveMainFlag) {
            return false;
        }

        // 如果当天上过这个小课,则不在排课
        var otherSubjectFlag = this.checkOtherSubjectIsOk(checkCompleteDTO);
        if (!otherSubjectFlag) {
            return false;
        }

        // 每门主课不能一天不能超过2节课(上限为2）
        var mainSubjectMaxFlag = this.checkMainSubjectMaxIsOk(checkCompleteDTO);
        if (!mainSubjectMaxFlag) {
            return false;
        }

        // 判断教室是否空闲
        var classRoomIsOkFlag = this.checkClassRoomIsOkDTO(checkCompleteDTO);
        if (!classRoomIsOkFlag) {
            return false;
        }

        // 查看教师是否空闲
        var teacherIsOkFlag = this.checkTeacherIsOk(checkCompleteDTO);
        if (!teacherIsOkFlag) {
            return false;
        }

        // 按照一定的概率接受软约束条件
        BigDecimal random = BigDecimal.valueOf(Math.random());
        if (random.compareTo(ACCEPT_PRO) > 0) {
            // 早上第一节必须是主课
            var firstClassIsMainFlag = this.checkFirstClassIsMainIsOk(checkCompleteDTO);
            if (!firstClassIsMainFlag) {
                return false;
            }

            // 学生不能上连堂课(上限为2)
            var studentContinueClassFlag = this.checkStudentContinueClassIsOk(checkCompleteDTO);
            if (!studentContinueClassFlag) {
                return false;
            }

            // 教师不能上连堂课(上限为3) 并且一个教师一天最多上4节课，如果超过4节课，不在排课
            var checkTeacherMaxAndContinueClassIsOkFlag = this.checkTeacherMaxAndContinueClassIsOk(checkCompleteDTO);
            if (!checkTeacherMaxAndContinueClassIsOkFlag) {
                return false;
            }

            // 查看体育课是否在第4节课
            var sportIsOkFlag = this.checkSportIsOk(checkCompleteDTO);
            if (!sportIsOkFlag) {
                return false;
            }
        }

        // 查看是否无解
        return this.checkSolvable(checkCompleteDTO);
    }

    /**
     * 检查体育课是否合适
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkSportIsOk(CheckCompleteDTO checkCompleteDTO) {
        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        if (time.equals(SchoolTimeTableDefaultValueDTO.getAfternoonFirTime())
                && subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSportId())) {
            return false;
        }

        return true;
    }

    /**
     * 检查方程是否无解
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkSolvable(CheckCompleteDTO checkCompleteDTO) {
        var order = checkCompleteDTO.getOrder();
        var orderSubjectIdCanUseMap = checkCompleteDTO.getOrderSubjectIdCanUseMap();
        for (int key = order + 1; key <= checkCompleteDTO.getOrderSubjectIdCanUseMap().size(); key++) {
            var flag = orderSubjectIdCanUseMap.get(key).values().stream().allMatch(x -> x.equals(false));
            if (flag) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查所有条件都满足要求
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkComplete(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        // 保证每天有主课上
        var everyDayHaveMainFlag = this.checkEveryDayHaveMainIsOk(checkCompleteUseBacktrackingDTO);
        if (!everyDayHaveMainFlag) {
            return false;
        }

        // 如果当天上过这个小课,则不在排课
        var otherSubjectFlag = this.checkOtherSubjectIsOk(checkCompleteUseBacktrackingDTO);
        if (!otherSubjectFlag) {
            return false;
        }

        // 每门主课不能一天不能超过2节课(上限为2）
        var mainSubjectMaxFlag = this.checkMainSubjectMaxIsOk(checkCompleteUseBacktrackingDTO);
        if (!mainSubjectMaxFlag) {
            return false;
        }

        // 判断教室是否空闲
        var classRoomIsOkFlag = this.checkClassRoomIsOkDTO(checkCompleteUseBacktrackingDTO);
        if (!classRoomIsOkFlag) {
            return false;
        }

        BigDecimal random = BigDecimal.valueOf(Math.random());
        if (random.compareTo(ACCEPT_PRO) > 0) {
            // 早上第一节必须主课
            var classIsMainFlag = this.checkClassIsMainIsOk(checkCompleteUseBacktrackingDTO.getSubjectDTO(), checkCompleteUseBacktrackingDTO.getTime());
            if (!classIsMainFlag) {
                return false;
            }

            // 学生不能上连堂课(上限为2)
            var studentContinueClassFlag = this.checkStudentContinueClassIsOk(checkCompleteUseBacktrackingDTO);
            if (!studentContinueClassFlag) {
                return false;
            }

            // 教师不能上连堂课(上限为4) 并且一个教师一天最多上4节课，如果超过4节课，不在排课
            var checkTeacherMaxAndContinueClassIsOkFlag = this.checkTeacherMaxAndContinueClassIsOk(checkCompleteUseBacktrackingDTO);
            if (!checkTeacherMaxAndContinueClassIsOkFlag) {
                return false;
            }

            // 判断体育课是否在第四节
            var checkSportIsOkFlag = this.checkSportIsOk(checkCompleteUseBacktrackingDTO);
            if (!checkSportIsOkFlag) {
                return false;
            }
        }

        // 判断教师是否空闲
        return this.checkTeacherIsOk(checkCompleteUseBacktrackingDTO);
    }

    /**
     * 检查每天都有主课上
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkEveryDayHaveMainIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);

        var gradeClassNumSubjectFrequencyMap = checkCompleteDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getClassNum());
        var subjectId = subjectDTO.getSubjectId();
        var frequency = subjectFrequencyMap.get(subjectId);

        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        return frequency > SchoolTimeTableDefaultValueDTO.getWorkDay() - workDay;
    }

    /**
     * 检查每天都有主课上
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkEveryDayHaveMainIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var gradeClassNumSubjectFrequencyMap = checkCompleteUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var subjectId = subjectDTO.getSubjectId();
        var frequency = subjectFrequencyMap.get(subjectId);

        return frequency > SchoolTimeTableDefaultValueDTO.getWorkDay() - checkCompleteUseBacktrackingDTO.getWorkDay();
    }

    /**
     * 检查每天每个班每一种小课只能上一节
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkOtherSubjectIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        var otherFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectDTO.getType())
                || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType());
        if (!otherFlag) {
            return true;
        }

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        var timeTableMap = checkCompleteDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);

        if (CollectionUtils.isEmpty(timeSubjectIdMap.keySet())) {
            return true;
        }

        return timeSubjectIdMap.values().stream().filter(Objects::nonNull).noneMatch(x -> x.equals(subjectDTO.getSubjectId()));
    }

    /**
     * 检查每天每个班每一种小课只能上一节
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkOtherSubjectIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        var otherFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectDTO.getType())
                || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType());
        if (!otherFlag) {
            return true;
        }

        var timeTableMap = checkCompleteUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());

        if (CollectionUtils.isEmpty(timeSubjectIdMap.keySet())) {
            return true;
        }

        return timeSubjectIdMap.values().stream().filter(Objects::nonNull).noneMatch(x -> x.equals(subjectDTO.getSubjectId()));
    }

    /**
     * 检查学生是否连着上了3门一样的主课
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkStudentContinueClassIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        var subjectId = subjectDTO.getSubjectId();
        if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
            return true;
        }
        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        if (gradeClassNumWorkDayTimeDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }

        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var timeTableMap = checkCompleteDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);

        for (Integer[] x : SchoolTimeTableDefaultValueDTO.getStudentContinueTime()) {
            var pastTime = gradeClassNumWorkDayTimeDTO.getTime() - SubjectDefaultValueDTO.getOneCount();
            if (x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeLastIndex()].equals(pastTime)) {
                var firstIndex = x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeFirstIndex()];
                var lastIndex = x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeLastIndex()];
                if (timeSubjectIdMap.get(lastIndex) == null) {
                    return true;
                }
                var studentContinueClassFlag = timeSubjectIdMap.get(firstIndex).equals(timeSubjectIdMap.get(lastIndex))
                        && timeSubjectIdMap.get(lastIndex).equals(subjectId);
                if (studentContinueClassFlag) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查学生是否连着上了3门一样的主课
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkStudentContinueClassIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        var subjectId = subjectDTO.getSubjectId();
        if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
            return true;
        }

        var timeTableMap = checkCompleteUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());

        for (Integer[] x : SchoolTimeTableDefaultValueDTO.getStudentContinueTime()) {
            var pastTime = checkCompleteUseBacktrackingDTO.getTime() - SubjectDefaultValueDTO.getOneCount();
            if (x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeLastIndex()].equals(pastTime)) {
                var firstIndex = x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeFirstIndex()];
                var lastIndex = x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeLastIndex()];
                if (timeSubjectIdMap.get(lastIndex) == null) {
                    return true;
                }
                var studentContinueClassFlag = timeSubjectIdMap.get(firstIndex).equals(timeSubjectIdMap.get(lastIndex))
                        && timeSubjectIdMap.get(lastIndex).equals(subjectId);
                if (studentContinueClassFlag) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查主课每天不能超过两节课
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkMainSubjectMaxIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        if (gradeClassNumWorkDayTimeDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }

        var timeTableMap = checkCompleteDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(gradeClassNumWorkDayTimeDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(gradeClassNumWorkDayTimeDTO.getWorkDay());

        int count = SubjectDefaultValueDTO.getOneCount();
        var subjectId = subjectDTO.getSubjectId();
        for (Integer x : timeSubjectIdMap.values()) {
            if (subjectId.equals(x)) {
                count = count + SubjectDefaultValueDTO.getOneCount();
            }
        }
        return count < SubjectDefaultValueDTO.getThreeCount();
    }

    /**
     * 检查主课每天不能超过两节课
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkMainSubjectMaxIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var timeTableMap = checkCompleteUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());

        var subjectId = subjectDTO.getSubjectId();
        Integer count = SubjectDefaultValueDTO.getOneCount();
        for (Integer x : timeSubjectIdMap.values()) {
            if (subjectId.equals(x)) {
                count = count + SubjectDefaultValueDTO.getOneCount();
            }
        }
        return count < SubjectDefaultValueDTO.getThreeCount();
    }

    /**
     * 检查教师是否连续上了3节课
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkTeacherMaxAndContinueClassIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        if (gradeClassNumWorkDayTimeDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }

        var orderTeacherWorkDayTimeMap = checkCompleteDTO.getOrderTeacherWorkDayTimeMap();
        if (orderTeacherWorkDayTimeMap.isEmpty()) {
            return true;
        }

        var subjectGradeClassTeacherMap = checkCompleteDTO.getSubjectGradeClassTeacherMap();
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var subjectId = subjectDTO.getSubjectId();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO(subjectId, grade, classNum);
        var teacherId = this.getTeachingTeacherId(subjectGradeClassDTO, subjectGradeClassTeacherMap);

        List<Integer> timeList = new ArrayList<>();
        for (Integer key : orderTeacherWorkDayTimeMap.keySet()) {
            var teacherWorkDayTimeMap = orderTeacherWorkDayTimeMap.get(key);
            for (Integer teacher : teacherWorkDayTimeMap.keySet()) {
                if (teacher.equals(teacherId)) {
                    var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
                    for (Integer date : workDayTimeMap.keySet()) {
                        if (date.equals(workDay)) {
                            var time = workDayTimeMap.get(date);
                            if (!timeList.contains(time)) {
                                timeList.add(time);
                            }
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(timeList)) {
            return true;
        }

        if (SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize() < timeList.size()) {
            return false;
        }

        var teacherContinueTime = SchoolTimeTableDefaultValueDTO.getTeacherContinueTime();
        for (Integer[] x : teacherContinueTime) {
            if (Arrays.asList(x).equals(timeList)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查教师是否连续上了3节课
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkTeacherMaxAndContinueClassIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var subjectGradeClassTeacherMap = checkCompleteUseBacktrackingDTO.getSubjectGradeClassTeacherMap();
        var grade = checkCompleteUseBacktrackingDTO.getGrade();
        var classNo = checkCompleteUseBacktrackingDTO.getClassNum();
        var subjectId = subjectDTO.getSubjectId();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO(subjectId, grade, classNo);
        var teacherId = this.getTeachingTeacherId(subjectGradeClassDTO, subjectGradeClassTeacherMap);

        var orderTeacherWorkDayTimeMap = checkCompleteUseBacktrackingDTO.getOrderTeacherWorkDayTimeMap();
        if (orderTeacherWorkDayTimeMap.isEmpty()) {
            return true;
        }

        var workDay = checkCompleteUseBacktrackingDTO.getWorkDay();
        List<Integer> timeList = new ArrayList<>();
        for (Integer order : orderTeacherWorkDayTimeMap.keySet()) {
            var teacherWorkDayTimeMap = orderTeacherWorkDayTimeMap.get(order);
            for (Integer teacher : teacherWorkDayTimeMap.keySet()) {
                if (teacher.equals(teacherId)) {
                    var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
                    for (Integer date : workDayTimeMap.keySet()) {
                        if (date.equals(workDay)) {
                            var time = workDayTimeMap.get(date);
                            if (!timeList.contains(time)) {
                                timeList.add(time);
                            }
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(timeList)) {
            return true;
        }

        if (SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize() < timeList.size()) {
            return false;
        }

        var teacherContinueTime = SchoolTimeTableDefaultValueDTO.getTeacherContinueTime();
        for (Integer[] x : teacherContinueTime) {
            if (Arrays.asList(x).equals(timeList)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查第一节课为主课
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkFirstClassIsMainIsOk(CheckCompleteDTO checkCompleteDTO) {
        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTime = orderGradeClassNumWorkDayTimeMap.get(order);
        if (!(SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(gradeClassNumWorkDayTime.getTime())
                || SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(gradeClassNumWorkDayTime.getTime()))) {
            return true;
        }
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        return SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType());
    }


    /**
     * 检查第一节课为主课
     *
     * @param subjectDTO
     * @param time
     * @return
     */
    private Boolean checkClassIsMainIsOk(SubjectDTO subjectDTO, Integer time) {
        if (!(SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time) || SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time))) {
            return true;
        }
        return SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType());
    }

    /**
     * 检查教室是否合适
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkClassRoomIsOkDTO(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var classroomMaxCapacityMap = checkCompleteDTO.getClassroomMaxCapacity();
        var maxCount = classroomMaxCapacityMap.get(subjectDTO.getSubjectId());

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var orderClassRoomUsedCountMap = checkCompleteDTO.getOrderClassRoomUsedCountMap();
        Integer count = SchoolTimeTableDefaultValueDTO.getStartCount();
        for (Integer key : orderClassRoomUsedCountMap.keySet()) {
            var classRoomUsedMap = orderClassRoomUsedCountMap.get(key);
            for (Integer subject : classRoomUsedMap.keySet()) {
                if (subject.equals(subjectDTO.getSubjectId())) {
                    var workDayTimeCountMap = classRoomUsedMap.get(subject);
                    for (Integer data : workDayTimeCountMap.keySet()) {
                        if (data.equals(workDay)) {
                            var timeCountMap = workDayTimeCountMap.get(workDay);
                            for (Integer times : timeCountMap.keySet()) {
                                if (times.equals(time)) {
                                    count = count + timeCountMap.get(time);
                                }
                            }
                        }
                    }
                }
            }
        }

        return count <= maxCount;
    }

    /**
     * 检查教室是否合适
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkClassRoomIsOkDTO(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        if (!SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var classroomMaxCapacityMap = checkCompleteUseBacktrackingDTO.getClassroomMaxCapacity();
        var maxCount = classroomMaxCapacityMap.get(subjectDTO.getSubjectId());

        var workDay = checkCompleteUseBacktrackingDTO.getWorkDay();
        var time = checkCompleteUseBacktrackingDTO.getTime();

        var orderClassRoomUsedCountMap = checkCompleteUseBacktrackingDTO.getOrderClassRoomUsedCountMap();
        Integer count = SchoolTimeTableDefaultValueDTO.getStartCount();
        for (Integer order : orderClassRoomUsedCountMap.keySet()) {
            var classRoomUsedMap = orderClassRoomUsedCountMap.get(order);
            for (Integer subject : classRoomUsedMap.keySet()) {
                if (subject.equals(subjectDTO.getSubjectId())) {
                    var workDayTimeCountMap = classRoomUsedMap.get(subject);
                    for (Integer data : workDayTimeCountMap.keySet()) {
                        if (data.equals(workDay)) {
                            var timeCountMap = workDayTimeCountMap.get(workDay);
                            for (Integer times : timeCountMap.keySet()) {
                                if (times.equals(time)) {
                                    count = count + timeCountMap.get(time);
                                }
                            }
                        }
                    }
                }
            }
        }

        return count <= maxCount;
    }

    /**
     * 检查体育课是否合适
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkSportIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {

        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();
        var time = checkCompleteUseBacktrackingDTO.getTime();
        if (time.equals(SchoolTimeTableDefaultValueDTO.getAfternoonFirTime())
                && subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSportId())) {
            return false;
        }

        return true;
    }

    /**
     * 检查教师是否空闲
     *
     * @param checkCompleteDTO
     * @return
     */
    private boolean checkTeacherIsOk(CheckCompleteDTO checkCompleteDTO) {
        var subjectDTO = checkCompleteDTO.getSubjectDTO();

        // 课程如果是特殊课程，直接返回true
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNo = gradeClassNumWorkDayTimeDTO.getClassNum();
        var subjectId = subjectDTO.getSubjectId();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO(subjectId, grade, classNo);
        var teacherId = this.getTeachingTeacherId(subjectGradeClassDTO, checkCompleteDTO.getSubjectGradeClassTeacherMap());

        var orderTeacherWorkDayTimeMap = checkCompleteDTO.getOrderTeacherWorkDayTimeMap();
        if (orderTeacherWorkDayTimeMap.isEmpty()) {
            return true;
        }

        for (Integer key : orderTeacherWorkDayTimeMap.keySet()) {
            var teacherWorkDayTimeMap = orderTeacherWorkDayTimeMap.get(key);
            if (teacherWorkDayTimeMap != null) {
                var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
                if (workDayTimeMap != null) {
                    var time = workDayTimeMap.get(gradeClassNumWorkDayTimeDTO.getWorkDay());
                    if (gradeClassNumWorkDayTimeDTO.getTime().equals(time)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 检查教师是否空闲
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private boolean checkTeacherIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectDTO = checkCompleteUseBacktrackingDTO.getSubjectDTO();

        // 课程如果是特殊课程，直接返回true
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var grade = checkCompleteUseBacktrackingDTO.getGrade();
        var classNo = checkCompleteUseBacktrackingDTO.getClassNum();
        var subjectId = subjectDTO.getSubjectId();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO(subjectId, grade, classNo);
        var teacherId = this.getTeachingTeacherId(subjectGradeClassDTO, checkCompleteUseBacktrackingDTO.getSubjectGradeClassTeacherMap());

        var orderTeacherWorkDayTimeMap = checkCompleteUseBacktrackingDTO.getOrderTeacherWorkDayTimeMap();
        if (orderTeacherWorkDayTimeMap.isEmpty()) {
            return true;
        }

        for (Integer order : orderTeacherWorkDayTimeMap.keySet()) {
            var teacherWorkDayTimeMap = orderTeacherWorkDayTimeMap.get(order);
            if (teacherWorkDayTimeMap != null) {
                var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
                if (workDayTimeMap != null) {
                    var time = workDayTimeMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());
                    if (checkCompleteUseBacktrackingDTO.getTime().equals(time)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 获取上课教师id
     *
     * @param subjectGradeClassDTO
     * @param subjectGradeClassTeacherMap
     * @return
     */
    private Integer getTeachingTeacherId(SubjectGradeClassDTO subjectGradeClassDTO,
                                         HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        return subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
    }


    /**
     * 转化为基因列表  是否固定+年级+班级+课程编号+课程节次+课程属性+教师编号+开课时间
     *
     * @param timeTableMap
     * @param gradeSubjectDTOMap
     * @param subjectGradeClassTeacherMap
     * @return
     */
    private List<String> convertToGeneList(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap,
                                           HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap,
                                           HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        List<String> geneList = new ArrayList<>();
        for (Integer grade : timeTableMap.keySet()) {
            var classNoWorkDayTimeSubjectMap = timeTableMap.get(grade);
            var subjectDTOMap = gradeSubjectDTOMap.get(grade);
            for (Integer classNo : classNoWorkDayTimeSubjectMap.keySet()) {
                var workDayTimeSubjectMap = classNoWorkDayTimeSubjectMap.get(classNo);
                for (Integer wordDay : workDayTimeSubjectMap.keySet()) {
                    var timeSubjectMap = workDayTimeSubjectMap.get(wordDay);
                    for (Integer time : timeSubjectMap.keySet()) {
                        Integer subjectId = timeSubjectMap.get(time);
                        if (subjectId == null) {
                            continue;
                        }
                        SubjectDTO subjectDTO = subjectDTOMap.get(subjectId);
                        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
                        subjectGradeClassDTO.setSubjectId(subjectId);
                        subjectGradeClassDTO.setGrade(grade);
                        subjectGradeClassDTO.setClassNum(classNo);
                        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);

                        Integer order = time + (wordDay - SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex()) * SchoolTimeTableDefaultValueDTO.getClassTime();

                        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
                            String fixed = GeneticDefaultValueDTO.UN_FIXED;
                            String standardGrade = geneticService.getStandard(grade.toString(), GeneticDefaultValueDTO.GRADE_STANDARD_LENGTH);
                            String standardClassNo = geneticService.getStandard(classNo.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
                            String standardSubjectId = geneticService.getStandard(subjectId.toString(), GeneticDefaultValueDTO.SUBJECT_ID_STANDARD_LENGTH);
                            String standardSubjectFrequency = geneticService.getStandard(subjectDTO.getFrequency().toString(), GeneticDefaultValueDTO.SUBJECT_FREQUENCY_STANDARD_LENGTH);
                            String standardTeacherId = geneticService.getStandard(teacherId.toString(), GeneticDefaultValueDTO.TEACHER_ID_STANDARD_LENGTH);
                            String standardOrder = geneticService.getStandard(order.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);

                            String gene = fixed + standardGrade + standardClassNo + standardSubjectId + standardSubjectFrequency + subjectDTO.getType() + standardTeacherId + standardOrder;
                            geneList.add(gene);
                        }

                    }
                }
            }
        }

        return geneList;
    }

}
