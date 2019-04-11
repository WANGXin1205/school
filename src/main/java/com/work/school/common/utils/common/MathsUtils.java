package com.work.school.common.utils.common;

import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 12:15 AM
 * @Description
 */
public class MathsUtils {

    /**
     * 获取最大的数字
     *
     * @param numList
     * @return
     */
    public static Integer getMaxNum(List<Integer> numList) {
        if (CollectionUtils.isEmpty(numList)) {
            return null;
        }
        if (numList.size() == 1) {
            return numList.get(0);
        }
        Collections.sort(numList);
        Collections.reverse(numList);
        return numList.get(0);
    }

    /**
     * 比赛计分 去掉一个最高分 去掉一个最低分 再取平均分
     * @param strings
     * @return
     */
    public static BigDecimal getMatchScore(String[] strings) {

        List<BigDecimal> bigDecimals = new ArrayList<>();
        for (String x : strings) {
            BigDecimal bigDecimal = new BigDecimal(x);
            bigDecimals.add(bigDecimal);
        }
        Collections.sort(bigDecimals);
        bigDecimals.remove(0);
        bigDecimals.remove(bigDecimals.size()-1);

        BigDecimal allScore = bigDecimals.stream().reduce(BigDecimal::add).get();
        var avgScore = allScore.divide(new BigDecimal(bigDecimals.size()),2, RoundingMode.HALF_UP);
        System.out.println("计算出平均分是: " + avgScore);
        return avgScore;
    }
}
