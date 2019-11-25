package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.FitnessFunctionEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class SimulateAnnealService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulateAnnealService.class);

    private static final int ZERO = 0;
    private static final int GENE_TIMES = 1000;
    // 初始温度
    private static final BigDecimal INIT_TEMPERATURE = new BigDecimal("100");
    // 每次下降温度
    private static final BigDecimal STEP_TEMPERATURE = new BigDecimal("1");
    // 最低温度
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("0.001");

    @Resource
    private GeneticService geneticService;

    /**
     * 模拟退火算发
     * @param timeTablingUseSimulateAnnealDTO
     * @return
     */
    public CattyResult<HashMap<String, List<String>>> algorithmInPlanTimeTableWithSimulateAnneal(TimeTablingUseSimulateAnnealDTO timeTablingUseSimulateAnnealDTO){
        CattyResult<HashMap<String, List<String>>> cattyResult = new CattyResult<>()
;
        TimeTablingUseGeneticDTO timeTablingUseGeneticDTO = new TimeTablingUseGeneticDTO();
        BeanUtils.copyProperties(timeTablingUseSimulateAnnealDTO,timeTablingUseGeneticDTO);

        // 获取初始基因编码
        var geneMap = geneticService.initGene(timeTablingUseGeneticDTO);

        // 进行初步的时间分配
        var initTimeGradeSubjectList = geneticService.initTime(geneMap, timeTablingUseGeneticDTO.getGradeClassCountMap());

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

        var oldList = geneticService.merge(gradeClassNoGeneMap);
        while (temperature.compareTo(MIN_TEMPERATURE) > ZERO){

            for (int i = ZERO; i < GENE_TIMES; i++) {
                // 先按照班级合并
                var oldScore = geneticService.computerFitnessFunction(oldList, FitnessFunctionEnum.HARD_SATISFIED);

                var newList = geneticService.selectGene(oldList);
                var newScore = geneticService.computerFitnessFunction(newList,FitnessFunctionEnum.HARD_SATISFIED);
                if (newScore > oldScore){
                    oldList = newList;
                }else {
                    double sqrt = (oldScore - newScore)/temperature.doubleValue();
                    var accept_probability = Math.pow(Math.E,sqrt);
                    if (accept_probability > Math.random()){
                        oldList = newList;
                    }
                }

                //将冲突消除后的个体再次进行分割，按班级进行分配准备进入下一次的进化
                gradeClassNoGeneMap = geneticService.getGradeClassNoGeneMap(newList);
            }

            temperature = temperature.subtract(STEP_TEMPERATURE);
        }

        return gradeClassNoGeneMap;
    }


}