package com.mixed.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CompanyEntityRepo<T> extends JpaRepository<T, Long> {



}
