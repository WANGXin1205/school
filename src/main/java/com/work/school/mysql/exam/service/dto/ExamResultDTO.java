package com.work.school.mysql.exam.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/3/28 7:36 PM
 * @Description
 */
public class ExamResultDTO implements Serializable {
    /**
     * 年级
     */
    private Integer grade;
    /**
     * 班级名称
     */
    private String className;
    /**
     * 科目名称
     */
    private String subjectName;
    /**
     * 考试人数
     */
    private Integer examineesCount;
    /**
     * 总分
     */
    private BigDecimal totalScore;
    /**
     * 平均分
     */
    private BigDecimal avgScore;
    /**
     * 优秀率
     */
    private BigDecimal excellentRate;
    /**
     * 及格率
     */
    private BigDecimal passRate;
    /**
     * 最高分
     */
    private BigDecimal maxScore;
    /**
     * 最高分学生Map
     */
    private Map<String,BigDecimal> maxScoreNameMap;
    /**
     * 最低分
     */
    private BigDecimal minScore;
    /**
     * 最低分学生Map
     */
    private Map<String,BigDecimal> minScoreNameMap;
    /**
     * 优秀人数 90分以上
     */
    private Integer excellentExamineesCount;
    /**
     * 优秀学生Map 姓名 分数
     */
    private Map<String,BigDecimal> excellentExamineesNameScoreMap;
    /**
     * 良好人数 1，2年级 80-89分，3-6年级75-89分
     */
    private Integer goodExamineesCount;
    /**
     * 良好学生Map 姓名 分数
     */
    private Map<String,BigDecimal> goodExamineesNameScoreMap;
    /**
     * 中等人数 1，2年级70-79分，3-6年级60-74分
     */
    private Integer middleExamineesCount;
    /**
     * 中等学生Map 姓名 分数
     */
    private Map<String,BigDecimal> middleExamineesNameScoreMap;
    /**
     * 不达标人数 1，2年级 69分以下 3-6年级 60分以下
     */
    private Integer notPassExamineesCount;
    /**
     * 不达标学生Map 姓名 分数
     */
    private Map<String,BigDecimal> notPassExamineesNameScoreMap;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getExamineesCount() {
        return examineesCount;
    }

    public void setExamineesCount(Integer examineesCount) {
        this.examineesCount = examineesCount;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore) {
        this.avgScore = avgScore;
    }

    public BigDecimal getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(BigDecimal excellentRate) {
        this.excellentRate = excellentRate;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public void setPassRate(BigDecimal passRate) {
        this.passRate = passRate;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore) {
        this.minScore = minScore;
    }

    public Map<String, BigDecimal> getMaxScoreNameMap() {
        return maxScoreNameMap;
    }

    public void setMaxScoreNameMap(Map<String, BigDecimal> maxScoreNameMap) {
        this.maxScoreNameMap = maxScoreNameMap;
    }

    public Map<String, BigDecimal> getMinScoreNameMap() {
        return minScoreNameMap;
    }

    public void setMinScoreNameMap(Map<String, BigDecimal> minScoreNameMap) {
        this.minScoreNameMap = minScoreNameMap;
    }

    public Integer getExcellentExamineesCount() {
        return excellentExamineesCount;
    }

    public void setExcellentExamineesCount(Integer excellentExamineesCount) {
        this.excellentExamineesCount = excellentExamineesCount;
    }

    public Map<String, BigDecimal> getExcellentExamineesNameScoreMap() {
        return excellentExamineesNameScoreMap;
    }

    public void setExcellentExamineesNameScoreMap(Map<String, BigDecimal> excellentExamineesNameScoreMap) {
        this.excellentExamineesNameScoreMap = excellentExamineesNameScoreMap;
    }

    public Integer getGoodExamineesCount() {
        return goodExamineesCount;
    }

    public void setGoodExamineesCount(Integer goodExamineesCount) {
        this.goodExamineesCount = goodExamineesCount;
    }

    public Map<String, BigDecimal> getGoodExamineesNameScoreMap() {
        return goodExamineesNameScoreMap;
    }

    public void setGoodExamineesNameScoreMap(Map<String, BigDecimal> goodExamineesNameScoreMap) {
        this.goodExamineesNameScoreMap = goodExamineesNameScoreMap;
    }

    public Integer getMiddleExamineesCount() {
        return middleExamineesCount;
    }

    public void setMiddleExamineesCount(Integer middleExamineesCount) {
        this.middleExamineesCount = middleExamineesCount;
    }

    public Map<String, BigDecimal> getMiddleExamineesNameScoreMap() {
        return middleExamineesNameScoreMap;
    }

    public void setMiddleExamineesNameScoreMap(Map<String, BigDecimal> middleExamineesNameScoreMap) {
        this.middleExamineesNameScoreMap = middleExamineesNameScoreMap;
    }

    public Integer getNotPassExamineesCount() {
        return notPassExamineesCount;
    }

    public void setNotPassExamineesCount(Integer notPassExamineesCount) {
        this.notPassExamineesCount = notPassExamineesCount;
    }

    public Map<String, BigDecimal> getNotPassExamineesNameScoreMap() {
        return notPassExamineesNameScoreMap;
    }

    public void setNotPassExamineesNameScoreMap(Map<String, BigDecimal> notPassExamineesNameScoreMap) {
        this.notPassExamineesNameScoreMap = notPassExamineesNameScoreMap;
    }

    @Override
    public String toString() {
        return "ExamResultDTO{" +
                "grade=" + grade +
                ", className='" + className + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", examineesCount=" + examineesCount +
                ", totalScore=" + totalScore +
                ", avgScore=" + avgScore +
                ", excellentRate=" + excellentRate +
                ", passRate=" + passRate +
                ", maxScore=" + maxScore +
                ", maxScoreNameMap=" + maxScoreNameMap +
                ", minScore=" + minScore +
                ", minScoreNameMap=" + minScoreNameMap +
                ", excellentExamineesCount=" + excellentExamineesCount +
                ", excellentExamineesNameScoreMap=" + excellentExamineesNameScoreMap +
                ", goodExamineesCount=" + goodExamineesCount +
                ", goodExamineesNameScoreMap=" + goodExamineesNameScoreMap +
                ", middleExamineesCount=" + middleExamineesCount +
                ", middleExamineesNameScoreMap=" + middleExamineesNameScoreMap +
                ", notPassExamineesCount=" + notPassExamineesCount +
                ", notPassExamineesNameScoreMap=" + notPassExamineesNameScoreMap +
                '}';
    }
}
