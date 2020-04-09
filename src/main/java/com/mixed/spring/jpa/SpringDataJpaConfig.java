package com.mixed.spring.jpa;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.platform.database.SQLServerPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy // <!--Enable AOP--> <aop:aspectj-autoproxy />
@ComponentScan({"com.mixed"})
@EnableJpaRepositories(basePackages = {"com.mixed.data.repo" })
public class SpringDataJpaConfig {

    public static final String H2_URL = "jdbc:h2:mem:dbo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;INIT=create schema if not exists dbo\\;SET SCHEMA dbo\\;SET TRACE_LEVEL_SYSTEM_OUT=1\\;SET MODE MSSQLServer\\;SET IGNORECASE TRUE\\;";
    public static final String H2_USER = "dbo";  // "dbo"

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDataJpaConfig.class);
    private static final String JPA_CONNECTION_POOL_INITIAL = "25";
    private boolean isWebApplicationContext = false;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "entityManager")
    public EntityManager entityManager() {
        EntityManager em = entityManagerFactory().createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        return em;
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public EntityManagerFactory entityManagerFactory() {
        return getEntityManagerFactory(false);
    }

    private EntityManagerFactory getEntityManagerFactory(boolean readOnlyDB) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setJpaVendorAdapter(jpaVendorAdapter());
        lef.setPackagesToScan(new String[]{"com.mixed"});
        lef.setLoadTimeWeaver(new CustomLoadTimeWeaver());

        /*
        * Setup the EclipseLink specific properties to make the Server session
        * similar/identical to the one created through ORM API by common.jar
        */
        Properties properties = new Properties();
        properties.put("exclude-unlisted-classes", "false");
        //eclipse link settings
        properties.put(PersistenceUnitProperties.SESSIONS_XML, "eclipselink_session_jpa_test.xml");
        properties.put(PersistenceUnitProperties.SESSION_NAME, "default");
        properties.put(PersistenceUnitProperties.WEAVING, "static");
        properties.put(PersistenceUnitProperties.JDBC_BIND_PARAMETERS, "true");
        properties.put(PersistenceUnitProperties.CACHE_STATEMENTS, "true");
        //set eclipselink.session-event-listener from Common.jar src/iop/Common/src/com/paycycle/data/SessionEventHandler.java
        // properties.put(PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, "com.paycycle.data.SessionEventHandler");

        // driver settings
        // properties.put(PersistenceUnitProperties.JDBC_DRIVER, "com.inet.tds.TdsDriver");

        // temporarily use mssql driver
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");

        setDBParams(properties, readOnlyDB);

        String connectionPoolRead = getReadPoolConnection(readOnlyDB);
        String connectionPoolWrite = getWritePoolConnection(readOnlyDB);
        //if it's unit test context where PropertiesManager is not initialized, just set default value
        if (!isWebApplicationContext && connectionPoolRead == null && connectionPoolWrite == null) {
            connectionPoolRead = JPA_CONNECTION_POOL_INITIAL;
            connectionPoolWrite = JPA_CONNECTION_POOL_INITIAL;
        }
        //common.jar uses exclusive connection pool
        properties.put(PersistenceUnitProperties.CONNECTION_POOL_READ + PersistenceUnitProperties.CONNECTION_POOL_INITIAL, connectionPoolRead);
        properties.put(PersistenceUnitProperties.CONNECTION_POOL_READ + PersistenceUnitProperties.CONNECTION_POOL_MIN, connectionPoolRead);
        properties.put(PersistenceUnitProperties.CONNECTION_POOL_READ + PersistenceUnitProperties.CONNECTION_POOL_MAX, connectionPoolRead);
        //default connection policy should also match the common.jar
        properties.put(PersistenceUnitProperties.CONNECTION_POOL + "default." + PersistenceUnitProperties.CONNECTION_POOL_INITIAL, connectionPoolWrite);
        properties.put(PersistenceUnitProperties.CONNECTION_POOL + "default." + PersistenceUnitProperties.CONNECTION_POOL_MIN, connectionPoolWrite);
        properties.put(PersistenceUnitProperties.CONNECTION_POOL + "default." + PersistenceUnitProperties.CONNECTION_POOL_MAX, connectionPoolWrite);

        // create tables in H2 datasource
//        properties.put("javax.persistence.schema-generation.database.action", "create");
//        properties.put("eclipselink.ddl-generation", "create-tables");
//        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.logging.level", "FINE");

        lef.setJpaProperties(properties);
        lef.afterPropertiesSet();

        return lef.getObject();
    }

    private String getReadPoolConnection(boolean readOnlyDB) {
        return JPA_CONNECTION_POOL_INITIAL;
    }

    private String getWritePoolConnection(boolean readOnlyDB) {
        return JPA_CONNECTION_POOL_INITIAL;
    }

    private void setDBParams(Properties properties, boolean readOnlyDB) {
        properties.put(PersistenceUnitProperties.JDBC_URL,H2_URL);
        properties.put(PersistenceUnitProperties.JDBC_USER,H2_USER);
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        SQLServerPlatform.setShouldIgnoreCaseOnFieldComparisons(true);
        EclipseLinkJpaVendorAdapter jpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.SQL_SERVER);
        jpaVendorAdapter.setDatabasePlatform(SQLServerPlatform.class.getName());
        jpaVendorAdapter.setGenerateDdl(false);
        return jpaVendorAdapter;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager platformTransactionManager = new JpaTransactionManager();
        platformTransactionManager.setEntityManagerFactory(entityManagerFactory());
        return platformTransactionManager;
    }

}

