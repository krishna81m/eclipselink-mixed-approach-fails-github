package com.mixed.spring.jpa;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * The wrapper bean of EntityManager to provide convenient api to access database.
 */
@Component
public class DatabaseQuery {
    private EntityManager entityManager;
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> List executeNativeQuery(String sqlString, Class<T> resultClass) {
        Query query = this.entityManager.createNativeQuery(sqlString, resultClass);
        List result = query.getResultList();
        return result;
    }

    public <T> T executeNativeQuerySingle(String sqlString, Class<T> resultClass, Object... arguments) {
        Query query = this.entityManager.createNativeQuery(sqlString, resultClass);
        int position = 1;
        for (Object argument: arguments) {
            query.setParameter(position, argument);
            position++;
        }
        List<T> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return this.entityManager.getCriteriaBuilder();
    }

    public <T> List executeCriteriaQuery(CriteriaQuery<T> criteriaQuery) {
        Query query = this.entityManager.createQuery(criteriaQuery);
        List result = query.getResultList();
        return result;
    }

    public <T> List executeQuery(String sql, List<Object> values) {
        Query query = this.entityManager.createQuery(sql);
        int index = 1;
        for (Object value: values) {
            query.setParameter("p"+index, value);
            index++;
        }
        List result = query.getResultList();
        return result;
    }

    public void executeUpdate(String sql, List<Object> values) {
        Query query = this.entityManager.createQuery(sql);
        int index = 1;
        for (Object value: values) {
            query.setParameter("p"+index, value);
            index++;
        }
        query.executeUpdate();
    }

    public void merge(Object entity) {
        this.entityManager.merge(entity);
    }

    public void lock(Object entity) {
        this.entityManager.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }
}
