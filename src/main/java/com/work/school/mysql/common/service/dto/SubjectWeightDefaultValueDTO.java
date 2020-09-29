package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2019/9/23 10:02 AM
 * @Description
 */
public final class SubjectWeightDefaultValueDTO implements Serializable {
    /**
     * 最大权重
     */
    private static final Integer MAX_WEIGHT = 9999;
    /**
     * 最小权重
     */
    private static final Integer MIN_WEIGHT = 0;
    /**
     * 早上下午上课 特殊科目添加的权重
     */
    private static final Integer EXTEND_WEIGHT = 10;
    /**
     * 0次
     */
    private static final Integer ZERO_FREQUENCY = 0;
    /**
     * 步长1
     */
    private static final Integer ONE_STEP = 1;
    /**
     * 停机条件
     */
    private static final Integer STOP_WEIGHT = -10;

    public static Integer getMaxWeight() {
        return MAX_WEIGHT;
    }

    public static Integer getMinWeight() {
        return MIN_WEIGHT;
    }

    public static Integer getExtendWeight() {
        return EXTEND_WEIGHT;
    }

    public static Integer getZeroFrequency() {
        return ZERO_FREQUENCY;
    }

    public static Integer getOneStep() {
        return ONE_STEP;
    }

    public static Integer getStopWeight() {
        return STOP_WEIGHT;
    }

    @Override
    public String toString() {
        return "SubjectWeightDefaultValueDTO{}";
    }
}
