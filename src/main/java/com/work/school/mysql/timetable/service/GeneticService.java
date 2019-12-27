package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.common.MathsUtils;
import com.work.school.mysql.common.service.ClassroomMaxCapacityService;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.FitnessFunctionEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class GeneticService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneticService.class);

    private static final int ZERO = 0;
    private static final int START_INDEX = 1;
    private static final int STEP = 1;


    private static final int ONLY_ONE_COUNT = 1;
    /**
     * 教师一天最多上4节课
     */
    private static final int TEACHER_MAX_TIME = 4;
    /**
     * 开始分数
     */
    private static final int START_SCORE = 99;
    /**
     * 递加分数
     */
    private static final int ADD_SCORE = 1;
    private static final int BIG_SCORE = 5;
    /**
     * 初始种群
     */
    private static final int POPULATION = 10;
    /**
     * 进化次数
     */
    private static final int EVOLUTION_TIMES = 500;
    /**
     * 交叉概率
     */
    private static final double CROSSOVER_RATE = 0.6;
    /**
     * 变异概率
     */
    private static final double MUTATE_RATE = 0.01;

    @Autowired
    private ClassroomMaxCapacityService classroomMaxCapacityService;

    /**
     * 结果展示
     *
     * @param geneMap
     * @param allSubjectNameMap
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>>
    convertToTimeTableNameMap(HashMap<String, List<String>> geneMap, Map<Integer, String> allSubjectNameMap) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();
        HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>> timeTableMap = new HashMap<>();
        for (String gradeClassNo : geneMap.keySet()) {
            var geneList = geneMap.get(gradeClassNo);
            for (String gene : geneList) {
                var gradeString = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_INDEX, gene);
                var grade = Integer.valueOf(gradeString);

                var classNoString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_NO_INDEX, gene);
                var classNo = Integer.valueOf(classNoString);

                var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
                var workDayTimeDTO = this.convertWorkDayTime(classTime);
                var workDay = workDayTimeDTO.getWorkDay();
                var time = workDayTimeDTO.getTime();

                var subjectIdString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);
                var subjectId = Integer.valueOf(subjectIdString);
                var subjectName = allSubjectNameMap.get(subjectId);

                var classNoWorkDayTimeSubjectMap = timeTableMap.get(grade);
                if (classNoWorkDayTimeSubjectMap == null) {
                    classNoWorkDayTimeSubjectMap = new HashMap<>();
                    HashMap<Integer, HashMap<Integer, String>> workDayTimeSubjectMap = new HashMap<>();
                    HashMap<Integer, String> timeSubjectMap = new HashMap<>();
                    timeSubjectMap.put(time, subjectName);
                    workDayTimeSubjectMap.put(workDay, timeSubjectMap);
                    classNoWorkDayTimeSubjectMap.put(classNo, workDayTimeSubjectMap);
                    timeTableMap.put(grade, classNoWorkDayTimeSubjectMap);
                } else {
                    var workDayTimeSubjectMap = classNoWorkDayTimeSubjectMap.get(classNo);
                    if (workDayTimeSubjectMap == null) {
                        workDayTimeSubjectMap = new HashMap<>();
                        HashMap<Integer, String> timeSubjectMap = new HashMap<>();
                        timeSubjectMap.put(time, subjectName);
                        workDayTimeSubjectMap.put(workDay, timeSubjectMap);
                        classNoWorkDayTimeSubjectMap.put(classNo, workDayTimeSubjectMap);
                        timeTableMap.put(grade, classNoWorkDayTimeSubjectMap);
                    } else {
                        var timeSubjectMap = workDayTimeSubjectMap.get(workDay);
                        if (timeSubjectMap == null) {
                            timeSubjectMap = new HashMap<>();
                            timeSubjectMap.put(time, subjectName);
                            workDayTimeSubjectMap.put(workDay, timeSubjectMap);
                            classNoWorkDayTimeSubjectMap.put(classNo, workDayTimeSubjectMap);
                            timeTableMap.put(grade, classNoWorkDayTimeSubjectMap);
                        } else {
                            timeSubjectMap.put(time, subjectName);
                            workDayTimeSubjectMap.put(workDay, timeSubjectMap);
                            classNoWorkDayTimeSubjectMap.put(classNo, workDayTimeSubjectMap);
                            timeTableMap.put(grade, classNoWorkDayTimeSubjectMap);
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
     * 转换工作日和时间
     *
     * @param classTime
     * @return
     */
    private WorkDayTimeDTO convertWorkDayTime(String classTime) {
        var workDayAndTime = Integer.valueOf(classTime);

        // 计算第几个工作日
        var workday = this.computerWorkDay(workDayAndTime);
        // 计算第几节课
        var time = this.computerTime(workDayAndTime);

        WorkDayTimeDTO workDayTimeDTO = new WorkDayTimeDTO();
        workDayTimeDTO.setWorkDay(workday);
        workDayTimeDTO.setTime(time);
        return workDayTimeDTO;
    }

    /**
     * 计算第几个工作日
     *
     * @param classTime
     * @return
     */
    private Integer computerWorkDay(Integer classTime) {
        Integer workday = null;
        for (int i = START_INDEX; i < SchoolTimeTableDefaultValueDTO.getWorkDay(); i++) {
            var subClassTime = i * SchoolTimeTableDefaultValueDTO.getClassTime();
            var supClassTime = (i + STEP) * SchoolTimeTableDefaultValueDTO.getClassTime();
            if (classTime <= SchoolTimeTableDefaultValueDTO.getClassTime()) {
                workday = SchoolTimeTableDefaultValueDTO.getStartWorkDayIndex();
                break;
            }
            if (subClassTime < classTime && classTime <= supClassTime) {
                workday = i + STEP;
                break;
            }
        }
        return workday;
    }

    /**
     * 计算第几节课
     *
     * @param classTime
     * @return
     */
    private Integer computerTime(Integer classTime) {
        var time = classTime % SchoolTimeTableDefaultValueDTO.getClassTime();
        if (ZERO == time) {
            time = SchoolTimeTableDefaultValueDTO.getClassTime();
        }
        return time;
    }

    /**
     * 使用遗传算法排课
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
    public CattyResult<HashMap<String, List<String>>> algorithmInPlanTimeTableWithGenetic(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
        CattyResult<HashMap<String, List<String>>> cattyResult = new CattyResult<>();

        // 获取初始基因编码
        int bestScore = ZERO;
        List<HashMap<String, List<String>>> allGeneMapList = new ArrayList<>();
        // 产生初始种群
        for (int i = ZERO; i < POPULATION; i++) {
            var geneMap = this.initGene(timeTablingUseGeneticDTO);

            // 进行初步的时间分配
            var initTimeGradeSubjectList = this.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());

            // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
            var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(initTimeGradeSubjectList);

            allGeneMapList.add(gradeClassNoGeneMap);
        }

        // 遗传算法 满足硬约束
        HashMap<String, List<String>> bestGeneMap = new HashMap<>();
        for (var map : allGeneMapList) {
            var geneMap = this.geneticHardConstraintCore(map);
            var geneList = this.merge(geneMap);
            var score = this.computerFitnessScore(geneList);
            if (bestScore < score) {
                bestScore = score;
//                System.out.println(bestScore);
                bestGeneMap = geneMap;
            }
        }

        // 满足硬约束和软约束 有迭代次数限制
//        geneMap = this.geneticMoreSatisfiedCore(gradeClassNoGeneMap);

        cattyResult.setData(bestGeneMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 使用遗传算法排课
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
    public CattyResult<HashMap<String, List<String>>> algorithmInPlanTimeTableWithPopulationGenetic(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
        CattyResult<HashMap<String, List<String>>> cattyResult = new CattyResult<>();
        HashMap<String, List<String>> bestGeneMap = new HashMap<>();

        HashMap<Integer, List<String>> popHashMap = new HashMap<>();
        for (int i = START_INDEX; i <= POPULATION; i++) {
            // 获取初始基因编码
            var geneMap = this.initGene(timeTablingUseGeneticDTO);

            // 进行初步的时间分配
            var initRandomTimeList = this.initRandomTime(geneMap);

            popHashMap.put(i, initRandomTimeList);
        }

        for (int evolutionTime = START_INDEX; evolutionTime <= EVOLUTION_TIMES; evolutionTime++) {
            // 随机交叉
            // 计算有多少种群要交叉
            HashMap<Integer, List<String>> crossoverPopHashMap = new HashMap<>(popHashMap);
            double crossoverPopulation = CROSSOVER_RATE * POPULATION;
            int crossoverNo = (int) crossoverPopulation;
            for (int i = START_INDEX; i <= crossoverNo; i++) {
                // 先随机选择两个种群
                int firstNo = START_INDEX + (int) (Math.random() * POPULATION);
                List<String> firstGeneList = crossoverPopHashMap.get(firstNo);
                var oldFirstGeneListScore = this.computerFitnessScore(firstGeneList);
                int secNo = START_INDEX + (int) (Math.random() * POPULATION);
                List<String> secGeneList = crossoverPopHashMap.get(secNo);
                var oldSecGeneListScore = this.computerFitnessScore(secGeneList);

                // 计算随机基因
                var firstRandomGeneDTO = this.computerRandomGeneDTO(firstGeneList);
                var secRandomGeneDTO = this.computerRandomGeneDTO(secGeneList);

                // 组装新基因
                var firstNewGene = firstRandomGeneDTO.getGeneWithoutClassTime() + secRandomGeneDTO.getClassTime();
                var secNewGene = secRandomGeneDTO.getGeneWithoutClassTime() + firstRandomGeneDTO.getClassTime();
                firstGeneList.set(firstRandomGeneDTO.getRandomNo(), firstNewGene);
                secGeneList.set(secRandomGeneDTO.getRandomNo(), secNewGene);

                var newFirstGeneListScore = this.computerFitnessScore(firstGeneList);
                var newSecGeneListScore = this.computerFitnessScore(secGeneList);
                if (oldFirstGeneListScore < newFirstGeneListScore && oldFirstGeneListScore < newSecGeneListScore) {
                    popHashMap.put(firstNo, firstGeneList);
                }
                if (oldSecGeneListScore < newFirstGeneListScore && oldSecGeneListScore < newSecGeneListScore) {
                    popHashMap.put(secNo, secGeneList);
                }

            }

            // 变异
            double mutateRatePopulation = MUTATE_RATE * POPULATION;
            int mutateNo = (int) mutateRatePopulation;
            for (int i = START_INDEX; i <= mutateNo; i++) {
                int random = START_INDEX + (int) (Math.random() * POPULATION);
                List<String> geneList = crossoverPopHashMap.get(random);
                var oldScore = this.computerFitnessScore(geneList);
                var randomGeneDTO = this.computerRandomGeneDTO(geneList);
                var gene = this.mutate(randomGeneDTO.getGene());
                geneList.set(randomGeneDTO.getRandomNo(), gene);

                var newScore = this.computerFitnessScore(geneList);
                if (oldScore < newScore) {
                    popHashMap.put(random, geneList);
                }

            }

            // 解决冲突
            for (Integer i : popHashMap.keySet()) {
                var geneList = popHashMap.get(i);
                geneList = this.eliminateConflicts(geneList);
                popHashMap.put(i, geneList);
            }

            int bestScore = ZERO;
            List<String> bestGeneList = new ArrayList<>();
            for (Integer i : popHashMap.keySet()) {
                var geneList = popHashMap.get(i);
                var score = this.computerFitnessScore(geneList);
                if (bestScore < score) {
                    bestScore = score;
                    bestGeneList = geneList;
                }
            }

            var score = this.computerFitnessScore(bestGeneList);
            System.out.println(score);
            List<String> fixedGeneList = new ArrayList<>();
            List<String> unFixedGeneList = new ArrayList<>();
            for (String gene : bestGeneList) {
                var fixFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
                if (GeneticDefaultValueDTO.FIXED.equals(fixFlag)) {
                    fixedGeneList.add(gene);
                }
                if (GeneticDefaultValueDTO.UN_FIXED.equals(fixFlag)) {
                    unFixedGeneList.add(gene);
                }
            }
            bestGeneMap.put(GeneticDefaultValueDTO.FIXED, fixedGeneList);
            bestGeneMap.put(GeneticDefaultValueDTO.UN_FIXED, unFixedGeneList);
        }

        cattyResult.setData(bestGeneMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 计算随机基因
     *
     * @param geneList
     * @return
     */
    private RandomGeneDTO computerRandomGeneDTO(List<String> geneList) {
        int geneIndex;
        String gene;
        String fixed;
        do {
            geneIndex = START_INDEX + (int) (Math.random() * geneList.size() - START_INDEX);
            gene = geneList.get(geneIndex);
            fixed = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
        } while (GeneticDefaultValueDTO.FIXED.equals(fixed));
        var geneWithoutClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
        var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
        RandomGeneDTO randomGeneDTO = new RandomGeneDTO();
        randomGeneDTO.setRandomNo(geneIndex);
        randomGeneDTO.setGene(gene);
        randomGeneDTO.setGeneWithoutClassTime(geneWithoutClassTime);
        randomGeneDTO.setClassTime(classTime);
        return randomGeneDTO;
    }

    /**
     * 计算适应度函数
     *
     * @param geneList
     * @return
     */
    public int computerFitnessScore(List<String> geneList) {

        // 获取特殊教室的班级上限
        var subjectClassroomMaxMap = classroomMaxCapacityService.getClassroomMaxCapacityMap();

        // 初始化年级班级和节次
        HashMap<String, List<Integer>> gradeClassNoClassTimeMap = new HashMap<>();
        // 初始化小课不在早上第一二节的数量
        int otherSubjectFitnessCount = ZERO;
        // 初始化年级班级工作日各小课的数量map
        HashMap<String, Integer> gradeClassNoWorkDayOtherSubjectCountMap = new HashMap<>();
        // 初始化时间需要教室课程的数量map
        HashMap<Integer, HashMap<Integer, Integer>> otherSubjectClassTimeCountMap = new HashMap<>();
        // 获取年级班级时间上课数量map
        HashMap<String, Integer> gradeClassNoWorkDaySubjectCountMap = new HashMap<>();
        // 获取语文课和数学课map
        HashMap<String, List<Integer>> gradeClassNoWorkDayChineseTimeMap = new HashMap<>();
        HashMap<String, List<Integer>> gradeClassNoWorkDayMathsTimeMap = new HashMap<>();
        // 获取教师冲突map
        HashMap<String, Integer> teacherClassTimeCountMap = new HashMap<>();
        // 获取教师工作日连堂课次数map
        HashMap<String, Integer> teacherIdWorkDayCountMap = new HashMap<>();
        for (String gene : geneList) {
            // 截取上课时间是否固定，如果固定，就替换为下一条gene
            var fixTime = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
            if (GeneticDefaultValueDTO.FIXED.equals(fixTime)) {
                continue;
            }

            // 截取时间
            var classTimeString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
            var classTime = Integer.valueOf(classTimeString);
            // 计算天数
            var workDay = this.computerWorkDay(classTime);
            // 获取第几节课
            var time = this.computerTime(classTime);
            // 截取科目
            var subjectIdString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);
            var subjectId = Integer.valueOf(subjectIdString);
            // 截取科目类型
            var subjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX, gene);
            var subjectType = Integer.valueOf(subjectTypeString);
            // 截取年级和班级
            var gradeClassNoString = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
            // 截取教师id
            var teacherIdString = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);

            // 记录每个班是否每节课都有课
            var gradeClassNoClassTime = gradeClassNoString + classTimeString;
            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNoClassTime);
            if (CollectionUtils.isEmpty(classTimeList)) {
                classTimeList = new ArrayList<>();
                classTimeList.add(classTime);
                gradeClassNoClassTimeMap.put(gradeClassNoClassTime, classTimeList);
            } else {
                classTimeList.add(classTime);
                gradeClassNoClassTimeMap.put(gradeClassNoClassTime, classTimeList);
            }

            // 记录每天每个班每节课课程数量
            gradeClassNoWorkDaySubjectCountMap.merge(gradeClassNoClassTime, STEP, Integer::sum);

            // 记录小课在第三节或者下午的数量
            boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectType)
                    || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType);
            boolean otherSubjectNotInOneOrTwoClassTimeFlag = otherSubjectFlag && time > SchoolTimeTableDefaultValueDTO.getMorningSecTime();
            if (otherSubjectNotInOneOrTwoClassTimeFlag) {
                otherSubjectFitnessCount = otherSubjectFitnessCount + STEP;
            }

            // 记录每天小课的数量
            if (otherSubjectFlag) {
                var gradeClassNoWorkDaySubjectId = gradeClassNoString + workDay.toString() + subjectIdString;
                gradeClassNoWorkDayOtherSubjectCountMap.merge(gradeClassNoWorkDaySubjectId, STEP, Integer::sum);
            }

            // 记录同一时间三种特殊教室数量
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType)) {

                if (otherSubjectClassTimeCountMap.get(subjectId) == null) {
                    HashMap<Integer, Integer> classTimeCountMap = new HashMap<>();
                    classTimeCountMap.put(classTime, STEP);
                    otherSubjectClassTimeCountMap.put(subjectId, classTimeCountMap);
                } else {
                    var classTimeCountMap = otherSubjectClassTimeCountMap.get(subjectId);
                    var count = classTimeCountMap.get(classTime);
                    if (count == null) {
                        count = ZERO;
                    }
                    classTimeCountMap.put(classTime, count + STEP);
                    otherSubjectClassTimeCountMap.put(subjectId, classTimeCountMap);
                }
            }

            // 记录每天语文课和数学课间隔
            boolean chineseFlag = SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(subjectId);
            if (chineseFlag) {
                this.markGradeClassNoWorkDayList(gradeClassNoString, workDay, time, gradeClassNoWorkDayChineseTimeMap);
            }
            boolean mathsFlag = SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(subjectId);
            if (mathsFlag) {
                this.markGradeClassNoWorkDayList(gradeClassNoString, workDay, time, gradeClassNoWorkDayMathsTimeMap);
            }

            // 记录教师上课节次，大于1说明查看冲突
            var teacherIdClassTime = teacherIdString + classTimeString;
            teacherClassTimeCountMap.merge(teacherIdClassTime, STEP, Integer::sum);

            // 记录教师每天上课节数
            var teacherIdWorkDay = teacherIdString + workDay;
            teacherIdWorkDayCountMap.merge(teacherIdWorkDay, STEP, Integer::sum);
        }

        int fitnessScore = ZERO;
        // 如果每个班级都有课，那么ADD_BIG_SCORE 分
        for (String gradeClassNo : gradeClassNoClassTimeMap.keySet()) {
            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNo);
            var count = (int) classTimeList.stream().distinct().count();
            fitnessScore = fitnessScore + count * BIG_SCORE;
        }

        // 如果每天每个班每节课只有一种课程，则加 ADD_BIG_SCORE 分
        var classTimeOnlyCount = (int) gradeClassNoWorkDaySubjectCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
        fitnessScore = fitnessScore + classTimeOnlyCount * BIG_SCORE;

        // 如果小课在第三节或者下午，则加 ADD_SCORE 分
        fitnessScore = fitnessScore + otherSubjectFitnessCount * ADD_SCORE;

        // 如果相同的小课每个班每天只有一节，则加 ADD_BIG_SCORE 分
        if (CollectionUtils.isNotEmpty(gradeClassNoWorkDayOtherSubjectCountMap.keySet())) {
            int allMatchCount = (int) gradeClassNoWorkDayOtherSubjectCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
            fitnessScore = fitnessScore + allMatchCount * BIG_SCORE;
        }

        // 如果同一时间三种特殊教室没有超上限数量，则加 ADD_SCORE 分
        for (Integer subjectId : otherSubjectClassTimeCountMap.keySet()) {
            var maxCount = subjectClassroomMaxMap.get(subjectId);
            var classTimeCountMap = otherSubjectClassTimeCountMap.get(subjectId);
            for (Integer classTime : classTimeCountMap.keySet()) {
                var count = classTimeCountMap.get(classTime);
                if (count <= maxCount) {
                    fitnessScore = fitnessScore + count * BIG_SCORE;
                }
            }
        }

        // 如果两节相同的主课相隔时间长，则加分，分数 = 下一节 - 本节课
        for (String gradeClassNoWorkDay : gradeClassNoWorkDayChineseTimeMap.keySet()) {
            var timeList = gradeClassNoWorkDayChineseTimeMap.get(gradeClassNoWorkDay);
            if (timeList.contains(SchoolTimeTableDefaultValueDTO.getMorningFirTime())
                    || timeList.contains(SchoolTimeTableDefaultValueDTO.getMorningSecTime())) {
                fitnessScore = fitnessScore + ADD_SCORE;
            }
            var minInterval = MathsUtils.getMinInterval(timeList);
            if (minInterval != null) {
                fitnessScore = fitnessScore + minInterval * ADD_SCORE;
            }
        }
        for (String gradeClassNoWorkDay : gradeClassNoWorkDayMathsTimeMap.keySet()) {
            var timeList = gradeClassNoWorkDayMathsTimeMap.get(gradeClassNoWorkDay);
            if (timeList.contains(SchoolTimeTableDefaultValueDTO.getMorningFirTime())
                    || timeList.contains(SchoolTimeTableDefaultValueDTO.getMorningSecTime())) {
                fitnessScore = fitnessScore + ADD_SCORE;
            }
            var minInterval = MathsUtils.getMinInterval(timeList);
            if (minInterval != null) {
                fitnessScore = fitnessScore + minInterval * ADD_SCORE;
            }
        }

        // 如果教师没有冲突，则加 ADD_BIG_SCORE 分
        var teacherClassTimeCount = (int) teacherClassTimeCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
        fitnessScore = fitnessScore + teacherClassTimeCount * BIG_SCORE;

        // 如果教师一天课程没有超过4节，则加 ADD_SCORE 分
        for (String teacherIdClassTime : teacherIdWorkDayCountMap.keySet()) {
            var count = teacherIdWorkDayCountMap.get(teacherIdClassTime);
            if (count <= SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize()) {
                fitnessScore = fitnessScore + (SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize() - count) * ADD_SCORE;
            }
        }

        return fitnessScore;
    }

    /**
     * 记录上课次数
     *
     * @param gradeClassNoString
     * @param workDay
     * @param time
     * @param gradeClassNoWorkDayMap
     * @return
     */
    private HashMap<String, List<Integer>> markGradeClassNoWorkDayList(String gradeClassNoString, Integer workDay, Integer time, HashMap<String, List<Integer>> gradeClassNoWorkDayMap) {
        var gradeClassNoWorkDay = gradeClassNoString + workDay;
        if (gradeClassNoWorkDayMap.get(gradeClassNoWorkDay) == null) {
            List<Integer> timeList = new ArrayList<>();
            timeList.add(time);
            gradeClassNoWorkDayMap.put(gradeClassNoWorkDay, timeList);
        } else {
            var timeList = gradeClassNoWorkDayMap.get(gradeClassNoWorkDay);
            timeList.add(time);
            gradeClassNoWorkDayMap.put(gradeClassNoWorkDay, timeList);
        }

        return gradeClassNoWorkDayMap;
    }

    /**
     * 将课程进行编码，组成初始基因
     * 初始基因的规则为：是否固定+年级+班级+课程编号+课程节次+课程属性+教师编号+开课时间
     * 不固定为0，固定为1
     * 开课时间定为1-35
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
    public HashMap<String, List<String>> initGene(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
        var gradeSubjectMap = timeTablingUseGeneticDTO.getGradeSubjectMap();
        var gradeClassCountMap = timeTablingUseGeneticDTO.getGradeClassCountMap();
        var subjectGradeClassTeacherMap = timeTablingUseGeneticDTO.getSubjectGradeClassTeacherMap();
        var specialSubjectTimeMap = timeTablingUseGeneticDTO.getSpecialSubjectTimeMap();

        HashMap<String, List<String>> geneGradeSubjectMap = new HashMap<>();
        List<String> unFixedTimeGradeSubjectList = new ArrayList<>();
        List<String> fixedTimeGradeSubjectList = new ArrayList<>();

        for (Integer grade : gradeSubjectMap.keySet()) {
            var gradeString = this.getStandard(grade.toString(), GeneticDefaultValueDTO.GRADE_STANDARD_LENGTH);

            var classSize = gradeClassCountMap.get(grade);
            for (int classNo = START_INDEX; classNo <= classSize; classNo++) {
                var classNoString = this.getStandard(String.valueOf(classNo), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);

                var subjectDTOList = gradeSubjectMap.get(grade);
                for (SubjectDTO subjectDTO : subjectDTOList) {
                    if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {

                        SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
                        subjectGradeClassDTO.setSubjectId(subjectDTO.getSubjectId());
                        subjectGradeClassDTO.setGrade(grade);
                        subjectGradeClassDTO.setClassNum(classNo);
                        var teacherId = subjectGradeClassTeacherMap.get(subjectGradeClassDTO);
                        var teacherIdString = this.getStandard(teacherId.toString(), GeneticDefaultValueDTO.TEACHER_ID_STANDARD_LENGTH);

                        var subjectIdString = this.getStandard(subjectDTO.getSubjectId().toString(), GeneticDefaultValueDTO.SUBJECT_ID_STANDARD_LENGTH);
                        var subjectFrequency = subjectDTO.getFrequency();
                        for (int frequency = START_INDEX; frequency <= subjectFrequency; frequency++) {
                            var frequencyString = this.getStandard(String.valueOf(frequency), GeneticDefaultValueDTO.SUBJECT_FREQUENCY_STANDARD_LENGTH);
                            var gene = GeneticDefaultValueDTO.UN_FIXED + gradeString + classNoString + subjectIdString + frequencyString + subjectDTO.getType() + teacherIdString + GeneticDefaultValueDTO.CLASS_TIME_STANDARD;
                            unFixedTimeGradeSubjectList.add(gene);
                        }
                    }

                    if (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(subjectDTO.getType())) {
                        var subjectIdString = this.getStandard(subjectDTO.getSubjectId().toString(), GeneticDefaultValueDTO.SUBJECT_ID_STANDARD_LENGTH);
                        var subjectFrequency = subjectDTO.getFrequency();
                        for (int frequency = START_INDEX; frequency <= subjectFrequency; frequency++) {
                            var frequencyString = this.getStandard(String.valueOf(frequency), GeneticDefaultValueDTO.SUBJECT_FREQUENCY_STANDARD_LENGTH);

                            var subjectIdAndFrequency = subjectIdString.concat(frequencyString);
                            var classTime = specialSubjectTimeMap.get(subjectIdAndFrequency);
                            var gene = GeneticDefaultValueDTO.FIXED + gradeString + classNoString + subjectIdString + frequencyString + subjectDTO.getType() + GeneticDefaultValueDTO.NO_TEACHER_ID_STANDARD + classTime;
                            fixedTimeGradeSubjectList.add(gene);
                        }

                    }
                }
            }

        }

        geneGradeSubjectMap.put(GeneticDefaultValueDTO.FIXED, fixedTimeGradeSubjectList);
        geneGradeSubjectMap.put(GeneticDefaultValueDTO.UN_FIXED, unFixedTimeGradeSubjectList);

        return geneGradeSubjectMap;
    }

    /**
     * 初始化课程的时间分配
     *
     * @param geneMap
     * @return
     */
    public List<String> initRandomTime(HashMap<String, List<String>> geneMap) {

        List<String> initRandomList = new ArrayList<>();
        List<String> geneList = new ArrayList<>();
        for (String gene : geneMap.keySet()) {
            geneList.addAll(geneMap.get(gene));
        }

        for (String geneKey : geneMap.keySet()) {
            if (GeneticDefaultValueDTO.UN_FIXED.equals(geneKey)) {
                var unFixedGeneList = geneMap.get(geneKey);
                for (String gene : unFixedGeneList) {
                    int time = GeneticDefaultValueDTO.MIN_TIME + (int) (Math.random() * (GeneticDefaultValueDTO.MAX_TIME));
                    var classTime = this.getStandard(String.valueOf(time), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);
                    var initGene = gene.substring(GeneticDefaultValueDTO.GENE_BEGIN_INDEX, GeneticDefaultValueDTO.GENE_END_INDEX) + classTime;
                    initRandomList.add(initGene);
                }

            }
            if (GeneticDefaultValueDTO.FIXED.equals(geneKey)) {
                initRandomList.addAll(geneMap.get(geneKey));
            }
        }

        return initRandomList;
    }


    /**
     * 初始化课程的时间分配
     *
     * @param geneMap
     * @param gradeClassNoCountMap
     * @return
     */
    public List<String> initTime(HashMap<String, List<String>> geneMap, HashMap<Integer, Integer> gradeClassNoCountMap) {

        List<String> initTimeGradeSubjectList = new ArrayList<>();
        List<String> geneList = new ArrayList<>();
        for (String gene : geneMap.keySet()) {
            geneList.addAll(geneMap.get(gene));
        }

        var timeCountMap = this.initNumCountMap(geneList, gradeClassNoCountMap);

        for (String geneKey : geneMap.keySet()) {
            if (GeneticDefaultValueDTO.UN_FIXED.equals(geneKey)) {
                var unFixedGeneList = geneMap.get(geneKey);
                for (String gene : unFixedGeneList) {
                    Integer time = this.rouletteWheelSelection(timeCountMap);
                    var classTime = this.getStandard(time.toString(), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);
                    var initGene = gene.substring(GeneticDefaultValueDTO.GENE_BEGIN_INDEX, GeneticDefaultValueDTO.GENE_END_INDEX) + classTime;
                    initTimeGradeSubjectList.add(initGene);
                }

            }
            if (GeneticDefaultValueDTO.FIXED.equals(geneKey)) {
                initTimeGradeSubjectList.addAll(geneMap.get(geneKey));
            }
        }

        return initTimeGradeSubjectList;
    }

    /**
     * 截取基因
     *
     * @param partName
     * @param source
     * @return
     */
    private String cutGeneIndex(String partName, String source) {
        switch (partName) {
            case GeneticDefaultValueDTO.FIXED_INDEX:
                return source.substring(GeneticDefaultValueDTO.FIXED_BEGIN_INDEX, GeneticDefaultValueDTO.FIXED_END_INDEX);
            case GeneticDefaultValueDTO.GRADE_INDEX:
                return source.substring(GeneticDefaultValueDTO.GRADE_BEGIN_INDEX, GeneticDefaultValueDTO.GRADE_END_INDEX);
            case GeneticDefaultValueDTO.CLASS_NO_INDEX:
                return source.substring(GeneticDefaultValueDTO.CLASS_NO_BEGIN_INDEX, GeneticDefaultValueDTO.CLASS_NO_END_INDEX);
            case GeneticDefaultValueDTO.GRADE_CLASS_INDEX:
                return source.substring(GeneticDefaultValueDTO.GRADE_CLASS_BEGIN_INDEX, GeneticDefaultValueDTO.GRADE_CLASS_END_INDEX);
            case GeneticDefaultValueDTO.SUBJECT_ID_INDEX:
                return source.substring(GeneticDefaultValueDTO.SUBJECT_ID_BEGIN_INDEX, GeneticDefaultValueDTO.SUBJECT_ID_END_INDEX);
            case GeneticDefaultValueDTO.SUBJECT_FREQUENCY_INDEX:
                return source.substring(GeneticDefaultValueDTO.SUBJECT_FREQUENCY_BEGIN_INDEX, GeneticDefaultValueDTO.SUBJECT_FREQUENCY_END_INDEX);
            case GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX:
                return source.substring(GeneticDefaultValueDTO.SUBJECT_TYPE_BEGIN_INDEX, GeneticDefaultValueDTO.SUBJECT_TYPE_END_INDEX);
            case GeneticDefaultValueDTO.TEACHER_ID_INDEX:
                return source.substring(GeneticDefaultValueDTO.TEACHER_ID_BEGIN_INDEX, GeneticDefaultValueDTO.TEACHER_ID_END_INDEX);
            case GeneticDefaultValueDTO.CLASS_TIME_INDEX:
                return source.substring(GeneticDefaultValueDTO.CLASS_TIME_BEGIN_INDEX);
            case GeneticDefaultValueDTO.GENE_INDEX:
                return source.substring(GeneticDefaultValueDTO.GENE_BEGIN_INDEX, GeneticDefaultValueDTO.GENE_END_INDEX);
            default:
                throw new TransactionException("不能拆分基因: " + source);
        }

    }

    /**
     * 按照年级班级进行分组
     *
     * @param initTimeGradeSubjectList
     * @return
     */
    public HashMap<String, List<String>> getGradeClassNoGeneMap(List<String> initTimeGradeSubjectList) {
        HashMap<String, List<String>> gradeClassNoGeneMap = new HashMap<>();
        for (String gene : initTimeGradeSubjectList) {
            var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
            if (CollectionUtils.isEmpty(gradeClassNoGeneMap.get(gradeClassNo))) {
                List<String> timeGradeSubjectList = new ArrayList<>();
                timeGradeSubjectList.add(gene);
                gradeClassNoGeneMap.put(gradeClassNo, timeGradeSubjectList);
            } else {
                var timeGradeSubjectList = gradeClassNoGeneMap.get(gradeClassNo);
                timeGradeSubjectList.add(gene);
                gradeClassNoGeneMap.put(gradeClassNo, timeGradeSubjectList);
            }
        }

        return gradeClassNoGeneMap;
    }

    /**
     * 遗传算法核心 交叉和变异
     *
     * @param gradeClassNoGeneMap
     * @return
     */
    private HashMap<String, List<String>> geneticHardConstraintCore(HashMap<String, List<String>> gradeClassNoGeneMap) {

        List<Integer> fitnessScoreList = new ArrayList<>();
        int score = START_SCORE;
        while (score != ZERO) {
            // 先按照班级合并
            var mergeList = this.merge(gradeClassNoGeneMap);

            //第一步完成交叉操作,产生新一代的父本
            var crossoverList = this.crossover(mergeList, FitnessFunctionEnum.HARD_SATISFIED);

            // 由于没有空余时间，所以不能变异
//            var mutateList = this.mutate(crossoverList, timeCountMap);

            // 计算适度函数
            score = this.computerFitnessFunction(crossoverList, FitnessFunctionEnum.HARD_SATISFIED);

            // 计算适度函数
            var fitnessScore = this.computerFitnessScore(crossoverList);
            fitnessScoreList.add(fitnessScore);

            //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
            gradeClassNoGeneMap = this.getGradeClassNoGeneMap(crossoverList);
        }

        // 记录分数
        this.markToTXT(String.valueOf(ZERO), fitnessScoreList);

        return gradeClassNoGeneMap;
    }

    /**
     * 记录分数到txt
     *
     * @param txtName
     * @param scoreList
     */
    private void markToTXT(String txtName, List<Integer> scoreList) {
        try {
            File newLog = new File("/Users/wangxin/Downloads/" + txtName + ".txt");
            if (!newLog.isFile()) {
                boolean createFlag = newLog.createNewFile();
                if (!createFlag) {
                    throw new TransactionException("生成记录表失败");
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newLog), "utf-8"));
            for (Integer x : scoreList) {
                bw.write(x + "\r\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 遗传算法核心 交叉和变异
     *
     * @param gradeClassNoGeneMap
     * @return
     */
    private HashMap<String, List<String>> geneticMoreSatisfiedCore(HashMap<String, List<String>> gradeClassNoGeneMap) {

        for (int i = ZERO; i < GeneticDefaultValueDTO.GENE_TIMES; i++) {
            // 先按照班级合并
            var mergeList = this.merge(gradeClassNoGeneMap);

            //第一步完成交叉操作,产生新一代的父本
            var crossoverList = this.crossover(mergeList, FitnessFunctionEnum.MORE_SATISFIED);

            // 由于没有空余时间，所以不能变异
//            var mutateList = this.mutate(crossoverList, timeCountMap);

            //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
            gradeClassNoGeneMap = this.getGradeClassNoGeneMap(crossoverList);
        }

        return gradeClassNoGeneMap;
    }

    /**
     * 交叉操作
     *
     * @param mergeList
     * @param fitnessFunctionEnum
     * @return
     */
    public List<String> crossover(List<String> mergeList, FitnessFunctionEnum fitnessFunctionEnum) {

        // 进行基因交叉生成新个体
        List<String> newGeneList = this.selectGene(mergeList);

        // 分别计算适度函数值
        Integer oldScore = this.computerFitnessFunction(mergeList, fitnessFunctionEnum);
        Integer newScore = this.computerFitnessFunction(newGeneList, fitnessFunctionEnum);

        // 对父代和新生基因进行适度函数对比，选择适度高的基因进入下一代遗传
        if (oldScore > newScore) {
            return newGeneList;
        }

        return mergeList;
    }

    /**
     * 合并基因
     *
     * @param gradeClassNoGeneMap
     * @return
     */
    public List<String> merge(HashMap<String, List<String>> gradeClassNoGeneMap) {
        List<String> geneList = new ArrayList<>();
        gradeClassNoGeneMap.values().forEach(geneList::addAll);
        return geneList;
    }

    /**
     * 进行基因交叉生成新个体
     *
     * @param oldGeneList
     * @return
     */
    public List<String> selectGene(List<String> oldGeneList) {
        List<String> newGeneList = new ArrayList<>(oldGeneList);
        boolean flag = false;
        do {
            // 只选择有冲突的个体，有可能早熟
//            int firstRandomNo = this.getGeneIndex(oldGeneList);
            // 生成两个随机数
            int firstRandomNo = (int) (Math.random() * (oldGeneList.size()));
            int secRandomNo = (int) (Math.random() * (oldGeneList.size()));
            // 选取对应的两条基因
            var firstGene = newGeneList.get(firstRandomNo);
            var secGene = newGeneList.get(secRandomNo);
            // 判断是否固定时间的基因，如果是就重新选择
            var firstGeneFixedNo = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, firstGene);
            var secGeneFixedNo = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, secGene);
            var fixedFlag = GeneticDefaultValueDTO.FIXED.equals(firstGeneFixedNo) || GeneticDefaultValueDTO.FIXED.equals(secGeneFixedNo);
            // 如果不是相同基因或者固定基因，就可以选择这两条基因
            boolean selectFlag = !(firstRandomNo == secRandomNo || fixedFlag);
            if (selectFlag) {
                // 获取两条基因的时间
                String firstClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, firstGene);
                String secClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, secGene);

                // 互换时间
                String firstMainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, firstGene);
                String secMainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, secGene);

                String firstNewGene = firstMainGene.concat(secClassTime);
                String secNewGene = secMainGene.concat(firstClassTime);

                // 替换现有基因
                newGeneList.set(firstRandomNo, firstNewGene);
                newGeneList.set(secRandomNo, secNewGene);

                flag = true;
            }
        } while (!flag);

        return newGeneList;
    }

    /**
     * 获取一条基因
     *
     * @param oldGeneList
     * @return
     */
    public Integer getGeneIndex(List<String> oldGeneList) {
        HashMap<String, String> gradeClassNoClassTimeMap = new HashMap<>();
        HashMap<String, String> teacherIdClassTimeMap = new HashMap<>();
        HashMap<String, String> otherSubjectIdMap = new HashMap<>();
        int num = ZERO;
        for (String gene : oldGeneList) {
            var fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
            if (GeneticDefaultValueDTO.UN_FIXED.equals(fixedFlag)) {
                // 截取时间
                var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);

                // 课程冲突 同一时间一个班级上了多门课
                var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);

                // 拼接同一时间一个年级的班级
                var gradeClassNoClassTime = gradeClassNo.concat(classTime);

                if (gradeClassNoClassTimeMap.get(gradeClassNoClassTime) == null) {
                    gradeClassNoClassTimeMap.put(gradeClassNoClassTime, gene);
                } else {
                    return num;
                }

                // 教师冲突 同一时间一个教师上多个班的课
                var teacherId = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);

                // 拼接教师时间
                var teacherIdClassTime = teacherId.concat(classTime);

                if (teacherIdClassTimeMap.get(teacherIdClassTime) == null) {
                    teacherIdClassTimeMap.put(teacherIdClassTime, gene);
                } else {
                    return num;
                }

                // 拆分课程id 和 类型
                var subjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX, gene);
                var subjectType = Integer.valueOf(subjectTypeString);
                if (SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectType)
                        || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType)) {
                    var subjectId = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);

                    var workDay = this.computerWorkDay(Integer.valueOf(classTime));
                    var gradeClassNoOtherSubjectIdClassTime = gradeClassNo.concat(subjectId).concat(workDay.toString());
                    if (otherSubjectIdMap.get(gradeClassNoOtherSubjectIdClassTime) == null) {
                        otherSubjectIdMap.put(gradeClassNoOtherSubjectIdClassTime, gene);
                    } else {
                        return num;
                    }
                }

            }
            num++;
        }

        return num;
    }

    /**
     * 进行基因交叉生成新个体
     *
     * @param oldGeneList
     * @return
     */
    public List<String> selectGenes(List<String> oldGeneList, Integer nums) {
        List<String> newGeneList = new ArrayList<>();
        for (int i = ZERO; i <= nums; i++) {
            newGeneList = this.selectGene(oldGeneList);
            oldGeneList = newGeneList;
        }

        return newGeneList;
    }

    /**
     * 构造适度函数 查看冲突
     *
     * @param geneList
     * @return
     */
    private Integer constructingHardSatisfiedFitnessFunction(List<String> geneList) {

        HashMap<String, Integer> gradeClassNoClassTimeCountMap = new HashMap<>();
        HashMap<String, Integer> teacherIdClassTimeCountMap = new HashMap<>();
        HashMap<String, Integer> otherSubjectIdCountMap = new HashMap<>();
        for (String gene : geneList) {
            var fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
            if (GeneticDefaultValueDTO.UN_FIXED.equals(fixedFlag)) {
                // 截取时间
                var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);

                // 课程冲突 同一时间一个班级上了多门课
                var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);

                // 拼接同一时间一个年级的班级
                var gradeClassNoClassTime = gradeClassNo.concat(classTime);

                gradeClassNoClassTimeCountMap.merge(gradeClassNoClassTime, STEP, Integer::sum);

                // 教师冲突 同一时间一个教师上多个班的课
                var teacherId = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);

                // 拼接教师时间
                var teacherIdClassTime = teacherId.concat(classTime);

                teacherIdClassTimeCountMap.merge(teacherIdClassTime, STEP, Integer::sum);

                // 拆分课程id 和 类型
                var subjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX, gene);
                var subjectType = Integer.valueOf(subjectTypeString);
                if (SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectType)
                        || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType)) {
                    var subjectId = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);

                    var workDay = this.computerWorkDay(Integer.valueOf(classTime));
                    var gradeClassNoOtherSubjectIdClassTime = gradeClassNo.concat(subjectId).concat(workDay.toString());
                    otherSubjectIdCountMap.merge(gradeClassNoOtherSubjectIdClassTime, STEP, Integer::sum);
                }

            }
        }

        // 不满足硬约束条件的次数
        // 同一时间同一个班上了多节课
        int oneTimeOneClassMoreSubjectCount = ZERO;
        for (String gradeClassNoClassTime : gradeClassNoClassTimeCountMap.keySet()) {
            var gradeClassNoClassTimeCount = gradeClassNoClassTimeCountMap.get(gradeClassNoClassTime);
            oneTimeOneClassMoreSubjectCount = oneTimeOneClassMoreSubjectCount + gradeClassNoClassTimeCount - ONLY_ONE_COUNT;
        }

        // 同一时间同一个教师上多个班的课
        int oneTimeOneTeacherMoreClassCount = ZERO;
        for (String teacherIdClassTime : teacherIdClassTimeCountMap.keySet()) {
            var teacherIdClassTimeCount = teacherIdClassTimeCountMap.get(teacherIdClassTime);
            oneTimeOneTeacherMoreClassCount = oneTimeOneTeacherMoreClassCount + teacherIdClassTimeCount - ONLY_ONE_COUNT;
        }

        // 小课每个年级的每个班每天只上一次
        int oneClassMoreOtherSubject = ZERO;
        for (String gradeClassNoOtherSubjectIdClassTime : otherSubjectIdCountMap.keySet()) {
            oneClassMoreOtherSubject = oneClassMoreOtherSubject + otherSubjectIdCountMap.get(gradeClassNoOtherSubjectIdClassTime) - ONLY_ONE_COUNT;
        }

        int hardCount = oneTimeOneClassMoreSubjectCount + oneTimeOneTeacherMoreClassCount + oneClassMoreOtherSubject;
        return hardCount;
    }

    /**
     * 满足硬约束条件和软约束条件
     *
     * @param geneList
     * @return
     */
    private Integer constructingMoreSatisfiedFitnessFunction(List<String> geneList) {

        // 硬约束条件
        var hardCount = this.constructingHardSatisfiedFitnessFunction(geneList);

        HashMap<String, Integer> teacherWorkDayCountMap = new HashMap<>();
        HashMap<Integer, Integer> subjectTypeCountMap = new HashMap<>();
        for (String gene : geneList) {
            var fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
            if (GeneticDefaultValueDTO.UN_FIXED.equals(fixedFlag)) {
                var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
                var workDay = this.computerWorkDay(Integer.valueOf(classTime));

                // 获取教师id
                var teacherId = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);
                var teacherWorkDay = teacherId.concat(workDay.toString());
                teacherWorkDayCountMap.merge(teacherWorkDay, STEP, Integer::sum);

                // 确定第几节课程
                var time = this.computerTime(Integer.valueOf(classTime));
                // 如果是第一第二节课，要看是否为主课
                if (SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time)
                        || SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time)) {

                    // 获取课程类型
                    var subjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX, gene);
                    Integer subjectType = Integer.valueOf(subjectTypeString);

                    subjectTypeCountMap.merge(subjectType, STEP, Integer::sum);
                }
            }
        }

        // 教师一天上课不能超过4节
        int teacherOutMaxTimeCount = ZERO;
        for (String teacherWorkDay : teacherWorkDayCountMap.keySet()) {
            var count = teacherWorkDayCountMap.get(teacherWorkDay);
            if (count > TEACHER_MAX_TIME) {
                teacherOutMaxTimeCount = teacherOutMaxTimeCount + count - TEACHER_MAX_TIME;
            }
        }

        // 第一二节必须是语文和数学课
        int noMainSubjectCount = ZERO;
        for (Integer subjectType : subjectTypeCountMap.keySet()) {
            if (!SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectType)) {
                var typeCount = subjectTypeCountMap.get(subjectType);
                noMainSubjectCount = noMainSubjectCount + typeCount;
            }
        }

        // 不满足软约束条件的次数
        var softCount = teacherOutMaxTimeCount + noMainSubjectCount;
        var score = hardCount * hardCount + softCount;

        return score;
    }

    /**
     * 计算适度函数值
     *
     * @param geneList
     * @return
     */
    public Integer computerFitnessFunction(List<String> geneList, FitnessFunctionEnum fitnessFunctionEnum) {
        Integer score = ZERO;
        if (FitnessFunctionEnum.HARD_SATISFIED == fitnessFunctionEnum) {
            score = this.constructingHardSatisfiedFitnessFunction(geneList);
        }
        if (FitnessFunctionEnum.MORE_SATISFIED == fitnessFunctionEnum) {
            score = this.constructingMoreSatisfiedFitnessFunction(geneList);
        }

        return score;
    }

    /**
     * 变异
     *
     * @param gene
     * @return
     */
    private String mutate(String gene) {
        var geneWithoutClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
        int time = GeneticDefaultValueDTO.MIN_TIME + (int) (Math.random() * (GeneticDefaultValueDTO.MAX_TIME));
        var classTime = this.getStandard(String.valueOf(time), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);
        var newGene = geneWithoutClassTime + classTime;
        return newGene;
    }

    /**
     * 消除冲突
     *
     * @param geneList
     * @return
     */
    private List<String> eliminateConflicts(List<String> geneList) {

        // 消除同一时间一个班级上多节课冲突
        HashMap<String, List<String>> gradeClassNoGeneMap = new HashMap<>();
        HashMap<String, List<String>> gradeClassNoClassTimeMap = new HashMap<>();
        for (int i = ZERO; i < geneList.size(); i++) {
            var gene = geneList.get(i);
            var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
            var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);

            var gradeClassNoGeneList = gradeClassNoGeneMap.get(gradeClassNo);
            if (CollectionUtils.isEmpty(gradeClassNoGeneList)) {
                gradeClassNoGeneList = new ArrayList<>();
                gradeClassNoGeneList.add(gene);
                gradeClassNoGeneMap.put(gradeClassNo, gradeClassNoGeneList);
            } else {
                gradeClassNoGeneList.add(gene);
                gradeClassNoGeneMap.put(gradeClassNo, gradeClassNoGeneList);
            }

            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNo);
            if (CollectionUtils.isEmpty(classTimeList)) {
                classTimeList = new ArrayList<>();
                classTimeList.add(classTime);
                gradeClassNoClassTimeMap.put(gradeClassNo, classTimeList);
            } else {
                if (!classTimeList.contains(classTime)) {
                    classTimeList.add(classTime);
                    gradeClassNoClassTimeMap.put(gradeClassNo, classTimeList);
                }
            }
        }

        List<String> newGeneList = new ArrayList<>();
        for (String gradeClassNo : gradeClassNoClassTimeMap.keySet()) {

            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNo);
            if (classTimeList.size() != GeneticDefaultValueDTO.MAX_TIME) {
                var classTimeUsedMap = this.getClassTimeUsedMap();
                var gradeClassNoGeneList = gradeClassNoGeneMap.get(gradeClassNo);
                for (int i = ZERO; i < gradeClassNoGeneList.size(); i++) {

                    var gene = gradeClassNoGeneList.get(i);
                    String fixed = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
                    if (GeneticDefaultValueDTO.FIXED.equals(fixed)) {
                        continue;
                    }
                    var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
                    if (classTimeUsedMap.get(Integer.valueOf(classTime)).equals(Boolean.TRUE)) {
                        String newTime = this.randomTime(gene, gradeClassNoGeneList);
                        var geneWithoutClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
                        var newGene = geneWithoutClassTime.concat(newTime);
                        gradeClassNoGeneList.set(i, newGene);
                    } else {
                        classTimeUsedMap.put(Integer.valueOf(classTime), true);
                    }
                }
                newGeneList.addAll(gradeClassNoGeneList);
            }

        }

        return newGeneList;
    }

    /**
     * 获取新时间
     *
     * @param gene
     * @param geneList
     * @return
     */
    private String randomTime(String gene, List<String> geneList) {
        var goalGradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
        HashMap<Integer, Boolean> classTimeUsedMap = this.getClassTimeUsedMap();
        for (String x : geneList) {
            var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, x);
            if (goalGradeClassNo.equals(gradeClassNo)) {
                var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, x);
                classTimeUsedMap.put(Integer.valueOf(classTime), true);
            }
        }

        List<Integer> unUsedClassTimeList = new ArrayList<>();
        for (Integer classTime : classTimeUsedMap.keySet()) {
            var usedFlag = classTimeUsedMap.get(classTime);
            if (!usedFlag) {
                unUsedClassTimeList.add(classTime);
            }
        }

        var index = ZERO + (int) (Math.random() * unUsedClassTimeList.size());
        var time = unUsedClassTimeList.get(index);
        return this.getStandard(time.toString(), GeneticDefaultValueDTO.CLASS_TIME_STANDARD_LENGTH);
    }

    /**
     * 初始化 可使用的所有时间
     *
     * @param gradeClassCountMap
     * @return
     */
    private HashMap<Integer, Integer> initNumCountMap(List<String> geneList, HashMap<Integer, Integer> gradeClassCountMap) {
        int totalClassCount = ZERO;
        for (Integer grade : gradeClassCountMap.keySet()) {
            totalClassCount = totalClassCount + gradeClassCountMap.get(grade);
        }

        HashMap<Integer, Integer> timeCountMap = new HashMap<>();
        for (int time = START_INDEX; time <= GeneticDefaultValueDTO.MAX_TIME; time++) {
            timeCountMap.put(time, totalClassCount);
        }

        for (String gene : geneList) {
            var fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
            if (GeneticDefaultValueDTO.FIXED.equals(fixedFlag)) {
                var classTimeString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
                var classTime = Integer.valueOf(classTimeString);
                var oldCount = timeCountMap.get(classTime);
                var newCount = oldCount - STEP;
                timeCountMap.put(classTime, newCount);
            }
        }

        return timeCountMap;
    }

    /**
     * 获取时间表
     *
     * @return
     */
    private HashMap<Integer, Boolean> getClassTimeUsedMap() {
        HashMap<Integer, Boolean> classTimeUsedMap = new HashMap<>();
        for (int i = START_INDEX; i <= GeneticDefaultValueDTO.MAX_TIME; i++) {
            classTimeUsedMap.put(i, Boolean.FALSE);
        }
        return classTimeUsedMap;
    }

    /**
     * 轮盘赌选择
     *
     * @return
     */
    private Integer rouletteWheelSelection(HashMap<Integer, Integer> timeCountMap) {
        int num = GeneticDefaultValueDTO.MIN_TIME + (int) (Math.random() * (GeneticDefaultValueDTO.MAX_TIME));
        if (timeCountMap.get(num) != ZERO) {
            var count = timeCountMap.get(num);
            var newCount = count - STEP;
            timeCountMap.put(num, newCount);
        } else {
            while (timeCountMap.get(num) == ZERO) {
                num = GeneticDefaultValueDTO.MIN_TIME + (int) (Math.random() * (GeneticDefaultValueDTO.MAX_TIME));
            }
            var count = timeCountMap.get(num);
            var newCount = count - STEP;
            timeCountMap.put(num, newCount);
        }

        return num;
    }

    /**
     * 数据标准化
     *
     * @param string
     * @param length
     * @return
     */
    public String getStandard(String string, int length) {
        if (string.length() < length) {
            StringBuilder zeroStandard = new StringBuilder();
            var needLength = length - string.length();
            for (int zeroIndex = START_INDEX; zeroIndex <= needLength; zeroIndex++) {
                zeroStandard.append(0);
            }
            string = zeroStandard.toString().concat(string);
        }
        if (string.length() == length) {
            return string;
        }
        if (string.length() > length) {
            throw new TransactionException("数据标准化异常，异常数据为[" + string + "],长度为" + string.length() + "标准化长度为[" + length + "]");
        }

        return string;
    }


}
