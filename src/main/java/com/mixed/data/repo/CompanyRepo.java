package com.mixed.data.repo;

import com.mixed.domain.data.SampleCompany;
import com.mixed.domain.data.SampleEmployee;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompanyRepo<T extends SampleCompany> extends CompanyEntityRepo<T>{

    @Query(value="select c.m_Employees from com.mixed.domain.data.SampleCompany c where c.id = ?1")
    public List<SampleEmployee> activeEmployees(Long companyId);

}
