package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.service.dto.CheckAllCompleteIsOkDTO;
import com.work.school.mysql.timetable.service.dto.CheckClassRoomIsOkDTO;
import com.work.school.mysql.timetable.service.dto.CheckCompleteUseBacktrackingDTO;
import com.work.school.mysql.timetable.service.dto.CheckTeacherIsOkDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class BacktrackingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktrackingService.class);

    @Resource
    private SubjectService subjectService;
    @Resource
    private PrepareService prepareService;

    /**
     * 回溯算法核心
     *
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> algorithmInPlanTimeTableWithBacktracking(TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();

        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();

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
                var subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, timeTablingUseBacktrackingDTO);

                // 根据上课次数和时间点判断是否能够选择的课程
                this.updateSubjectIdCanUseMap(subjectIdCanUseMap, subjectFrequencyMap, workDay, time);

                // 检查是否有回溯课程
                var backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                if (backFlag) {
                    order = this.rollback(order, timeTablingUseBacktrackingDTO);
                    gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
                    grade = gradeClassNumWorkDayTimeDTO.getGrade();
                    classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
                    workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                    time = gradeClassNumWorkDayTimeDTO.getTime();
                }
                if (!backFlag) {
                    // 选择一个课程
                    Integer firstCanUseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);

                    // 更新课程使用状态
                    subjectIdCanUseMap.put(firstCanUseSubjectId, false);

                    // 检查是否满足排课需求
                    var checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, firstCanUseSubjectId, timeTablingUseBacktrackingDTO);
                    boolean completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                    if (completeFlag) {
                        this.updateAllStatus(grade, classNum, workDay, time, firstCanUseSubjectId, timeTablingUseBacktrackingDTO);
                    }
                    // 如果不满足排课需求，就需要回溯
                    while (!completeFlag) {
                        // 判断这一层的回溯点是否都已经使用，如果没有使用完毕，不需要回溯，选择下一个课程
                        subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, timeTablingUseBacktrackingDTO);
                        backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                        if (!backFlag) {
                            // 回溯点不清零，记录该点的排课课程，下次不再选择这么课程
                            firstCanUseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                            // 更新课程使用状态
                            subjectIdCanUseMap.put(firstCanUseSubjectId, false);
                            // 检查是否满足排课需求
                            checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, firstCanUseSubjectId, timeTablingUseBacktrackingDTO);
                            completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                            if (completeFlag) {
                                this.updateAllStatus(grade, classNum, workDay, time, firstCanUseSubjectId, timeTablingUseBacktrackingDTO);
                            }
                        }

                        // 如果课程使用完毕，则找上一层的回溯点
                        if (backFlag) {
                            order = this.rollback(order, timeTablingUseBacktrackingDTO);
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

        cattyResult.setData(timeTablingUseBacktrackingDTO.getTimeTableMap());
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 回溯数据状态
     *
     * @param order
     * @param timeTablingUseBacktrackingDTO
     */
    private Integer rollback(Integer order, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        if (SchoolTimeTableDefaultValueDTO.getStartOrder().equals(order)) {
            throw new TransactionException("不能回溯");
        }

        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        // 确定回溯点
        var subjectIdCanUseMap = this.getSubjectIdCanUseMap(grade, classNum, workDay, time, timeTablingUseBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap());
        var flag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        while (flag) {
            order = order - SchoolTimeTableDefaultValueDTO.getStep();
            subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, timeTablingUseBacktrackingDTO);
            flag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        }

        // 回溯
        for (int clearOrder = order; clearOrder <= orderGradeClassNumWorkDayTimeMap.size(); clearOrder++) {
            gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(clearOrder);
            grade = gradeClassNumWorkDayTimeDTO.getGrade();
            classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
            workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            time = gradeClassNumWorkDayTimeDTO.getTime();

            // 还课次数
            this.rollbackSubjectFrequency(grade, classNum, workDay, time, timeTablingUseBacktrackingDTO);
            // 后面的回溯记录表也要清空,回溯点不清空可使用课程
            if (clearOrder != order) {
                this.rollbackSubjectIdCanUseMap(grade, classNum, workDay, time, timeTablingUseBacktrackingDTO);
            }
            // 课表也回溯
            this.rollbackTimeTableMap(grade, classNum, workDay, time, timeTablingUseBacktrackingDTO);
            // 教师状态回溯
            this.rollbackTeacherTeaching(workDay, time, timeTablingUseBacktrackingDTO);
            // 教室状态回溯
            this.rollbackClassroom(workDay, time, timeTablingUseBacktrackingDTO);
        }

        return order;
    }

    /**
     * 回溯数据状态
     *
     * @param order
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private Integer rollback(Integer order, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        if (SchoolTimeTableDefaultValueDTO.getStartOrder().equals(order)) {
            throw new TransactionException("不能回溯");
        }

        var orderGradeClassNumWorkDayTimeMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        // 确定回溯点
        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap();
        var subjectIdCanUseMap = this.getSubjectIdCanUseMap(grade, classNum, workDay, time, gradeClassNumWorkDayTimeSubjectIdCanUseMap);
        var flag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        while (flag) {
            order = order - SchoolTimeTableDefaultValueDTO.getStep();
            subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, orderGradeClassNumWorkDayTimeMap, gradeClassNumWorkDayTimeSubjectIdCanUseMap);
            flag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        }

        // 回溯
        for (int clearOrder = order; clearOrder <= orderGradeClassNumWorkDayTimeMap.size(); clearOrder++) {
            gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(clearOrder);
            grade = gradeClassNumWorkDayTimeDTO.getGrade();
            classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
            workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            time = gradeClassNumWorkDayTimeDTO.getTime();

            // 还课次数
            this.rollbackSubjectFrequency(grade, classNum, workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
            // 后面的回溯记录表也要清空,回溯点不清空可使用课程
            if (clearOrder != order) {
                this.rollbackSubjectIdCanUseMap(grade, classNum, workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
            }
            // 课表也回溯
            this.rollbackTimeTableMap(grade, classNum, workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
            // 教师状态回溯
            this.rollbackTeacherTeaching(workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
            // 教室状态回溯
            this.rollbackClassroom(workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
        }

        return order;
    }


    /**
     * 获取可用的课程列表
     *
     * @param order
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer order, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {

        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = timeTablingUseBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap();
        var classNumWorkDayTimeSubjectIdCanUseMap = gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        var workDayTimeSubjectIdCanUseMap = classNumWorkDayTimeSubjectIdCanUseMap.get(classNum);
        var timeSubjectIdCanUseMap = workDayTimeSubjectIdCanUseMap.get(workDay);
        return timeSubjectIdCanUseMap.get(time);
    }

    /**
     * 获取可用的课程列表
     *
     * @param order
     * @param orderGradeClassNumWorkDayTimeMap
     * @param gradeClassNumWorkDayTimeSubjectIdCanUseMap
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer order,
                                                            HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap,
                                                            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> gradeClassNumWorkDayTimeSubjectIdCanUseMap) {

        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        var classNumWorkDayTimeSubjectIdCanUseMap = gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        var workDayTimeSubjectIdCanUseMap = classNumWorkDayTimeSubjectIdCanUseMap.get(classNum);
        var timeSubjectIdCanUseMap = workDayTimeSubjectIdCanUseMap.get(workDay);
        return timeSubjectIdCanUseMap.get(time);
    }

    /**
     * 获取可用的课程列表
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param gradeClassNumWorkDayTimeSubjectIdCanUseMap
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer grade,
                                                            Integer classNum,
                                                            Integer workDay,
                                                            Integer time,
                                                            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> gradeClassNumWorkDayTimeSubjectIdCanUseMap) {

        var classNumWorkDayTimeSubjectIdCanUseMap = gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        var workDayTimeSubjectIdCanUseMap = classNumWorkDayTimeSubjectIdCanUseMap.get(classNum);
        var timeSubjectIdCanUseMap = workDayTimeSubjectIdCanUseMap.get(workDay);
        return timeSubjectIdCanUseMap.get(time);
    }

    /**
     * 回溯可使用课程
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void rollbackSubjectIdCanUseMap(Integer grade, Integer classNum, Integer workDay, Integer time,
                                            TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = this.rollbackSubjectIdCanUseMapCore(grade, classNum, workDay, time,
                timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap(), timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setGradeClassNumWorkDayTimeSubjectIdCanUseMap(gradeClassNumWorkDayTimeSubjectIdCanUseMap);
    }

    /**
     * 回溯可使用课程
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackSubjectIdCanUseMap(Integer grade, Integer classNum, Integer workDay, Integer time,
                                            TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = this.rollbackSubjectIdCanUseMapCore(grade, classNum, workDay, time,
                timeTablingUseBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap(), timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        timeTablingUseBacktrackingDTO.setGradeClassNumWorkDayTimeSubjectIdCanUseMap(gradeClassNumWorkDayTimeSubjectIdCanUseMap);
    }

    /**
     * 回溯可使用课程
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param gradeClassNumWorkDayTimeSubjectIdCanUseMap
     * @param gradeClassNumSubjectFrequencyMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> rollbackSubjectIdCanUseMapCore(Integer grade, Integer classNum, Integer workDay, Integer time,
                                                                                                                                             HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>> gradeClassNumWorkDayTimeSubjectIdCanUseMap,
                                                                                                                                             HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        var classNumWorkDayTimeSubjectIdCanUseMap = gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        var workDayTimeSubjectIdCanUseMap = classNumWorkDayTimeSubjectIdCanUseMap.get(classNum);
        var timeSubjectIdCanUseMap = workDayTimeSubjectIdCanUseMap.get(workDay);
        var subjectIdCanUseMap = timeSubjectIdCanUseMap.get(time);
        subjectIdCanUseMap.replaceAll((key, value) -> true);

        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);

        this.updateSubjectIdCanUseMap(subjectIdCanUseMap, subjectFrequencyMap, workDay, time);
        timeSubjectIdCanUseMap.put(time, subjectIdCanUseMap);
        workDayTimeSubjectIdCanUseMap.put(workDay, timeSubjectIdCanUseMap);
        classNumWorkDayTimeSubjectIdCanUseMap.put(classNum, workDayTimeSubjectIdCanUseMap);
        gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        return gradeClassNumWorkDayTimeSubjectIdCanUseMap;
    }

    /**
     * 清除教师上课状态
     *
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackTeacherTeaching(Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var teacherTeachingMap = rollbackTeacherTeachingCore(workDay, time, timeTablingUseBacktrackingDTO.getTeacherTeachingMap());
        timeTablingUseBacktrackingDTO.setTeacherTeachingMap(teacherTeachingMap);
    }

    /**
     * 清除教师上课状态
     *
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void rollbackTeacherTeaching(Integer workDay, Integer time, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var teacherTeachingMap = rollbackTeacherTeachingCore(workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap());
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setTeacherTeachingMap(teacherTeachingMap);
    }

    /**
     * 清除教师上课状态核心
     *
     * @param workDay
     * @param time
     * @param teacherTeachingMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, List<Integer>>> rollbackTeacherTeachingCore(Integer workDay, Integer time, HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        for (Integer teacherId : teacherTeachingMap.keySet()) {
            var workDayTimeListMap = teacherTeachingMap.get(teacherId);
            var timeList = workDayTimeListMap.get(workDay);
            if (CollectionUtils.isNotEmpty(timeList)) {
                timeList.remove(time);
            }
            workDayTimeListMap.put(workDay, timeList);
            teacherTeachingMap.put(teacherId, workDayTimeListMap);
        }

        return teacherTeachingMap;
    }

    /**
     * 清除所有教室
     *
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackClassroom(Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var classRoomUsedCountMap = rollbackClassRoomUsedCountMapCore(workDay, time, timeTablingUseBacktrackingDTO.getClassRoomUsedCountMap());
        timeTablingUseBacktrackingDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
    }

    /**
     * 归还所有教室
     *
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void rollbackClassroom(Integer workDay, Integer time, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var classRoomUsedCountMap = rollbackClassRoomUsedCountMapCore(workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassRoomUsedCountMap());
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
    }

    /**
     * 归还所有教室
     *
     * @param workDay
     * @param time
     * @param classRoomUsedCountMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> rollbackClassRoomUsedCountMapCore(Integer workDay, Integer time,
                                                                                                            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> classRoomUsedCountMap) {
        for (Integer subjectId : classRoomUsedCountMap.keySet()) {
            var workDayTimeCountMap = classRoomUsedCountMap.get(subjectId);
            var timeCountMap = workDayTimeCountMap.get(workDay);
            timeCountMap.put(time, SubjectDefaultValueDTO.getZeroFrequency());
            workDayTimeCountMap.put(workDay, timeCountMap);
            classRoomUsedCountMap.put(subjectId, workDayTimeCountMap);
        }

        return classRoomUsedCountMap;
    }

    /**
     * 还课
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void rollbackSubjectFrequency(Integer grade, Integer classNum, Integer workDay, Integer time, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var timeTableMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap();
        var subjectId = this.getSubjectIdFromTimeTableMap(grade, classNum, workDay, time, timeTableMap);
        if (subjectId == null) {
            return;
        }

        var gradeClassNumSubjectFrequencyMap = this.rollbackSubjectFrequencyCore(grade, classNum, subjectId, timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
    }

    /**
     * 还课
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackSubjectFrequency(Integer grade, Integer classNum, Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var timeTableMap = timeTablingUseBacktrackingDTO.getTimeTableMap();
        var subjectId = this.getSubjectIdFromTimeTableMap(grade, classNum, workDay, time, timeTableMap);
        if (subjectId == null) {
            return;
        }

        var gradeClassNumSubjectFrequencyMap = this.rollbackSubjectFrequencyCore(grade, classNum, subjectId, timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        timeTablingUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
    }

    /**
     * 归还年级班级下对应课程次数
     *
     * @param grade
     * @param classNum
     * @param subjectId
     * @param gradeClassNumSubjectFrequencyMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> rollbackSubjectFrequencyCore(Integer grade, Integer classNum, Integer subjectId, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        subjectFrequencyMap.put(subjectId, subjectFrequencyMap.get(subjectId) + SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
        return gradeClassNumSubjectFrequencyMap;
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
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackTimeTableMap(Integer grade, Integer classNum, Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var timeTableMap = timeTablingUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        timeSubjectIdMap.put(time, null);
        workDayTimeSubjectIdMap.put(workDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseBacktrackingDTO.setTimeTableMap(timeTableMap);
    }

    /**
     * 回溯课程表
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void rollbackTimeTableMap(Integer grade, Integer classNum, Integer workDay, Integer time, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var timeTableMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        timeSubjectIdMap.put(time, null);
        workDayTimeSubjectIdMap.put(workDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setTimeTableMap(timeTableMap);
    }

    /**
     * 更新一系列状态
     *
     * @param grade
     * @param classNum
     * @param worDay
     * @param time
     * @param firstCanUseSubjectId
     * @param timeTablingUseBacktrackingDTO
     */
    private void updateAllStatus(Integer grade, Integer classNum, Integer worDay, Integer time,
                                 Integer firstCanUseSubjectId,
                                 TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var subjectGradeClassTeacherMap = timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherMap();
        var teacherTeachingMap = timeTablingUseBacktrackingDTO.getTeacherTeachingMap();
        var gradeSubjectDTOMap = timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap();
        var classRoomUsedCountMap = timeTablingUseBacktrackingDTO.getClassRoomUsedCountMap();
        var timeTableMap = timeTablingUseBacktrackingDTO.getTimeTableMap();

        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(firstCanUseSubjectId);

        // 课程中的次数减少1次
        this.updateFrequency(firstCanUseSubjectId, grade, classNum, timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());

        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            // 更新教师状态
            var teacherId = this.getTeacherId(firstCanUseSubjectId, grade, classNum, subjectGradeClassTeacherMap);

            var workDayTimeMap = teacherTeachingMap.get(teacherId);
            var timeList = workDayTimeMap.get(worDay);
            timeList.add(time);
            workDayTimeMap.put(worDay, timeList);
            teacherTeachingMap.put(teacherId, workDayTimeMap);

            // 如果使用教室 更新教室状态
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
                var gradeWorkDayTimeClassRoomUsedCountMap = classRoomUsedCountMap.get(subjectDTO.getSubjectId());
                var workDayTimeClassRoomUsedCountMap = gradeWorkDayTimeClassRoomUsedCountMap.get(worDay);
                var count = workDayTimeClassRoomUsedCountMap.get(firstCanUseSubjectId);
                workDayTimeClassRoomUsedCountMap.put(time, count);
                gradeWorkDayTimeClassRoomUsedCountMap.put(worDay, workDayTimeClassRoomUsedCountMap);
                classRoomUsedCountMap.put(firstCanUseSubjectId, gradeWorkDayTimeClassRoomUsedCountMap);
            }

        }

        // 如果满足要求 给timeTableMap 赋值
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(worDay);
        timeSubjectIdMap.put(time, firstCanUseSubjectId);
        workDayTimeSubjectIdMap.put(worDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseBacktrackingDTO.setTimeTableMap(timeTableMap);
    }

    /**
     * 更新一系列状态
     *
     * @param grade
     * @param classNum
     * @param worDay
     * @param time
     * @param firstCanUseSubjectId
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     */
    private void updateAllStatus(Integer grade, Integer classNum, Integer worDay, Integer time,
                                 Integer firstCanUseSubjectId,
                                 TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        var subjectGradeClassTeacherMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getSubjectGradeClassTeacherMap();
        var teacherTeachingMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap();
        var gradeSubjectDTOMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeSubjectDTOMap();
        var classRoomUsedCountMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassRoomUsedCountMap();
        var timeTableMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap();

        var subjectDTOMap = gradeSubjectDTOMap.get(grade);
        var subjectDTO = subjectDTOMap.get(firstCanUseSubjectId);

        // 课程中的次数减少1次
        this.updateFrequency(firstCanUseSubjectId, grade, classNum, timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());

        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            // 更新教师状态
            var teacherId = this.getTeacherId(firstCanUseSubjectId, grade, classNum, subjectGradeClassTeacherMap);

            var workDayTimeMap = teacherTeachingMap.get(teacherId);
            var timeList = workDayTimeMap.get(worDay);
            timeList.add(time);
            workDayTimeMap.put(worDay, timeList);
            teacherTeachingMap.put(teacherId, workDayTimeMap);

            // 如果使用教室 更新教室状态
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType())) {
                var gradeWorkDayTimeClassRoomUsedCountMap = classRoomUsedCountMap.get(subjectDTO.getSubjectId());
                var workDayTimeClassRoomUsedCountMap = gradeWorkDayTimeClassRoomUsedCountMap.get(worDay);
                var count = workDayTimeClassRoomUsedCountMap.get(time);
                workDayTimeClassRoomUsedCountMap.put(time, count);
                gradeWorkDayTimeClassRoomUsedCountMap.put(worDay, workDayTimeClassRoomUsedCountMap);
                classRoomUsedCountMap.put(firstCanUseSubjectId, gradeWorkDayTimeClassRoomUsedCountMap);
            }

        }

        // 如果满足要求 给timeTableMap 赋值
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(worDay);
        timeSubjectIdMap.put(time, firstCanUseSubjectId);
        workDayTimeSubjectIdMap.put(worDay, timeSubjectIdMap);
        classNumWorkDayTimeSubjectIdMap.put(classNum, workDayTimeSubjectIdMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectIdMap);
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setTimeTableMap(timeTableMap);
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
     * @param gradeClassNumSubjectFrequencyMap
     */
    private void updateFrequency(Integer subjectId, Integer grade, Integer classNum, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassNumSubjectFrequencyMap) {
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        var frequency = subjectFrequencyMap.get(subjectId);
        subjectFrequencyMap.put(subjectId, frequency - SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
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
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectIdCanUseMap
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private Integer getMaxWeightSubjectId(Integer grade, Integer classNum, Integer workDay, Integer time,
                                          HashMap<Integer, Boolean> subjectIdCanUseMap, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {

        var gradeClassSubjectWeightMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassSubjectWeightMap();

        // 筛选出某个年级下某个班级要赋值的课程,并且初始化所有课程的权重
        var subjectWeightDTOList = this.listSubjectWeightDTO(grade, classNum, gradeClassSubjectWeightMap);

        // 组装computerSubjectWeightDTO
        var computerSubjectWeightDTO = this.packComputerSubjectWeightDTO(grade, classNum, workDay, time, subjectWeightDTOList, timeTablingUseDynamicWeightsAndBacktrackingDTO);

        // 计算权重
        subjectService.computerSubjectWeightDTO(computerSubjectWeightDTO);

        // 返回最大权重的课程
        return subjectWeightDTOList.stream().filter(x -> x.getFrequency() > SubjectWeightDefaultValueDTO.getZeroFrequency())
                .filter(x -> subjectIdCanUseMap.get(x.getSubjectId()).equals(true))
                .max(Comparator.comparing(SubjectWeightDTO::getWeight))
                .map(SubjectWeightDTO::getSubjectId).get();
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
     * 排课核心算法
     *
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> algorithmInPlanTimeTableWithDynamicWeightsAndBacktracking(TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();
        var timeTableMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap();

        var orderGradeClassNumWorkDayTimeMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumSubjectFrequencyMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap();

        for (int order = SchoolTimeTableDefaultValueDTO.getStartOrder(); order <= orderGradeClassNumWorkDayTimeMap.keySet().size(); order++) {
            var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
            var grade = gradeClassNumWorkDayTimeDTO.getGrade();
            var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
            var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            var time = gradeClassNumWorkDayTimeDTO.getTime();

            while (timeTableMap.get(grade).get(classNum).get(workDay).get(time) == null) {
                // 获取某个年级某个班的课程和使用次数
                var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
                var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);

                // 获取课程使用表
                var subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, orderGradeClassNumWorkDayTimeMap, gradeClassNumWorkDayTimeSubjectIdCanUseMap);

                // 根据上课次数和时间点判断是否能够选择的课程
                this.updateSubjectIdCanUseMap(subjectIdCanUseMap, subjectFrequencyMap, workDay, time);

                // 检查是否有回溯课程
                var backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                if (backFlag) {
                    order = this.rollback(order, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                    gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
                    grade = gradeClassNumWorkDayTimeDTO.getGrade();
                    classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
                    workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                    time = gradeClassNumWorkDayTimeDTO.getTime();
                }
                if (!backFlag) {

                    // 选择权重最大的课程
                    Integer maxWeightSubjectId = this.getMaxWeightSubjectId(grade, classNum, workDay, time, subjectIdCanUseMap, timeTablingUseDynamicWeightsAndBacktrackingDTO);

                    // 更新课程使用状态
                    subjectIdCanUseMap.put(maxWeightSubjectId, false);

                    // 检查是否满足排课需求
                    var checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, maxWeightSubjectId, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                    boolean completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                    if (completeFlag) {
                        this.updateAllStatus(grade, classNum, workDay, time, maxWeightSubjectId, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                    }
                    // 如果不满足排课需求，就需要回溯
                    while (!completeFlag) {
                        // 判断这一层的回溯点是否都已经使用，如果没有使用完毕，不需要回溯，选择下一个课程
                        subjectIdCanUseMap = this.getSubjectIdCanUseMap(order, orderGradeClassNumWorkDayTimeMap, gradeClassNumWorkDayTimeSubjectIdCanUseMap);
                        backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                        if (!backFlag) {
                            // 回溯点不清零，记录该点的排课课程，下次不再选择这么课程
                            maxWeightSubjectId = this.getMaxWeightSubjectId(grade, classNum, workDay, time, subjectIdCanUseMap, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                            // 更新课程使用状态
                            subjectIdCanUseMap.put(maxWeightSubjectId, false);
                            // 检查是否满足排课需求
                            checkCompleteUseBacktrackingDTO = this.packCheckCompleteUseBacktrackingDTO(grade, classNum, workDay, time, maxWeightSubjectId, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                            completeFlag = this.checkComplete(checkCompleteUseBacktrackingDTO);
                            if (completeFlag) {
                                this.updateAllStatus(grade, classNum, workDay, time, maxWeightSubjectId, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                            }
                        }

                        // 如果课程使用完毕，则找上一层的回溯点
                        if (backFlag) {
                            order = this.rollback(order, timeTablingUseDynamicWeightsAndBacktrackingDTO);
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


        cattyResult.setSuccess(true);
        cattyResult.setData(timeTableMap);
        return cattyResult;
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
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private ComputerSubjectWeightDTO packComputerSubjectWeightDTO(Integer grade,
                                                                  Integer classNum,
                                                                  Integer workDay,
                                                                  Integer time,
                                                                  List<SubjectWeightDTO> subjectWeightDTOList,
                                                                  TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        ComputerSubjectWeightDTO computerSubjectWeightDTO = new ComputerSubjectWeightDTO();

        computerSubjectWeightDTO.setGrade(grade);
        computerSubjectWeightDTO.setClassNum(classNum);
        computerSubjectWeightDTO.setWorkDay(workDay);
        computerSubjectWeightDTO.setTime(time);
        computerSubjectWeightDTO.setSubjectWeightDTOList(subjectWeightDTOList);
        computerSubjectWeightDTO.setSubjectGradeClassTeacherCountMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getSubjectGradeClassTeacherCountMap());
        computerSubjectWeightDTO.setTeacherTeachingMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap());
        computerSubjectWeightDTO.setTeacherSubjectListMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherSubjectListMap());
        computerSubjectWeightDTO.setGradeClassNumWorDaySubjectCountMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumWorkDaySubjectCountMap());
        computerSubjectWeightDTO.setTimeTableMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap());

        return computerSubjectWeightDTO;
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

        CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO = new CheckCompleteUseBacktrackingDTO();
        checkCompleteUseBacktrackingDTO.setSubjectId(subjectId);
        checkCompleteUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        checkCompleteUseBacktrackingDTO.setTimeTableMap(timeTablingUseBacktrackingDTO.getTimeTableMap());
        checkCompleteUseBacktrackingDTO.setGradeSubjectDTOMap(timeTablingUseBacktrackingDTO.getGradeSubjectDTOMap());
        checkCompleteUseBacktrackingDTO.setSubjectGradeClassTeacherMap(timeTablingUseBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkCompleteUseBacktrackingDTO.setTeacherTeachingMap(timeTablingUseBacktrackingDTO.getTeacherTeachingMap());
        checkCompleteUseBacktrackingDTO.setClassroomMaxCapacity(timeTablingUseBacktrackingDTO.getClassroomMaxCapacityMap());
        checkCompleteUseBacktrackingDTO.setClassroomUsedCountMap(timeTablingUseBacktrackingDTO.getClassRoomUsedCountMap());
        checkCompleteUseBacktrackingDTO.setGrade(grade);
        checkCompleteUseBacktrackingDTO.setClassNum(classNum);
        checkCompleteUseBacktrackingDTO.setWorkDay(workDay);
        checkCompleteUseBacktrackingDTO.setTime(time);

        return checkCompleteUseBacktrackingDTO;
    }

    /**
     * 组装CheckAllCompleteIsOkDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectId
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private CheckCompleteUseBacktrackingDTO packCheckCompleteUseBacktrackingDTO(Integer grade,
                                                                                Integer classNum,
                                                                                Integer workDay,
                                                                                Integer time,
                                                                                Integer subjectId,
                                                                                TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {

        CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO = new CheckCompleteUseBacktrackingDTO();
        checkCompleteUseBacktrackingDTO.setSubjectId(subjectId);
        checkCompleteUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumSubjectFrequencyMap());
        checkCompleteUseBacktrackingDTO.setTimeTableMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap());
        checkCompleteUseBacktrackingDTO.setGradeSubjectDTOMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeSubjectDTOMap());
        checkCompleteUseBacktrackingDTO.setSubjectGradeClassTeacherMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkCompleteUseBacktrackingDTO.setTeacherTeachingMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap());
        checkCompleteUseBacktrackingDTO.setClassroomMaxCapacity(timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassroomMaxCapacityMap());
        checkCompleteUseBacktrackingDTO.setClassroomUsedCountMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassRoomUsedCountMap());
        checkCompleteUseBacktrackingDTO.setGrade(grade);
        checkCompleteUseBacktrackingDTO.setClassNum(classNum);
        checkCompleteUseBacktrackingDTO.setWorkDay(workDay);
        checkCompleteUseBacktrackingDTO.setTime(time);

        return checkCompleteUseBacktrackingDTO;
    }

    /**
     * 组装CheckTeacherIsOkDTO
     *
     * @param checkAllCompleteIsOkDTO
     * @return
     */
    private CheckTeacherIsOkDTO packCheckTeacherIsOkDTO(CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO) {
        CheckTeacherIsOkDTO checkTeacherIsOkDTO = new CheckTeacherIsOkDTO();
        checkTeacherIsOkDTO.setGrade(checkAllCompleteIsOkDTO.getGrade());
        checkTeacherIsOkDTO.setClassNum(checkAllCompleteIsOkDTO.getClassNum());
        checkTeacherIsOkDTO.setWorkDay(checkAllCompleteIsOkDTO.getWorkDay());
        checkTeacherIsOkDTO.setTime(checkAllCompleteIsOkDTO.getTime());
        checkTeacherIsOkDTO.setSubjectMaxWeightDTO(checkAllCompleteIsOkDTO.getSubjectMaxWeightDTO());
        checkTeacherIsOkDTO.setTeacherTeachingMap(checkAllCompleteIsOkDTO.getTeacherTeachingMap());
        checkTeacherIsOkDTO.setSubjectGradeClassTeacherMap(checkAllCompleteIsOkDTO.getSubjectGradeClassTeacherMap());
        return checkTeacherIsOkDTO;
    }

    /**
     * 检查所有条件都满足要求
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkComplete(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        // 早上第一节必须主课
        var firstClassIsMainFlag = this.checkFirstClassIsMainIsOk(checkCompleteUseBacktrackingDTO);
        if (!firstClassIsMainFlag) {
            return false;
        }
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

        // 学生不能上连堂课(上限为2)
        var studentContinueClassFlag = this.checkStudentContinueClassIsOk(checkCompleteUseBacktrackingDTO);
        if (!studentContinueClassFlag) {
            return false;
        }

        // 每门主课不能一天不能超过2节课(上限为2）
        var mainSubjectMaxFlag = this.checkMainSubjectMaxIsOk(checkCompleteUseBacktrackingDTO);
        if (!mainSubjectMaxFlag) {
            return false;
        }

        // 教师不能上连堂课(上限为3)
        var teacherContinueClassFlag = this.checkTeacherContinueClassIsOk(checkCompleteUseBacktrackingDTO);
        if (!teacherContinueClassFlag) {
            return false;
        }

        // 一个教师一天最多上4节课，若果超过4节课，不在排课
        var teacherMaxTeachingFlag = this.checkTeacherMaxTeachingIsOk(checkCompleteUseBacktrackingDTO);
        if (!teacherMaxTeachingFlag) {
            return false;
        }

        // 判断教师是否有时间和教室是否空闲
        return this.checkTeacherAndClassRoomIsOk(checkCompleteUseBacktrackingDTO);
    }

    /**
     * 检查每天都有主课上
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkEveryDayHaveMainIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectId = checkCompleteUseBacktrackingDTO.getSubjectId();
        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectMap.get(subjectId);

        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var gradeClassNumSubjectFrequencyMap = checkCompleteUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var frequency = subjectFrequencyMap.get(subjectId);

        return frequency > SchoolTimeTableDefaultValueDTO.getWorkDay() - checkCompleteUseBacktrackingDTO.getWorkDay();
    }

    /**
     * 检查每天每个班每一种小课只能上一节
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkOtherSubjectIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectId = checkCompleteUseBacktrackingDTO.getSubjectId();
        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectMap.get(subjectId);
        var otherFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectDTO.getType()) || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDTO.getType());
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

        var subjectCountLongMap = timeSubjectIdMap.values().stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        HashMap<Integer, Integer> subjectCountMap = new HashMap<>();
        for (Integer x : subjectCountLongMap.keySet()) {
            subjectCountMap.put(x, subjectCountLongMap.get(x).intValue());
        }
        for (Integer x : subjectCountMap.keySet()) {
            if (subjectId.equals(x)) {
                Integer count = subjectCountMap.get(x);
                if (SubjectDefaultValueDTO.getOneCount().equals(count)) {
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
        if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(checkCompleteUseBacktrackingDTO.getSubjectId())) {
            return true;
        }
        var timeTableMap = checkCompleteUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());
        var subjectId = checkCompleteUseBacktrackingDTO.getSubjectId();

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
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkMainSubjectMaxIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }
        var subjectId = checkCompleteUseBacktrackingDTO.getSubjectId();

        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectDTOMap.get(subjectId);
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var timeTableMap = checkCompleteUseBacktrackingDTO.getTimeTableMap();
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getClassNum());
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(checkCompleteUseBacktrackingDTO.getWorkDay());

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
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkTeacherContinueClassIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }

        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectDTOMap.get(checkCompleteUseBacktrackingDTO.getSubjectId());
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var timeList = this.getTimeList(checkCompleteUseBacktrackingDTO);
        var teacherContinueTime = SchoolTimeTableDefaultValueDTO.getTeacherContinueTime();
        for (Integer[] x : teacherContinueTime) {
            if (Arrays.asList(x).equals(timeList)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查一个教师是否一天上了4节以上的课
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkTeacherMaxTeachingIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        if (checkCompleteUseBacktrackingDTO.getTime() < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
            return true;
        }

        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectDTOMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectDTOMap.get(checkCompleteUseBacktrackingDTO.getSubjectId());
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        var timeList = this.getTimeList(checkCompleteUseBacktrackingDTO);
        return timeList.size() != SchoolTimeTableDefaultValueDTO.getTeacherContinueTimeMaxSize();
    }

    /**
     * 获取教师上课的时间表
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private List<Integer> getTimeList(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var subjectId = checkCompleteUseBacktrackingDTO.getSubjectId();

        var grade = checkCompleteUseBacktrackingDTO.getGrade();
        var classNum = checkCompleteUseBacktrackingDTO.getClassNum();

        var subjectGradeClassTeacherMap = checkCompleteUseBacktrackingDTO.getSubjectGradeClassTeacherMap();
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
        subjectGradeClassDTO.setGrade(grade);
        subjectGradeClassDTO.setClassNum(classNum);
        subjectGradeClassDTO.setSubjectId(subjectId);
        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);

        var teacherTeachingMap = checkCompleteUseBacktrackingDTO.getTeacherTeachingMap();
        var workDayTimeList = teacherTeachingMap.get(teacherId);
        var workDay = checkCompleteUseBacktrackingDTO.getWorkDay();
        return workDayTimeList.get(workDay);
    }

    /**
     * 检查第一节课为主课
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkFirstClassIsMainIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var gradeSubjectDTOMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectMap = gradeSubjectDTOMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectMap.get(checkCompleteUseBacktrackingDTO.getSubjectId());
        if (!SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(checkCompleteUseBacktrackingDTO.getTime())) {
            return true;
        }
        return SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDTO.getType());
    }

    /**
     * 检查教师和教室是否合适
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private Boolean checkTeacherAndClassRoomIsOk(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var checkAllCompleteIsOkDTO = this.packCheckAllCompleteIsOkDTO(checkCompleteUseBacktrackingDTO);
        return this.checkTeacherAndClassRoomIsOk(checkAllCompleteIsOkDTO);
    }

    /**
     * 组装CheckAllCompleteIsOkDTO
     *
     * @param checkCompleteUseBacktrackingDTO
     * @return
     */
    private CheckAllCompleteIsOkDTO packCheckAllCompleteIsOkDTO(CheckCompleteUseBacktrackingDTO checkCompleteUseBacktrackingDTO) {
        var gradeSubjectMap = checkCompleteUseBacktrackingDTO.getGradeSubjectDTOMap();
        var subjectMap = gradeSubjectMap.get(checkCompleteUseBacktrackingDTO.getGrade());
        var subjectDTO = subjectMap.get(checkCompleteUseBacktrackingDTO.getSubjectId());
        SubjectWeightDTO subjectWeightDTO = new SubjectWeightDTO();
        subjectWeightDTO.setSubjectId(checkCompleteUseBacktrackingDTO.getSubjectId());
        subjectWeightDTO.setType(subjectDTO.getType());

        CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO = new CheckAllCompleteIsOkDTO();
        checkAllCompleteIsOkDTO.setGrade(checkCompleteUseBacktrackingDTO.getGrade());
        checkAllCompleteIsOkDTO.setClassNum(checkCompleteUseBacktrackingDTO.getClassNum());
        checkAllCompleteIsOkDTO.setWorkDay(checkCompleteUseBacktrackingDTO.getWorkDay());
        checkAllCompleteIsOkDTO.setTime(checkCompleteUseBacktrackingDTO.getTime());
        checkAllCompleteIsOkDTO.setSubjectMaxWeightDTO(subjectWeightDTO);
        checkAllCompleteIsOkDTO.setClassroomMaxCapacity(checkCompleteUseBacktrackingDTO.getClassroomMaxCapacity());
        checkAllCompleteIsOkDTO.setClassroomUsedCountMap(checkCompleteUseBacktrackingDTO.getClassroomUsedCountMap());
        checkAllCompleteIsOkDTO.setTeacherTeachingMap(checkCompleteUseBacktrackingDTO.getTeacherTeachingMap());
        checkAllCompleteIsOkDTO.setSubjectGradeClassTeacherMap(checkCompleteUseBacktrackingDTO.getSubjectGradeClassTeacherMap());

        return checkAllCompleteIsOkDTO;
    }

    /**
     * 检查教师和教室是否合适
     *
     * @param checkAllCompleteIsOkDTO
     * @return
     */
    private Boolean checkTeacherAndClassRoomIsOk(CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO) {
        // 判断教师是否空闲
        var checkTeacherIsOkDTO = this.packCheckTeacherIsOkDTO(checkAllCompleteIsOkDTO);
        boolean teacherIsOK = this.checkTeacherIsOk(checkTeacherIsOkDTO);
        if (!teacherIsOK) {
            return false;
        }

        // 判断是否需要场地
        boolean needAreaFlag = SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType()
                .equals(checkTeacherIsOkDTO.getSubjectMaxWeightDTO().getType());
        // 如果是需要教室的课程，判断教室是否可以使用
        if (!needAreaFlag) {
            return true;
        }
        var checkClassRoomIsOkDTO = this.packCheckClassRoomIsOkDTO(checkAllCompleteIsOkDTO);
        return this.checkClassRoomIsOk(checkClassRoomIsOkDTO);
    }

    /**
     * 检查特殊课程是否合适
     *
     * @param checkAllCompleteIsOkDTO
     * @return
     */
    private Boolean checkSpecialSubjectIdIsOk(CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO) {
        // 特殊课程检验
        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(checkAllCompleteIsOkDTO.getSubjectMaxWeightDTO().getType())) {
            return true;
        }

        boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(checkAllCompleteIsOkDTO.getWorkDay())
                && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(checkAllCompleteIsOkDTO.getTime());
        boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(checkAllCompleteIsOkDTO.getWorkDay())
                && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(checkAllCompleteIsOkDTO.getTime());
        boolean schoolBaseFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(checkAllCompleteIsOkDTO.getWorkDay())
                && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(checkAllCompleteIsOkDTO.getTime());

        return classMeetingFlag || writingFlag || schoolBaseFlag;
    }

    /**
     * 检查教师是否空闲
     *
     * @param checkTeacherIsOkDTO
     * @return
     */
    private boolean checkTeacherIsOk(CheckTeacherIsOkDTO checkTeacherIsOkDTO) {
        SubjectWeightDTO subjectMaxWeightDTO = checkTeacherIsOkDTO.getSubjectMaxWeightDTO();
        // 课程如果是特殊课程，直接返回true
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectMaxWeightDTO.getType())) {
            return true;
        }
        SubjectGradeClassDTO subjectGradeClassDTO = this.packSubjectGradeClassDTO(checkTeacherIsOkDTO.getSubjectMaxWeightDTO(),
                checkTeacherIsOkDTO.getGrade(), checkTeacherIsOkDTO.getClassNum());
        Integer teacherId = this.getTeachingTeacherId(subjectGradeClassDTO, checkTeacherIsOkDTO.getSubjectGradeClassTeacherMap());
        List<Integer> timeList = this.getTeacherTeachingTimeList(teacherId, checkTeacherIsOkDTO.getWorkDay(), checkTeacherIsOkDTO.getTeacherTeachingMap());
        return !timeList.contains(checkTeacherIsOkDTO.getTime());
    }


    /**
     * 组装 CheckClassRoomIsOkDTO
     *
     * @param checkAllCompleteIsOkDTO
     * @return
     */
    private CheckClassRoomIsOkDTO packCheckClassRoomIsOkDTO(CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO) {
        CheckClassRoomIsOkDTO checkClassRoomIsOkDTO = new CheckClassRoomIsOkDTO();

        var subjectMaxWeightDTO = checkAllCompleteIsOkDTO.getSubjectMaxWeightDTO();

        checkClassRoomIsOkDTO.setSubjectId(subjectMaxWeightDTO.getSubjectId());
        checkClassRoomIsOkDTO.setWorkDay(checkAllCompleteIsOkDTO.getWorkDay());
        checkClassRoomIsOkDTO.setTime(checkAllCompleteIsOkDTO.getTime());
        checkClassRoomIsOkDTO.setClassroomMaxCapacity(checkAllCompleteIsOkDTO.getClassroomMaxCapacity());
        checkClassRoomIsOkDTO.setClassroomUsedCountMap(checkAllCompleteIsOkDTO.getClassroomUsedCountMap());

        return checkClassRoomIsOkDTO;
    }


    /**
     * 检查教室是否ok
     *
     * @param checkClassRoomIsOkDTO
     * @return
     */
    private boolean checkClassRoomIsOk(CheckClassRoomIsOkDTO checkClassRoomIsOkDTO) {

        var subjectId = checkClassRoomIsOkDTO.getSubjectId();
        var workDay = checkClassRoomIsOkDTO.getWorkDay();
        var time = checkClassRoomIsOkDTO.getTime();
        var classroomMaxCapacity = checkClassRoomIsOkDTO.getClassroomMaxCapacity();
        var classroomUsedCountMap = checkClassRoomIsOkDTO.getClassroomUsedCountMap();

        var workDayTimeClassRoomUsedCountMap = classroomUsedCountMap.get(subjectId);
        var maxCapacity = classroomMaxCapacity.get(subjectId);
        var timeClassRoomUsedCountMap = workDayTimeClassRoomUsedCountMap.get(workDay);
        var usedCapacity = timeClassRoomUsedCountMap.get(time);
        if (usedCapacity > maxCapacity) {
            return false;
        }
        return true;
    }


    /**
     * 组装subjectGradeClassDTO
     *
     * @param subjectMaxWeightDTO
     * @param grade
     * @param classNum
     * @return
     */
    private SubjectGradeClassDTO packSubjectGradeClassDTO(SubjectWeightDTO subjectMaxWeightDTO,
                                                          Integer grade,
                                                          Integer classNum) {
        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
        subjectGradeClassDTO.setSubjectId(subjectMaxWeightDTO.getSubjectId());
        subjectGradeClassDTO.setGrade(grade);
        subjectGradeClassDTO.setClassNum(classNum);
        return subjectGradeClassDTO;
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
     * 获取上课教师某日的上课时间
     *
     * @param teacherId
     * @param workDay
     * @param teacherTeachingMap
     * @return
     */
    private List<Integer> getTeacherTeachingTimeList(Integer teacherId,
                                                     Integer workDay,
                                                     HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        var teachingTimeMap = teacherTeachingMap.get(teacherId);
        return teachingTimeMap.get(workDay);
    }


}