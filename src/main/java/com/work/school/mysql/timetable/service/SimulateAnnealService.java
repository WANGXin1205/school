package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.timetable.service.dto.FitnessScoreDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class SimulateAnnealService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulateAnnealService.class);


    private static final long STOP_TIME = 300000;
    /**
     * 初始种群
     */
    private static final int ZERO = 0;
    private static final int GENE_TIMES = 20;
    private static final int SELECT_NUM = 1;
    private static final int SAVE_NUM = 2;
    // 最大概率
    private static final int MAX_PRO = 1;
    // 初始温度
    private static final BigDecimal INIT_TEMPERATURE = new BigDecimal("50");
    // 每次下降温度
    private static final BigDecimal STEP_TEMPERATURE = new BigDecimal("0.95");
    // 最低温度
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("0.0001");
    // 不接受温度
    private static final BigDecimal NO_ACCEPTANCE_TEMPERATURE = new BigDecimal("40");
    // 增加温度
    private static final BigDecimal BACK_TEMPERATURE = new BigDecimal("5");

    @Resource
    private GeneticService geneticService;
    @Resource
    private BacktrackingService backtrackingService;

    /**
     * 模拟退火算法
     *
     * @param timeTablingUseSimulateAnnealDTO
     * @return
     */
    public CattyResult<HashMap<String, List<String>>> algorithmInPlanTimeTableWithSimulateAnneal(TimeTablingUseSimulateAnnealDTO timeTablingUseSimulateAnnealDTO) {
        CattyResult<HashMap<String, List<String>>> cattyResult = new CattyResult<>();
        TimeTablingUseGeneticDTO timeTablingUseGeneticDTO = new TimeTablingUseGeneticDTO();
        BeanUtils.copyProperties(timeTablingUseSimulateAnnealDTO, timeTablingUseGeneticDTO);

        // 获取初始基因编码
        var geneMap = geneticService.initGene(timeTablingUseGeneticDTO);

        // 进行初步的时间分配
        List<String> initTimeGradeSubjectList = geneticService.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());

        // 对已经分配好时间的基因进行分类，生成以年级班级为类别的Map
        var gradeClassNoGeneMap = geneticService.getGradeClassNoGeneMap(initTimeGradeSubjectList);

        // 模拟退火算法
        geneMap = simulateAnnealCore(gradeClassNoGeneMap);

        cattyResult.setData(geneMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 模拟退火算法核心
     *
     * @param gradeClassNoGeneMap
     * @return
     */
    private HashMap<String, List<String>> simulateAnnealCore(HashMap<String, List<String>> gradeClassNoGeneMap) {
        BigDecimal temperature = INIT_TEMPERATURE;
        long start = System.currentTimeMillis();

        List<String> messageList = new ArrayList<>();
        FitnessScoreDTO oldFitnessScore = new FitnessScoreDTO();
        FitnessScoreDTO newFitnessScore = new FitnessScoreDTO();
        FitnessScoreDTO bestFitnessScore = new FitnessScoreDTO();
        var oldList = geneticService.merge(gradeClassNoGeneMap);

        while (temperature.compareTo(MIN_TEMPERATURE) > ZERO) {

            for (int i = ZERO; i < GENE_TIMES; i++) {
                // 先按照班级合并
                oldFitnessScore = geneticService.computerFitnessScore(oldFitnessScore, oldList);
                BeanUtils.copyProperties(oldFitnessScore, bestFitnessScore);

                var newList = geneticService.selectGenes(oldList, SELECT_NUM);
                newFitnessScore = geneticService.computerFitnessScore(newFitnessScore, newList);
                if (newFitnessScore.getScore() < oldFitnessScore.getScore()) {
                    oldList = new ArrayList<>(newList);
                    BeanUtils.copyProperties(newFitnessScore, oldFitnessScore);
                } else {
                    double sqrt = (oldFitnessScore.getScore() - newFitnessScore.getScore()) / temperature.doubleValue();
                    var probability = Math.pow(Math.E, sqrt);
                    var acceptProbability = Math.random();
                    if (probability > acceptProbability && probability < MAX_PRO) {
                        BeanUtils.copyProperties(newFitnessScore, oldFitnessScore);
                        oldList = new ArrayList<>(newList);
                    }
                    if (oldFitnessScore.getScore() < bestFitnessScore.getScore()) {
                        BeanUtils.copyProperties(oldFitnessScore, bestFitnessScore);
                    }
                }

                //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
                gradeClassNoGeneMap = geneticService.getGradeClassNoGeneMap(oldList);
            }

            temperature = STEP_TEMPERATURE.multiply(temperature);
            if (temperature.compareTo(MIN_TEMPERATURE) < ZERO) {
                temperature = temperature.add(BACK_TEMPERATURE);
            }

            long end = System.currentTimeMillis();
            long time = end - start;
            if (time > STOP_TIME || bestFitnessScore.getHardScore().equals(0)) {

                String message = backtrackingService.packMessage(bestFitnessScore);
                messageList.add(message);

                geneticService.markToTXT(String.valueOf(start), messageList);
                return gradeClassNoGeneMap;
            }

            String message = backtrackingService.packMessage(bestFitnessScore);
            messageList.add(message);
        }

        return gradeClassNoGeneMap;
    }

}
