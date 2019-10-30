package com.work.school.mysql.common.service.dto;

import java.io.Serializable;

/**
 * @Author : Growlithe
 * @Date : 2019/9/23 10:02 AM
 * @Description
 */
public final class SubjectDefaultValueDTO implements Serializable {
    /**
     * 0次
     */
    private static final Integer ZERO_FREQUENCY = 0;
    /**
     * 一次
     */
    private static final Integer ONE_COUNT = 1;
    /**
     * 3次
     */
    private static final Integer THREE_COUNT = 3;

    public static Integer getZeroFrequency() {
        return ZERO_FREQUENCY;
    }

    public static Integer getOneCount() {
        return ONE_COUNT;
    }

    public static Integer getThreeCount() {
        return THREE_COUNT;
    }
}
