package com.work.school.mysql.common.service.dto;

public class RandomGeneDTO {

    private Integer randomNo;

    private String gene;

    private String geneWithoutClassTime;

    private String classTime;

    public Integer getRandomNo() {
        return randomNo;
    }

    public void setRandomNo(Integer randomNo) {
        this.randomNo = randomNo;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getGeneWithoutClassTime() {
        return geneWithoutClassTime;
    }

    public void setGeneWithoutClassTime(String geneWithoutClassTime) {
        this.geneWithoutClassTime = geneWithoutClassTime;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    @Override
    public String toString() {
        return "RandomGeneDTO{" +
                "randomNo=" + randomNo +
                ", gene='" + gene + '\'' +
                ", geneWithoutClassTime='" + geneWithoutClassTime + '\'' +
                ", classTime='" + classTime + '\'' +
                '}';
    }
}
