package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.service.ClassroomMaxCapacityService;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.FitnessFunctionEnum;
import com.work.school.mysql.timetable.service.dto.FitnessScoreDTO;
import com.work.school.mysql.timetable.service.dto.PopulationDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

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
     * 操场合适的班级数量
     */
    private static final int TEACHER_BEST_TEACHING_COUNT = 2;
    private static final int MAX_TEACHER_TEACHING_SCORE = 5;
    private static final int PE_BEST_COUNT = 2;
    private static final int INTERVAL_STANDARD = 4;
    private static final int MAX_INTERVAL_STANDARD_SCORE = 7;
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
    private static final int BIG_SCORE = 10;
    private static final int BEST_SEPARATE_SCORE = 5;
    /**
     * 初始种群
     */
    private static final int POPULATION = 100;
    /**
     * 进化次数
     */
    private static final int EVOLUTION_TIMES = 2000;
    /**
     * 交叉概率
     */
    private static final double CROSSOVER_RATE = 0.8;
    /**
     * 交叉次数
     */
    private static final int CROSSOVER_TIMES = (int) (CROSSOVER_RATE * POPULATION);
    /**
     * 保留数量
     */
    private static final int KEEP_POPULATION = (int) (POPULATION * (1 - CROSSOVER_RATE));
    /**
     * 变异概率
     */
    private static final double MUTATE_RATE = 0.01;
    /**
     * 保留数量
     */
    private static final int MUTATE_POPULATION = (int) (POPULATION * MUTATE_RATE);
    /**
     * 初始冲突数
     */
    private static final int INIT_CONFLICT_NO = 999;
    /**
     * 主课最好的时间
     */
    private static final Integer[] MAIN_SUBJECT_BEST_TIMES = {1, 2, 8, 9, 15, 16, 22, 23, 29, 30};

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

        // 陈璐论文使用的方法
//        var bestGeneMap = this.chenGenetic(timeTablingUseGeneticDTO);

        // 最为原始的遗传算法
        var bestGeneMap = this.originGenetic(timeTablingUseGeneticDTO);

        // 解决冲突的遗传算法
