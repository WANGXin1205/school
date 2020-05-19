package com.work.school.mysql.timetable.service.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PopulationDTO {
    /**
     * 评分
     */
    private Integer score;
    /**
     * 基因
     */
    private HashMap<String, List<String>> geneMap;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public HashMap<String, List<String>> getGeneMap() {
        return geneMap;
    }

    public void setGeneMap(HashMap<String, List<String>> geneMap) {
        this.geneMap = geneMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PopulationDTO that = (PopulationDTO) o;
        return Objects.equals(score, that.score) &&
                Objects.equals(geneMap, that.geneMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, geneMap);
    }

    @Override
    public String toString() {
        return "PopulationDTO{" +
                "score=" + score +
                ", geneMap=" + geneMap +
                '}';
    }
}
