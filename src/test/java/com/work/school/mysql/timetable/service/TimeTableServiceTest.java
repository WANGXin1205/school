package com.work.school.mysql.timetable.service;

import com.work.school.mysql.timetable.service.enums.BacktrackingTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:47 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TimeTableServiceTest {

    @Resource
    private TimeTableService timeTableService;


    @Test
    public void backtrackingBATest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            try {
                Integer grade = null;
                var cattyResult = timeTableService.backtracking(grade, BacktrackingTypeEnum.BA);
            } catch (Exception e) {
                continue;
            }
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
    }

    @Test
    public void backtrackingDWTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            Integer grade = null;
            var cattyResult = timeTableService.backtracking(grade, BacktrackingTypeEnum.DW_BA);
            Assert.assertTrue(cattyResult.isSuccess());
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
    }

    @Test
    public void forwardBacktrackingFCTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            Integer grade = null;
            try {
                var cattyResult = timeTableService.forwardBacktracking(grade, BacktrackingTypeEnum.FC_BA);
            } catch (Exception e) {
                continue;
            }
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
    }

    @Test
    public void forwardBacktrackingFCDWBATest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            Integer grade = null;
            try {
                var cattyResult = timeTableService.forwardBacktracking(grade, BacktrackingTypeEnum.FC_DW_BA);
                Assert.assertTrue(cattyResult.isSuccess());
            } catch (Exception e) {
                continue;
            }
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
    }

    @Test
    public void planTimeTableWithBacktrackingTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            long start = System.currentTimeMillis();
            var cattyResult = timeTableService.planTimeTableWithBacktracking();
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void planTimeTableWithDynamicWeightsAndBacktrackingTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            long start = System.currentTimeMillis();
            var cattyResult = timeTableService.planTimeTableUseDynamicWeightsAndBacktracking();
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void planTimeTableWithForwardCheckAndDynamicWeightBacktrackingBATest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            long start = System.currentTimeMillis();
            var cattyResult = timeTableService.planTimeTableWithForwardCheckDynamicWeightBacktracking(BacktrackingTypeEnum.FC_BA);
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void planTimeTableWithForwardCheckAndDynamicWeightBacktrackingDWTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            long start = System.currentTimeMillis();
            try {
                var cattyResult = timeTableService.planTimeTableWithForwardCheckDynamicWeightBacktracking(BacktrackingTypeEnum.FC_DW_BA);
                Assert.assertTrue(cattyResult.isSuccess());
            } catch (Exception ignored) {
                System.out.println("失败次序为:" + i);
            }
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void planTimeTableWithGeneticAlgorithmTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            var cattyResult = timeTableService.planTimeTableWithGenetic();
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void planTimeTableWithSimulateAnnealTest() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            long start = System.currentTimeMillis();
            var cattyResult = timeTableService.planTimeTableWithSimulateAnneal();
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        for (Long x : times) {
            System.out.println(x);
        }
//        Assert.assertTrue(cattyResult.isSuccess());
    }

}