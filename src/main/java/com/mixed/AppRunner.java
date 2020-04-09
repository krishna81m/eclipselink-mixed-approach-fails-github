package com.mixed;

import com.mixed.data.repo.CompanyRepo;
import com.mixed.domain.data.SampleCompany;
import com.mixed.domain.data.SampleEmployee;
import com.mixed.spring.jpa.SpringDataJpaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.persistence.EntityManager;
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
        // IOP Session access by EL session graph way << store a reference before replacing it
        // Session iopSession = TxHelper.getSession();

        CompanyRepo companyRepo = context.getBean(CompanyRepo.class);
        SampleCompany company = (SampleCompany) companyRepo.findOne(COMPANY_ID);

        try {
            List<SampleEmployee> employees = company.getEmployees();
            System.out.println(employees.size());
            logger.info(">>>>>>> Successfully accessed employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< ");
        } catch (Exception ex){
            logger.error(">>>>>>> Exception accessing employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< ", ex);
        }

    }

}
