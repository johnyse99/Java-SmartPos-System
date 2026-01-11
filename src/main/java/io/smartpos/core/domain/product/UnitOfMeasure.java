package io.smartpos.core.domain.product;

public class UnitOfMeasure {
    private Integer id;
    private String name;
    private String abbreviation;

    public UnitOfMeasure() {
    }

    public UnitOfMeasure(Integer id, String name, String abbreviation) {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return abbreviation + " - " + name;
    }
}
