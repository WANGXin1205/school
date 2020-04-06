package com.work.school.mysql.timetable.service;


import com.work.school.common.CattyResult;
import com.work.school.mysql.common.service.dto.TimeTablingUseSimulateAnnealDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class ParticleSwarmOptimizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleSwarmOptimizationService.class);


    /**
     * 粒子群算法
     *
     * @param
     * @return
     */
    public CattyResult<HashMap<String, List<String>>> algorithmInPSO() {
        CattyResult<HashMap<String, List<String>>> cattyResult = new CattyResult<>();

        cattyResult.setSuccess(true);
        return cattyResult;
    }

}
