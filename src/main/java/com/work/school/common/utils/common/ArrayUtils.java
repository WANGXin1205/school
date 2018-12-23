package com.work.school.common.utils.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 数组扩展操作类
 * Created by matrixkeymac on 16-9-30.
 */
public class ArrayUtils {

    /**
     * 判断数组是否为null或长度为0
     *
     * @param arr 要判定的数组
     * @param <T> 数据类型
     * @return 为null或长度为0返回true, 否则返回false
     */
    public static <T> boolean isNullOrEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    /**
     * 将数组转换成List
     *
     * @param arr 要转换的数组
     * @param <T> 数据类型
     * @return List
     */
    public static <T> List<T> toList(T[] arr) {
        List<T> result = new ArrayList<>();
        for (T item : arr) {
            result.add(item);
        }

        return result;
    }

    /**
     * 判定数组中是否存在某项元素
     *
     * @param arr    数组
     * @param target 目标项
     * @param <T>    数据类型
     * @return 存在返回true, 否则返回false
     */
    public static <T> boolean contains(T[] arr, T target) {
        boolean result = false;
        Class<String> c = String.class;
        for (T item : arr) {
            if (target.getClass() == c) {
                if (target.equals(item)) {
                    result = true;
                    break;
                }
            } else {
                if (target == item) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 将字符串数组转化成长整型数组
     *
     * @param arr                    要转化得字符串数组
     * @param ignoreConvertException 是否忽略转化异常
     * @return 长整型数组
     * @throws Exception 转化异常
     */
    public static Long[] toLongArray(String[] arr, Boolean ignoreConvertException) throws Exception {
        List<Long> result = new ArrayList<>();

        for (String item : arr) {
            try {
                Long temp = Long.parseLong(item);
                result.add(temp);
            } catch (Exception e) {
                if (!ignoreConvertException) {
                    throw new Exception("ArrayUtils中的方法[toLongArray]发生数据类型转换异常.", e.getCause());
                }
            }
        }

        Long[] r = new Long[result.size()];
        result.toArray(r);
        return r;
    }
}
