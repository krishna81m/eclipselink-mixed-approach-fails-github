package com.mixed.domain.data;

import java.util.ArrayList;
import java.util.List;

public class SampleBasicCompany {
    private Long id;
    private Long version;
    private String name;
    private String description;
    private String type;

    protected List<SampleEmployee> m_Employees;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SampleEmployee> getEmployees() {
        return m_Employees;
    }

    public SampleBasicCompany() {
        m_Employees = new ArrayList<>();
    }

}
