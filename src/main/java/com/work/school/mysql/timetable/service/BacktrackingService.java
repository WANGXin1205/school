package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.*;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.service.dto.*;
import com.work.school.mysql.timetable.service.enums.BacktrackingTypeEnum;
import com.work.school.mysql.timetable.service.enums.CheckBackTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.AnnotationUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final double MAX = 1;
    private static final double SUP = 0.4;
    private static final double PERCENT = 1;

    private static final long STOP_TIME = 300000;
    /**
     * 接受概率
     */
    private static final BigDecimal ACCEPT_PRO = new BigDecimal("1");

    @Resource
    private SubjectService subjectService;
    @Resource
    private GeneticService geneticService;

    /**
     * 朴素回溯算法
     *
     * @param prepareDTO
     * @return
     */
    public CattyResult<TreeMap<Integer, Integer>> forwardBacktracking(PrepareDTO prepareDTO) {
        CattyResult<TreeMap<Integer, Integer>> cattyResult = new CattyResult<>();

        // 评分信息
        List<String> messageList = new ArrayList<>();
        // 停机条件
        long start = System.currentTimeMillis();

        var timeTableMap = prepareDTO.getTimeTableMap();
        var orderSubjectIdCanUseMap = prepareDTO.getOrderSubjectIdCanUseMap();
        var orderGradeClassNumWorkDayTimeMap = prepareDTO.getOrderGradeClassNumWorkDayTimeMap();
        var subjectClassTeacherDOList = prepareDTO.getSubjectClassTeacherDOList();
        var teacherOrderListMap = prepareDTO.getTeacherOrderListMap();
        var subjectConstraintDTOList = prepareDTO.getSubjectConstraintDTOList();
        var allSubjectMap = prepareDTO.getAllSubjectMap();
        for (int order = SchoolTimeTableDefaultValueDTO.getStartOrder(); order <= orderGradeClassNumWorkDayTimeMap.keySet().size(); order++) {

            while (timeTableMap.get(order) == null) {
                // 先更新可用科目
                var subjectCanUseMap = orderSubjectIdCanUseMap.get(order);

                this.updateSubjectIdCanUseMap(subjectConstraintDTOList, orderSubjectIdCanUseMap);

                // 选择课程
                CheckBackTypeEnum checkBackTypeEnum = this.checkBack(order, orderSubjectIdCanUseMap);
                if (CheckBackTypeEnum.GOOD.equals(checkBackTypeEnum)) {

                    Integer subjectId = null;
                    if (BacktrackingTypeEnum.FC_BA.equals(prepareDTO.getBacktrackingTypeEnum())) {
                        // 前行检测回溯算法
                        subjectId = this.chooseFirstSubject(subjectCanUseMap);
                    }
                    if (BacktrackingTypeEnum.FC_DW_BA.equals(prepareDTO.getBacktrackingTypeEnum())) {
                        // 前行检测和动态权重回溯算法
                        subjectId = this.chooseMaxWeightSubject(order, prepareDTO);
                    }

                    // 标记
                    this.addSubjectConstraint(subjectConstraintDTOList, order, subjectId);

                    // 冲突检测
                    CheckMatchDTO checkMatchDTO = this.packCheckMatchDTO(subjectId, order, prepareDTO.getAllSubjectMap(),
                            orderGradeClassNumWorkDayTimeMap, prepareDTO.getGradeClassNumWorkDayTimeOrderMap(),
                            prepareDTO.getClassroomMaxCapacityMap(), teacherOrderListMap, subjectClassTeacherDOList, timeTableMap);
                    Boolean matchFlag = this.checkMatch(checkMatchDTO);
                    if (matchFlag) {
                        // 满足赋值
                        UpdateStatusDTO updateStatusDTO = this.packUpdateStatusDTO(checkMatchDTO);
                        this.updateStatus(updateStatusDTO);
                        // 添加约束
                        AddSubjectConstraintDTO addSubjectConstraintDTO = this.packAddSubjectConstraintDTO(order, subjectId,
                                subjectConstraintDTOList, allSubjectMap, subjectClassTeacherDOList, teacherOrderListMap,
                                orderGradeClassNumWorkDayTimeMap, prepareDTO.getGradeClassNumWorkDayTimeOrderMap(), timeTableMap);
                        subjectConstraintDTOList = this.addSubjectConstraint(addSubjectConstraintDTO);
                    }
                }
                if (CheckBackTypeEnum.BAD_NOW.equals(checkBackTypeEnum)) {

                    // 回溯节点判断
                    order = this.getBackOrder(order, orderSubjectIdCanUseMap);
                    // 回溯
                    this.rollback(order, prepareDTO);
                    // 清除约束
                    subjectConstraintDTOList = this.clearSubjectConstraintDTO(order, subjectConstraintDTOList);

                }
                if (CheckBackTypeEnum.BAD_NEXT.equals(checkBackTypeEnum)) {
                    // todo 重来没有进来
                    order = order - 1;
//                    this.addSubjectConstraint(subjectConstraintDTOList, order, subjectId);
                }


                // 评分
                var fitnessScoreDTO = this.computerFitnessScore(prepareDTO);
                String message = this.packMessage(fitnessScoreDTO);
                messageList.add(message);
                long end = System.currentTimeMillis();
                if (end - start > STOP_TIME) {
                    break;
                }
            }
        }

        geneticService.markToTXT(String.valueOf(start), messageList);

        cattyResult.setData(timeTableMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查是否需要回溯
     *
     * @param order
     * @param orderSubjectIdCanUseMap
     * @return
     */
    private CheckBackTypeEnum checkBack(Integer order, HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        boolean backFlag;
        for (Integer checkOrder : orderSubjectIdCanUseMap.keySet()) {
            if (checkOrder.equals(order)) {
                var checkOrderSubjectCanUseMap = orderSubjectIdCanUseMap.get(checkOrder);
                backFlag = checkOrderSubjectCanUseMap.values().stream().allMatch(x -> x.equals(Boolean.FALSE));
                if (backFlag) {
                    return CheckBackTypeEnum.BAD_NOW;
                }
            }
            if (checkOrder > order) {
                var checkOrderSubjectCanUseMap = orderSubjectIdCanUseMap.get(checkOrder);
                backFlag = checkOrderSubjectCanUseMap.values().stream().allMatch(x -> x.equals(Boolean.FALSE));
                if (backFlag) {
                    return CheckBackTypeEnum.BAD_NEXT;
                }
            }
        }

        return CheckBackTypeEnum.GOOD;
    }


    /**
     * 清除约束
     *
     * @param order
     * @param subjectConstraintDTOList
     */
    private List<SubjectConstraintDTO> clearSubjectConstraintDTO(Integer order,
                                                                 List<SubjectConstraintDTO> subjectConstraintDTOList) {

        // 清除该order后的约束
        List<SubjectConstraintDTO> saveSubjectConstraintDTOList = new ArrayList<>();
        for (SubjectConstraintDTO subjectConstraintDTO : subjectConstraintDTOList) {
            if (order > subjectConstraintDTO.getOrder()
                    || (order.equals(subjectConstraintDTO.getOrder()) && order.equals(subjectConstraintDTO.getOrderConstraint()))) {
                saveSubjectConstraintDTOList.add(subjectConstraintDTO);
            }
        }

        return saveSubjectConstraintDTOList;
    }

    /**
     * 组装添加约束
     *
     * @param order
     * @param subjectId
     * @param subjectConstraintDTOList
     * @param allSubjectMap
     * @param subjectClassTeacherDOList
     * @param teacherOrderListMap
     * @param orderGradeClassNumWorkDayTimeMap
     * @param gradeClassNumWorkDayTimeOrderMap
     * @return
     */
    private AddSubjectConstraintDTO packAddSubjectConstraintDTO(Integer order,
                                                                Integer subjectId,
                                                                List<SubjectConstraintDTO> subjectConstraintDTOList,
                                                                Map<Integer, SubjectDO> allSubjectMap,
                                                                List<SubjectClassTeacherDO> subjectClassTeacherDOList,
                                                                HashMap<Integer, List<Integer>> teacherOrderListMap,
                                                                HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap,
                                                                HashMap<GradeClassNumWorkDayTimeDTO, Integer> gradeClassNumWorkDayTimeOrderMap,
                                                                TreeMap<Integer, Integer> timeTableMap) {

        AddSubjectConstraintDTO addSubjectConstraintDTO = new AddSubjectConstraintDTO();
        addSubjectConstraintDTO.setOrder(order);
        addSubjectConstraintDTO.setSubjectId(subjectId);
        addSubjectConstraintDTO.setSubjectConstraintDTOList(subjectConstraintDTOList);
        addSubjectConstraintDTO.setAllSubjectMap(allSubjectMap);
        addSubjectConstraintDTO.setSubjectClassTeacherDOList(subjectClassTeacherDOList);
        addSubjectConstraintDTO.setTeacherOrderListMap(teacherOrderListMap);
        addSubjectConstraintDTO.setOrderGradeClassNumWorkDayTimeMap(orderGradeClassNumWorkDayTimeMap);
        addSubjectConstraintDTO.setGradeClassNumWorkDayTimeOrderMap(gradeClassNumWorkDayTimeOrderMap);
        addSubjectConstraintDTO.setTimetableMap(timeTableMap);

        return addSubjectConstraintDTO;
    }

    /**
     * 增加约束
     *
     * @param subjectConstraintDTOList
     * @param order
     * @param subjectId
     */
    private void addSubjectConstraint(List<SubjectConstraintDTO> subjectConstraintDTOList,
                                      Integer order, Integer subjectId) {

        // 1.选择的课程不能再选，添加约束
        SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
        subjectConstraintDTO.setOrder(order);
        subjectConstraintDTO.setOrderConstraint(order);
        subjectConstraintDTO.setSubjectIdConstraint(subjectId);
        boolean repeatFlag = subjectConstraintDTOList.stream().anyMatch(x -> x.getOrderConstraint().equals(order)
                && x.getSubjectIdConstraint().equals(subjectId));
        if (!repeatFlag) {
            subjectConstraintDTOList.add(subjectConstraintDTO);
        }
    }

    /**
     * 添加约束
     *
     * @param addSubjectConstraintDTO
     */
    private List<SubjectConstraintDTO> addSubjectConstraint(AddSubjectConstraintDTO addSubjectConstraintDTO) {

        var order = addSubjectConstraintDTO.getOrder();
        var subjectId = addSubjectConstraintDTO.getSubjectId();
        var subjectConstraintDTOList = addSubjectConstraintDTO.getSubjectConstraintDTOList();

        var gradeClassNumWorkDayTimeOrderMap = addSubjectConstraintDTO.getGradeClassNumWorkDayTimeOrderMap();
        var orderGradeClassNumWorkDayTimeMap = addSubjectConstraintDTO.getOrderGradeClassNumWorkDayTimeMap();

        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        // 获取每周每班对应的节点
        List<Integer> workDayOrderList = new ArrayList<>();
        // 获取每天每班对应的节点
        List<Integer> timeOrderList = new ArrayList<>();
        for (int i = 1; i <= SchoolTimeTableDefaultValueDTO.getWorkDay(); i++) {
            for (int j = 1; j <= SchoolTimeTableDefaultValueDTO.getClassTime(); j++) {
                GradeClassNumWorkDayTimeDTO key = new GradeClassNumWorkDayTimeDTO();
                key.setGrade(grade);
                key.setClassNum(classNum);
                key.setWorkDay(i);
                key.setTime(j);
                var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
                // 获取每天每班对应的节点
                if (workDay.equals(i)) {
                    timeOrderList.add(matchOrder);
                }
                workDayOrderList.add(matchOrder);
            }
        }

        // 2.添加课程如果次数为0，后续该班级的该课程也清除为0
        var subjectClassTeacherDOList = addSubjectConstraintDTO.getSubjectClassTeacherDOList();
        var zeroFlag = subjectClassTeacherDOList.stream().anyMatch(x -> x.getSubjectId().equals(subjectId)
                && x.getGrade().equals(grade) && x.getClassNum().equals(classNum) && x.getFrequency().equals(0));
        if (zeroFlag) {
            for (Integer matchOrder : workDayOrderList) {
                if (matchOrder > order) {
                    SubjectConstraintDTO matchSubjectConstraintDTO = new SubjectConstraintDTO();
                    matchSubjectConstraintDTO.setOrder(order);
                    matchSubjectConstraintDTO.setOrderConstraint(matchOrder);
                    matchSubjectConstraintDTO.setSubjectIdConstraint(subjectId);
                    var repeatFlag = subjectConstraintDTOList.stream().anyMatch(x -> x.getOrderConstraint().equals(matchOrder)
                            && x.getSubjectIdConstraint().equals(subjectId));
                    if (!repeatFlag) {
                        subjectConstraintDTOList.add(matchSubjectConstraintDTO);
                    }
                }
            }
        }

        // 3.如果当天上过这种类型的小课，那么当天的该类型小课在该班清零
        var allSubjectMap = addSubjectConstraintDTO.getAllSubjectMap();
        var subjectDO = allSubjectMap.get(subjectId);
        boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectDO.getType())
                || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDO.getType());
        if (otherSubjectFlag) {
            for (Integer matchOrder : timeOrderList) {
                if (order < matchOrder) {
                    SubjectConstraintDTO constraintDTO = new SubjectConstraintDTO();
                    constraintDTO.setOrder(order);
                    constraintDTO.setOrderConstraint(matchOrder);
                    constraintDTO.setSubjectIdConstraint(subjectId);
                    if (!subjectConstraintDTOList.contains(constraintDTO)) {
                        subjectConstraintDTOList.add(constraintDTO);
                    }
                }
            }
        }

        // 4.当前节点对应教师 不能再次出现在其他可选择节点
        var teacherDOList = subjectClassTeacherDOList.stream().filter(x -> x.getSubjectId().equals(subjectId)
                && x.getGrade().equals(grade) && x.getClassNum().equals(classNum)).map(SubjectClassTeacherDO::getTeacherId).collect(Collectors.toList());
        Integer teacherId = teacherDOList.get(0);
        var matchSubjectClassTeacherDOList = subjectClassTeacherDOList.stream()
                .filter(x -> x.getTeacherId() != null && x.getTeacherId().equals(teacherId)).collect(Collectors.toList());
        HashMap<Integer, List<Integer>> matchOrderSubjectMap = new HashMap<>();
        for (SubjectClassTeacherDO matchSubjectClassTeacherDO : matchSubjectClassTeacherDOList) {
            GradeClassNumWorkDayTimeDTO matchGradeClassNumWorkDayTimeDTO = new GradeClassNumWorkDayTimeDTO();
            matchGradeClassNumWorkDayTimeDTO.setGrade(matchSubjectClassTeacherDO.getGrade());
            matchGradeClassNumWorkDayTimeDTO.setClassNum(matchSubjectClassTeacherDO.getClassNum());
            matchGradeClassNumWorkDayTimeDTO.setWorkDay(gradeClassNumWorkDayTimeDTO.getWorkDay());
            matchGradeClassNumWorkDayTimeDTO.setTime(gradeClassNumWorkDayTimeDTO.getTime());

            var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(matchGradeClassNumWorkDayTimeDTO);
            List<Integer> subjectList = matchOrderSubjectMap.get(matchOrder);
            if (CollectionUtils.isEmpty(subjectList)) {
                subjectList = new ArrayList<>();
            }
            if (!subjectList.contains(matchSubjectClassTeacherDO.getSubjectId())) {
                subjectList.add(matchSubjectClassTeacherDO.getSubjectId());
            }
            matchOrderSubjectMap.put(matchOrder, subjectList);
        }

        for (Integer key : matchOrderSubjectMap.keySet()) {
            if (key > order) {
                var matchSubjectList = matchOrderSubjectMap.get(key);
                for (Integer matchSubjectId : matchSubjectList) {
                    SubjectConstraintDTO constraintDTO = new SubjectConstraintDTO();
                    constraintDTO.setOrder(order);
                    constraintDTO.setOrderConstraint(key);
                    constraintDTO.setSubjectIdConstraint(matchSubjectId);
                    if (!subjectConstraintDTOList.contains(constraintDTO)) {
                        subjectConstraintDTOList.add(constraintDTO);
                    }
                }
            }
        }

        // 5.保证每天都要有主课上 影响节点少

        // 6.每门主课不能一天不能超过3节课(上限为2）影响节点少
//        var timeTableMap = addSubjectConstraintDTO.getTimetableMap();
//        HashMap<Integer, Integer> mainSubjectCountMap = new HashMap<>();
//        for (Integer matchOrder : timeOrderList) {
//            var matchSubjectId = timeTableMap.get(matchOrder);
//            if (matchSubjectId != null) {
//                var matchSubjectDO = allSubjectMap.get(matchSubjectId);
//                if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(matchSubjectDO.getType())) {
//                    Integer count = mainSubjectCountMap.get(matchSubjectDO.getId());
//                    if (count == null) {
//                        count = 0;
//                    }
//                    count = count + 1;
//                    mainSubjectCountMap.put(matchSubjectDO.getId(), count);
//                }
//            }
//            if (matchOrder > order) {
//                for (Integer key : mainSubjectCountMap.keySet()) {
//                    var count = mainSubjectCountMap.get(key);
//                    if (count > SubjectDefaultValueDTO.getTwoCount()) {
//                        SubjectConstraintDTO constraintDTO = new SubjectConstraintDTO();
//                        constraintDTO.setOrder(order);
//                        constraintDTO.setOrderConstraint(matchOrder);
//                        constraintDTO.setSubjectIdConstraint(key);
//                        if (!subjectConstraintDTOList.contains(constraintDTO)) {
//                            subjectConstraintDTOList.add(constraintDTO);
//                        }
//                    }
//                }
//
//            }
//        }


        // 7.教室的影响

        return subjectConstraintDTOList;
    }

    /**
     * 朴素回溯算法和动态权重回溯算法
     *
     * @param prepareDTO
     * @return
     */
    public CattyResult<TreeMap<Integer, Integer>> backtracking(PrepareDTO prepareDTO) {
        CattyResult<TreeMap<Integer, Integer>> cattyResult = new CattyResult<>();

        // 评分信息
        List<String> messageList = new ArrayList<>();
        long start = System.currentTimeMillis();

        var timeTableMap = prepareDTO.getTimeTableMap();
        var orderSubjectIdCanUseMap = prepareDTO.getOrderSubjectIdCanUseMap();
        var orderGradeClassNumWorkDayTimeMap = prepareDTO.getOrderGradeClassNumWorkDayTimeMap();
        var subjectClassTeacherDOList = prepareDTO.getSubjectClassTeacherDOList();
        var teacherOrderListMap = prepareDTO.getTeacherOrderListMap();
        for (int order = SchoolTimeTableDefaultValueDTO.getStartOrder(); order <= orderGradeClassNumWorkDayTimeMap.keySet().size(); order++) {

            while (timeTableMap.get(order) == null) {
                // 先更新可用科目
                var subjectCanUseMap = orderSubjectIdCanUseMap.get(order);
                var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);

                this.updateSubjectIdCanUseMap(subjectCanUseMap, gradeClassNumWorkDayTimeDTO, subjectClassTeacherDOList);

                // 选择课程
                boolean backFlag = subjectCanUseMap.values().stream().allMatch(x -> x.equals(Boolean.FALSE));
                if (backFlag) {
                    // 回溯节点判断
                    order = this.getBackOrder(order, orderSubjectIdCanUseMap);
                    // 回溯
                    this.rollback(order, prepareDTO);
                }
                Integer subjectId = null;
                if (BacktrackingTypeEnum.BA.equals(prepareDTO.getBacktrackingTypeEnum())) {
                    // 朴素回溯算法
                    subjectId = this.chooseFirstSubject(subjectCanUseMap);
                }
                if (BacktrackingTypeEnum.DW_BA.equals(prepareDTO.getBacktrackingTypeEnum())) {
                    // 动态权重
                    subjectId = this.chooseMaxWeightSubject(order, prepareDTO);
                }

                // 标记
                subjectCanUseMap.put(subjectId, false);

                // 冲突检测
                CheckMatchDTO checkMatchDTO = this.packCheckMatchDTO(subjectId, order, prepareDTO.getAllSubjectMap(),
                        orderGradeClassNumWorkDayTimeMap, prepareDTO.getGradeClassNumWorkDayTimeOrderMap(),
                        prepareDTO.getClassroomMaxCapacityMap(), teacherOrderListMap, subjectClassTeacherDOList, timeTableMap);
                Boolean matchFlag = this.checkMatch(checkMatchDTO);
                if (matchFlag) {
                    // 满足赋值
                    UpdateStatusDTO updateStatusDTO = this.packUpdateStatusDTO(checkMatchDTO);
                    this.updateStatus(updateStatusDTO);
                } else {
                    // 检查是否需要回溯
                    backFlag = subjectCanUseMap.values().stream().allMatch(x -> x.equals(Boolean.FALSE));
                    if (backFlag) {
                        // 回溯节点判断
                        order = this.getBackOrder(order, orderSubjectIdCanUseMap);
                        // 回溯
                        this.rollback(order, prepareDTO);
                    }
                }
                // 评分
                var fitnessScoreDTO = this.computerFitnessScore(prepareDTO);
                String message = this.packMessage(fitnessScoreDTO);
                messageList.add(message);

                long end = System.currentTimeMillis();
                if (end - start > STOP_TIME){
                    break;
                }
            }
        }

        geneticService.markToTXT(String.valueOf(start), messageList);

        cattyResult.setData(timeTableMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 选择权重最大的课程
     *
     * @param order
     * @param prepareDTO
     * @return
     */
    private Integer chooseMaxWeightSubject(Integer order, PrepareDTO prepareDTO) {

        var orderSubjectIdCanUseMap = prepareDTO.getOrderSubjectIdCanUseMap();
        var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(order);

        // 获取排课时间
        var orderGradeClassNumWorkDayTimeMap = prepareDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        // 获取可选择的课程
        var allSubjectMap = prepareDTO.getAllSubjectMap();
        var subjectClassTeacherDOList = prepareDTO.getSubjectClassTeacherDOList();
        var subjectFrequencyMap = subjectClassTeacherDOList.stream()
                .filter(x -> x.getGrade().equals(grade) && x.getClassNum().equals(classNum))
                .collect(Collectors.toMap(SubjectClassTeacherDO::getSubjectId, SubjectClassTeacherDO::getFrequency));
        List<SubjectWeightDTO> subjectWeightDTOList = new ArrayList<>();
        for (Integer subjectId : subjectIdCanUseMap.keySet()) {
            boolean matchFlag = subjectIdCanUseMap.get(subjectId);
            if (matchFlag) {
                var subjectDO = allSubjectMap.get(subjectId);
                int frequency = subjectFrequencyMap.get(subjectId);
                int initWeight = frequency * (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType() - subjectDO.getType());

                SubjectWeightDTO subjectWeightDTO = new SubjectWeightDTO();
                subjectWeightDTO.setSubjectId(subjectId);
                subjectWeightDTO.setType(subjectDO.getType());
                subjectWeightDTO.setFrequency(frequency);
                subjectWeightDTO.setWeight(initWeight);
                subjectWeightDTOList.add(subjectWeightDTO);
            }
        }

        // 特殊课程的选择
        boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(time);
        boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
        boolean schoolBasedFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(workDay) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
        boolean specialFlag = classMeetingFlag || writingFlag || schoolBasedFlag;
        for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
            if (classMeetingFlag && SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectWeightDTO.getSubjectId())) {
                subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            if (writingFlag && SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectWeightDTO.getSubjectId())) {
                subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            if (schoolBasedFlag && SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectWeightDTO.getSubjectId())) {
                subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            boolean specialIdFlag = SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectWeightDTO.getSubjectId())
                    || SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectWeightDTO.getSubjectId())
                    || SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectWeightDTO.getSubjectId());
            if (!specialFlag && specialIdFlag) {
                subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
            }
        }

        // 早上第1、2节课程必须是主课，且最好不同
        var timeTableMap = prepareDTO.getTimeTableMap();
        if (SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time)) {
            for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectWeightDTO.getType())) {
                    int weight = subjectWeightDTO.getWeight() + SubjectWeightDefaultValueDTO.getExtendWeight();
                    subjectWeightDTO.setWeight(weight);
                }
            }
        }
        if (SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time)) {
            var pastOrder = order - 1;
            var pastSubjectId = timeTableMap.get(pastOrder);
            if (SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(pastSubjectId)) {
                for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                    if (SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(subjectWeightDTO.getSubjectId())) {
                        int weight = subjectWeightDTO.getWeight() + SubjectWeightDefaultValueDTO.getExtendWeight();
                        subjectWeightDTO.setWeight(weight);
                    }
                }
            }
            if (SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(pastSubjectId)) {
                for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                    if (SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(subjectWeightDTO.getSubjectId())) {
                        int weight = subjectWeightDTO.getWeight() + SubjectWeightDefaultValueDTO.getExtendWeight();
                        subjectWeightDTO.setWeight(weight);
                    }
                }
            }
        }
        // 如果过了2节课，适当削弱主课权重，并增加小课权重
        boolean morningFirstOrSecFlag = SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time)
                || SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time);
        if (!morningFirstOrSecFlag) {
            for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectWeightDTO.getType())) {
                    var weight = subjectWeightDTO.getWeight() + SubjectWeightDefaultValueDTO.getExtendWeight();
                    subjectWeightDTO.setWeight(weight);
                }
            }
        }

        // 需要教室的课程优先排课
        var random = Math.random() * (SUP + (MAX - SUP) * (timeTableMap.size() - order) / timeTableMap.size());
        var classroomMaxCapacityMap = prepareDTO.getClassroomMaxCapacityMap();
        for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectWeightDTO.getType())) {
                var maxCapacity = classroomMaxCapacityMap.get(subjectWeightDTO.getSubjectId());
                int weight = 0;
                if (order < timeTableMap.size() * PERCENT) {
                    weight = subjectWeightDTO.getWeight() + (int) (random * SubjectWeightDefaultValueDTO.getExtendWeight()
                            * (SchoolTimeTableDefaultValueDTO.getClassroomMaxCount() - maxCapacity));
                } else {
                    weight = subjectWeightDTO.getWeight() + SubjectWeightDefaultValueDTO.getExtendWeight()
                            * (SchoolTimeTableDefaultValueDTO.getClassroomMaxCount() - maxCapacity);
                }
                subjectWeightDTO.setWeight(weight);
            }
        }

        GradeClassNumWorkDayTimeDTO key = new GradeClassNumWorkDayTimeDTO();
        key.setGrade(grade);
        key.setClassNum(classNum);
        key.setWorkDay(workDay);
        var gradeClassNumWorkDayTimeOrderMap = prepareDTO.getGradeClassNumWorkDayTimeOrderMap();
        List<Integer> matchOrderList = new ArrayList<>();
        for (int i = 1; i <= SchoolTimeTableDefaultValueDTO.getClassTime(); i++) {
            key.setTime(i);
            Integer matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
            matchOrderList.add(matchOrder);
        }
        TreeMap<Integer, Integer> matchOrderSubjectMap = new TreeMap<>();
        for (Integer matchOrder : matchOrderList) {
            var matchSubjectId = timeTableMap.get(matchOrder);
            if (matchSubjectId != null) {
                matchOrderSubjectMap.put(matchOrder, matchSubjectId);
            }
        }

        if (CollectionUtils.isNotEmpty(matchOrderSubjectMap.values())) {
            // 如果当天排了小课
            for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                boolean matchFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectWeightDTO.getType())
                        || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectWeightDTO.getType());
                if (matchFlag && matchOrderSubjectMap.containsValue(subjectWeightDTO.getSubjectId())) {
                    subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getZeroFrequency());
                }
            }

            // 如果学生出现连堂课
            for (Integer[] times : SchoolTimeTableDefaultValueDTO.getStudentContinueTimes()) {
                Integer firstTime = times[0];
                Integer secTime = times[1];
                Integer firstSubjectId = matchOrderSubjectMap.get(firstTime);
                if (firstSubjectId != null && time.equals(secTime)) {
                    for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectWeightDTO.getType())
                                && firstSubjectId.equals(subjectWeightDTO.getSubjectId())) {
                            subjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
                        }
                    }
                }

            }

        }

        // 教师带的班级多的课程，优先排课 基本没啥用
