package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    private BacktrackingService backtrackingService;
    @Resource
    private GeneticService geneticService;
    @Resource
    private SimulateAnnealService simulateAnnealService;
    @Resource
    private PrepareService prepareService;

    /**
     * 模拟退火算法 这个方法基本没有成功
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableWithSimulateAnneal() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        var prepareTimeTablingUseSimulateAnnealResult = prepareService.prepareTimeTablingUseSimulateAnneal();
        if (!prepareTimeTablingUseSimulateAnnealResult.isSuccess()) {
            LOGGER.warn(prepareTimeTablingUseSimulateAnnealResult.getMessage());
            cattyResult.setMessage(prepareTimeTablingUseSimulateAnnealResult.getMessage());
            return cattyResult;
        }
        TimeTablingUseSimulateAnnealDTO timeTablingUseSimulateAnnealDTO = prepareTimeTablingUseSimulateAnnealResult.getData();

        var algorithmInPlanTimeTableWithSimulateAnnealResult = simulateAnnealService.algorithmInPlanTimeTableWithSimulateAnneal(timeTablingUseSimulateAnnealDTO);
        if (!algorithmInPlanTimeTableWithSimulateAnnealResult.isSuccess()) {
            LOGGER.warn(algorithmInPlanTimeTableWithSimulateAnnealResult.getMessage());
            cattyResult.setMessage(algorithmInPlanTimeTableWithSimulateAnnealResult.getMessage());
            return cattyResult;
        }
        var geneMap = algorithmInPlanTimeTableWithSimulateAnnealResult.getData();

        var convertToTimeTableNameMapResult = geneticService.convertToTimeTableNameMap(geneMap, timeTablingUseSimulateAnnealDTO.getAllSubjectNameMap());
        if (!convertToTimeTableNameMapResult.isSuccess()) {
            LOGGER.warn(convertToTimeTableNameMapResult.getMessage());
            cattyResult.setMessage(convertToTimeTableNameMapResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = convertToTimeTableNameMapResult.getData();

        cattyResult.setData(timeTableMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 排课算法 回溯算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableWithBacktracking() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        var prepareTimeTablingUseBacktrackingResult = prepareService.prepareTimeTablingUseBacktracking();
        if (!prepareTimeTablingUseBacktrackingResult.isSuccess()) {
            LOGGER.warn(prepareTimeTablingUseBacktrackingResult.getMessage());
            cattyResult.setMessage(prepareTimeTablingUseBacktrackingResult.getMessage());
            return cattyResult;
        }
        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = prepareTimeTablingUseBacktrackingResult.getData();

        // 排课核心算法
        var algorithmInPlanTimeTableWithBacktrackingResult = backtrackingService.algorithmInPlanTimeTableWithBacktracking(timeTablingUseBacktrackingDTO);
        if (!algorithmInPlanTimeTableWithBacktrackingResult.isSuccess()) {
            LOGGER.warn(algorithmInPlanTimeTableWithBacktrackingResult.getMessage());
            cattyResult.setMessage(algorithmInPlanTimeTableWithBacktrackingResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = algorithmInPlanTimeTableWithBacktrackingResult.getData();

        // 结果展示
        var timeTableNameMap = backtrackingService.convertTimeTableMapToTimeTableNameMap(timeTablingUseBacktrackingDTO.getAllSubjectNameMap(), timeTableMap);

        cattyResult.setData(timeTableNameMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

//    /**
//     * 排课接口 使用动态权重和回溯算法
//     *
//     * @return
//     */
//    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableUseDynamicWeightsAndBacktracking() {
//        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();
//
//        // 准备默认学校配置
//        var prepareTimeTablingUseDynamicWeightsAndBacktrackingResult = prepareService.prepareTimeTablingUseDynamicWeightsAndBacktracking();
//        if (!prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.isSuccess()) {
//            LOGGER.warn(prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.getMessage());
//            cattyResult.setMessage(prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.getMessage());
//            return cattyResult;
//        }
//        TimeTablingUseDynamicWeightsAndBacktrackingDTO timeTablingUseDynamicWeightsAndBacktrackingDTO = prepareTimeTablingUseDynamicWeightsAndBacktrackingResult.getData();
//
//        // 排课核心算法
//        var algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult = backtrackingService.algorithmInPlanTimeTableWithDynamicWeightsAndBacktracking(timeTablingUseDynamicWeightsAndBacktrackingDTO);
//        if (!algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.isSuccess()) {
//            LOGGER.warn(algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.getMessage());
//            cattyResult.setMessage(algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.getMessage());
//            return cattyResult;
//        }
//        var timeTableMap = algorithmInPlanTimeTableWithDynamicWeightsAndBacktrackingResult.getData();
//
//        // 结果展示
//        var timeTableNameMap = backtrackingService.convertTimeTableMapToTimeTableNameMap(timeTablingUseDynamicWeightsAndBacktrackingDTO.getAllSubjectNameMap(), timeTableMap);
//
//        cattyResult.setData(timeTableNameMap);
//        cattyResult.setSuccess(true);
//        return cattyResult;
//    }

    /**
     * 排课算法 遗传算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableWithGenetic() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        var prepareTimeTablingUseGeneticResult = prepareService.prepareTimeTablingUseGenetic();
        if (!prepareTimeTablingUseGeneticResult.isSuccess()) {
            LOGGER.warn(prepareTimeTablingUseGeneticResult.getMessage());
            cattyResult.setMessage(prepareTimeTablingUseGeneticResult.getMessage());
            return cattyResult;
        }
        TimeTablingUseGeneticDTO timeTablingUseGeneticDTO = prepareTimeTablingUseGeneticResult.getData();

        var algorithmInPlanTimeTableWithGeneticResult = geneticService.algorithmInPlanTimeTableWithGenetic(timeTablingUseGeneticDTO);
        if (!algorithmInPlanTimeTableWithGeneticResult.isSuccess()) {
            LOGGER.warn(algorithmInPlanTimeTableWithGeneticResult.getMessage());
            cattyResult.setMessage(algorithmInPlanTimeTableWithGeneticResult.getMessage());
            return cattyResult;
        }
        var geneMap = algorithmInPlanTimeTableWithGeneticResult.getData();

        var convertToTimeTableNameMapResult = geneticService.convertToTimeTableNameMap(geneMap, timeTablingUseGeneticDTO.getAllSubjectNameMap());
        if (!convertToTimeTableNameMapResult.isSuccess()) {
            LOGGER.warn(convertToTimeTableNameMapResult.getMessage());
            cattyResult.setMessage(convertToTimeTableNameMapResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = convertToTimeTableNameMapResult.getData();

        cattyResult.setData(timeTableMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

}
