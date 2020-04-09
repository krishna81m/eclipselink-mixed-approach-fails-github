package com.mixed.domain.data;

import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.indirection.ValueHolderInterface;

public class SampleEmployee {
    protected Long id = 0L;
    protected ValueHolderInterface m_company;
    protected String firstName;
    protected String lastName;
    protected String middleInitial;
    protected long version;

    public SampleEmployee() {
        m_company = new ValueHolder();
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public SampleCompany getCompany() {
        return (SampleCompany) m_company.getValue();
    }
    public void setCompany(SampleCompany company) {
        m_company.setValue(company);
    }

}
