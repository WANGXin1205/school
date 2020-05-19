package com.work.school.mysql.timetable.service.dto;

public class FitnessScoreDTO {
    /**
     * 硬约束得分
     */
    private Integer hardScore;
    /**
     * 软约束得分
     */
    private Integer softScore;
    /**
     * 总得分
     */
    private Integer score;

    private Integer everyTimeHaveSubjectCount;
    private Integer oneTimeOneClassMoreSubjectCount;
    private Integer oneTimeOneTeacherMoreClassCount;
    private Integer fixedSubjectIdCount;
    private Integer oneClassMoreOtherSubject;
    private Integer needAreaSubjectCount;
    private Integer teacherOutMaxTimeCount;
    private Integer noMainSubjectCount;
    private Integer studentContinueSameClassCount;

    public Integer getHardScore() {
        return hardScore;
    }

    public void setHardScore(Integer hardScore) {
        this.hardScore = hardScore;
    }

    public Integer getSoftScore() {
        return softScore;
    }

    public void setSoftScore(Integer softScore) {
        this.softScore = softScore;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getEveryTimeHaveSubjectCount() {
        return everyTimeHaveSubjectCount;
    }

    public void setEveryTimeHaveSubjectCount(Integer everyTimeHaveSubjectCount) {
        this.everyTimeHaveSubjectCount = everyTimeHaveSubjectCount;
    }

    public Integer getOneTimeOneClassMoreSubjectCount() {
        return oneTimeOneClassMoreSubjectCount;
    }

    public void setOneTimeOneClassMoreSubjectCount(Integer oneTimeOneClassMoreSubjectCount) {
        this.oneTimeOneClassMoreSubjectCount = oneTimeOneClassMoreSubjectCount;
    }

    public Integer getOneTimeOneTeacherMoreClassCount() {
        return oneTimeOneTeacherMoreClassCount;
    }

    public void setOneTimeOneTeacherMoreClassCount(Integer oneTimeOneTeacherMoreClassCount) {
        this.oneTimeOneTeacherMoreClassCount = oneTimeOneTeacherMoreClassCount;
    }

    public Integer getFixedSubjectIdCount() {
        return fixedSubjectIdCount;
    }

    public void setFixedSubjectIdCount(Integer fixedSubjectIdCount) {
        this.fixedSubjectIdCount = fixedSubjectIdCount;
    }

    public Integer getOneClassMoreOtherSubject() {
        return oneClassMoreOtherSubject;
    }

    public void setOneClassMoreOtherSubject(Integer oneClassMoreOtherSubject) {
        this.oneClassMoreOtherSubject = oneClassMoreOtherSubject;
    }

    public Integer getNeedAreaSubjectCount() {
        return needAreaSubjectCount;
    }

    public void setNeedAreaSubjectCount(Integer needAreaSubjectCount) {
        this.needAreaSubjectCount = needAreaSubjectCount;
    }

    public Integer getTeacherOutMaxTimeCount() {
        return teacherOutMaxTimeCount;
    }

    public void setTeacherOutMaxTimeCount(Integer teacherOutMaxTimeCount) {
        this.teacherOutMaxTimeCount = teacherOutMaxTimeCount;
    }

    public Integer getNoMainSubjectCount() {
        return noMainSubjectCount;
    }

    public void setNoMainSubjectCount(Integer noMainSubjectCount) {
        this.noMainSubjectCount = noMainSubjectCount;
    }

    public Integer getStudentContinueSameClassCount() {
        return studentContinueSameClassCount;
    }

    public void setStudentContinueSameClassCount(Integer studentContinueSameClassCount) {
        this.studentContinueSameClassCount = studentContinueSameClassCount;
    }

    @Override
    public String toString() {
        return "FitnessScoreDTO{" +
                "hardScore=" + hardScore +
                ", softScore=" + softScore +
                ", score=" + score +
                ", everyTimeHaveSubjectCount=" + everyTimeHaveSubjectCount +
                ", oneTimeOneClassMoreSubjectCount=" + oneTimeOneClassMoreSubjectCount +
                ", oneTimeOneTeacherMoreClassCount=" + oneTimeOneTeacherMoreClassCount +
                ", fixedSubjectIdCount=" + fixedSubjectIdCount +
                ", oneClassMoreOtherSubject=" + oneClassMoreOtherSubject +
                ", needAreaSubjectCount=" + needAreaSubjectCount +
                ", teacherOutMaxTimeCount=" + teacherOutMaxTimeCount +
                ", noMainSubjectCount=" + noMainSubjectCount +
                ", studentContinueSameClassCount=" + studentContinueSameClassCount +
                '}';
    }
}
