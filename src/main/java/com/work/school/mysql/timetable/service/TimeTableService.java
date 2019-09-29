package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.service.dto.*;
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
     * 排课接口
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTable() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        var preparePlanTimeTableResult = this.preparePlanTimeTable();
        if (!preparePlanTimeTableResult.isSuccess()) {
            cattyResult.setMessage(preparePlanTimeTableResult.getMessage());
            return cattyResult;
        }
        PlanTimeTablePrepareDTO planTimeTablePrepareDTO = preparePlanTimeTableResult.getData();

        // 排课核心算法
        var algorithmInPlanTimeTableResult = this.algorithmInPlanTimeTable(planTimeTablePrepareDTO);
        if (!algorithmInPlanTimeTableResult.isSuccess()) {
            cattyResult.setMessage(algorithmInPlanTimeTableResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = algorithmInPlanTimeTableResult.getData();

        // 结果展示
        var timeTableNameMap = this.convertTimeTableMapToTimeTableNameMap(planTimeTablePrepareDTO.getAllSubjectNameMap(), timeTableMap);

        cattyResult.setSuccess(true);
        cattyResult.setData(timeTableNameMap);
        return cattyResult;
    }

    /**
     * 准备默认学校配置
     *
     * @return
     */
    private CattyResult<PlanTimeTablePrepareDTO> preparePlanTimeTable() {
        CattyResult<PlanTimeTablePrepareDTO> cattyResult = new CattyResult<>();

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
        HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> gradeClassSubjectWeightMap = this.getGradeClassSubjectWeightMap(gradeClassCountMap, gradeSubjectMap);
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap = this.getTimeTableMap(gradeClassSubjectWeightMap);
        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap = this.getTeacherTeachingMap(allWorkTeacher);
        var gradeClassNumWorkDaySubjectCountMap = this.getGradeClassNumWorkDaySubjectCountMap(gradeClassCountMap, allSubjects);

        // 检查年级信息无误
        CattyResult checkGradeResult = this.checkGrade(gradeClassCountMap, gradeSubjectMap);
        if (!checkGradeResult.isSuccess()) {
            cattyResult.setMessage(checkGradeResult.getMessage());
            return cattyResult;
        }

        // 检查教师和上课教师信息无误
        CattyResult checkTeacherResult = this.checkTeacher(allWorkTeacher, teacherTeachingMap);
        if (!checkTeacherResult.isSuccess()) {
            cattyResult.setMessage(checkTeacherResult.getMessage());
            return cattyResult;
        }
        // 检查问题有解
        var checkTimeTableSolutionResult = this.checkTimeTableSolution(gradeSubjectMap, allSubjectNameMap, subjectGradeClassTeacherMap);
        if (!checkTimeTableSolutionResult.isSuccess()) {
            cattyResult.setMessage(checkTimeTableSolutionResult.getMessage());
            return cattyResult;
        }

        // 组装planTimeTablePrepareDTO
        PlanTimeTablePrepareDTO planTimeTablePrepareDTO = new PlanTimeTablePrepareDTO();
        planTimeTablePrepareDTO.setAllGradeClassInfo(allGradeClassInfo);
        planTimeTablePrepareDTO.setGradeClassCountMap(gradeClassCountMap);
        planTimeTablePrepareDTO.setAllSubject(allSubjects);
        planTimeTablePrepareDTO.setAllSubjectNameMap(allSubjectNameMap);
        planTimeTablePrepareDTO.setGradeSubjectMap(gradeSubjectMap);
        planTimeTablePrepareDTO.setGradeClassNumWorkDaySubjectCountMap(gradeClassNumWorkDaySubjectCountMap);
        planTimeTablePrepareDTO.setClassroomMaxCapacityMap(classroomMaxCapacityMap);
        planTimeTablePrepareDTO.setClassRoomUsedCountMap(classRoomUsedCountMap);
        planTimeTablePrepareDTO.setAllWorkTeacher(allWorkTeacher);
        planTimeTablePrepareDTO.setTeacherTeachingMap(teacherTeachingMap);
        planTimeTablePrepareDTO.setAllSubjectTeacherGradeClassDTO(allSubjectTeacherGradeClassDTO);
        planTimeTablePrepareDTO.setSubjectGradeClassTeacherMap(subjectGradeClassTeacherMap);
        planTimeTablePrepareDTO.setSubjectGradeClassTeacherCountMap(subjectGradeClassTeacherCountMap);
        planTimeTablePrepareDTO.setTeacherSubjectListMap(teacherSubjectListMap);
        planTimeTablePrepareDTO.setGradeClassSubjectWeightMap(gradeClassSubjectWeightMap);
        planTimeTablePrepareDTO.setTimeTableMap(timeTableMap);

        cattyResult.setSuccess(true);
        cattyResult.setData(planTimeTablePrepareDTO);
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
     * 获取每个年级每个班的课程权重
     *
     * @param gradeClassCountMap
     * @param allGradeSubjectMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, List<SubjectWeightDTO>>> getGradeClassSubjectWeightMap(HashMap<Integer, Integer> gradeClassCountMap,
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
     * @param planTimeTablePrepareDTO
     * @return
     */
    private CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> algorithmInPlanTimeTable(PlanTimeTablePrepareDTO planTimeTablePrepareDTO) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>> cattyResult = new CattyResult<>();
        var timeTableMap = planTimeTablePrepareDTO.getTimeTableMap();

        var gradeClassCountMap = planTimeTablePrepareDTO.getGradeClassCountMap();

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
                        var subjectMaxWeightDTO = this.planTimeTableCore(grade, classNum, workDay, time, planTimeTablePrepareDTO);

                        // 权重清零并且赋值的科目次数减少1
                        this.clearAllWeight(grade, classNum, subjectMaxWeightDTO, planTimeTablePrepareDTO.getGradeClassSubjectWeightMap());

                        // 教师要记录上课节次
                        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectMaxWeightDTO.getType())) {
                            var subjectGradeClassTeacherMap = planTimeTablePrepareDTO.getSubjectGradeClassTeacherMap();

                            var subjectGradeClassDTO = this.packSubjectGradeClassDTO(subjectMaxWeightDTO, grade, classNum);
                            var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
                            var teacherTeachingMap = planTimeTablePrepareDTO.getTeacherTeachingMap();
                            var workDayTimeTeaching = teacherTeachingMap.get(teacherId);
                            var teachingList = workDayTimeTeaching.get(time);
                            if (CollectionUtils.isEmpty(teachingList)) {
                                teachingList = new ArrayList<>();
                            }
                            teachingList.add(time);
                            workDayTimeTeaching.put(workDay, teachingList);
                            teacherTeachingMap.put(teacherId, workDayTimeTeaching);

                            // 记录上课次数
                            var gradeClassNumWorkDaySubjectCountMap = planTimeTablePrepareDTO.getGradeClassNumWorkDaySubjectCountMap();
                            var classNumWorkDaySubjectCountMap = gradeClassNumWorkDaySubjectCountMap.get(grade);
                            var workDaySubjectCountMap = classNumWorkDaySubjectCountMap.get(classNum);
                            var subjectCountMap = workDaySubjectCountMap.get(workDay);
                            var subjectCount = subjectCountMap.get(subjectMaxWeightDTO.getSubjectId());
                            subjectCountMap.put(subjectMaxWeightDTO.getSubjectId(), subjectCount + SubjectWeightDefaultValueDTO.getOneStep());
                            workDaySubjectCountMap.put(workDay, subjectCountMap);
                            classNumWorkDaySubjectCountMap.put(classNum, workDaySubjectCountMap);
                            gradeClassNumWorkDaySubjectCountMap.put(grade, classNumWorkDaySubjectCountMap);

                            // 如果有占用教室，记录教室使用情况
                            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectMaxWeightDTO.getType())) {
                                var classRoomUsedCountMap = planTimeTablePrepareDTO.getClassRoomUsedCountMap();
                                var workDaytimeClassroomUsedMap = classRoomUsedCountMap.get(subjectGradeClassDTO.getSubjectId());
                                var timeClassroomUsedMap = workDaytimeClassroomUsedMap.get(workDay);
                                timeClassroomUsedMap.put(time, timeClassroomUsedMap.get(time) + 1);
                                workDaytimeClassroomUsedMap.put(workDay, timeClassroomUsedMap);
                                classRoomUsedCountMap.put(subjectMaxWeightDTO.getSubjectId(), workDaytimeClassroomUsedMap);
                            }
                        }

                        // 组装成课表
                        var classNumWorkDayTimeSubjectMap = timeTableMap.get(grade);
                        var workDayTimeSubjectMap = classNumWorkDayTimeSubjectMap.get(classNum);
                        var timeSubjectMap = workDayTimeSubjectMap.get(workDay);
                        timeSubjectMap.put(time, subjectMaxWeightDTO.getSubjectId());
                        workDayTimeSubjectMap.put(workDay, timeSubjectMap);
                        classNumWorkDayTimeSubjectMap.put(classNum, workDayTimeSubjectMap);
                        timeTableMap.put(grade, classNumWorkDayTimeSubjectMap);
                    }
                }

            }

        }


        cattyResult.setSuccess(true);
        cattyResult.setData(timeTableMap);
        return cattyResult;
    }

    /**
     * 排课核心算法
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param planTimeTablePrepareDTO
     * @return
     */
    private SubjectWeightDTO planTimeTableCore(Integer grade, Integer classNum, Integer workDay, Integer time, PlanTimeTablePrepareDTO planTimeTablePrepareDTO) {
        // 筛选出某个年级下某个班级要赋值的课程,并且初始化所有课程的权重
        var subjectWeightDTOList = this.listSubjectWeightDTO(grade, classNum, planTimeTablePrepareDTO.getGradeClassSubjectWeightMap());

        // 组装computerSubjectWeightDTO
        var computerSubjectWeightDTO = this.packComputerSubjectWeightDTO(grade, classNum, workDay, time, subjectWeightDTOList, planTimeTablePrepareDTO);

        // 计算权重
        subjectService.computerSubjectWeightDTO(computerSubjectWeightDTO);

        // 筛选出权重最大的课程
        var subjectMaxWeightDTO = subjectService.filterMaxSubjectWeightDTO(subjectWeightDTOList);

        // 组装一切就绪的DTO
        var checkAllCompleteIsOkDTO = this.packCheckAllCompleteIsOKDTO(grade,
                classNum, workDay, time, subjectMaxWeightDTO, planTimeTablePrepareDTO);
        // 查看一切是否就绪
        boolean completeFlag = this.checkAllCompleteFlag(checkAllCompleteIsOkDTO);
        while (!completeFlag) {
            // 重新计算权重
            subjectService.fixSubjectWeightDTO(computerSubjectWeightDTO.getSubjectWeightDTOList());

            // 筛选权重最大的课程
            subjectMaxWeightDTO = subjectService.filterMaxSubjectWeightDTO(subjectWeightDTOList);

            // 组装组装一切就绪的DTO
            checkAllCompleteIsOkDTO = this.packCheckAllCompleteIsOKDTO(grade,
                    classNum, workDay, time, subjectMaxWeightDTO, planTimeTablePrepareDTO);

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
                .forEach(x -> x.setWeight(x.getFrequency() * x.getType()));
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
     * @param planTimeTablePrepareDTO
     * @return
     */
    private ComputerSubjectWeightDTO packComputerSubjectWeightDTO(Integer grade,
                                                                  Integer classNum,
                                                                  Integer workDay,
                                                                  Integer time,
                                                                  List<SubjectWeightDTO> subjectWeightDTOList,
                                                                  PlanTimeTablePrepareDTO planTimeTablePrepareDTO) {
        ComputerSubjectWeightDTO computerSubjectWeightDTO = new ComputerSubjectWeightDTO();

        computerSubjectWeightDTO.setGrade(grade);
        computerSubjectWeightDTO.setClassNum(classNum);
        computerSubjectWeightDTO.setWorkDay(workDay);
        computerSubjectWeightDTO.setTime(time);
        computerSubjectWeightDTO.setSubjectWeightDTOList(subjectWeightDTOList);
        computerSubjectWeightDTO.setSubjectGradeClassTeacherCountMap(planTimeTablePrepareDTO.getSubjectGradeClassTeacherCountMap());
        computerSubjectWeightDTO.setTeacherTeachingMap(planTimeTablePrepareDTO.getTeacherTeachingMap());
        computerSubjectWeightDTO.setTeacherSubjectListMap(planTimeTablePrepareDTO.getTeacherSubjectListMap());
        computerSubjectWeightDTO.setGradeClassNumWorDaySubjectCountMap(planTimeTablePrepareDTO.getGradeClassNumWorkDaySubjectCountMap());
        computerSubjectWeightDTO.setTimeTableMap(planTimeTablePrepareDTO.getTimeTableMap());

        return computerSubjectWeightDTO;
    }

    /**
     * 组装CheckAllCompleteIsOkDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectMaxWeightDTO
     * @param planTimeTablePrepareDTO
     * @return
     */
    private CheckAllCompleteIsOkDTO packCheckAllCompleteIsOKDTO(Integer grade,
                                                                Integer classNum,
                                                                Integer workDay,
                                                                Integer time,
                                                                SubjectWeightDTO subjectMaxWeightDTO,
                                                                PlanTimeTablePrepareDTO planTimeTablePrepareDTO) {

        CheckAllCompleteIsOkDTO checkAllCompleteIsOkDTO = new CheckAllCompleteIsOkDTO();
        checkAllCompleteIsOkDTO.setGrade(grade);
        checkAllCompleteIsOkDTO.setClassNum(classNum);
        checkAllCompleteIsOkDTO.setWorkDay(workDay);
        checkAllCompleteIsOkDTO.setTime(time);
        checkAllCompleteIsOkDTO.setSubjectMaxWeightDTO(subjectMaxWeightDTO);
        checkAllCompleteIsOkDTO.setSubjectGradeClassTeacherMap(planTimeTablePrepareDTO.getSubjectGradeClassTeacherMap());
        checkAllCompleteIsOkDTO.setTeacherTeachingMap(planTimeTablePrepareDTO.getTeacherTeachingMap());
        checkAllCompleteIsOkDTO.setClassroomMaxCapacity(planTimeTablePrepareDTO.getClassroomMaxCapacityMap());
        checkAllCompleteIsOkDTO.setClassroomUsedCountMap(planTimeTablePrepareDTO.getClassRoomUsedCountMap());

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

        // 特殊课程直接OK
        if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(checkAllCompleteIsOkDTO.getSubjectMaxWeightDTO().getType())) {
            return true;
        }

        // 判断教师是否空闲
        var checkTeacherIsOkDTO = this.packCheckTeacherIsOkDTO(checkAllCompleteIsOkDTO);
        boolean teacherIsNotOK = this.checkTeacherIsOk(checkTeacherIsOkDTO);

        // 判断是否需要场地
        boolean needAreaFlag = SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType()
                .equals(checkTeacherIsOkDTO.getSubjectMaxWeightDTO().getType());
        if (!needAreaFlag) {
            return teacherIsNotOK;
        }

        // 如果是需要教室的课程，判断教室是否可以使用
        var checkClassRoomIsOkDTO = this.packCheckClassRoomIsOkDTO(checkAllCompleteIsOkDTO);
        return this.checkClassRoomIsOk(checkClassRoomIsOkDTO);
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

    private void markTeacher() {

    }
}