//        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherGradeClassMap = new HashMap<>();
//        for (SubjectClassTeacherDO subjectClassTeacherDO : subjectClassTeacherDOList) {
//            if (subjectClassTeacherDO.getTeacherId() != null) {
//                var gradeClassMap = teacherGradeClassMap.get(subjectClassTeacherDO.getTeacherId());
//                if (gradeClassMap == null) {
//                    gradeClassMap = new HashMap<>();
//                }
//                List<Integer> classList = gradeClassMap.get(subjectClassTeacherDO.getGrade());
//                if (CollectionUtils.isEmpty(classList)) {
//                    classList = new ArrayList<>();
//                }
//                if (!classList.contains(subjectClassTeacherDO.getClassNum())) {
//                    classList.add(subjectClassTeacherDO.getClassNum());
//                }
//                gradeClassMap.put(subjectClassTeacherDO.getGrade(), classList);
//                teacherGradeClassMap.put(subjectClassTeacherDO.getTeacherId(), gradeClassMap);
//            }
//        }
//
//        HashMap<Integer, Integer> teacherClassCountMap = new HashMap<>();
//        int maxClassCount = 0;
//        for (Integer teacherId : teacherGradeClassMap.keySet()) {
//            var gradeClassCountMap = teacherGradeClassMap.get(teacherId);
//            int count = 0;
//            for (Integer x : gradeClassCountMap.keySet()) {
//                var classList = gradeClassCountMap.get(x);
//                count = count + classList.size();
//            }
//            if (count > maxClassCount) {
//                maxClassCount = count;
//            }
//            teacherClassCountMap.put(teacherId, count);
//        }
//
//        Map<Integer, Integer> subjectTeacherMap = subjectClassTeacherDOList.stream()
//                .filter(x -> x.getGrade().equals(grade) && x.getClassNum().equals(classNum) && x.getTeacherId() != null)
//                .collect(Collectors.toMap(SubjectClassTeacherDO::getSubjectId, SubjectClassTeacherDO::getTeacherId));
//
//        random = Math.random() * (SUP + (MAX - SUP) * (timeTableMap.size() - order) / timeTableMap.size());
//        for (SubjectWeightDTO subjectWeightDTO:subjectWeightDTOList){
//            var subjectId = subjectWeightDTO.getSubjectId();
//            Integer teacherId = subjectTeacherMap.get(subjectId);
//            if (teacherId != null){
//                var classCount = teacherClassCountMap.get(teacherId);
//                int weight = subjectWeightDTO.getWeight()+(int) (random * classCount/maxClassCount * 2);
//                subjectWeightDTO.setWeight(weight);
//            }
//        }

        var bestSubjectWeightDTO = new SubjectWeightDTO();
        bestSubjectWeightDTO.setWeight(SubjectWeightDefaultValueDTO.getStopWeight());
        for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
            if (subjectWeightDTO.getWeight() > bestSubjectWeightDTO.getWeight()) {
                bestSubjectWeightDTO = subjectWeightDTO;
            }
        }
        return bestSubjectWeightDTO.getSubjectId();
    }

    /**
     * 计算评分
     *
     * @param prepareDTO
     * @return
     */
    private FitnessScoreDTO computerFitnessScore(PrepareDTO prepareDTO) {

        var orderGradeClassNumWorkDayTimeMap = prepareDTO.getOrderGradeClassNumWorkDayTimeMap();
        var timeTableMap = prepareDTO.getTimeTableMap();
        var allSubjectMap = prepareDTO.getAllSubjectMap();

        // 1.任何时刻每个班都上课
        int everyTimeHaveSubjectCount = 0;
        // 2.同一时间一个班级上了多节课 回溯算法无意义
        int oneClassMoreSubjectCount = 0;
        // 3.同一时间一个教师上了多个班级的课程 回溯算法无意义
        int oneTeacherMoreClassCount = 0;
        // 4.固定时间上固定的课程
        int fixedSubjectIdCount = 0;
        // 5.小课一天最多只上一节小课 回溯算法无太大意义
        int justOneOtherSubjectCount = 0;
        // 6.功能部室不能超过最大班级数 回溯算法无太大意义
        int needAreaSubjectCount = 0;
        // 软约束
        // 2.第1，2节课必须是语文，数学课
        int noMainSubjectCount = 0;
        // 3.体育课不在第4节
        int sportNoFinalCount = 0;
        // 4.围棋课最好在下午
        int goTimeMorningCount = 0;
        // 5.连堂课
        HashMap<Integer, HashMap<GradeClassWorkDayDTO, List<Integer>>> subjectGradeClassWorkDayTimesMap = new HashMap<>();
        for (Integer order : timeTableMap.keySet()) {
            // 计算任何时刻每个班都上课
            var subjectId = timeTableMap.get(order);
            if (subjectId == null) {
                everyTimeHaveSubjectCount = everyTimeHaveSubjectCount + 1;
            }

            // 计算固定时间上固定的课程
            var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
            var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
            var time = gradeClassNumWorkDayTimeDTO.getTime();
            boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(workDay)
                    && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(time);
            if (classMeetingFlag) {
                if (!SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectId)) {
                    fixedSubjectIdCount = fixedSubjectIdCount + 1;
                }
            }
            boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(workDay)
                    && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
            if (writingFlag) {
                if (!SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectId)) {
                    fixedSubjectIdCount = fixedSubjectIdCount + 1;
                }
            }
            boolean schoolBasedFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(workDay)
                    && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
            if (schoolBasedFlag) {
                if (!SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
                    fixedSubjectIdCount = fixedSubjectIdCount + 1;
                }
            }

            // 计算第1，2节课必须是主课 只针对排课的
            if (time < SchoolTimeTableDefaultValueDTO.getMorningLastTime() && subjectId != null) {
                var subjectDO = allSubjectMap.get(subjectId);
                if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDO.getType())) {
                    noMainSubjectCount = noMainSubjectCount + 1;
                }
            }

            // 计算体育课是否在第4节的次数
            if (SchoolTimeTableDefaultValueDTO.getAfternoonFirTime().equals(time) && SchoolTimeTableDefaultValueDTO.getSubjectSportId().equals(subjectId)) {
                sportNoFinalCount = sportNoFinalCount + 1;
            }

            // 计算围棋课在上午的次数
            if (time < SchoolTimeTableDefaultValueDTO.getAfternoonFirTime() && SchoolTimeTableDefaultValueDTO.getSubjectGoId().equals(subjectId)) {
                goTimeMorningCount = goTimeMorningCount + 1;
            }

            // 计算学生连堂课的次数
            if (subjectId != null) {
                var subjectDO = allSubjectMap.get(subjectId);
                if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDO.getType())) {
                    var gradeClassWorkDayTimesMap = subjectGradeClassWorkDayTimesMap.get(subjectId);
                    GradeClassWorkDayDTO gradeClassWorkDayDTO = new GradeClassWorkDayDTO();
                    gradeClassWorkDayDTO.setGrade(gradeClassNumWorkDayTimeDTO.getGrade());
                    gradeClassWorkDayDTO.setClassNum(gradeClassNumWorkDayTimeDTO.getClassNum());
                    gradeClassWorkDayDTO.setWorkDay(workDay);

                    List<Integer> timeList;
                    if (gradeClassWorkDayTimesMap == null) {
                        timeList = new ArrayList<>();
                        timeList.add(time);
                        gradeClassWorkDayTimesMap = new HashMap<>();
                    } else {
                        timeList = gradeClassWorkDayTimesMap.get(gradeClassWorkDayDTO);
                        if (CollectionUtils.isEmpty(timeList)) {
                            timeList = new ArrayList<>();
                        }
                        timeList.add(time);
                    }
                    gradeClassWorkDayTimesMap.put(gradeClassWorkDayDTO, timeList);
                    subjectGradeClassWorkDayTimesMap.put(subjectId, gradeClassWorkDayTimesMap);
                }
            }

        }

        // 1.教师每天上课不能超过4节课
        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherWorkDayTimeMap = new HashMap<>();
        var teacherOrderListMap = prepareDTO.getTeacherOrderListMap();
        for (Integer teacherId : teacherOrderListMap.keySet()) {
            HashMap<Integer, List<Integer>> workDayTimeMap = new HashMap<>();
            var orderList = teacherOrderListMap.get(teacherId);
            for (Integer order : orderList) {
                var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
                var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
                var time = gradeClassNumWorkDayTimeDTO.getTime();
                var timeList = workDayTimeMap.get(workDay);
                if (CollectionUtils.isEmpty(timeList)) {
                    timeList = new ArrayList<>();
                }
                timeList.add(time);
                workDayTimeMap.put(workDay, timeList);
            }
            teacherWorkDayTimeMap.put(teacherId, workDayTimeMap);
        }

        // 1.教师每天上课不能超过4节课
        int teacherTiredCount = 0;
        for (Integer teacherId : teacherWorkDayTimeMap.keySet()) {
            var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
            for (Integer workDay : workDayTimeMap.keySet()) {
                var timeList = workDayTimeMap.get(workDay);
                if (timeList.size() > SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize()) {
                    teacherTiredCount = teacherTiredCount + 1;
                }
            }
        }

        // 5.学生不能上连堂课
        int studentContinueSameClassCount = 0;
        for (Integer subjectId : subjectGradeClassWorkDayTimesMap.keySet()) {
            var gradeClassWorkDayTimesMap = subjectGradeClassWorkDayTimesMap.get(subjectId);
            for (GradeClassWorkDayDTO gradeClassWorkDayDTO : gradeClassWorkDayTimesMap.keySet()) {
                var timeList = gradeClassWorkDayTimesMap.get(gradeClassWorkDayDTO);

                for (Integer[] continueTime : SchoolTimeTableDefaultValueDTO.getStudentContinueTimes()) {
                    Integer firstTime = continueTime[0];
                    Integer secTime = continueTime[1];
                    if (timeList.contains(firstTime) && timeList.contains(secTime)) {
                        studentContinueSameClassCount = studentContinueSameClassCount + 1;
                    }
                }
            }
        }

        // 先计算硬约束冲突评分
        int hardScore = everyTimeHaveSubjectCount + oneClassMoreSubjectCount + oneTeacherMoreClassCount
                + fixedSubjectIdCount + justOneOtherSubjectCount + needAreaSubjectCount;
        hardScore = hardScore * BIG_SCORE;

        // 再计算软约束评分
        int noBestTimeBestClassCount = noMainSubjectCount + sportNoFinalCount;
        int softScore = (teacherTiredCount + noBestTimeBestClassCount + goTimeMorningCount + studentContinueSameClassCount) * ADD_SCORE;

        // 最后计算一共的得分
        int score = hardScore + softScore;

        FitnessScoreDTO fitnessScoreDTO = new FitnessScoreDTO();
        fitnessScoreDTO.setHardScore(hardScore);
        fitnessScoreDTO.setSoftScore(softScore);
        fitnessScoreDTO.setScore(score);
        fitnessScoreDTO.setEveryTimeHaveSubjectCount(everyTimeHaveSubjectCount);
        fitnessScoreDTO.setOneTimeOneClassMoreSubjectCount(oneClassMoreSubjectCount);
        fitnessScoreDTO.setOneTimeOneTeacherMoreClassCount(oneTeacherMoreClassCount);
        fitnessScoreDTO.setFixedSubjectIdCount(fixedSubjectIdCount);
        fitnessScoreDTO.setOneClassMoreOtherSubject(justOneOtherSubjectCount);
        fitnessScoreDTO.setNeedAreaSubjectCount(needAreaSubjectCount);
        fitnessScoreDTO.setTeacherOutMaxTimeCount(teacherTiredCount);
        fitnessScoreDTO.setNoBestTimeBestSubjectCount(noBestTimeBestClassCount);
        fitnessScoreDTO.setStudentContinueSameClassCount(studentContinueSameClassCount);
        fitnessScoreDTO.setNoMainSubjectCount(noMainSubjectCount);
        fitnessScoreDTO.setSportNoFinalClassCount(sportNoFinalCount);
        fitnessScoreDTO.setGoTimeNoAfternoonCount(goTimeMorningCount);

        return fitnessScoreDTO;
    }

    /**
     * 回溯
     *
     * @param order
     * @param prepareDTO
     */
    private void rollback(Integer order, PrepareDTO prepareDTO) {

        var orderSubjectIdCanUseMap = prepareDTO.getOrderSubjectIdCanUseMap();
        for (Integer key : orderSubjectIdCanUseMap.keySet()) {
            if (key > order) {
                var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(key);
                for (Integer subjectId : subjectIdCanUseMap.keySet()) {
                    subjectIdCanUseMap.put(subjectId, true);
                }
            }
        }

        // 清理课表,还课
        var subjectClassTeacherDOList = prepareDTO.getSubjectClassTeacherDOList();

        var orderGradeClassNumWorkDayTimeMap = prepareDTO.getOrderGradeClassNumWorkDayTimeMap();
        var timeTableMap = prepareDTO.getTimeTableMap();
        for (Integer key : timeTableMap.keySet()) {
            if (key >= order) {
                var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(key);
                var grade = gradeClassNumWorkDayTimeDTO.getGrade();
                var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
                var subjectId = timeTableMap.get(key);
                if (subjectId != null) {
                    for (SubjectClassTeacherDO subjectClassTeacherDO : subjectClassTeacherDOList) {
                        boolean matchFlag = subjectClassTeacherDO.getSubjectId().equals(subjectId)
                                && subjectClassTeacherDO.getGrade().equals(grade)
                                && subjectClassTeacherDO.getClassNum().equals(classNum);
                        if (matchFlag) {
                            int frequency = subjectClassTeacherDO.getFrequency() + 1;
                            subjectClassTeacherDO.setFrequency(frequency);
                        }
                    }
                }
                timeTableMap.put(key, null);
            }
        }

        // 清理教师状态
        var teacherOrderListMap = prepareDTO.getTeacherOrderListMap();
        for (Integer teacherId : teacherOrderListMap.keySet()) {
            var orderList = teacherOrderListMap.get(teacherId);
            orderList.removeIf(matchOrder -> matchOrder >= order);
        }

    }

    /**
     * 确定回溯点
     *
     * @param order
     * @param orderSubjectIdCanUseMap
     * @return
     */
    private Integer getBackOrder(Integer order,
                                 HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap) {
        if (order == 1) {
            throw new TransactionException("此问题无解");
        }

        int backOrder = order;
        boolean continueFlag = true;
        while (continueFlag) {
            backOrder = backOrder - 1;
            var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(backOrder);
            continueFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
        }

        return backOrder;
    }

    /**
     * 组装更新参数
     *
     * @param checkMatchDTO
     * @return
     */
    private UpdateStatusDTO packUpdateStatusDTO(CheckMatchDTO checkMatchDTO) {
        UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO();
        BeanUtils.copyProperties(checkMatchDTO, updateStatusDTO);
        return updateStatusDTO;
    }

    /**
     * 更新状态
     *
     * @param updateStatusDTO
     */
    private void updateStatus(UpdateStatusDTO updateStatusDTO) {

        // 课程表更新
        var order = updateStatusDTO.getOrder();
        var subjectId = updateStatusDTO.getSubjectId();
        var timetableMap = updateStatusDTO.getTimetableMap();
        timetableMap.put(order, subjectId);

        // 课程表数量减少1
        var orderGradeClassNumWorkDayTimeMap = updateStatusDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();

        var subjectClassTeacherDOList = updateStatusDTO.getSubjectClassTeacherDOList();
        for (SubjectClassTeacherDO subjectClassTeacherDO : subjectClassTeacherDOList) {
            boolean matchFlag = subjectClassTeacherDO.getSubjectId().equals(subjectId)
                    && subjectClassTeacherDO.getGrade().equals(grade)
                    && subjectClassTeacherDO.getClassNum().equals(classNum);
            if (matchFlag) {
                var frequency = subjectClassTeacherDO.getFrequency() - 1;
                subjectClassTeacherDO.setFrequency(frequency);
            }
        }

        if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
            return;
        }

        // 教师更新
        var teacherDOList = subjectClassTeacherDOList.stream().filter(x -> x.getSubjectId().equals(subjectId)
                && x.getGrade().equals(grade) && x.getClassNum().equals(classNum)).map(SubjectClassTeacherDO::getTeacherId).collect(Collectors.toList());
        var teacherId = teacherDOList.get(0);

        var teacherOrderListMap = updateStatusDTO.getTeacherOrderListMap();
        var orderList = teacherOrderListMap.get(teacherId);
        if (CollectionUtils.isEmpty(orderList)) {
            orderList = new ArrayList<>();
        }
        orderList.add(order);
        teacherOrderListMap.put(teacherId, orderList);
    }

    /**
     * 组装检查DTO
     *
     * @param subjectId
     * @param order
     * @param allSubjectMap
     * @param orderGradeClassNumWorkDayTimeMap
     * @param gradeClassNumWorkDayTimeOrderMap
     * @param classroomMaxCapacityMap
     * @param teacherOrderListMap
     * @param subjectClassTeacherDOList
     * @param timeTableMap
     * @return
     */
    private CheckMatchDTO packCheckMatchDTO(Integer subjectId, Integer order,
                                            Map<Integer, SubjectDO> allSubjectMap,
                                            HashMap<Integer, GradeClassNumWorkDayTimeDTO> orderGradeClassNumWorkDayTimeMap,
                                            HashMap<GradeClassNumWorkDayTimeDTO, Integer> gradeClassNumWorkDayTimeOrderMap,
                                            HashMap<Integer, Integer> classroomMaxCapacityMap,
                                            HashMap<Integer, List<Integer>> teacherOrderListMap,
                                            List<SubjectClassTeacherDO> subjectClassTeacherDOList,
                                            TreeMap<Integer, Integer> timeTableMap) {
        CheckMatchDTO checkMatchDTO = new CheckMatchDTO();
        checkMatchDTO.setSubjectId(subjectId);
        checkMatchDTO.setOrder(order);
        checkMatchDTO.setAllSubjectMap(allSubjectMap);
        checkMatchDTO.setOrderGradeClassNumWorkDayTimeMap(orderGradeClassNumWorkDayTimeMap);
        checkMatchDTO.setGradeClassNumWorkDayTimeOrderMap(gradeClassNumWorkDayTimeOrderMap);
        checkMatchDTO.setClassroomMaxCapacityMap(classroomMaxCapacityMap);
        checkMatchDTO.setTeacherOrderListMap(teacherOrderListMap);
        checkMatchDTO.setSubjectClassTeacherDOList(subjectClassTeacherDOList);
        checkMatchDTO.setTimetableMap(timeTableMap);

        return checkMatchDTO;
    }

    /**
     * 检查选择的课程是否满足要求
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkMatch(CheckMatchDTO checkMatchDTO) {
        // 保证每天有主课上
        var everyDayHaveMainFlag = this.checkEveryDayHaveMainIsOk(checkMatchDTO);
        if (!everyDayHaveMainFlag) {
            return false;
        }

        // 每门主课不能一天不能超过3节课(上限为2）
        var mainSubjectMaxFlag = this.checkMainSubjectMaxIsOk(checkMatchDTO);
        if (!mainSubjectMaxFlag) {
            return false;
        }

        // 如果当天上过这个小课,则不在排课
        var otherSubjectFlag = this.checkOtherSubjectIsOk(checkMatchDTO);
        if (!otherSubjectFlag) {
            return false;
        }

        // 判断教室是否空闲
        var classRoomIsOkFlag = this.checkClassRoomIsOkDTO(checkMatchDTO);
        if (!classRoomIsOkFlag) {
            return false;
        }

        // 判断教师是否空闲
        var teacherIsOKFlag = this.checkTeacherIsOk(checkMatchDTO);
        if (!teacherIsOKFlag) {
            return false;
        }

        // 判断特殊课程是否按时上课
        var specialIsOKFlag = this.checkSpecialIsOK(checkMatchDTO);
        if (!specialIsOKFlag) {
            return false;
        }

        // 以一定概率接受软约束检查
        BigDecimal random = BigDecimal.valueOf(Math.random());
        if (random.compareTo(ACCEPT_PRO) > 0) {
            // 早上第一节必须主课
            var mainIsOkFlag = this.checkMainIsOk(checkMatchDTO);
            if (!mainIsOkFlag) {
                return false;
            }

            // 学生不能上连堂课(上限为2)
            var studentContinueIsOkFlag = this.checkStudentContinueIsOk(checkMatchDTO);
            if (!studentContinueIsOkFlag) {
                return false;
            }

            // 教师不能上连堂课(上限为4) 并且一个教师一天最多上4节课，如果超过4节课，不在排课
            var checkTeacherMaxAndContinueClassIsOkFlag = this.checkTeacherMaxAndContinueClassIsOk(checkMatchDTO);
            if (!checkTeacherMaxAndContinueClassIsOkFlag) {
                return false;
            }

            // 判断体育课是否在第四节
            var checkSportIsOkFlag = this.checkSportIsOk(checkMatchDTO);
            if (!checkSportIsOkFlag) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查课程剩余次数是否大于1
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkSubjectIsOk(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();

        var subjectClassTeacherDOList = checkMatchDTO.getSubjectClassTeacherDOList();
        for (SubjectClassTeacherDO subjectClassTeacherDO : subjectClassTeacherDOList) {
            boolean matchFlag = subjectClassTeacherDO.getSubjectId().equals(subjectId)
                    && subjectClassTeacherDO.getGrade().equals(grade)
                    && subjectClassTeacherDO.getClassNum().equals(classNum);
            if (matchFlag) {
                return subjectClassTeacherDO.getFrequency() > 0;
            }
        }

        return true;
    }

    /**
     * 检查特殊课程是否合适
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkSpecialIsOK(CheckMatchDTO checkMatchDTO) {
        Integer order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        var subjectId = checkMatchDTO.getSubjectId();

        // 周一班会课
        boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(workDay)
                && SchoolTimeTableDefaultValueDTO.getClassTime().equals(time);
        if (classMeetingFlag) {
            return SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectId);
        }
        // 周三书法课
        boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(workDay)
                && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
        if (writingFlag) {
            return SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectId);
        }
        // 周五校本课程
        boolean schoolBaseFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(workDay)
                && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
        if (schoolBaseFlag) {
            return SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId);
        }

        return !(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId().equals(subjectId)
                || SchoolTimeTableDefaultValueDTO.getWritingId().equals(subjectId)
                || SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId));
    }

    /**
     * 检查体育课是否合适
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkSportIsOk(CheckMatchDTO checkMatchDTO) {

        var subjectId = checkMatchDTO.getSubjectId();
        if (!SchoolTimeTableDefaultValueDTO.getSubjectSportId().equals(subjectId)) {
            return true;
        }

        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var time = gradeClassNumWorkDayTimeDTO.getTime();

        if (SchoolTimeTableDefaultValueDTO.getAfternoonFirTime().equals(time)) {
            return false;
        }

        return true;
    }

    /**
     * 检查教师是否连续上了3节课
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkTeacherMaxAndContinueClassIsOk(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();

        if (SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId().equals(subjectId)) {
            return true;
        }

        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();

        var subjectClassTeacherDOList = checkMatchDTO.getSubjectClassTeacherDOList();
        var teacherList = subjectClassTeacherDOList.stream().filter(x -> x.getSubjectId().equals(subjectId)
                && x.getGrade().equals(grade) && x.getClassNum().equals(classNum)).map(SubjectClassTeacherDO::getTeacherId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teacherList)) {
            throw new TransactionException("没有查询到相关上课教师");
        }

        var teacherId = teacherList.get(0);
        var teacherOrderListMap = checkMatchDTO.getTeacherOrderListMap();
        var matchOrderList = teacherOrderListMap.get(teacherId);
        if (CollectionUtils.isEmpty(matchOrderList)) {
            return true;
        }

        HashMap<Integer, List<Integer>> workDayTimesMap = new HashMap<>();
        for (Integer matchOrder : matchOrderList) {
            var matchGradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(matchOrder);
            var matchWorkDay = matchGradeClassNumWorkDayTimeDTO.getWorkDay();
            var matchTime = matchGradeClassNumWorkDayTimeDTO.getTime();
            if (workDayTimesMap.get(matchWorkDay) == null) {
                List<Integer> matchTimeList = new ArrayList<>();
                matchTimeList.add(matchTime);
                workDayTimesMap.put(matchWorkDay, matchTimeList);
            } else {
                List<Integer> matchTimeList = workDayTimesMap.get(matchWorkDay);
                matchTimeList.add(matchTime);
                workDayTimesMap.put(matchWorkDay, matchTimeList);
            }
        }

        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var matchTimeList = workDayTimesMap.get(workDay);
        if (CollectionUtils.isEmpty(matchTimeList)) {
            return true;
        }

        if (matchTimeList.size() >= SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize() - 1) {
            return false;
        }
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        var teacherContinueTime = SchoolTimeTableDefaultValueDTO.getTeacherContinueTime();
        for (Integer[] continueTime : teacherContinueTime) {
            Integer firstTime = continueTime[0];
            Integer secTime = continueTime[1];
            Integer thirdTime = continueTime[2];
            boolean continueFlag = matchTimeList.contains(firstTime) && matchTimeList.contains(secTime) && thirdTime.equals(time);
            if (continueFlag) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查学生是否连着上了3次一样的主课
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkStudentContinueIsOk(CheckMatchDTO checkMatchDTO) {
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        GradeClassNumWorkDayTimeDTO key = new GradeClassNumWorkDayTimeDTO();
        key.setGrade(grade);
        key.setClassNum(classNum);
        key.setWorkDay(workDay);
        var gradeClassNumWorkDayTimeOrderMap = checkMatchDTO.getGradeClassNumWorkDayTimeOrderMap();
        HashMap<Integer, Integer> matchOrderSubjectMap = new HashMap<>();
        var timetableMap = checkMatchDTO.getTimetableMap();
        for (int i = 1; i <= SchoolTimeTableDefaultValueDTO.getClassTime(); i++) {
            key.setTime(i);
            var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
            var subjectId = timetableMap.get(matchOrder);
            matchOrderSubjectMap.put(matchOrder, subjectId);
        }

        var subjectId = checkMatchDTO.getSubjectId();
        var studentContinueTime = SchoolTimeTableDefaultValueDTO.getStudentContinueTime();
        for (Integer[] continueTime : studentContinueTime) {
            Integer pastTime = continueTime[0];
            Integer nextTime = continueTime[1];
            var pastSubjectId = matchOrderSubjectMap.get(pastTime);
            var nextSubjectId = matchOrderSubjectMap.get(nextTime);
            if (pastSubjectId == null || nextSubjectId == null) {
                continue;
            }
            if (pastSubjectId.equals(subjectId) && nextSubjectId.equals(subjectId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查第一节课为主课
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkMainIsOk(CheckMatchDTO checkMatchDTO) {
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        if (!(SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time)
                || SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time))) {
            return true;
        }

        var subjectId = checkMatchDTO.getSubjectId();
        var allSubjectMap = checkMatchDTO.getAllSubjectMap();
        var subjectDO = allSubjectMap.get(subjectId);

        return SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDO.getType());
    }

    /**
     * 检查教师是否空闲
     *
     * @param checkMatchDTO
     * @return
     */
    private boolean checkTeacherIsOk(CheckMatchDTO checkMatchDTO) {
        // 先查一下是哪个教师
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();

        var subjectId = checkMatchDTO.getSubjectId();

        var subjectClassTeacherDOList = checkMatchDTO.getSubjectClassTeacherDOList();
        List<Integer> teacherIdList = new ArrayList<>();
        for (SubjectClassTeacherDO subjectClassTeacherDO : subjectClassTeacherDOList) {
            boolean matchFlag = subjectClassTeacherDO.getSubjectId().equals(subjectId)
                    && subjectClassTeacherDO.getGrade().equals(grade)
                    && subjectClassTeacherDO.getClassNum().equals(classNum)
                    && subjectClassTeacherDO.getTeacherId() != null;
            if (matchFlag) {
                teacherIdList.add(subjectClassTeacherDO.getTeacherId());
            }
        }

        // 无人上课的科目，直接返回true
        if (CollectionUtils.isEmpty(teacherIdList)) {
            return true;
        }

        var teacherId = teacherIdList.get(0);

        var teacherOrderListMap = checkMatchDTO.getTeacherOrderListMap();
        var orderList = teacherOrderListMap.get(teacherId);
        if (CollectionUtils.isEmpty(orderList)) {
            return true;
        }

        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        for (Integer matchOrder : orderList) {
            var matchGradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(matchOrder);
            if (matchGradeClassNumWorkDayTimeDTO.getWorkDay().equals(workDay)
                    && matchGradeClassNumWorkDayTimeDTO.getTime().equals(time)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查教室是否合适
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkClassRoomIsOkDTO(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();
        var allSubjectMap = checkMatchDTO.getAllSubjectMap();
        var subjectDO = allSubjectMap.get(subjectId);
        if (!SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDO.getType())) {
            return true;
        }

        // 先看课程表中有没有这么课程
        var timetableMap = checkMatchDTO.getTimetableMap();
        List<Integer> matchOrderList = new ArrayList<>();
        for (Integer x : timetableMap.keySet()) {
            var matchSubjectId = timetableMap.get(x);
            if (matchSubjectId != null) {
                if (matchSubjectId.equals(subjectId)) {
                    matchOrderList.add(x);
                }
            }
        }

        if (CollectionUtils.isEmpty(matchOrderList)) {
            return true;
        }

        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        int count = 0;
        for (Integer matchOrder : matchOrderList) {
            var matchGradeClassNumWorkDayTime = orderGradeClassNumWorkDayTimeMap.get(matchOrder);
            if (matchGradeClassNumWorkDayTime.getWorkDay().equals(workDay)
                    && matchGradeClassNumWorkDayTime.getTime().equals(time)) {
                count = count + 1;
            }
        }

        var classroomMaxCapacityMap = checkMatchDTO.getClassroomMaxCapacityMap();
        var maxCount = classroomMaxCapacityMap.get(subjectId);

        return count < maxCount;
    }

    /**
     * 检查主课每天不能超过两节课
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkMainSubjectMaxIsOk(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();
        var allSubjectMap = checkMatchDTO.getAllSubjectMap();
        var subjectDO = allSubjectMap.get(subjectId);
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDO.getType())) {
            return true;
        }

        // 如果是主课，检查每班每天是否超过2节课
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeDTO = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeDTO.get(order);
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        var gradeClassNumWorkDayTimeOrderMap = checkMatchDTO.getGradeClassNumWorkDayTimeOrderMap();
        GradeClassNumWorkDayTimeDTO key = new GradeClassNumWorkDayTimeDTO();
        key.setGrade(grade);
        key.setClassNum(classNum);
        key.setWorkDay(workDay);
        List<Integer> matchOrderList = new ArrayList<>();
        for (int i = 1; i <= SchoolTimeTableDefaultValueDTO.getClassTime(); i++) {
            key.setTime(i);
            var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
            matchOrderList.add(matchOrder);
        }

        var timetableMap = checkMatchDTO.getTimetableMap();
        int count = 0;
        for (Integer matchOrder : matchOrderList) {
            var matchSubjectId = timetableMap.get(matchOrder);
            if (matchSubjectId != null) {
                if (matchSubjectId.equals(subjectId)) {
                    count = count + 1;
                }
            }
        }

        return count < SubjectDefaultValueDTO.getTwoCount();
    }

    /**
     * 检查每天是否都有主课上
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkEveryDayHaveMainIsOk(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();
        var allSubjectMap = checkMatchDTO.getAllSubjectMap();
        SubjectDO subjectDO = allSubjectMap.get(subjectId);
        if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectDO.getType())) {
            return true;
        }

        // 如果是主课，检查是否都有课上
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeDTO = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeDTO.get(order);
        Integer grade = gradeClassNumWorkDayTimeDTO.getGrade();
        Integer classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        Integer workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        var subjectClassTeacherDOList = checkMatchDTO.getSubjectClassTeacherDOList();
        var matchList = subjectClassTeacherDOList.stream().filter(x -> x.getSubjectId().equals(subjectId)
                && x.getGrade().equals(grade) && x.getClassNum().equals(classNum)).collect(Collectors.toList());
        if (matchList.size() != 1) {
            throw new TransactionException("未找到匹配的课程次数");
        }
        Integer frequency = matchList.get(0).getFrequency();
        return frequency > SchoolTimeTableDefaultValueDTO.getWorkDay() - workDay;
    }

    /**
     * 检查每天每个班每一种小课只能上一节
     *
     * @param checkMatchDTO
     * @return
     */
    private Boolean checkOtherSubjectIsOk(CheckMatchDTO checkMatchDTO) {
        var subjectId = checkMatchDTO.getSubjectId();
        var subjectDOMap = checkMatchDTO.getAllSubjectMap();
        var subjectDO = subjectDOMap.get(subjectId);
        var otherFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectDO.getType())
                || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectDO.getType());
        if (!otherFlag) {
            return true;
        }

        // 如果是小课，则要检查每班每天只上一次课程
        var order = checkMatchDTO.getOrder();
        var orderGradeClassNumWorkDayTimeDTO = checkMatchDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeDTO.get(order);
        Integer grade = gradeClassNumWorkDayTimeDTO.getGrade();
        Integer classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        Integer workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();

        var gradeClassNumWorkDayTimeOrderMap = checkMatchDTO.getGradeClassNumWorkDayTimeOrderMap();
        GradeClassNumWorkDayTimeDTO key = new GradeClassNumWorkDayTimeDTO();
        key.setGrade(grade);
        key.setClassNum(classNum);
        key.setWorkDay(workDay);
        List<Integer> matchOrderList = new ArrayList<>();
        for (int i = 1; i <= SchoolTimeTableDefaultValueDTO.getClassTime(); i++) {
            key.setTime(i);
            var matchOrder = gradeClassNumWorkDayTimeOrderMap.get(key);
            matchOrderList.add(matchOrder);
        }

        var timeTableMap = checkMatchDTO.getTimetableMap();
        for (Integer matchOrder : matchOrderList) {
            var matchSubjectId = timeTableMap.get(matchOrder);
            if (matchSubjectId != null) {
                if (matchSubjectId.equals(subjectId)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 选择第一个可用课程
     *
     * @param subjectCanUseMap
     * @return
     */
    private Integer chooseFirstSubject(HashMap<Integer, Boolean> subjectCanUseMap) {
        Integer subjectId = null;
        for (Integer subject : subjectCanUseMap.keySet()) {
            Boolean useFlag = subjectCanUseMap.get(subject);
            if (useFlag) {
                subjectId = subject;
                break;
            }
        }
        return subjectId;
    }


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
                var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getSubjectConstraintDTOList();
                this.getOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap, timeTableConstraintDTOList);

                var subjectIdCanUseMap = getSubjectIdCanUseMap(order, orderSubjectIdCanUseMap, timeTablingUseFCDWBacktrackingDTO);

                var backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                // 检查是否有回溯课程
                if (backFlag) {
                    var rollbackInFCDWDTO = this.getRollbackDTO(order, timeTablingUseFCDWBacktrackingDTO);
                    this.rollback(rollbackInFCDWDTO);
                    this.getTimeTablingUseFCDWBacktrackingDTO(rollbackInFCDWDTO, timeTablingUseFCDWBacktrackingDTO);
                    order = rollbackInFCDWDTO.getOrder();
                    this.clearConstraint(order, timeTablingUseFCDWBacktrackingDTO);
                } else {
                    subjectIdCanUseMap = getSubjectIdCanUseMap(order, orderSubjectIdCanUseMap, timeTablingUseFCDWBacktrackingDTO);
                    Integer chooseSubjectId = null;
                    if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_BA)) {
                        // 选择一个课程
                        chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                    }
                    if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_DW_BA)) {
                        chooseSubjectId = this.getMaxWeightSubjectId(order, timeTablingUseFCDWBacktrackingDTO);
                    }
                    subjectIdCanUseMap.put(chooseSubjectId, false);
                    orderSubjectIdCanUseMap.put(order, subjectIdCanUseMap);
                    timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);
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
                        this.listConstraint(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);

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
                        timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getSubjectConstraintDTOList();
                        this.getOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap, timeTableConstraintDTOList);

                        // 判断这一层的回溯点是否都已经使用，如果没有使用完毕，不需要回溯，选择下一个课程
                        backFlag = subjectIdCanUseMap.values().stream().allMatch(x -> x.equals(false));
                        if (!backFlag) {
                            subjectIdCanUseMap = getSubjectIdCanUseMap(order, orderSubjectIdCanUseMap, timeTablingUseFCDWBacktrackingDTO);
                            // 回溯点不清零，记录该点的排课课程，下次不再选择这么课程
                            chooseSubjectId = null;
                            if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_BA)) {
                                // 选择一个课程
                                chooseSubjectId = this.getFirstCanUseSubjectIdInSubjectIdCanUseMap(subjectIdCanUseMap);
                            }
                            if (backtrackingTypeEnum.equals(BacktrackingTypeEnum.FC_DW_BA)) {
                                chooseSubjectId = this.getMaxWeightSubjectId(order, timeTablingUseFCDWBacktrackingDTO);
                            }
                            subjectIdCanUseMap.put(chooseSubjectId, false);
                            orderSubjectIdCanUseMap.put(order, subjectIdCanUseMap);
                            timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);

                            checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                            fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                            message = this.packMessage(fitnessScoreDTO);
                            messageList.add(message);

                            checkCompleteDTO = this.packCheckCompleteDTO(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                            completeFlag = this.checkAllComplete(checkCompleteDTO);
                            if (completeFlag) {
                                this.updateAllStatus(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);
                                this.listConstraint(order, chooseSubjectId, timeTablingUseFCDWBacktrackingDTO);

                                if (order == orderSubjectIdMap.keySet().size()) {
                                    checkFitnessScoreDTO = this.packFitnessScore(timeTablingUseFCDWBacktrackingDTO);
                                    fitnessScoreDTO = this.computerFitnessScore(checkFitnessScoreDTO);
                                    message = this.packMessage(fitnessScoreDTO);
                                    messageList.add(message);
                                }
                            }
                        }
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
    public String packMessage(FitnessScoreDTO fitnessScoreDTO) {
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
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getSubjectConstraintDTOList();

        for (Integer key : orderGradeClassNumWorkDayTimeMap.keySet()) {
            var specialGradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(key);
            var specialWork = specialGradeClassNumWorkDayTimeDTO.getWorkDay();
            var specialTime = specialGradeClassNumWorkDayTimeDTO.getTime();
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getMondayNum()) && specialTime.equals(SchoolTimeTableDefaultValueDTO.getClassMeetingTime()))) {
                SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                subjectConstraintDTO.setOrder(0);
                subjectConstraintDTO.setOrderConstraint(key);
                subjectConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId());
                timeTableConstraintDTOList.add(subjectConstraintDTO);
            }
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getWednesdayNum()) && specialTime.equals(SchoolTimeTableDefaultValueDTO.getWritingTime()))) {
                SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                subjectConstraintDTO.setOrder(0);
                subjectConstraintDTO.setOrderConstraint(key);
                subjectConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getWritingId());
                timeTableConstraintDTOList.add(subjectConstraintDTO);
            }
            if (!(specialWork.equals(SchoolTimeTableDefaultValueDTO.getFridayNum()) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(specialTime))) {
                SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                subjectConstraintDTO.setOrder(0);
                subjectConstraintDTO.setOrderConstraint(key);
                subjectConstraintDTO.setSubjectIdConstraint(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId());
                timeTableConstraintDTOList.add(subjectConstraintDTO);
            }

            var subjectDOList = timeTablingUseFCDWBacktrackingDTO.getAllSubject();
            for (SubjectDO subjectDO : subjectDOList) {
                boolean unClassMeetingFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getMondayNum())
                        && specialTime.equals(SchoolTimeTableDefaultValueDTO.getClassMeetingTime())
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId());
                if (unClassMeetingFlag) {
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(0);
                    subjectConstraintDTO.setOrderConstraint(key);
                    subjectConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
                }

                boolean unWritingFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getWednesdayNum())
                        && specialTime.equals(SchoolTimeTableDefaultValueDTO.getWritingTime())
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getWritingId());
                if (unWritingFlag) {
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(0);
                    subjectConstraintDTO.setOrderConstraint(key);
                    subjectConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
                }

                boolean unSchoolBaseFlag = specialWork.equals(SchoolTimeTableDefaultValueDTO.getFridayNum())
                        && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(specialTime)
                        && !subjectDO.getId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId());
                if (unSchoolBaseFlag) {
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(0);
                    subjectConstraintDTO.setOrderConstraint(key);
                    subjectConstraintDTO.setSubjectIdConstraint(subjectDO.getId());
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
                }
            }

        }

        // 先约束，再给orderSubjectIdCanUseMap 赋值
        timeTableConstraintDTOList = timeTableConstraintDTOList.stream().distinct().collect(Collectors.toList());
        for (SubjectConstraintDTO subjectConstraintDTO : timeTableConstraintDTOList) {
            var subjectCanUseMap = orderSubjectIdCanUseMap.get(subjectConstraintDTO.getOrderConstraint());
            if (subjectCanUseMap.get(subjectConstraintDTO.getSubjectIdConstraint()) != null) {
                subjectCanUseMap.put(subjectConstraintDTO.getSubjectIdConstraint(), false);
                orderSubjectIdCanUseMap.put(subjectConstraintDTO.getOrderConstraint(), subjectCanUseMap);
                timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);
            }
        }

        timeTablingUseFCDWBacktrackingDTO.setSubjectConstraintDTOList(timeTableConstraintDTOList);
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
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getSubjectConstraintDTOList();

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
                if (subjectId.equals(subject) && frequency == 0 && matchOrder > order) {
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(order);
                    subjectConstraintDTO.setOrderConstraint(matchOrder);
                    subjectConstraintDTO.setSubjectIdConstraint(subject);
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
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
                if (orderConstraint != null && orderConstraint > order) {
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(order);
                    subjectConstraintDTO.setOrderConstraint(orderConstraint);
                    subjectConstraintDTO.setSubjectIdConstraint(x.getSubjectId());
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
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
                    SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
                    subjectConstraintDTO.setOrder(order);
                    subjectConstraintDTO.setOrderConstraint(key);
                    subjectConstraintDTO.setSubjectIdConstraint(subjectId);
                    timeTableConstraintDTOList.add(subjectConstraintDTO);
                }
            }
        }

        // 选择课程约束
        SubjectConstraintDTO subjectConstraintDTO = new SubjectConstraintDTO();
        subjectConstraintDTO.setOrder(order);
        subjectConstraintDTO.setOrderConstraint(order);
        subjectConstraintDTO.setSubjectIdConstraint(subjectId);
        timeTableConstraintDTOList.add(subjectConstraintDTO);

        timeTableConstraintDTOList = timeTableConstraintDTOList.stream().distinct().collect(Collectors.toList());

        timeTablingUseFCDWBacktrackingDTO.setSubjectConstraintDTOList(timeTableConstraintDTOList);
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
        var timeTableConstraintDTOList = timeTablingUseFCDWBacktrackingDTO.getSubjectConstraintDTOList();
        var orderSubjectIdCanUseMap = timeTablingUseFCDWBacktrackingDTO.getOrderSubjectIdCanUseMap();

        List<SubjectConstraintDTO> newSubjectConstraintDTOList = new ArrayList<>();
        for (SubjectConstraintDTO subjectConstraintDTO : timeTableConstraintDTOList) {
            if (subjectConstraintDTO.getOrder() > order && subjectConstraintDTO.getOrderConstraint() > order) {
                var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(subjectConstraintDTO.getOrderConstraint());
                subjectIdCanUseMap.put(subjectConstraintDTO.getSubjectIdConstraint(), true);
                orderSubjectIdCanUseMap.put(subjectConstraintDTO.getOrderConstraint(), subjectIdCanUseMap);
                timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdCanUseMap(orderSubjectIdCanUseMap);
            }
            if ((subjectConstraintDTO.getOrder().equals(order) || subjectConstraintDTO.getOrder() < order)
                    && subjectConstraintDTO.getOrderConstraint().equals(order)) {
                newSubjectConstraintDTOList.add(subjectConstraintDTO);
            }
        }

        timeTablingUseFCDWBacktrackingDTO.setSubjectConstraintDTOList(newSubjectConstraintDTOList);

        return timeTablingUseFCDWBacktrackingDTO;
    }

    /**
     * @param order
     * @param orderSubjectIdCanUseMap
     * @param timeTablingUseFCDWBacktrackingDTO
     * @return
     */
    private HashMap<Integer, Boolean> getSubjectIdCanUseMap(Integer order, HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap, TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO) {

        var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(order);
        var orderGradeClassNumWorkDayTimeMap = timeTablingUseFCDWBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var gradeClassNumSubjectFrequencyMap = timeTablingUseFCDWBacktrackingDTO.getGradeClassNumSubjectFrequencyMap();
        var classNumSubjectFrequencyMap = gradeClassNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getGrade());
        var subjectFrequencyMap = classNumSubjectFrequencyMap.get(gradeClassNumWorkDayTimeDTO.getClassNum());
        for (Integer key : subjectFrequencyMap.keySet()) {
            var frequency = subjectFrequencyMap.get(key);
            if (frequency <= 0) {
                subjectIdCanUseMap.put(key, false);
            }
        }

        return subjectIdCanUseMap;
    }

    /**
     * 约束变为选课
     *
     * @param orderSubjectIdCanUseMap
     * @param subjectConstraintDTOList
     * @return
     */
    private HashMap<Integer, HashMap<Integer, Boolean>> getOrderSubjectIdCanUseMap(HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectIdCanUseMap,
                                                                                   List<SubjectConstraintDTO> subjectConstraintDTOList) {
        for (SubjectConstraintDTO subjectConstraintDTO : subjectConstraintDTOList) {
            var subjectIdCanUseMap = orderSubjectIdCanUseMap.get(subjectConstraintDTO.getOrder());
            if (subjectIdCanUseMap != null) {
                if (subjectIdCanUseMap.get(subjectConstraintDTO.getSubjectIdConstraint()) != null) {
                    var subjectCanUseMap = orderSubjectIdCanUseMap.get(subjectConstraintDTO.getOrderConstraint());
                    subjectCanUseMap.put(subjectConstraintDTO.getSubjectIdConstraint(), false);
                    orderSubjectIdCanUseMap.put(subjectConstraintDTO.getOrderConstraint(), subjectCanUseMap);
                }
            }
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
     * 更新可使用课程
     *
     * @param subjectConstraintDTOList
     * @param orderSubjectCanUseMap
     */
    private void updateSubjectIdCanUseMap(List<SubjectConstraintDTO> subjectConstraintDTOList,
                                          HashMap<Integer, HashMap<Integer, Boolean>> orderSubjectCanUseMap) {

        for (SubjectConstraintDTO subjectConstraintDTO : subjectConstraintDTOList) {
            var subjectCanUseMap = orderSubjectCanUseMap.get(subjectConstraintDTO.getOrderConstraint());
            if (subjectCanUseMap.get(subjectConstraintDTO.getSubjectIdConstraint()) != null) {
                subjectCanUseMap.put(subjectConstraintDTO.getSubjectIdConstraint(), false);
                orderSubjectCanUseMap.put(subjectConstraintDTO.getOrderConstraint(), subjectCanUseMap);
            }
        }

    }

    /**
     * 更新可使用课程
     *
     * @param subjectIdCanUseMap
     * @param gradeClassNumWorkDayTimeDTO
     * @param subjectClassTeacherDOList
     */
    private void updateSubjectIdCanUseMap(HashMap<Integer, Boolean> subjectIdCanUseMap,
                                          GradeClassNumWorkDayTimeDTO gradeClassNumWorkDayTimeDTO,
                                          List<SubjectClassTeacherDO> subjectClassTeacherDOList) {
        // 根据剩余次数 如果已经不可用，则不要更改状态
        var grade = gradeClassNumWorkDayTimeDTO.getGrade();
        var classNum = gradeClassNumWorkDayTimeDTO.getClassNum();
        var subjectZeroList = subjectClassTeacherDOList.stream().filter(x -> x.getGrade().equals(grade)
                && x.getClassNum().equals(classNum) && x.getFrequency().equals(0)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(subjectZeroList)) {
            for (SubjectClassTeacherDO subjectClassTeacherDO : subjectZeroList) {
                subjectIdCanUseMap.put(subjectClassTeacherDO.getSubjectId(), false);
            }
        }

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

        // 早上第一节必须是主课
        var firstClassIsMainFlag = this.checkFirstClassIsMainIsOk(checkCompleteDTO);
        if (!firstClassIsMainFlag) {
            return false;
        }

        // 查看美术是否在第3、4节课
        var artIsOkFlag = this.checkArtIsOk(checkCompleteDTO);
        if (!artIsOkFlag) {
            return false;
        }

        // 查看特殊课程
        var specialIsOKFlag = this.checkSpecial(checkCompleteDTO);
        if (!specialIsOKFlag) {
            return false;
        }

        // 按照一定的概率接受软约束条件
        BigDecimal random = BigDecimal.valueOf(Math.random());
        if (random.compareTo(ACCEPT_PRO) > 0) {

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

        }

        // 查看是否无解
        return this.checkSolvable(checkCompleteDTO);
    }

    private Boolean checkSpecial(CheckCompleteDTO checkCompleteDTO) {
        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var workDay = gradeClassNumWorkDayTimeDTO.getWorkDay();
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        var subjectDTO = checkCompleteDTO.getSubjectDTO();

        boolean meetIdFlag = subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId());
        boolean meetTimeFlag = workDay.equals(SchoolTimeTableDefaultValueDTO.getMondayNum()) && time.equals(SchoolTimeTableDefaultValueDTO.getClassMeetingTime());
        if (meetIdFlag && meetTimeFlag) {
            return true;
        }
        if (meetTimeFlag || meetIdFlag) {
            return false;
        }
        boolean writeFlag = subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getWritingId());
        boolean writeTimeFlag = workDay.equals(SchoolTimeTableDefaultValueDTO.getWednesdayNum()) && time.equals(SchoolTimeTableDefaultValueDTO.getWritingTime());
        if (writeFlag && writeTimeFlag) {
            return true;
        }
        if (writeFlag || writeTimeFlag) {
            return false;
        }
        boolean schoolBasedFlag = subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId());
        boolean schoolBasedTimeFlag = workDay.equals(SchoolTimeTableDefaultValueDTO.getFridayNum()) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
        if (schoolBasedFlag && schoolBasedTimeFlag) {
            return true;
        }
        if (schoolBasedFlag || schoolBasedTimeFlag) {
            return false;
        }

        if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
            return true;
        }

        return false;
    }

    /**
     * 检查体育课是否合适
     *
     * @param checkCompleteDTO
     * @return
     */
    private Boolean checkArtIsOk(CheckCompleteDTO checkCompleteDTO) {
        var order = checkCompleteDTO.getOrder();
        var orderGradeClassNumWorkDayTimeMap = checkCompleteDTO.getOrderGradeClassNumWorkDayTimeMap();
        var gradeClassNumWorkDayTimeDTO = orderGradeClassNumWorkDayTimeMap.get(order);
        var time = gradeClassNumWorkDayTimeDTO.getTime();
        var subjectDTO = checkCompleteDTO.getSubjectDTO();
        boolean artFlag = subjectDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectArtId());
        boolean timeFlag = time.equals(SchoolTimeTableDefaultValueDTO.getAfternoonFirTime()) || time.equals(SchoolTimeTableDefaultValueDTO.getMorningLastTime());
        if (artFlag && !timeFlag) {
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

        return frequency >= SchoolTimeTableDefaultValueDTO.getWorkDay() - checkCompleteUseBacktrackingDTO.getWorkDay();
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