//        var bestGeneMap = this.reduceConflictGenetic(timeTablingUseGeneticDTO);
//
        cattyResult.setData(bestGeneMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 陈璐论文使用的方法，这个方法很水有漏洞，我要是审稿人肯定退稿了
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
//    private HashMap<String, List<String>> chenGenetic(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
//        // 初始化种群
//        List<HashMap<String, List<String>>> allGeneMapList = new ArrayList<>();
//        for (int i = ZERO; i < POPULATION; i++) {
//            var geneMap = this.initGene(timeTablingUseGeneticDTO);
//
//            // 进行初步的时间分配
//            var initTimeGradeSubjectList = this.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());
//
//            // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
//            var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(initTimeGradeSubjectList);
//
//            var mergeList = this.merge(gradeClassNoGeneMap);
//
//            var score = this.computerFitnessFunction(mergeList, FitnessFunctionEnum.HARD_SATISFIED);
//
//            while (!score.equals(ZERO)) {
//                List<String> tempList = mergeList;
//                tempList = selectGene(tempList);
//                var newScore = this.computerFitnessFunction(tempList, FitnessFunctionEnum.HARD_SATISFIED);
//                if (score > newScore) {
//                    score = newScore;
//                    mergeList = tempList;
//                }
//            }
//
//            gradeClassNoGeneMap = this.getGradeClassNoGeneMap(mergeList);
//            allGeneMapList.add(gradeClassNoGeneMap);
//        }
//
//        // 进行交叉操作，不要变异操作
//
//
//        // 获取最好的基因
//        HashMap<String, List<String>> bestGeneMap = new HashMap<>();
//
//        return bestGeneMap;
//    }

    /**
     * 解决冲突的遗传算法
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
//    private HashMap<String, List<String>> reduceConflictGenetic(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
//        // 初始化种群
//        List<HashMap<String, List<String>>> allGeneMapList = new ArrayList<>();
//        for (int i = ZERO; i < POPULATION; i++) {
//            var geneMap = this.initGene(timeTablingUseGeneticDTO);
//
//            // 进行初步的时间分配
//            var initTimeGradeSubjectList = this.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());
//
//            // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
//            var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(initTimeGradeSubjectList);
//
//            allGeneMapList.add(gradeClassNoGeneMap);
//        }
//
//        HashMap<String, List<String>> bestGeneMap = new HashMap<>();
//        int conflictNo = INIT_CONFLICT_NO;
//        while (conflictNo != ZERO) {
//
//            List<HashMap<String, List<String>>> newGeneMapList = new ArrayList<>();
//            for (var geneMap : allGeneMapList) {
//                var mergeList = this.merge(geneMap);
//                double random = Math.random();
//                // 交叉
//                if (CROSSOVER_RATE > random) {
//                    mergeList = this.crossover(mergeList, FitnessFunctionEnum.HARD_SATISFIED);
//                }
//                // 变异
//                if (MUTATE_RATE > random) {
//                    mergeList = this.crossover(mergeList, FitnessFunctionEnum.HARD_SATISFIED);
//                }
//
//                conflictNo = this.computerFitnessFunction(mergeList, FitnessFunctionEnum.HARD_SATISFIED);
//                if (conflictNo == ZERO) {
//                    bestGeneMap = this.getGradeClassNoGeneMap(mergeList);
//                    break;
//                }
//                geneMap = this.getGradeClassNoGeneMap(mergeList);
//
//                newGeneMapList.add(geneMap);
//            }
//
//            allGeneMapList = newGeneMapList;
//        }
//
////        var mergeList = this.merge(bestGeneMap);
////        var score = this.computerFitnessScore(mergeList);
////        System.out.println(score);
//
//        return bestGeneMap;
//    }

    /**
     * 最为原始的遗传算法
     *
     * @param timeTablingUseGeneticDTO
     * @return
     */
    private HashMap<String, List<String>> originGenetic(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
        // 获取初始基因编码
        HashMap<Integer, PopulationDTO> allGeneMap = new HashMap<>();
        FitnessScoreDTO fitnessScoreDTO = new FitnessScoreDTO();
        // 产生初始种群
        for (int i = ZERO; i < POPULATION; i++) {
            var geneMap = this.initGene(timeTablingUseGeneticDTO);

            // 进行初步的时间分配
            var initTimeGradeSubjectList = this.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());

            // 计算适应度评分
            fitnessScoreDTO = this.computerFitnessScore(fitnessScoreDTO, initTimeGradeSubjectList);

            // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
            var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(initTimeGradeSubjectList);

            PopulationDTO populationDTO = new PopulationDTO();
            populationDTO.setScore(fitnessScoreDTO.getScore());
            populationDTO.setGeneMap(gradeClassNoGeneMap);

            allGeneMap.put(i, populationDTO);
        }

        var bestPopulation = allGeneMap.get(ZERO);
        var bestScore = bestPopulation.getScore();
        var bestFitnessScore = new FitnessScoreDTO();
        bestFitnessScore.setScore(bestScore);
        List<String> messageList = new ArrayList<>();
        for (int evolutionTime = ZERO; evolutionTime < EVOLUTION_TIMES; evolutionTime++) {
            // 交叉和变异
            int evolutionTimes = CROSSOVER_TIMES + MUTATE_POPULATION;
            for (int i = ZERO; i < evolutionTimes; i++) {
                int randomPopulationIndex = (int) (Math.random() * allGeneMap.size());
                var populationDTO = allGeneMap.get(randomPopulationIndex);

                var gradeClassNoGeneMap = populationDTO.getGeneMap();
                var oldScore = populationDTO.getScore();

                // 先按照班级合并
                var mergeList = this.merge(gradeClassNoGeneMap);

                // 第一步完成交叉操作,产生新一代的父本
                List<String> geneList = new ArrayList<>(mergeList);
                var crossoverList = this.crossover(geneList);

                // 计算适应度函数
                fitnessScoreDTO = this.computerFitnessScore(fitnessScoreDTO, crossoverList);

                //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
                gradeClassNoGeneMap = this.getGradeClassNoGeneMap(crossoverList);
                if (fitnessScoreDTO.getScore() < oldScore) {
                    populationDTO.setScore(fitnessScoreDTO.getScore());
                    populationDTO.setGeneMap(gradeClassNoGeneMap);
                    allGeneMap.put(randomPopulationIndex, populationDTO);
                }
                if (fitnessScoreDTO.getScore() < bestFitnessScore.getScore()) {
                    bestPopulation.setScore(fitnessScoreDTO.getScore());
                    bestPopulation.setGeneMap(gradeClassNoGeneMap);
                    bestFitnessScore = new FitnessScoreDTO();
                    BeanUtils.copyProperties(fitnessScoreDTO, bestFitnessScore);
                }

            }
            String message = bestFitnessScore.getScore()
                    + " " + bestFitnessScore.getHardScore()
                    + " " + bestFitnessScore.getEveryTimeHaveSubjectCount()
                    + " " + bestFitnessScore.getOneTimeOneClassMoreSubjectCount()
                    + " " + bestFitnessScore.getOneTimeOneTeacherMoreClassCount()
                    + " " + bestFitnessScore.getFixedSubjectIdCount()
                    + " " + bestFitnessScore.getOneClassMoreOtherSubject()
                    + " " + bestFitnessScore.getNeedAreaSubjectCount()
                    + " " + bestFitnessScore.getSoftScore()
                    + " " + bestFitnessScore.getTeacherOutMaxTimeCount()
                    + " " + bestFitnessScore.getNoBestTimeBestSubjectCount()
                    + " " + bestFitnessScore.getStudentContinueSameClassCount()
                    + " " + bestFitnessScore.getNoMainSubjectCount()
                    + " " + bestFitnessScore.getSportNoFinalClassCount()
                    + " " + bestFitnessScore.getGoTimeNoAfternoonCount();
            messageList.add(message);
        }

        long start = System.currentTimeMillis();
        this.markToTXT(String.valueOf(start), messageList);

        return bestPopulation.getGeneMap();
    }


    /**
     * 计算适应度函数
     *
     * @param geneList
     * @return
     */
