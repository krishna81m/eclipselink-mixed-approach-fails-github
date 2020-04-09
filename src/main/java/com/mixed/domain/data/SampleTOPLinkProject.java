package com.mixed.domain.data;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.VersionLockingPolicy;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.DatabaseLogin;

/**
 * Sample TOPLinkProject
 *
 *  BasicCompany
 *      |
 *  SampleCompany --> Employees (Many)
 */

public class SampleTOPLinkProject extends org.eclipse.persistence.sessions.Project {

    public SampleTOPLinkProject() {
        setName("Test");
        applyLogin();
        addDescriptor(buildBasicCompanyClassDescriptor());
        addDescriptor(buildSampleCompanyClassDescriptor());
        addDescriptor(buildEmployeeClassDescriptor());
    }

    public void applyLogin() {
        DatabaseLogin login = new DatabaseLogin();
        setDatasourceLogin(login);
    }

    public ClassDescriptor buildBasicCompanyClassDescriptor() {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(SampleBasicCompany.class);
        descriptor.addTableName("Companies");
        descriptor.addPrimaryKeyFieldName("Companies.Id");

        // Inheritance Properties.
        descriptor.getInheritancePolicy().setClassIndicatorFieldName("Companies.Type");
        descriptor.getInheritancePolicy().addClassIndicator(SampleBasicCompany.class, "B");
        descriptor.getInheritancePolicy().addClassIndicator(SampleCompany.class, "C");
        // ClassDescriptor Properties.
        descriptor.useSoftCacheWeakIdentityMap();
        descriptor.setIdentityMapSize(100);
        descriptor.useRemoteSoftCacheWeakIdentityMap();
        descriptor.setRemoteIdentityMapSize(100);
        descriptor.setSequenceNumberFieldName("Companies.Id");
        descriptor.setSequenceNumberName("Id");
        VersionLockingPolicy lockingPolicy = new VersionLockingPolicy();
        lockingPolicy.setWriteLockFieldName("Companies.Version");
        lockingPolicy.storeInObject();
        descriptor.setOptimisticLockingPolicy(lockingPolicy);
        descriptor.setAmendmentClass(SampleBasicCompany.class);
        descriptor.setAmendmentMethodName("addToDescriptor");

        // Query Manager.
        descriptor.getQueryManager().checkCacheForDoesExist();


        // Event Manager.

        // Mappings.
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setFieldName("Companies.Id");
        descriptor.addMapping(idMapping);

        DirectToFieldMapping versionMapping = new DirectToFieldMapping();
        versionMapping.setAttributeName("version");
        versionMapping.setFieldName("Companies.Version");
        descriptor.addMapping(versionMapping);

        DirectToFieldMapping nameMapping = new DirectToFieldMapping();
        nameMapping.setAttributeName("name");
        nameMapping.setFieldName("Companies.Name");
        descriptor.addMapping(nameMapping);

        DirectToFieldMapping descMapping = new DirectToFieldMapping();
        descMapping.setAttributeName("description");
        descMapping.setFieldName("Companies.Description");
        descriptor.addMapping(descMapping);

        DirectToFieldMapping typeMapping = new DirectToFieldMapping();
        typeMapping.setAttributeName("type");
        typeMapping.setFieldName("Companies.Type");
        descriptor.addMapping(typeMapping);

        // TODO: add ORM inheritance
        OneToManyMapping m_EmployeesMapping = new OneToManyMapping();
        m_EmployeesMapping.setAttributeName("m_Employees");
        m_EmployeesMapping.privateOwnedRelationship();
        m_EmployeesMapping.setReferenceClass(SampleEmployee.class);
        m_EmployeesMapping.useTransparentCollection();
        m_EmployeesMapping.useCollectionClass(org.eclipse.persistence.indirection.IndirectList.class);
        m_EmployeesMapping.addAscendingOrdering("lastName");
        m_EmployeesMapping.addAscendingOrdering("firstName");
        m_EmployeesMapping.addTargetForeignKeyFieldName("Employees.CompanyID", "Companies.Id");
        descriptor.addMapping(m_EmployeesMapping);

        // add custom selection criteria for Employees
        descriptor.setAmendmentClass(SampleCompany.class);
        descriptor.setAmendmentMethodName("addToDescriptor");
        descriptor.applyAmendmentMethod();

        return descriptor;
    }

    public ClassDescriptor buildSampleCompanyClassDescriptor() {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(SampleCompany.class);
        descriptor.addTableName("Companies");

        // Inheritance Properties.
        descriptor.getInheritancePolicy().setParentClass(SampleBasicCompany.class);

        // ClassDescriptor Properties.
        descriptor.onlyRefreshCacheIfNewerVersion();
        descriptor.setAlias("");


        // Query Manager.
        descriptor.getQueryManager().checkCacheForDoesExist();
        return descriptor;
    }


    public ClassDescriptor buildEmployeeClassDescriptor() {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(SampleEmployee.class);
        descriptor.addTableName("Employees");
        descriptor.addPrimaryKeyFieldName("Employees.Id");

        // ClassDescriptor Properties.
        descriptor.onlyRefreshCacheIfNewerVersion();
        descriptor.setAlias("Employees");
        descriptor.setSequenceNumberFieldName("Employees.Id");
        descriptor.setSequenceNumberName("Id");

        VersionLockingPolicy lockingPolicy = new VersionLockingPolicy();
        lockingPolicy.setWriteLockFieldName("Employees.Version");
        lockingPolicy.storeInObject();
        descriptor.setOptimisticLockingPolicy(lockingPolicy);

        // Query Manager.
        descriptor.getQueryManager().checkCacheForDoesExist();

        // Mappings.
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setFieldName("Employees.Id");
        descriptor.addMapping(idMapping);

        DirectToFieldMapping lastNameMapping = new DirectToFieldMapping();
        lastNameMapping.setAttributeName("lastName");
        lastNameMapping.setFieldName("Employees.LastName");
        descriptor.addMapping(lastNameMapping);

        DirectToFieldMapping firstNameMapping = new DirectToFieldMapping();
        firstNameMapping.setAttributeName("firstName");
        firstNameMapping.setFieldName("Employees.FirstName");
        descriptor.addMapping(firstNameMapping);

        DirectToFieldMapping middleInitialMapping = new DirectToFieldMapping();
        middleInitialMapping.setAttributeName("middleInitial");
        middleInitialMapping.setFieldName("Employees.MiddleInitial");
        descriptor.addMapping(middleInitialMapping);

        OneToOneMapping companyMapping = new OneToOneMapping();
        companyMapping.setAttributeName("m_company");
        companyMapping.setReferenceClass(SampleCompany.class);
        companyMapping.useBasicIndirection();
        companyMapping.addForeignKeyFieldName("Employees.CompanyID", "Companies.Id");
        descriptor.addMapping(companyMapping);

        DirectToFieldMapping versionMapping = new DirectToFieldMapping();
        versionMapping.setAttributeName("version");
        versionMapping.setFieldName("Employees.Version");
        descriptor.addMapping(versionMapping);

        return descriptor;
    }

}
