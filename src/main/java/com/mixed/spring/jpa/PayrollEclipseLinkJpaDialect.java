package com.mixed.spring.jpa;

import org.eclipse.persistence.config.EntityManagerProperties;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.SQLException;

public class PayrollEclipseLinkJpaDialect extends EclipseLinkJpaDialect {
    @Override
    public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
            throws PersistenceException, SQLException, TransactionException {
        //Setting this flag so JPA RepeatableUnitOfWorkImpl will fall back to UnitOfWorkImpl during
        //calculate the changeset where it doesn't require cascade persist to be defined for the entity.
        entityManager.setProperty(EntityManagerProperties.PERSISTENCE_CONTEXT_COMMIT_WITHOUT_PERSIST_RULES, "true");
        return super.beginTransaction(entityManager, definition);
    }
}
