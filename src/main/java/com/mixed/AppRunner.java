package com.mixed;

import com.mixed.data.repo.CompanyRepo;
import com.mixed.domain.data.SampleCompany;
import com.mixed.domain.data.SampleEmployee;
import com.mixed.spring.jpa.SpringDataJpaConfig;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.text.html.parser.Entity;
import java.util.List;

public class AppRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private static ApplicationContext context;

    public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(SpringDataJpaConfig.class);
        try {
            // verify JPA and EL graph usage
            runAssertions();

            // compare stacktraces, toString() using Aspect when accessing activeEmployees
        } catch (Throwable t){
            t.printStackTrace();
        } finally {
            EntityManager entityManager = (EntityManager) context.getBean("entityManager");
            entityManager.close();
        }
    }

    private static void runAssertions() {
        final Long COMPANY_ID = 2l;

        EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);
        CompanyRepo companyRepo = context.getBean(CompanyRepo.class);

        EntityManagerFactoryInfo entityManagerFactoryInfo = (EntityManagerFactoryInfo) entityManagerFactory;
        JpaEntityManagerFactory jpaEntityManagerFactory = (JpaEntityManagerFactory) entityManagerFactoryInfo.getNativeEntityManagerFactory();
        ServerSession jpaServerSession = jpaEntityManagerFactory.getServerSession();

        try {
            // access Natively!! using JPA ServerSession
            ReadObjectQuery readObjectQuery = new ReadObjectQuery();    // Contains common behavior for all read queries using objects
            readObjectQuery.setReferenceClass(SampleCompany.class);
            readObjectQuery.setSelectionId(COMPANY_ID);
            SampleCompany companyByEL = (SampleCompany) jpaServerSession.executeQuery(readObjectQuery);
            companyByEL.getEmployees();

            // access via JPA!!! using same JPA ServerSession
        SampleCompany company = (SampleCompany) companyRepo.findOne(COMPANY_ID);

            // returns a null vector of size 0
            List<SampleEmployee>  jpaEmpList = companyRepo.activeEmployees(COMPANY_ID);
            logger.info("JPA employee list size: " + jpaEmpList.size());

            List<SampleEmployee> employees = company.getEmployees();
            logger.info("EL Native employee list size: " + employees.size());

            logger.info(">>>>>>> Successfully accessed employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< ");
        } catch (Exception ex){
            logger.error(">>>>>>> Exception accessing employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< ", ex);
        }

    }

}
