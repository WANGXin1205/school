package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.enums.BacktrackingTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    private BacktrackingService backtrackingService;
    @Resource
    private GeneticService geneticService;
    @Resource
    private SimulateAnnealService simulateAnnealService;
    @Resource
    private PrepareService prepareService;

    /**
     * 准备默认学校配置
     *
     * @return
     */
    private TimeTablingUseBacktrackingDTO getTimeTablingUseBacktrackingDTO() {
        // 准备默认学校配置
        var prepareTimeTablingUseBacktrackingResult = prepareService.prepareTimeTablingUseBacktracking();
        if (!prepareTimeTablingUseBacktrackingResult.isSuccess()) {
            LOGGER.warn(prepareTimeTablingUseBacktrackingResult.getMessage());
            throw new TransactionException(prepareTimeTablingUseBacktrackingResult.getMessage());
        }
        return prepareTimeTablingUseBacktrackingResult.getData();
    }

    /**
     * 准备默认学校配置
     *
     * @return
     */
    private TimeTablingUseFCDWBacktrackingDTO getTimeTablingUseFCDWBacktrackingDTO(TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO) {
        TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO = new TimeTablingUseFCDWBacktrackingDTO();
        BeanUtils.copyProperties(timeTablingUseBacktrackingDTO, timeTablingUseFCDWBacktrackingDTO);
        backtrackingService.getDefaultConstraint(timeTablingUseFCDWBacktrackingDTO);
        HashMap<Integer, Integer> orderSubjectIdMap = new HashMap<>();
        for (Integer order:timeTablingUseBacktrackingDTO.getOrderGradeClassNumWorkDayTimeMap().keySet()){
            orderSubjectIdMap.put(order,null);
        }
        timeTablingUseFCDWBacktrackingDTO.setOrderSubjectIdMap(orderSubjectIdMap);
        return timeTablingUseFCDWBacktrackingDTO;
    }

    /**
     * 排课算法 前行检测和动态回溯回溯算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableWithForwardCheckDynamicWeightBacktracking(BacktrackingTypeEnum backtrackingTypeEnum) {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = this.getTimeTablingUseBacktrackingDTO();
        TimeTablingUseFCDWBacktrackingDTO timeTablingUseFCDWBacktrackingDTO = this.getTimeTablingUseFCDWBacktrackingDTO(timeTablingUseBacktrackingDTO);

        var forwardCheckDynamicWeightBacktrackingResult = backtrackingService.forwardCheckDynamicWeightBacktracking(timeTablingUseFCDWBacktrackingDTO, backtrackingTypeEnum);
        if (!forwardCheckDynamicWeightBacktrackingResult.isSuccess()) {
            cattyResult.setMessage(forwardCheckDynamicWeightBacktrackingResult.getMessage());
            return cattyResult;
        }
        var timeTableMap = forwardCheckDynamicWeightBacktrackingResult.getData();

        // 结果展示
        var timeTableNameMap = backtrackingService.convertTimeTableMapToTimeTableNameMap(timeTablingUseBacktrackingDTO.getAllSubjectNameMap(), timeTableMap);

        cattyResult.setData(timeTableNameMap);
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
        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = this.getTimeTablingUseBacktrackingDTO();

        // 排课核心算法
        var algorithmInPlanTimeTableWithBacktrackingResult = backtrackingService.backtracking(timeTablingUseBacktrackingDTO, BacktrackingTypeEnum.BA);
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

    /**
     * 排课接口 使用动态权重和回溯算法
     *
     * @return
     */
    public CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> planTimeTableUseDynamicWeightsAndBacktracking() {
        CattyResult<HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>>> cattyResult = new CattyResult<>();

        // 准备默认学校配置
        TimeTablingUseBacktrackingDTO timeTablingUseBacktrackingDTO = this.getTimeTablingUseBacktrackingDTO();

        // 排课核心算法
        var algorithmInPlanTimeTableWithBacktrackingResult = backtrackingService.backtracking(timeTablingUseBacktrackingDTO, BacktrackingTypeEnum.DW_BA);
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

}
