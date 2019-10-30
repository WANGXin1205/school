package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.service.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class TimeTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableService.class);

    public static final int START_INDEX = 1;

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

    /**
     * 排课算法 回溯算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableWithBacktracking() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        var prepareTimeTablingUseBacktrackingResult = this.prepareTimeTablingUseBacktracking();
        if (!prepareTimeTablingUseBacktrackingResult.isSuccess()) {
            cattyResult.setMessage(prepareTimeTablingUseBacktrackingResult.getMessage());
            return cattyResult;
        }
        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = prepareTimeTablingUseBacktrackingResult.getData();

        // 排课核心算法
        var algorithmInPlanTimeTableWithBacktrackingResult = this.algorithmInPlanTimeTableWithBacktracking(timeTablingUseBacktrackingDTO);
        if (!algorithmInPlanTimeTableWithBacktrackingResult.isSuccess()) {
            cattyResult.setMessage(algorithmInPlanTimeTableWithBacktrackingResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = algorithmInPlanTimeTableWithBacktrackingResult.getData();

        // 结果展示
        var timeTableNameMap = this.convertTimeTableMapToTimeTableNameMap(timeTablingUseBacktrackingDTO.getAllSubjectNameMap(), timeTableMap);

        cattyResult.setData(timeTableNameMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 回溯算法核心
     *
     * @param timeTablingUseBacktrackingDTO
     * @return
     */
    private CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> algorithmInPlanTimeTableWithBacktracking(TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();

        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();

        for (int order = START_INDEX; order <= orderGradeClassNumWorkDayTimeMap.keySet().size(); order++) {
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
            if (clearOrder != order){
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
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackSubjectIdCanUseMap(Integer grade, Integer classNum, Integer workDay, Integer time,
                                            TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var gradeClassNumWorkDayTimeSubjectIdCanUseMap = timeTablingUseBacktrackingDTO.getGradeClassNumWorkDayTimeSubjectIdCanUseMap();
        var classNumWorkDayTimeSubjectIdCanUseMap = gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        var workDayTimeSubjectIdCanUseMap = classNumWorkDayTimeSubjectIdCanUseMap.get(classNum);
        var timeSubjectIdCanUseMap = workDayTimeSubjectIdCanUseMap.get(workDay);
        var subjectIdCanUseMap = timeSubjectIdCanUseMap.get(time);
        subjectIdCanUseMap.replaceAll((key, value) -> true);

        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);

        this.updateSubjectIdCanUseMap(subjectIdCanUseMap, subjectFrequencyMap, workDay, time);
        timeSubjectIdCanUseMap.put(time, subjectIdCanUseMap);
        workDayTimeSubjectIdCanUseMap.put(workDay, timeSubjectIdCanUseMap);
        classNumWorkDayTimeSubjectIdCanUseMap.put(classNum, workDayTimeSubjectIdCanUseMap);
        gradeClassNumWorkDayTimeSubjectIdCanUseMap.get(grade);
        timeTablingUseBacktrackingDTO.setGradeClassNumWorkDayTimeSubjectIdCanUseMap(gradeClassNumWorkDayTimeSubjectIdCanUseMap);
    }

    /**
     * 清除教师上课状态
     *
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackTeacherTeaching(Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var teacherTeachingMap = timeTablingUseBacktrackingDTO.getTeacherTeachingMap();

        for (Integer teacherId : teacherTeachingMap.keySet()) {
            var workDayTimeListMap = teacherTeachingMap.get(teacherId);
            var timeList = workDayTimeListMap.get(workDay);
            if (CollectionUtils.isNotEmpty(timeList)) {
                timeList.remove(time);
            }
            workDayTimeListMap.put(workDay, timeList);
            teacherTeachingMap.put(teacherId, workDayTimeListMap);
        }

        timeTablingUseBacktrackingDTO.setTeacherTeachingMap(teacherTeachingMap);
    }

    /**
     * 清除所有教室
     *
     * @param workDay
     * @param time
     * @param timeTablingUseBacktrackingDTO
     */
    private void rollbackClassroom(Integer workDay, Integer time, TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        var classRoomUsedCountMap = timeTablingUseBacktrackingDTO.getClassRoomUsedCountMap();
        for (Integer subjectId : classRoomUsedCountMap.keySet()) {
            var workDayTimeCountMap = classRoomUsedCountMap.get(subjectId);
            var timeCountMap = workDayTimeCountMap.get(workDay);
            timeCountMap.put(time, SubjectDefaultValueDTO.getZeroFrequency());
            workDayTimeCountMap.put(workDay, timeCountMap);
            classRoomUsedCountMap.put(subjectId, workDayTimeCountMap);
        }
        timeTablingUseBacktrackingDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
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
        var classNumWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classNumWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        var subjectId = timeSubjectIdMap.get(time);
        if (subjectId == null) {
            return;
        }

        var gradeClassNumSubjectFrequencyMap = timeTablingUseBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(grade);
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(classNum);
        subjectFrequencyMap.put(subjectId, subjectFrequencyMap.get(subjectId) + SubjectDefaultValueDTO.getOneCount());
        classNumSubjectFrequencyMap.put(classNum, subjectFrequencyMap);
        gradeClassNumSubjectFrequencyMap.put(grade, classNumSubjectFrequencyMap);
        timeTablingUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassNumSubjectFrequencyMap);
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
     * 根据上课次数和时间点判断是否能够选择的课程
     *
     * @param subjectIdCanUseMap
     * @param subjectFrequencyMap
     * @param worDay
     * @param time
     * @return
     */
    private HashMap<Integer, Boolean> updateSubjectIdCanUseMap(HashMap<Integer, Boolean> subjectIdCanUseMap,
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

        return subjectIdCanUseMap;
    }

    /**
     * 排课接口 使用动态权重和回溯算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableUseDynamicWeightsAndBacktracking() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        var prepareTimeTablingUseDynamicWeightsAndBacktrackingResult = this.prepareTimeTablingUseDynamicWeightsAndBacktracking();
        if (!prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.isSuccess()) {
            cattyResult.setMessage(prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.getMessage());
            return cattyResult;
        }
        TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO = prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.getData();

        // 排课核心算法
        var algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult = this.algorithmInPlanTimeTableWithDynamicWeightsAndBacktracking(timeTablingUseDynamicWeightsAndBacktrackingDTO);
        if (!algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.isSuccess()) {
            cattyResult.setMessage(algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.getData();

        // 结果展示
        var timeTableNameMap = this.convertTimeTableMapToTimeTableNameMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getAllSubjectNameMap(), timeTableMap);

        cattyResult.setData(timeTableNameMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 准备默认学校配置
     *
     * @return
     */
    private PrepareTimeTablingDTO prepareTimeTabling() {

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
        OrderDTO orderDTO = this.getOrderMap(gradeClassCountMap);

        // 检查年级信息无误
        CattyResult checkGradeResult = this.checkGrade(gradeClassCountMap, gradeSubjectMap);
        if (!checkGradeResult.isSuccess()) {
            throw new TransactionException(checkGradeResult.getMessage());
        }

        // 检查教师和上课教师信息无误
        CattyResult checkTeacherResult = this.checkTeacher(allWorkTeacher, teacherTeachingMap);
        if (!checkTeacherResult.isSuccess()) {
            throw new TransactionException(checkTeacherResult.getMessage());
        }
        // 检查问题有解
        var checkTimeTableSolutionResult = this.checkTimeTableSolution(gradeSubjectMap, allSubjectNameMap, subjectGradeClassTeacherMap);
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
        prepareTimeTablingDTO.setGradeClassSubjectWeightMap(gradeClassSubjectWeightMap);
        prepareTimeTablingDTO.setTimeTableMap(timeTableMap);
        prepareTimeTablingDTO.setOrderGradeClassNumWorkDayTimeMap(orderDTO.getOrderGradeClassNumWorkDayTimeMap());
        prepareTimeTablingDTO.setGradeClassNumWorkDayTimeOrderMap(orderDTO.getGradeClassNumWorkDayTimeOrderMap());

        return prepareTimeTablingDTO;
    }

    /**
     * 动态权重回溯算法准备默认学校配置
     *
     * @return
     */
    private CattyResult<TimeTablingUseBacktrackingDTO> prepareTimeTablingUseBacktracking() {
        CattyResult<TimeTablingUseBacktrackingDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();

        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = new TimeTablingUseBacktrackingDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseBacktrackingDTO);

        var gradeSubjectListMap = prepareTimeTablingDTO.getGradeSubjectMap();
        HashMap<Integer, HashMap<Integer, SubjectDTO>> gradeSubjectDTOMap = new HashMap<>();
        for (Integer grade : gradeSubjectListMap.keySet()) {
            var subjectDTOList = gradeSubjectListMap.get(grade);
            var subjectDTOMap = (HashMap<Integer, SubjectDTO>) subjectDTOList.stream().collect(Collectors.toMap(SubjectDTO::getSubjectId, Function.identity()));
            gradeSubjectDTOMap.put(grade, subjectDTOMap);
        }
        timeTablingUseBacktrackingDTO.setGradeSubjectDTOMap(gradeSubjectDTOMap);

        HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> gradeClassSubjectFrequencyMap
                = this.getGradeClassSubjectFrequencyMap(prepareTimeTablingDTO.getGradeSubjectMap(), timeTablingUseBacktrackingDTO.getGradeClassCountMap());
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>>>
                subjectIdCanUseMap = this.getGradeClassNumWorkDayTimeSubjectIdCanUseMap(prepareTimeTablingDTO.getGradeSubjectMap(), timeTablingUseBacktrackingDTO.getGradeClassCountMap());

        timeTablingUseBacktrackingDTO.setGradeClassNumSubjectFrequencyMap(gradeClassSubjectFrequencyMap);
        timeTablingUseBacktrackingDTO.setGradeClassNumWorkDayTimeSubjectIdCanUseMap(subjectIdCanUseMap);
        timeTablingUseBacktrackingDTO.setOrderGradeClassNumWorkDayTimeMap(prepareTimeTablingDTO.getOrderGradeClassNumWorkDayTimeMap());
        timeTablingUseBacktrackingDTO.setGradeClassNumWorkDayTimeOrderMap(prepareTimeTablingDTO.getGradeClassNumWorkDayTimeOrderMap());

        cattyResult.setData(timeTablingUseBacktrackingDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 动态权重回溯算法准备默认学校配置
     *
     * @return
     */
    private CattyResult<TimeTablingUseDynamicWeightsAndBacktrackingDTO> prepareTimeTablingUseDynamicWeightsAndBacktracking() {
        CattyResult<TimeTablingUseDynamicWeightsAndBacktrackingDTO> cattyResult = new CattyResult<>();

        PrepareTimeTablingDTO prepareTimeTablingDTO = this.prepareTimeTabling();

        TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO = new TimeTablingUseDynamicWeightsAndBacktrackingDTO();
        BeanUtils.copyProperties(prepareTimeTablingDTO, timeTablingUseDynamicWeightsAndBacktrackingDTO);

        cattyResult.setSuccess(true);
        cattyResult.setData(timeTablingUseDynamicWeightsAndBacktrackingDTO);
        return cattyResult;
    }


    /**
     * 检查年级是否一致
     *
     * @param gradeClassCountMap
     * @param gradeSubjectMap
     * @return
     */
    private CattyResult checkGrade(HashMap<Integer, Integer> gradeClassCountMap,
                                   Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        CattyResult cattyResult = new CattyResult();

        if (!gradeClassCountMap.keySet().equals(gradeSubjectMap.keySet())) {
            cattyResult.setMessage("年级和科目对应关系不一致");
            return cattyResult;
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查教师信息一致
     *
     * @param allTeacher
     * @param teacherTeachingMap
     * @return
     */
    private CattyResult checkTeacher(List<TeacherDO> allTeacher, HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        CattyResult cattyResult = new CattyResult();
        for (TeacherDO x : allTeacher) {
            if (teacherTeachingMap.get(x.getId()) == null) {
                cattyResult.setMessage(x.getName() + "没有上课的班级");
                return cattyResult;
            }
        }
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查排课是否有解
     *
     * @param gradeSubjectMap
     * @param allSubjectNameMap
     * @param subjectGradeClassTeacherMap
     * @return
     */
    private CattyResult checkTimeTableSolution(Map<Integer, List<SubjectDTO>> gradeSubjectMap,
                                               Map<Integer, String> allSubjectNameMap,
                                               HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        CattyResult<Map<Integer, List<SubjectDO>>> cattyResult = new CattyResult<>();

        // 检查科目ok
        var checkSubjectResult = this.checkSubject(gradeSubjectMap);
        if (!checkSubjectResult.isSuccess()) {
            cattyResult.setMessage(checkSubjectResult.getMessage());
            return cattyResult;
        }

        // 检查每个班每种科目都有教师上课
        CattyResult checkAllSubjectTeacherGradeClassResult = this.checkAllSubjectTeacherGradeClass(allSubjectNameMap, subjectGradeClassTeacherMap);
        if (!checkAllSubjectTeacherGradeClassResult.isSuccess()) {
            cattyResult.setMessage(checkAllSubjectTeacherGradeClassResult.getMessage());
            return cattyResult;
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查科目OK
     *
     * @param gradeSubjectMap
     * @return
     */
    private CattyResult checkSubject(Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        CattyResult cattyResult = new CattyResult();

        for (Integer x : gradeSubjectMap.keySet()) {
            Integer times = gradeSubjectMap.get(x).stream().map(SubjectDTO::getFrequency).reduce(Integer::sum).get();
            if (!SchoolTimeTableDefaultValueDTO.getTotalFrequency().equals(times)) {
                cattyResult.setMessage(x + "年级的排课总量与目前排课量不相符");
                return cattyResult;
            }
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查所有科目教师年级班级都ok
     *
     * @param allSubjectNameMap
     * @param subjectGradeClassTeacherMap
     * @return
     */
    private CattyResult checkAllSubjectTeacherGradeClass(Map<Integer, String> allSubjectNameMap,
                                                         HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        CattyResult cattyResult = new CattyResult();

        for (SubjectGradeClassDTO x : subjectGradeClassTeacherMap.keySet()) {
            var teacherId = subjectGradeClassTeacherMap.get(x);
            if (teacherId == null) {
                var subjectName = allSubjectNameMap.get(x.getSubjectId());
                cattyResult.setMessage(x.getGrade() + "年级" + subjectName + " 科目没有教师带课");
                return cattyResult;
            }
        }

        cattyResult.setSuccess(true);
        return cattyResult;
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
            for (int classNum = START_INDEX; classNum <= classCount; classNum++) {
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
    private OrderDTO getOrderMap(HashMap<Integer, Integer> gradeClassCountMap) {
        OrderDTO orderDTO = new OrderDTO();
        HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap = new HashMap<>();
        HashMap<GradeClassNumWorkDayTimeDTO, Integer> gradeClassNumWorkDayTimeOrderMap = new HashMap<>();
        int count = START_INDEX;
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
                        gradeClassNumWorkDayTimeOrderMap.put(gradeClassNumWorkDayTimeDTO, count);

                        orderDTO.setOrderGradeClassNumWorkDayTimeMap(orderGradeClassNumWorkDayTimeMap);
                        orderDTO.setGradeClassNumWorkDayTimeOrderMap(gradeClassNumWorkDayTimeOrderMap);

                        count = count + SubjectDefaultValueDTO.getOneCount();
                    }
                }
            }
        }
        return orderDTO;
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

        for (int grade = START_INDEX; grade <= gradeSubjectMap.keySet().size(); grade++) {

            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>> classNumWorkDayTimeSubjectCanUseMap = new HashMap<>();
            Integer classCount = gradeClassCountMap.get(grade);
            for (int classNum = START_INDEX; classNum <= classCount; classNum++) {

                HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>> workDayTimeSubjectCanUseMap = new HashMap<>();
                for (int worDay = START_INDEX; worDay <= SchoolTimeTableDefaultValueDTO.getWorkDay(); worDay++) {

                    HashMap<Integer, HashMap<Integer, Boolean>> timeSubjectCanUseMap = new HashMap<>();
                    for (int time = START_INDEX; time <= SchoolTimeTableDefaultValueDTO.getClassTime(); time++) {

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

    /**
     * 排课核心算法
     *
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> algorithmInPlanTimeTableWithDynamicWeightsAndBacktracking(TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();
        var timeTableMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTimeTableMap();

        var gradeClassCountMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassCountMap();

        // 年级
        for (Integer grade : gradeClassCountMap.keySet()) {
            // 班级
            Integer classCount = gradeClassCountMap.get(grade);
            for (Integer classNum = SchoolTimeTableDefaultValueDTO.getStartClassIndex(); classNum <= classCount; classNum++) {
                // 工作日
                for (Integer workDay = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex(); workDay <= SchoolTimeTableDefaultValueDTO.getWorkDay(); workDay++) {
                    // 节次
                    for (Integer time = SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex(); time <= SchoolTimeTableDefaultValueDTO.getClassTime(); time++) {

                        // 排课
                        var subjectMaxWeightDTO = this.TimeTablingUseDynamicWeightsAndBacktrackingCore(grade, classNum, workDay, time, timeTablingUseDynamicWeightsAndBacktrackingDTO);
                        // 设置停机条件
                        if (subjectMaxWeightDTO.getSubjectId() < SubjectWeightDefaultValueDTO.getOneStep()) {
                            cattyResult.setMessage("在该权重下，未找到解");
                            return cattyResult;
                        }

                        // 权重清零并且赋值的科目次数减少1
                        this.clearAllWeight(grade, classNum, subjectMaxWeightDTO, timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassSubjectWeightMap());

                        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectMaxWeightDTO.getType())) {
                            var subjectGradeClassDTO = this.packSubjectGradeClassDTO(subjectMaxWeightDTO, grade, classNum);
                            // 教师要记录上课节次
                            this.markTeacher(timeTablingUseDynamicWeightsAndBacktrackingDTO, subjectGradeClassDTO, workDay, time);

                            // 记录上课次数
                            this.markGradeClassNumWorkDaySubjectCountMap(timeTablingUseDynamicWeightsAndBacktrackingDTO, grade, classNum, workDay, subjectMaxWeightDTO.getSubjectId());

                            // 如果有占用教室，记录教室使用情况
                            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectMaxWeightDTO.getType())) {
                                this.markClassroom(timeTablingUseDynamicWeightsAndBacktrackingDTO, subjectMaxWeightDTO.getSubjectId(), workDay, time);
                            }
                        }

                        // 组装成课表
                        this.markTimeTableMap(timeTableMap, grade, classNum, workDay, time, subjectMaxWeightDTO.getSubjectId());
                    }
                }

            }

        }

        cattyResult.setSuccess(true);
        cattyResult.setData(timeTableMap);
        return cattyResult;
    }

    /**
     * 教师记录上课时间
     *
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @param subjectGradeClassDTO
     * @param workDay
     * @param time
     */
    private void markTeacher(TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO,
                             SubjectGradeClassDTO subjectGradeClassDTO,
                             Integer workDay,
                             Integer time) {
        var subjectGradeClassTeacherMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getSubjectGradeClassTeacherMap();

        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
        var teacherTeachingMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap();
        var workDayTimeTeaching = teacherTeachingMap.get(teacherId);
        var teachingList = workDayTimeTeaching.get(workDay);
        if (CollectionUtils.isEmpty(teachingList)) {
            teachingList = new ArrayList<>();
        }
        teachingList.add(time);
        workDayTimeTeaching.put(workDay, teachingList);
        teacherTeachingMap.put(teacherId, workDayTimeTeaching);
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setTeacherTeachingMap(teacherTeachingMap);
    }

    /**
     * 记录上课次数
     *
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @param grade
     * @param classNum
     * @param workDay
     * @param subjectId
     */
    private void markGradeClassNumWorkDaySubjectCountMap(TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO,
                                                         Integer grade,
                                                         Integer classNum,
                                                         Integer workDay,
                                                         Integer subjectId) {
        var gradeClassNumWorkDaySubjectCountMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassNumWorkDaySubjectCountMap();
        var classNumWorkDaySubjectCountMap = gradeClassNumWorkDaySubjectCountMap.get(grade);
        var workDaySubjectCountMap = classNumWorkDaySubjectCountMap.get(classNum);
        var subjectCountMap = workDaySubjectCountMap.get(workDay);
        var subjectCount = subjectCountMap.get(subjectId);
        subjectCountMap.put(subjectId, subjectCount + SubjectWeightDefaultValueDTO.getOneStep());
        workDaySubjectCountMap.put(workDay, subjectCountMap);
        classNumWorkDaySubjectCountMap.put(classNum, workDaySubjectCountMap);
        gradeClassNumWorkDaySubjectCountMap.put(grade, classNumWorkDaySubjectCountMap);
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setGradeClassNumWorkDaySubjectCountMap(gradeClassNumWorkDaySubjectCountMap);
    }

    /**
     * 记录教室使用情况
     *
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @param subjectId
     * @param workDay
     * @param time
     */
    private void markClassroom(TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO,
                               Integer subjectId,
                               Integer workDay,
                               Integer time) {
        var classRoomUsedCountMap = timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassRoomUsedCountMap();
        var workDaytimeClassroomUsedMap = classRoomUsedCountMap.get(subjectId);
        var timeClassroomUsedMap = workDaytimeClassroomUsedMap.get(workDay);
        timeClassroomUsedMap.put(time, timeClassroomUsedMap.get(time) + 1);
        workDaytimeClassroomUsedMap.put(workDay, timeClassroomUsedMap);
        classRoomUsedCountMap.put(subjectId, workDaytimeClassroomUsedMap);
        timeTablingUseDynamicWeightsAndBacktrackingDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
    }

    /**
     * 记录课程表
     *
     * @param timeTableMap
     */
    private void markTimeTableMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap,
                                  Integer grade, Integer classNum, Integer workDay, Integer time, Integer subjectId) {
        var classNumWorkDayTimeSubjectMap = timeTableMap.get(grade);
        var workDayTimeSubjectMap = classNumWorkDayTimeSubjectMap.get(classNum);
        var timeSubjectMap = workDayTimeSubjectMap.get(workDay);
        timeSubjectMap.put(time, subjectId);
        workDayTimeSubjectMap.put(workDay, timeSubjectMap);
        classNumWorkDayTimeSubjectMap.put(classNum, workDayTimeSubjectMap);
        timeTableMap.put(grade, classNumWorkDayTimeSubjectMap);
    }


    /**
     * 排课核心算法
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private SubjectWeightDTO TimeTablingUseDynamicWeightsAndBacktrackingCore(Integer grade, Integer classNum, Integer workDay, Integer time, TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {
        // 筛选出某个年级下某个班级要赋值的课程,并且初始化所有课程的权重
        var subjectWeightDTOList = this.listSubjectWeightDTO(grade, classNum, timeTablingUseDynamicWeightsAndBacktrackingDTO.getGradeClassSubjectWeightMap());

        // 组装computerSubjectWeightDTO
        var computerSubjectWeightDTO = this.packComputerSubjectWeightDTO(grade, classNum, workDay, time, subjectWeightDTOList, timeTablingUseDynamicWeightsAndBacktrackingDTO);

        // 计算权重
        subjectService.computerSubjectWeightDTO(computerSubjectWeightDTO);

        // 筛选出权重最大的课程
        var subjectMaxWeightDTO = subjectService.filterMaxSubjectWeightDTO(subjectWeightDTOList);

        // 组装一切就绪的DTO
        var checkAllCompleteIsOkDTO = this.packCheckAllCompleteIsOKDTO(grade,
                classNum, workDay, time, subjectMaxWeightDTO, timeTablingUseDynamicWeightsAndBacktrackingDTO);
        // 查看一切是否就绪
        boolean completeFlag = this.checkAllCompleteFlag(checkAllCompleteIsOkDTO);
        while (!completeFlag) {
            // 重新计算权重
            subjectService.fixSubjectWeightDTO(computerSubjectWeightDTO.getSubjectWeightDTOList());

            // 筛选权重最大的课程
            subjectMaxWeightDTO = subjectService.filterMaxSubjectWeightDTO(subjectWeightDTOList);

            // 组装组装一切就绪的DTO
            checkAllCompleteIsOkDTO = this.packCheckAllCompleteIsOKDTO(grade,
                    classNum, workDay, time, subjectMaxWeightDTO, timeTablingUseDynamicWeightsAndBacktrackingDTO);

            // 查看一切是否就绪
            completeFlag = this.checkAllCompleteFlag(checkAllCompleteIsOkDTO);
        }

        return subjectMaxWeightDTO;
    }

    /**
     * TimeTableMap转换为TimeTableNameMap
     *
     * @param timeTableMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>
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
     * @param subjectMaxWeightDTO
     * @param timeTablingUseDynamicWeightsAndBacktrackingDTO
     * @return
     */
    private CheckAllCompleteIsOkDTO packCheckAllCompleteIsOKDTO(Integer grade,
                                                                Integer classNum,
                                                                Integer workDay,
                                                                Integer time,
                                                                SubjectWeightDTO subjectMaxWeightDTO,
                                                                TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO) {

        CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO = new CheckAllCompleteIsOkDTO();
        checkAllCompleteIsOkDTO.setGrade(grade);
        checkAllCompleteIsOkDTO.setClassNum(classNum);
        checkAllCompleteIsOkDTO.setWorkDay(workDay);
        checkAllCompleteIsOkDTO.setTime(time);
        checkAllCompleteIsOkDTO.setSubjectMaxWeightDTO(subjectMaxWeightDTO);
        checkAllCompleteIsOkDTO.setSubjectGradeClassTeacherMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getSubjectGradeClassTeacherMap());
        checkAllCompleteIsOkDTO.setTeacherTeachingMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getTeacherTeachingMap());
        checkAllCompleteIsOkDTO.setClassroomMaxCapacity(timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassroomMaxCapacityMap());
        checkAllCompleteIsOkDTO.setClassroomUsedCountMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getClassRoomUsedCountMap());

        return checkAllCompleteIsOkDTO;
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
     * 检查所有条件都满足排课要求
     *
     * @param checkAllCompleteIsOkDTO
     * @return
     */
    private boolean checkAllCompleteFlag(CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO) {

        // 特殊课程检验
        var checkSpecialSubjectIdIsOk = this.checkSpecialSubjectIdIsOk(checkAllCompleteIsOkDTO);
        if (!checkSpecialSubjectIdIsOk) {
            return false;
        }

        // 检查教师和教室是否合适
        return this.checkTeacherAndClassRoomIsOk(checkAllCompleteIsOkDTO);
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

    /**
     * 清除所有课程的权重
     *
     * @param grade
     * @param classNum
     * @param subjectMaxWeightDTO
     * @param gradeClassSubjectWeightMap
     */
    private void clearAllWeight(Integer grade,
                                Integer classNum,
                                SubjectWeightDTO subjectMaxWeightDTO,
                                HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap) {
        var classSubjectWeightMap = gradeClassSubjectWeightMap.get(grade);
        var subjectWeightDTOList = classSubjectWeightMap.get(classNum);
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (subjectMaxWeightDTO.getSubjectId().equals(x.getSubjectId())) {
                x.setFrequency(x.getFrequency() - SubjectWeightDefaultValueDTO.getOneStep());
            }
            x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
        }
    }

}
