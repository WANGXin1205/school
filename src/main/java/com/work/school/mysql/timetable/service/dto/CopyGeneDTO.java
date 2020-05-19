package com.work.school.mysql.timetable.service.dto;

import java.io.Serializable;
import java.util.Objects;

public class CopyGeneDTO implements Serializable {

    private String gene;

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopyGeneDTO that = (CopyGeneDTO) o;
        return Objects.equals(gene, that.gene);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene);
    }

    @Override
    public String toString() {
        return "CopyGeneDTO{" +
                "gene='" + gene + '\'' +
                '}';
    }
}
