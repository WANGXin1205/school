package com.work.school.common.utils.common;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 12:15 AM
 * @Description
 */
public class MathsUtils {

    /**
     * 获取最大的数字
     * @param numList
     * @return
     */
    public static Integer getMaxNum(List<Integer> numList){
        if (CollectionUtils.isEmpty(numList)){
            return null;
        }
        if (numList.size() == 1){
            return numList.get(0);
        }
        Collections.sort(numList);
        Collections.reverse(numList);
        return numList.get(0);
    }
}
