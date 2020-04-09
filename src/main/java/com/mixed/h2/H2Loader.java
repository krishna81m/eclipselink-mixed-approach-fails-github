package com.mixed.h2;

import com.mixed.h2.platform.MSSqlCustomH2Populator;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class H2Loader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        EntityManagerFactoryInfo entityManagerFactoryInfo = (EntityManagerFactoryInfo) entityManagerFactory;
        JpaEntityManagerFactory jpaEntityManagerFactory = (JpaEntityManagerFactory) entityManagerFactoryInfo.getNativeEntityManagerFactory();
        ServerSession session = jpaEntityManagerFactory.getServerSession();

        // pass H2 settings into session login again before connecting
        setupH2(session);

        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        Resource resource = applicationContext.getResource("classpath:db.csv");

        // connect again with new settings
        session.connect();
        MSSqlCustomH2Populator msSqlCustomH2Populator = null;

        try {
            msSqlCustomH2Populator = new MSSqlCustomH2Populator(
                    resource.getFile());
            msSqlCustomH2Populator.initSchema(session.getAccessor().getConnection());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupH2(ServerSession session) {
        session.logout();

        DatabaseLogin h2Login = new DatabaseLogin(new MockDatabaseSessionImplWithH2.CustomEclipseLinkH2Platform());
        h2Login.setDriverClassName("org.h2.Driver");
        h2Login.setUserName("dbo");
        h2Login.setDatabaseURL("jdbc:h2:mem:dbo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;INIT=create schema if not exists dbo\\;SET SCHEMA dbo\\;SET TRACE_LEVEL_SYSTEM_OUT=1\\;SET MODE MSSQLServer\\;SET IGNORECASE TRUE\\;");
        h2Login.setDefaultSequence(session.getLogin().getDefaultSequence());

        session.getProject().setLogin(h2Login);
        session.setDefaultConnectionPool();
        session.setReadConnectionPool(h2Login);

        // setup other connection pool will be used by DAF as well
        for(String connectionPool : session.getConnectionPools().keySet()){
            if(connectionPool.equals(ServerSession.DEFAULT_POOL)){
                continue;
            }
            ConnectionPool primaryShardConnectionPool = session.getConnectionPool(connectionPool);
            primaryShardConnectionPool.setLogin(h2Login);
        }
    }

}