//    public int computerFitnessScore(List<String> geneList) {
//
//        // 获取特殊教室的班级上限
//        var subjectClassroomMaxMap = classroomMaxCapacityService.getClassroomMaxCapacityMap();
//
//        // 初始化年级班级和节次
//        HashMap<String, List<Integer>> gradeClassNoClassTimeMap = new HashMap<>();
//        // 初始化主课在早上、小课不在早上第一二节的数量
//        int mainSubjectFitnessCount = ZERO;
//        int otherSubjectFitnessCount = ZERO;
//        // 初始化年级班级工作日各小课的数量map
//        HashMap<String, Integer> gradeClassNoWorkDayOtherSubjectCountMap = new HashMap<>();
//        // 初始化时间需要教室课程的数量map
//        HashMap<Integer, HashMap<Integer, Integer>> otherSubjectClassTimeCountMap = new HashMap<>();
//        // 获取年级班级时间上课数量map
//        HashMap<String, Integer> gradeClassNoWorkDaySubjectCountMap = new HashMap<>();
//        // 获取语文课和数学课map
//        HashMap<String, HashMap<Integer, List<Integer>>> chineseGradeClassNoWorkDayTimeMap = new HashMap<>();
//        HashMap<String, HashMap<Integer, List<Integer>>> mathGradeClassNoWorkDayTimeMap = new HashMap<>();
//        // 获取教师冲突map
//        HashMap<String, Integer> teacherClassTimeCountMap = new HashMap<>();
//        // 获取教师工作日连堂课次数map
//        HashMap<String, Integer> teacherIdWorkDayCountMap = new HashMap<>();
//        for (String gene : geneList) {
//            // 截取上课时间是否固定，如果固定，就替换为下一条gene
//            var fixTime = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
//            if (GeneticDefaultValueDTO.FIXED.equals(fixTime)) {
//                continue;
//            }
//
//            // 截取时间
//            var classTimeString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
//            var classTime = Integer.valueOf(classTimeString);
//            // 计算天数
//            var workDay = this.computerWorkDay(classTime);
//            // 获取第几节课
//            var time = this.computerTime(classTime);
//            // 截取科目
//            var subjectIdString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);
//            var subjectId = Integer.valueOf(subjectIdString);
//            // 截取科目类型
//            var subjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX, gene);
//            var subjectType = Integer.valueOf(subjectTypeString);
//            // 截取年级和班级
//            var gradeClassNoString = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
//            // 截取教师id
//            var teacherIdString = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);
//
//            // 记录每个班是否每节课都有课
//            var gradeClassNoClassTime = gradeClassNoString + classTimeString;
//            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNoClassTime);
//            if (CollectionUtils.isEmpty(classTimeList)) {
//                classTimeList = new ArrayList<>();
//                classTimeList.add(classTime);
//                gradeClassNoClassTimeMap.put(gradeClassNoClassTime, classTimeList);
//            } else {
//                classTimeList.add(classTime);
//                gradeClassNoClassTimeMap.put(gradeClassNoClassTime, classTimeList);
//            }
//
//            // 记录每天每个班每节课课程数量
//            gradeClassNoWorkDaySubjectCountMap.merge(gradeClassNoClassTime, STEP, Integer::sum);
//
//            // 记录早上语文数学课的数量
//            boolean mainSubjectFlag = SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(subjectType);
//            boolean mainInMorningFlag = mainSubjectFlag && time <= SchoolTimeTableDefaultValueDTO.getMorningSecTime();
//            if (mainInMorningFlag) {
//                mainSubjectFitnessCount = mainSubjectFitnessCount + STEP;
//            }
//
//            // 记录小课在第三节或者下午的数量
//            boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(subjectType)
//                    || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType);
//            boolean otherSubjectFitnessCountFlag = otherSubjectFlag
//                    && SchoolTimeTableDefaultValueDTO.getMorningLastTime() < time
//                    && time <= SchoolTimeTableDefaultValueDTO.getAfternoonSecTime();
//            if (otherSubjectFitnessCountFlag) {
//                otherSubjectFitnessCount = otherSubjectFitnessCount + STEP;
//            }
//
//            // 记录每天小课的数量
//            if (otherSubjectFlag) {
//                var gradeClassNoWorkDaySubjectId = gradeClassNoString + workDay.toString() + subjectIdString;
//                gradeClassNoWorkDayOtherSubjectCountMap.merge(gradeClassNoWorkDaySubjectId, STEP, Integer::sum);
//            }
//
//            // 记录同一时间三种特殊教室数量
//            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType)) {
//
//                if (otherSubjectClassTimeCountMap.get(subjectId) == null) {
//                    HashMap<Integer, Integer> classTimeCountMap = new HashMap<>();
//                    classTimeCountMap.put(classTime, STEP);
//                    otherSubjectClassTimeCountMap.put(subjectId, classTimeCountMap);
//                } else {
//                    var classTimeCountMap = otherSubjectClassTimeCountMap.get(subjectId);
//                    var count = classTimeCountMap.get(classTime);
//                    if (count == null) {
//                        count = ZERO;
//                    }
//                    classTimeCountMap.put(classTime, count + STEP);
//                    otherSubjectClassTimeCountMap.put(subjectId, classTimeCountMap);
//                }
//            }
//
//            // 记录每天语文课和数学课间隔
//            boolean chineseFlag = SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(subjectId);
//            if (chineseFlag) {
//                this.markGradeClassNoWorkDayTimeListMap(gradeClassNoString, workDay, time, chineseGradeClassNoWorkDayTimeMap);
//            }
//            boolean mathsFlag = SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(subjectId);
//            if (mathsFlag) {
//                this.markGradeClassNoWorkDayTimeListMap(gradeClassNoString, workDay, time, mathGradeClassNoWorkDayTimeMap);
//            }
//
//            // 记录教师上课节次，大于1说明查看冲突
//            var teacherIdClassTime = teacherIdString + classTimeString;
//            teacherClassTimeCountMap.merge(teacherIdClassTime, STEP, Integer::sum);
//
//            // 记录教师每天上课节数
//            var teacherIdWorkDay = teacherIdString + workDay;
//            teacherIdWorkDayCountMap.merge(teacherIdWorkDay, STEP, Integer::sum);
//        }
//
//        int fitnessScore = ZERO;
//        // 如果教师一天课程没有超过4节，则加 ADD_SCORE 分
//        for (String teacherIdClassTime : teacherIdWorkDayCountMap.keySet()) {
//            var count = teacherIdWorkDayCountMap.get(teacherIdClassTime);
//            if (count <= SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize()) {
//                if (count <= TEACHER_BEST_TEACHING_COUNT) {
//                    fitnessScore = fitnessScore + count * ADD_SCORE;
//                } else {
//                    fitnessScore = fitnessScore + (MAX_TEACHER_TEACHING_SCORE - count) * ADD_SCORE;
//                }
//
//            }
//        }
//
//        // 如果两节相同的主课相隔时间长，则加分，分数 = 下一节 - 本节课
//        fitnessScore = this.getMainIntervalScore(fitnessScore, chineseGradeClassNoWorkDayTimeMap);
//        fitnessScore = this.getMainIntervalScore(fitnessScore, mathGradeClassNoWorkDayTimeMap);
//
//        // 如果主课在早上，则加 ADD_SCORE 分
//        fitnessScore = fitnessScore + mainSubjectFitnessCount * BIG_SCORE;
//        // 如果小课在第三节或者下午，则加 ADD_SCORE 分
//        fitnessScore = fitnessScore + otherSubjectFitnessCount * ADD_SCORE;
//
//        // 如果同一时间三种特殊教室没有超上限数量，则加 ADD_SCORE 分
//        for (Integer subjectId : otherSubjectClassTimeCountMap.keySet()) {
//            var maxCount = subjectClassroomMaxMap.get(subjectId);
//            var classTimeCountMap = otherSubjectClassTimeCountMap.get(subjectId);
//            for (Integer classTime : classTimeCountMap.keySet()) {
//                var count = classTimeCountMap.get(classTime);
//                if (count <= maxCount) {
//                    if (SchoolTimeTableDefaultValueDTO.getSubjectPeId().equals(subjectId)) {
//                        int peScore = Math.abs(count - PE_BEST_COUNT);
//                        fitnessScore = fitnessScore + peScore * ADD_SCORE;
//                    } else {
//                        fitnessScore = fitnessScore + count * ADD_SCORE;
//                    }
//                }
//            }
//        }
//
//        // 如果每个班级都有课，那么ADD_BIG_SCORE 分
//        for (String gradeClassNo : gradeClassNoClassTimeMap.keySet()) {
//            var classTimeList = gradeClassNoClassTimeMap.get(gradeClassNo);
//            var count = (int) classTimeList.stream().distinct().count();
//            fitnessScore = fitnessScore + count * BIG_SCORE;
//        }
//
//        // 如果每天每个班每节课只有一种课程，则加 ADD_BIG_SCORE 分
////        var classTimeOnlyCount = (int) gradeClassNoWorkDaySubjectCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
////        fitnessScore = fitnessScore + classTimeOnlyCount * BIG_SCORE;
//
//
//        // 如果相同的小课每个班每天只有一节，则加 ADD_BIG_SCORE 分
////        if (CollectionUtils.isNotEmpty(gradeClassNoWorkDayOtherSubjectCountMap.keySet())) {
////            int allMatchCount = (int) gradeClassNoWorkDayOtherSubjectCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
////            fitnessScore = fitnessScore + allMatchCount * BIG_SCORE;
////        }
//
//        // 如果教师没有冲突，则加 ADD_BIG_SCORE 分
////        var teacherClassTimeCount = (int) teacherClassTimeCountMap.values().stream().filter(x -> ONLY_ONE_COUNT == x).count();
////        fitnessScore = fitnessScore + teacherClassTimeCount * BIG_SCORE;
//
//        return fitnessScore;
//    }

    /**
     * 获取间隔分数
     *
     * @param fitnessScore
     * @param gradeClassNoWorkDayTimeMap
     * @return
     */
    private Integer getMainIntervalScore(Integer fitnessScore, HashMap<String, HashMap<Integer, List<Integer>>> gradeClassNoWorkDayTimeMap) {
        for (String gradeClassNo : gradeClassNoWorkDayTimeMap.keySet()) {
            var workDayTimeListMap = gradeClassNoWorkDayTimeMap.get(gradeClassNo);
            for (Integer workDay : workDayTimeListMap.keySet()) {
                var timeList = workDayTimeListMap.get(workDay);
                if (timeList.size() == ONLY_ONE_COUNT) {
                    fitnessScore = fitnessScore + BEST_SEPARATE_SCORE;
                } else {
                    for (int i = 0; i < timeList.size() - 1; i++) {
                        Collections.sort(timeList);
                        int interval = timeList.get(i + 1) - timeList.get(i);
                        if (interval < INTERVAL_STANDARD) {
                            fitnessScore = fitnessScore + interval * ADD_SCORE;
                        } else {
                            fitnessScore = fitnessScore + (MAX_INTERVAL_STANDARD_SCORE - interval) * ADD_SCORE;
                        }
                    }
                }
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
     * @param gradeClassNoWorkDayTimeListMap
     * @return
     */
    private HashMap<String, HashMap<Integer, List<Integer>>> markGradeClassNoWorkDayTimeListMap(String gradeClassNoString, Integer workDay, Integer
            time, HashMap<String, HashMap<Integer, List<Integer>>> gradeClassNoWorkDayTimeListMap) {
        if (gradeClassNoWorkDayTimeListMap.get(gradeClassNoString) == null) {
            List<Integer> timeList = new ArrayList<>();
            timeList.add(time);
            HashMap<Integer, List<Integer>> workTimeListMap = new HashMap<>();
            workTimeListMap.put(workDay, timeList);
            gradeClassNoWorkDayTimeListMap.put(gradeClassNoString, workTimeListMap);
        } else {
            HashMap<Integer, List<Integer>> workTimeListMap = gradeClassNoWorkDayTimeListMap.get(gradeClassNoString);
            if (workTimeListMap == null) {
                List<Integer> timeList = new ArrayList<>();
                timeList.add(time);
                workTimeListMap = new HashMap<>();
                workTimeListMap.put(workDay, timeList);
                gradeClassNoWorkDayTimeListMap.put(gradeClassNoString, workTimeListMap);
            } else {
                List<Integer> timeList = workTimeListMap.get(workDay);
                if (CollectionUtils.isEmpty(timeList)) {
                    timeList = new ArrayList<>();
                }
                timeList.add(time);
                workTimeListMap.put(workDay, timeList);
                gradeClassNoWorkDayTimeListMap.put(gradeClassNoString, workTimeListMap);
            }
        }

        return gradeClassNoWorkDayTimeListMap;
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
     * 初始化课程的时间分配
     *
     * @param geneMap
     * @param gradeClassNoCountMap
     * @return
     */
    public List<String> initTimeByClassNo(HashMap<String, List<String>> geneMap, HashMap<Integer, Integer> gradeClassNoCountMap) {
        List<String> geneList = new ArrayList<>();
        for (String gene : geneMap.keySet()) {
            geneList.addAll(geneMap.get(gene));
        }

        // 首先按照班级分配
        var gradeClassNoGeneMap = getGradeClassNoGeneMap(geneList);

        // 班级一个一个赋予时间
        List<String> initGeneList = new ArrayList<>();
        for (var gradeClassNo : gradeClassNoGeneMap.keySet()) {
            var gradeClassNoGeneList = gradeClassNoGeneMap.get(gradeClassNo);
            var timeCountMap = this.initEveryGradeClassNoTimeMap(gradeClassNoGeneList, gradeClassNoCountMap);

            for (String gene : gradeClassNoGeneList) {
                var fixFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
                if (GeneticDefaultValueDTO.UN_FIXED.equals(fixFlag)) {
                    var geneWithoutClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
                    Integer time = this.rouletteWheelSelection(timeCountMap);
                    var classTime = this.getStandard(time.toString(), GeneticDefaultValueDTO.CLASS_STANDARD_LENGTH);
                    var newGene = geneWithoutClassTime + classTime;
                    initGeneList.add(newGene);
                }
                if (GeneticDefaultValueDTO.FIXED.equals(fixFlag)) {
                    initGeneList.add(gene);
                }
            }

        }

        return initGeneList;
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
     * 记录信息到txt
     *
     * @param txtName
     * @param messageList
     */
    public void markToTXT(String txtName, List<String> messageList) {
        try {
            File newLog = new File("/Users/wangxin/Downloads/" + txtName + ".txt");
            if (!newLog.isFile()) {
                boolean createFlag = newLog.createNewFile();
                if (!createFlag) {
                    throw new TransactionException("生成记录表失败");
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newLog), "utf-8"));
            for (String x : messageList) {
                bw.write(x + "\r\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 交叉操作
     *
     * @param mergeList
     * @return
     */
    public List<String> crossover(List<String> mergeList) {
        return this.selectGene(mergeList);
    }

//    /**
//     * 交叉操作
//     *
//     * @param mergeList
//     * @param fitnessFunctionEnum
//     * @return
//     */
//    public List<String> crossover(List<String> mergeList, FitnessFunctionEnum fitnessFunctionEnum) {
//
//        // 进行基因交叉生成新个体
//        List<String> newGeneList = this.selectGene(mergeList);
//
//        // 分别计算适度函数值
//        Integer oldScore = this.computerFitnessFunction(mergeList, fitnessFunctionEnum);
//        Integer newScore = this.computerFitnessFunction(newGeneList, fitnessFunctionEnum);
//
//        // 对父代和新生基因进行适度函数对比，选择适度高的基因进入下一代遗传
//        if (oldScore > newScore) {
//            return newGeneList;
//        }
//
//        return mergeList;
//    }

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

        var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(oldGeneList);
//        int random = (int) (Math.random() * (gradeClassNoGeneMap.size()));
//        int i = 0;
//        List<String> geneList = null;
//        String geneKey = null;
//        for (String gradeClassNo:gradeClassNoGeneMap.keySet()) {
//            if (i == random){
//                geneList = gradeClassNoGeneMap.get(gradeClassNo);
//                geneKey = gradeClassNo;
//            }
//            i = i + ONLY_ONE_COUNT;
//        }
//
//        boolean flag = false;
//        do {
//            List<String> newGeneList = new ArrayList<>(geneList);
//            int firstGeneIndex = (int) (Math.random() * (geneList.size()));
//            int secGeneIndex = (int) (Math.random() * (geneList.size()));
//            var firstGene = geneList.get(firstGeneIndex);
//            var secGene = geneList.get(secGeneIndex);
//            // 判断是否固定时间的基因，如果是就重新选择
//            var firstGeneFixedNo = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, firstGene);
//            var secGeneFixedNo = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, secGene);
//            var fixedFlag = GeneticDefaultValueDTO.FIXED.equals(firstGeneFixedNo) || GeneticDefaultValueDTO.FIXED.equals(secGeneFixedNo);
//            boolean selectFlag = !(firstGeneIndex == secGeneIndex || fixedFlag);
//            if (selectFlag) {
//                // 获取两条基因的时间
//                String firstClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, firstGene);
//                String secClassTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, secGene);
//
//                // 互换时间
//                String firstMainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, firstGene);
//                String secMainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, secGene);
//
//                String firstNewGene = firstMainGene.concat(secClassTime);
//                String secNewGene = secMainGene.concat(firstClassTime);
//
//                // 替换现有基因
//                newGeneList.set(firstGeneIndex, firstNewGene);
//                newGeneList.set(secGeneIndex, secNewGene);
//                gradeClassNoGeneMap.put(geneKey, newGeneList);
//                flag = true;
//            }
//        } while (!flag);
//        return this.merge(gradeClassNoGeneMap);

        var newGeneList = new ArrayList<>(oldGeneList);
        boolean flag = false;
        do {
            int firstRandomNo = (int) (Math.random() * (oldGeneList.size()));

//            if (Math.random() < SELECT_RATE) {
//                // 只选择有冲突的个体，有可能早熟
//                firstRandomNo = this.getGeneIndex(oldGeneList);
//            } else {
//                // 生成两个随机数
//                firstRandomNo = (int) (Math.random() * (oldGeneList.size()));
//            }

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

//            var timeFitFlag = false;
//            var firstGeneSubjectType = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX,firstGene);
//            // 如果科目为1类 那么第二条时间只为1，2，8，9，15，16，22，23，29，30
//            if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(Integer.valueOf(firstGeneSubjectType))){
//                var secGeneClassTimeString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX,secGene);
//                var secGeneClassTime = Integer.valueOf(secGeneClassTimeString);
//                timeFitFlag = Arrays.asList(MAIN_SUBJECT_BEST_TIMES).contains(secGeneClassTime);
//                selectFlag = selectFlag && timeFitFlag;
//            }
//
//            var subjectFitFlag = false;
//            var firstGeneClassTimeString = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX,firstGene);
//            // 如果第一条基因时间为1，2，8，9，15，16，22，23，29，30节课 那么第二条科目只为1类
//            if (Arrays.asList(MAIN_SUBJECT_BEST_TIMES).contains(Integer.valueOf(firstGeneClassTimeString))){
//                var secGeneSubjectTypeString = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_TYPE_INDEX,secGene);
//                var secGeneSubjectType = Integer.valueOf(secGeneSubjectTypeString);
//                subjectFitFlag = SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(secGeneSubjectType);
//                selectFlag = selectFlag && subjectFitFlag;
//            }

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
     * 进行基因交叉生成新个体
     *
     * @param oldGeneList
     * @return
     */
    public List<String> selectGenes(List<String> oldGeneList, Integer nums) {
        List<String> newGeneList = new ArrayList<>(oldGeneList);
        for (int i = ZERO; i <= nums; i++) {
            newGeneList = this.selectGene(newGeneList);
        }

        return newGeneList;
    }

    /**
     * 构造适度函数 查看冲突
     *
     * @param fitnessScoreDTO
     * @param geneList
     * @return
     */
    public FitnessScoreDTO computerHardFitnessFunction(FitnessScoreDTO fitnessScoreDTO,
                                                       List<String> geneList) {

        HashMap<String, Integer> gradeClassNoClassTimeCountMap = new HashMap<>();
        HashMap<String, Integer> teacherIdClassTimeCountMap = new HashMap<>();
        HashMap<String, Integer> otherSubjectIdCountMap = new HashMap<>();
        HashMap<String, Integer> everyTimeHaveSubjectMap = new HashMap<>();
        HashMap<String, Integer> otherNeedAreaSubjectIdCountMap = new HashMap<>();
        for (String gene : geneList) {
            var subjectIdStr = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);
            var subjectId = Integer.valueOf(subjectIdStr);
            everyTimeHaveSubjectMap.put(gene, subjectId);

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

                    var workDay = this.computerWorkDay(Integer.valueOf(classTime));
                    var gradeClassNoOtherSubjectIdClassTime = gradeClassNo.concat(subjectIdStr).concat(workDay.toString());
                    otherSubjectIdCountMap.merge(gradeClassNoOtherSubjectIdClassTime, STEP, Integer::sum);
                }

                // 体育课
                if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(subjectType)) {
                    var subjectIdClassTime = subjectIdStr.concat(classTime);
                    otherNeedAreaSubjectIdCountMap.merge(subjectIdClassTime, STEP, Integer::sum);
                }

            }
        }

        // 不满足硬约束条件的次数
        // 同一时间同一个班上了多节课
        int oneTimeOneClassMoreSubjectCount = ZERO;
        for (String gradeClassNoClassTime : gradeClassNoClassTimeCountMap.keySet()) {
            var gradeClassNoClassTimeCount = gradeClassNoClassTimeCountMap.get(gradeClassNoClassTime);
            if (gradeClassNoClassTimeCount > ONLY_ONE_COUNT) {
                oneTimeOneClassMoreSubjectCount = oneTimeOneClassMoreSubjectCount + ONLY_ONE_COUNT;
            }
        }

        // 同一时间同一个教师上多个班的课
        int oneTimeOneTeacherMoreClassCount = ZERO;
        for (String teacherIdClassTime : teacherIdClassTimeCountMap.keySet()) {
            var teacherIdClassTimeCount = teacherIdClassTimeCountMap.get(teacherIdClassTime);
            if (teacherIdClassTimeCount > ONLY_ONE_COUNT) {
                oneTimeOneTeacherMoreClassCount = oneTimeOneTeacherMoreClassCount + ONLY_ONE_COUNT;
            }
        }

        // 每个年级每个班每节课都必须有人上课
        var everyTimeHaveSubjectList = everyTimeHaveSubjectMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        var everyTimeHaveSubjectCount = geneList.size() - everyTimeHaveSubjectList.size();

        // 固定课程需要按时上课
        var fixedSubjectIdCount = ZERO;

        // 小课每个年级的每个班每天只上一次
        int oneClassMoreOtherSubject = ZERO;
        for (String gradeClassNoOtherSubjectIdClassTime : otherSubjectIdCountMap.keySet()) {
            var otherSubjectIdCount = otherSubjectIdCountMap.get(gradeClassNoOtherSubjectIdClassTime);
            if (otherSubjectIdCount > ONLY_ONE_COUNT) {
                oneClassMoreOtherSubject = oneClassMoreOtherSubject + ONLY_ONE_COUNT;
            }
        }

        // 同一时间，功能部室内最多允许同一年级2个班上课，操场最多容纳3个班级上课
        int needAreaSubjectCount = ZERO;
        var classroomMaxCapacityMap = classroomMaxCapacityService.getClassroomMaxCapacityMap();
        for (String subjectIdClassTime : otherNeedAreaSubjectIdCountMap.keySet()) {
            var subjectIdStr = subjectIdClassTime.substring(0, 2);
            var subjectId = Integer.valueOf(subjectIdStr);
            var maxCount = classroomMaxCapacityMap.get(subjectId);
            var count = otherNeedAreaSubjectIdCountMap.get(subjectIdClassTime);
            if (count > maxCount) {
                needAreaSubjectCount = needAreaSubjectCount + ONLY_ONE_COUNT;
            }
        }

        int hardCount = oneTimeOneClassMoreSubjectCount + oneTimeOneTeacherMoreClassCount
                + oneClassMoreOtherSubject + everyTimeHaveSubjectCount + fixedSubjectIdCount + needAreaSubjectCount;

        fitnessScoreDTO.setHardScore(hardCount);
        fitnessScoreDTO.setEveryTimeHaveSubjectCount(everyTimeHaveSubjectCount);
        fitnessScoreDTO.setOneTimeOneClassMoreSubjectCount(oneTimeOneClassMoreSubjectCount);
        fitnessScoreDTO.setOneTimeOneTeacherMoreClassCount(oneTimeOneTeacherMoreClassCount);
        fitnessScoreDTO.setFixedSubjectIdCount(fixedSubjectIdCount);
        fitnessScoreDTO.setOneClassMoreOtherSubject(oneClassMoreOtherSubject);
        fitnessScoreDTO.setNeedAreaSubjectCount(needAreaSubjectCount);

        return fitnessScoreDTO;
    }

    /**
     * 满足软约束条件
     *
     * @param fitnessScoreDTO
     * @param geneList
     * @return
     */
    private FitnessScoreDTO computerSoftFitnessFunction(FitnessScoreDTO fitnessScoreDTO, List<String> geneList) {

        HashMap<String, Integer> teacherWorkDayCountMap = new HashMap<>();
        HashMap<Integer, Integer> subjectTypeCountMap = new HashMap<>();
        HashMap<String, List<Integer>> gradeClassNumWorkDaySubjectTimeListMap = new HashMap<>();
        HashMap<String, List<Integer>> goTimesHashMap = new HashMap<>();
        HashMap<String, List<Integer>> sportTimesHashMap = new HashMap<>();
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

                // 围棋课尽量在下午，因为围棋老师是外聘教师，最好来学校的次数少
                String subjectIdStr = this.cutGeneIndex(GeneticDefaultValueDTO.SUBJECT_ID_INDEX, gene);
                var gradeClassNumStr = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
                var gradeClassNumWorkDaySubject = gradeClassNumStr.concat(workDay.toString()).concat(subjectIdStr);

                Integer subjectId = Integer.valueOf(subjectIdStr);
                if (SchoolTimeTableDefaultValueDTO.getSubjectGoId().equals(subjectId)) {
                    var goTimeList = goTimesHashMap.get(gradeClassNumWorkDaySubject);
                    if (CollectionUtils.isEmpty(goTimeList)) {
                        goTimeList = new ArrayList<>();
                    }
                    goTimeList.add(time);
                    goTimesHashMap.put(gradeClassNumWorkDaySubject, goTimeList);
                }

                // 体育课尽量在上午最后一节(第3节)，或者下午最后三节(第5,6,7节)
                if (SchoolTimeTableDefaultValueDTO.getSubjectSportId().equals(subjectId)) {
                    var sportTimeList = sportTimesHashMap.get(gradeClassNumWorkDaySubject);
                    if (CollectionUtils.isEmpty(sportTimeList)) {
                        sportTimeList = new ArrayList<>();
                    }
                    sportTimeList.add(time);
                    sportTimesHashMap.put(gradeClassNumWorkDaySubject, sportTimeList);
                }

                // 获取年级班级日期课程 List<时间>
                var timeList = gradeClassNumWorkDaySubjectTimeListMap.get(gradeClassNumWorkDaySubject);
                if (CollectionUtils.isEmpty(timeList)) {
                    timeList = new ArrayList<>();
                }
                timeList.add(time);
                gradeClassNumWorkDaySubjectTimeListMap.put(gradeClassNumWorkDaySubject, timeList);
            }
        }

        // 教师一天上课不能超过4节
        int teacherOutMaxTimeCount = ZERO;
        for (String teacherWorkDay : teacherWorkDayCountMap.keySet()) {
            var count = teacherWorkDayCountMap.get(teacherWorkDay);
            if (count > TEACHER_MAX_TIME) {
                teacherOutMaxTimeCount = teacherOutMaxTimeCount + ONLY_ONE_COUNT;
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

        // 体育课最好在下午
        int sportNoFinalCount = ZERO;
        for (String key : sportTimesHashMap.keySet()) {
            var times = sportTimesHashMap.get(key);
            for (Integer time : times) {
                if (time < SchoolTimeTableDefaultValueDTO.getAfternoonSecTime() && !time.equals(SchoolTimeTableDefaultValueDTO.getMorningLastTime())) {
                    sportNoFinalCount = sportNoFinalCount + ONLY_ONE_COUNT;
                }
            }
        }

        // 学生上了连堂课
        var studentContinueSameClassCount = ZERO;
        for (String gradeClassNumWorkDaySubject : gradeClassNumWorkDaySubjectTimeListMap.keySet()) {
            var timeList = gradeClassNumWorkDaySubjectTimeListMap.get(gradeClassNumWorkDaySubject);
            if (timeList.size() > ONLY_ONE_COUNT) {
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
        }

        // 围棋课要在下午
        int goTimeNoAfternoonCount = ZERO;
        for (String key : goTimesHashMap.keySet()) {
            var times = goTimesHashMap.get(key);
            for (Integer time : times) {
                if (time < SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
                    goTimeNoAfternoonCount = goTimeNoAfternoonCount + ONLY_ONE_COUNT;
                }
            }
        }

        var noBestClassBestSubjectCount = noMainSubjectCount + sportNoFinalCount;

        // 不满足软约束条件的次数
        var softCount = teacherOutMaxTimeCount + noMainSubjectCount + studentContinueSameClassCount;
        fitnessScoreDTO.setSoftScore(softCount);
        fitnessScoreDTO.setTeacherOutMaxTimeCount(teacherOutMaxTimeCount);
        fitnessScoreDTO.setNoMainSubjectCount(noMainSubjectCount);
        fitnessScoreDTO.setStudentContinueSameClassCount(studentContinueSameClassCount);
        fitnessScoreDTO.setGoTimeNoAfternoonCount(goTimeNoAfternoonCount);
        fitnessScoreDTO.setSportNoFinalClassCount(sportNoFinalCount);
        fitnessScoreDTO.setNoBestTimeBestSubjectCount(noBestClassBestSubjectCount);

        return fitnessScoreDTO;
    }

    /**
     * 计算适应度评分
     *
     * @param geneList
     * @return
     */
    public FitnessScoreDTO computerFitnessScore(FitnessScoreDTO fitnessScoreDTO, List<String> geneList) {
        this.computerFitnessFunction(fitnessScoreDTO, geneList, FitnessFunctionEnum.HARD_SATISFIED);
        this.computerFitnessFunction(fitnessScoreDTO, geneList, FitnessFunctionEnum.SOFT_SATISFIED);
        var score = fitnessScoreDTO.getHardScore() * BIG_SCORE + fitnessScoreDTO.getSoftScore() * ADD_SCORE;
        fitnessScoreDTO.setScore(score);

        return fitnessScoreDTO;
    }

    /**
     * 计算适度函数值
     *
     * @param fitnessScoreDTO
     * @param geneList
     * @param fitnessFunctionEnum
     * @return
     */
    public FitnessScoreDTO computerFitnessFunction(FitnessScoreDTO fitnessScoreDTO, List<String> geneList, FitnessFunctionEnum fitnessFunctionEnum) {
        if (FitnessFunctionEnum.HARD_SATISFIED == fitnessFunctionEnum) {
            fitnessScoreDTO = this.computerHardFitnessFunction(fitnessScoreDTO, geneList);
        }
        if (FitnessFunctionEnum.SOFT_SATISFIED == fitnessFunctionEnum) {
            this.computerSoftFitnessFunction(fitnessScoreDTO, geneList);
        }

        return fitnessScoreDTO;
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
     * 初始化 可使用的所有时间
     *
     * @param gradeClassCountMap
     * @return
     */
    private HashMap<Integer, Integer> initEveryGradeClassNoTimeMap(List<String> geneList, HashMap<Integer, Integer> gradeClassCountMap) {
        HashMap<Integer, Integer> timeCountMap = new HashMap<>();
        for (int time = START_INDEX; time <= GeneticDefaultValueDTO.MAX_TIME; time++) {
            timeCountMap.put(time, START_INDEX);
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
