package com.mixed.h2;

import com.mixed.domain.data.SampleTOPLinkProject;
import com.mixed.h2.platform.MSSqlCustomH2Populator;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.sessions.server.ServerSession;

import java.io.File;

import static com.mixed.spring.jpa.SpringDataJpaConfig.H2_URL;
import static com.mixed.spring.jpa.SpringDataJpaConfig.H2_USER;

public class MockDatabaseSessionImplWithH2 extends DatabaseSessionImpl {
    private File cvsSchemaFile;
    public MockDatabaseSessionImplWithH2(File cvsSchemaFile) {
        super();
        this.cvsSchemaFile = cvsSchemaFile;
    }

    public static class CustomEclipseLinkH2Platform extends org.eclipse.persistence.platform.database.H2Platform {
        @Override
        protected void initializePlatformOperators() {
            super.initializePlatformOperators();
            addOperator(ExpressionOperator.right());
            addOperator(ExpressionOperator.datePart());
        }
    }

    public void login(AbstractSession session) throws Exception {
        SampleTOPLinkProject sampleTOPLinkProject = new SampleTOPLinkProject();
        Server server = sampleTOPLinkProject.createServerSession(10, 10);
        server.login();

        //init the H2 DB
        // AbstractSession session = inv.getInvokedInstance();
        setupH2Login((ServerSession) session);
        MSSqlCustomH2Populator msSqlCustomH2Populator = new MSSqlCustomH2Populator(cvsSchemaFile);
        msSqlCustomH2Populator.initSchema(session.getAccessor().getConnection());
    }

    private static void setupH2Login(ServerSession session) {
        session.logout();

        DatabaseLogin h2Login = new DatabaseLogin(new CustomEclipseLinkH2Platform());
        h2Login.setDriverClassName("org.h2.Driver");
        h2Login.setUserName(H2_USER);
        h2Login.setDatabaseURL(H2_URL);
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
