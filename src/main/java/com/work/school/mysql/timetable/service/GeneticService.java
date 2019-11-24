package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.FitnessFunctionEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 教师一天最多上4节课
     */
    private static final int TEACHER_MAX_TIME = 4;
    /**
     * 开始分数
     */
    private static final int START_SCORE = 99;
    /**
     * 变异概率
     */
    private static final double MUTATE_RATE = 0.01;

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
        var geneMap = this.initGene(timeTablingUseGeneticDTO);

        // 进行初步的时间分配
        var initTimeGradeSubjectList = this.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());

        // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
        var gradeClassNoGeneMap = this.getGradeClassNoGeneMap(initTimeGradeSubjectList);

        // 遗传算法 满足硬约束
//        geneMap = this.geneticHardConstraintCore(gradeClassNoGeneMap);

        // 满足硬约束和软约束 有迭代次数限制
        geneMap = this.geneticMoreSatisfiedCore(gradeClassNoGeneMap);

        cattyResult.setData(geneMap);
        cattyResult.setSuccess(true);
        return cattyResult;
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
    private HashMap<String, List<String>> initGene(TimeTablingUseGeneticDTO timeTablingUseGeneticDTO) {
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
    private List<String> initTime(HashMap<String, List<String>> geneMap, HashMap<Integer, Integer> gradeClassNoCountMap) {

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
    private HashMap<String, List<String>> getGradeClassNoGeneMap(List<String> initTimeGradeSubjectList) {
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

            //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
            gradeClassNoGeneMap = this.getGradeClassNoGeneMap(crossoverList);
        }

        return gradeClassNoGeneMap;
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
    private List<String> crossover(List<String> mergeList, FitnessFunctionEnum fitnessFunctionEnum) {

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
    private List<String> merge(HashMap<String, List<String>> gradeClassNoGeneMap) {
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
    private List<String> selectGene(List<String> oldGeneList) {
        List<String> newGeneList = new ArrayList<>(oldGeneList);
        boolean flag = false;
        do {
            // 生成两个随机数,这里可以改为轮盘赌
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
    private Integer computerFitnessFunction(List<String> geneList, FitnessFunctionEnum fitnessFunctionEnum) {
        Integer score = ZERO;
        if (FitnessFunctionEnum.HARD_SATISFIED == fitnessFunctionEnum) {
            score = this.constructingHardSatisfiedFitnessFunction(geneList);
        }
        if (FitnessFunctionEnum.MORE_SATISFIED == fitnessFunctionEnum) {
            score = this.constructingMoreSatisfiedFitnessFunction(geneList);
        }

        return score;
    }

//    /**
//     * 变异
//     *
//     * @param geneList
//     * @param timeCountMap
//     * @return
//     */
//    private List<String> mutate(List<String> geneList, HashMap<Integer, Integer> timeCountMap) {
//        // 筛选出所有固定时间的基因
//        List<String> fixedGeneList = new ArrayList<>();
//        for (String gene : geneList) {
//            var fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
//            if (GeneticDefaultValueDTO.FIXED.equals(fixedFlag)) {
//                fixedGeneList.add(gene);
//            }
//        }
//
//        var mutateNo = geneList.size() * MUTATE_RATE;
//        mutateNo = mutateNo < ONLY_ONE_COUNT ? ONLY_ONE_COUNT : mutateNo;
//        for (int i = 0; i < mutateNo; ) {
//            int randomNo = (int) (Math.random() * (GeneticDefaultValueDTO.MAX_TIME));
//            String gene = geneList.get(randomNo);
//            String oldGene = gene;
//            String fixedFlag = this.cutGeneIndex(GeneticDefaultValueDTO.FIXED_INDEX, gene);
//            if (GeneticDefaultValueDTO.FIXED.equals(fixedFlag)) {
//                break;
//            } else {
//                var time = this.rouletteWheelSelection(timeCountMap);
//                String classTime = time.toString();
//                var mainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
//                var newGene = mainGene.concat(classTime);
//
//                var oldScore = this.computerFitnessFunction(geneList);
//                geneList.set(randomNo, newGene);
//                var newScore = this.computerFitnessFunction(geneList);
//
//                if (oldScore < newScore) {
//                    geneList.set(randomNo, oldGene);
//                }
//
//                i++;
//            }
//        }
//
//        return geneList;
//    }

//    /**
//     * 消除冲突
//     *
//     * @param geneList
//     * @return
//     */
//    private List<String> eliminateConflicts(List<String> geneList) {
//
//        // 消除同一时间一个班级上多节课冲突
//        HashMap<String, String> gradeClassNoClassTimeMap = new HashMap<>();
//        for (int i = ZERO; i < geneList.size(); i++) {
//            var gene = geneList.get(i);
//            var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
//            // 同一时间一个班级上多节课
//            var gradeClassNo = this.cutGeneIndex(GeneticDefaultValueDTO.GRADE_CLASS_INDEX, gene);
//
//            if (gradeClassNoClassTimeMap.get(gradeClassNo) == null) {
//                gradeClassNoClassTimeMap.put(gradeClassNo, classTime);
//            } else {
//                var newTime = this.randomTime(gene, geneList);
//                var mainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
//                var newGene = mainGene.concat(newTime);
//                geneList.set(i, newGene);
//            }
//
//        }
//
//        // 消除一个教师同一时间上多节课,即一个教师由多个一样的时间
//        HashMap<String, String> teacherIdClassTimeMap = new HashMap<>();
//        for (int i = ZERO; i < geneList.size(); i++) {
//            var gene = geneList.get(i);
//            var classTime = this.cutGeneIndex(GeneticDefaultValueDTO.CLASS_TIME_INDEX, gene);
//            // 同一时间一个班级上多节课
//            var teacherId = this.cutGeneIndex(GeneticDefaultValueDTO.TEACHER_ID_INDEX, gene);
//
//            if (teacherIdClassTimeMap.get(teacherId) == null) {
//                teacherIdClassTimeMap.put(teacherId, classTime);
//            } else {
//                var newTime = this.randomTime(gene, geneList);
//                var mainGene = this.cutGeneIndex(GeneticDefaultValueDTO.GENE_INDEX, gene);
//                var newGene = mainGene.concat(newTime);
//                geneList.set(i, newGene);
//            }
//
//        }
//
//        return geneList;
//    }

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
    private String getStandard(String string, int length) {
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
