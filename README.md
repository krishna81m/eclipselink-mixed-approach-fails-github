# eclipselink-mixed-approach-fails-github
Sample project to prove that EL Mixed Approach Native EL + JPA fails for complex selection criteria mappings

Stack (3 maven dependencies):
	org.eclipse.persistence:eclipselink:jar:2.6.4:compile
	com.microsoft.sqlserver:mssql-jdbc:jar:6.1.0.jre8:compile
	org.springframework.data:spring-data-jpa:jar:1.11.3.RELEASE:compile

Bug: 
ReadAllQueries are cached by name and reuse common expressions SQL by caching such as ObjectKeyExpression hasMapping and when they are evaluated in a mixed approach, they fail to find the mappings.

Steps to reproduce:
Run com.mixed.AppRunner class to see exception
However, when you comment out CompanyRepo activeEmployees(), it works fine:

Reasoning:
on App startup, EntityManager sets up the EL ServerSession from com.mixed.domain.data.SampleTOPLinkProject ORM configuration with custom mapping com.mixed.domain.data.SampleCompany#addToDescriptor that adds the right selection criteria for m_Employees collection

However, when the com.mixed.data.repo.CompanyRepo.activeEmployees() JPQL query select c.m_Employees from com.mixed.domain.data.SampleCompany c where c.id = ?1 is evaluated during org.eclipse.persistence.internal.queries.ExpressionQueryMechanism#prepareReportQuerySelectAllRows, it seems to prepare the SQL correctly after setSQLStatement(statement) is done:

One of the QueryKeyExpressions for key firstName sets up the mapping with hasMapping=true:
	Query Key firstName
	   Query Key m_Employees
	      Base com.mixed.domain.data.SampleCompany
	mapping = {DirectToFieldMapping@3721} "org.eclipse.persistence.mappings.DirectToFieldMapping[firstName-->Employees.FirstName]"
	hasMapping = true

The SQL statement is prepared correctly:
	SQLSelectStatement(SELECT t0.Id, t0.FirstName, t0.LastName, t0.MiddleInitial, t0.Version, t0.CompanyID FROM {oj Companies t1 LEFT OUTER JOIN Employees t0 ON ((t0.CompanyID = t1.Id) AND (((t0.FirstName = ?) OR ((t0.LastName = ?) OR (t0.MiddleInitial IS NULL))) AND NOT ((t0.Version IS NULL))))} WHERE ((t1.Id = ?) AND (t1.Type = ?)))


The next method setCallFromStatement() seems to set the QueryKeyExpression for firstName sets hasMapping=false in the printSQL(printer) flows

And when we do get the Company and access Employees "Natively", 

        CompanyRepo companyRepo = context.getBean(CompanyRepo.class);
        SampleCompany company = (SampleCompany) companyRepo.findOne(COMPANY_ID);
        try {
            List<SampleEmployee> employees = company.getEmployees(); 
            System.out.println(employees.size()); <<<<< here, again accessing m_Employees collection natively >>>>

When you put a breakpoint in org.eclipse.persistence.internal.queries.ExpressionQueryMechanism#buildNormalSelectStatement, you can see that the cached/cloned QueryKeyExpression 


	Query Key firstName
	   Base com.mixed.domain.data.SampleEmployee
	 name = "firstName"
	...
	mapping = null
	hasMapping = false <<<

Does not find 
org.eclipse.persistence.internal.expressions.QueryKeyExpression#validateNode

        if ((queryKey == null) && (mapping == null)) {
            throw QueryException.invalidQueryKeyInExpression(getName());
        }


Solution:
It looks like if we use both EL natively and via JPA queries and access the same collection sub graphs, it throws an exception as hasMapping and isAttributeExpression etc., seemed to be cached for the Query Key clones. 

        fixClonedQueryKeyExpressions(clonedExpressions); <<< 
        selectStatement.normalize(getSession(), getDescriptor(), clonedExpressions);

	private void fixClonedQueryKeyExpressions(Map clonedExpressions) {
	clonedExpressions.values().stream()
	    .filter(e -> e instanceof QueryKeyExpression)
	    .forEach(expression -> {
		QueryKeyExpression queryKeyExpression = (QueryKeyExpression) expression;
		Expression baseExpression = queryKeyExpression.getBaseExpression();
		if(baseExpression != null && baseExpression instanceof  DataExpression){
		    ClassDescriptor descriptor = ((DataExpression) baseExpression).getDescriptor();
		    if(descriptor != null){
			Field field = com.paycycle.util.Helper.getField(QueryKeyExpression.class.getName(), "hasMapping");
			if (field != null) {
			    field.setAccessible(true);
			}
			try {
			    field.setBoolean(expression, true);
			} catch (IllegalAccessException e) {
			    throw new RuntimeException(e);
			}
		    }
		}
	    });
	}


Failure StackTrace:
[EL Warning]: 2020-04-08 17:03:31.165--ServerSession(951221468)--Thread(Thread[main,5,main])--Exception [EclipseLink-6015] (Eclipse Persistence Services - 2.6.4.v20160829-44060b6): org.eclipse.persistence.exceptions.QueryException
Exception Description: Invalid query key [firstName] in expression.
Query: ReadAllQuery(name="m_Employees" referenceClass=SampleEmployee )
17:03:31.167 [main] ERROR com.mixed.AppRunner - >>>>>>> Exception accessing employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< 
org.eclipse.persistence.exceptions.QueryException: 
Exception Description: Invalid query key [firstName] in expression.
Query: ReadAllQuery(name="m_Employees" referenceClass=SampleEmployee )
	at org.eclipse.persistence.exceptions.QueryException.invalidQueryKeyInExpression(QueryException.java:697)
	at org.eclipse.persistence.internal.expressions.QueryKeyExpression.validateNode(QueryKeyExpression.java:1011)
	at org.eclipse.persistence.expressions.Expression.normalize(Expression.java:3291)
	at org.eclipse.persistence.internal.expressions.DataExpression.normalize(DataExpression.java:369)
	at org.eclipse.persistence.internal.expressions.QueryKeyExpression.normalize(QueryKeyExpression.java:758)
	at org.eclipse.persistence.internal.expressions.QueryKeyExpression.normalize(QueryKeyExpression.java:671)
	at org.eclipse.persistence.internal.expressions.RelationExpression.normalize(RelationExpression.java:841)
	at org.eclipse.persistence.internal.expressions.CompoundExpression.normalize(CompoundExpression.java:224)
	at org.eclipse.persistence.internal.expressions.CompoundExpression.normalize(CompoundExpression.java:224)
	at org.eclipse.persistence.internal.expressions.CompoundExpression.normalize(CompoundExpression.java:232)
	at org.eclipse.persistence.internal.expressions.SQLSelectStatement.normalize(SQLSelectStatement.java:1521)
	at org.eclipse.persistence.internal.queries.ExpressionQueryMechanism.buildNormalSelectStatement(ExpressionQueryMechanism.java:550)
	at org.eclipse.persistence.internal.queries.ExpressionQueryMechanism.prepareSelectAllRows(ExpressionQueryMechanism.java:1722)
	at org.eclipse.persistence.queries.ReadAllQuery.prepareSelectAllRows(ReadAllQuery.java:885)
	at org.eclipse.persistence.queries.ReadAllQuery.prepare(ReadAllQuery.java:816)
	at org.eclipse.persistence.queries.DatabaseQuery.checkPrepare(DatabaseQuery.java:666)
	at org.eclipse.persistence.queries.ObjectLevelReadQuery.checkPrepare(ObjectLevelReadQuery.java:911)
	at org.eclipse.persistence.queries.DatabaseQuery.checkPrepare(DatabaseQuery.java:615)
	at org.eclipse.persistence.queries.DatabaseQuery.execute(DatabaseQuery.java:872)
	at org.eclipse.persistence.queries.ObjectLevelReadQuery.execute(ObjectLevelReadQuery.java:1134)
	at org.eclipse.persistence.queries.ReadAllQuery.execute(ReadAllQuery.java:460)
	at org.eclipse.persistence.internal.sessions.AbstractSession.internalExecuteQuery(AbstractSession.java:3271)
	at org.eclipse.persistence.internal.sessions.AbstractSession.executeQuery(AbstractSession.java:1857)
	at org.eclipse.persistence.internal.sessions.AbstractSession.executeQuery(AbstractSession.java:1839)
	at org.eclipse.persistence.internal.indirection.QueryBasedValueHolder.instantiate(QueryBasedValueHolder.java:133)
	at org.eclipse.persistence.internal.indirection.QueryBasedValueHolder.instantiate(QueryBasedValueHolder.java:120)
	at org.eclipse.persistence.internal.indirection.DatabaseValueHolder.getValue(DatabaseValueHolder.java:89)
	at org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder.instantiateImpl(UnitOfWorkValueHolder.java:173)
	at org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder.instantiate(UnitOfWorkValueHolder.java:234)
	at org.eclipse.persistence.internal.indirection.DatabaseValueHolder.getValue(DatabaseValueHolder.java:89)
	at org.eclipse.persistence.indirection.IndirectList.buildDelegate(IndirectList.java:271)
	at org.eclipse.persistence.indirection.IndirectList.getDelegate(IndirectList.java:455)
	at org.eclipse.persistence.indirection.IndirectList.size(IndirectList.java:829)
	at com.mixed.AppRunner.runAssertions(AppRunner.java:46)
	at com.mixed.AppRunner.main(AppRunner.java:25)
17:03:31.168 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Returning cached instance of singleton bean 'entityManager'

Process finished with exit code 0


Success stacktrace:
[EL Fine]: sql: 2020-04-08 17:06:25.418--ServerSession(209429254)--Connection(1278616846)--Thread(Thread[main,5,main])--SELECT Id, FirstName, LastName, MiddleInitial, Version, CompanyID FROM Employees WHERE ((CompanyID = ?) AND (((FirstName = ?) OR ((LastName = ?) OR (MiddleInitial IS NULL))) AND NOT ((Version IS NULL)))) ORDER BY LastName ASC, FirstName ASC
	bind => [2, I, AM]
0
17:06:25.424 [main] INFO com.mixed.AppRunner - >>>>>>> Successfully accessed employees subgraph in a mixed approach TopLinkProject Native way and JPA way <<<<<< 
17:06:25.424 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Returning cached instance of singleton bean 'entityManager'


Dependency list:
$ mvn dependency:list

	[INFO] The following files have been resolved:
	[INFO]    com.microsoft.azure:adal4j:jar:1.0.0:compile
	[INFO]    net.jcip:jcip-annotations:jar:1.0:compile
	[INFO]    com.microsoft.azure:azure-keyvault:jar:0.9.3:compile
	[INFO]    com.sun.jersey:jersey-json:jar:1.13:compile
	[INFO]    org.codehaus.jackson:jackson-jaxrs:jar:1.9.2:compile
	[INFO]    org.springframework.data:spring-data-commons:jar:1.13.3.RELEASE:compile
	[INFO]    com.sun.xml.bind:jaxb-impl:jar:2.2.3-1:compile
	[INFO]    org.springframework:spring-tx:jar:4.3.8.RELEASE:compile
	[INFO]    org.codehaus.jackson:jackson-mapper-asl:jar:1.9.2:compile
	[INFO]    org.apache.httpcomponents:httpclient:jar:4.3.6:compile
	[INFO]    org.glassfish:javax.json:jar:1.0.4:compile
	[INFO]    org.springframework:spring-beans:jar:4.3.8.RELEASE:compile
	[INFO]    ch.qos.logback:logback-classic:jar:1.2.3:compile
	[INFO]    org.eclipse.persistence:javax.persistence:jar:2.1.1:compile
	[INFO]    com.nimbusds:oauth2-oidc-sdk:jar:4.5:compile
	[INFO]    org.springframework:spring-expression:jar:4.3.8.RELEASE:compile
	[INFO]    org.eclipse.persistence:eclipselink:jar:2.6.4:compile
	[INFO]    org.springframework:spring-aop:jar:4.3.8.RELEASE:compile
	[INFO]    com.nimbusds:nimbus-jose-jwt:jar:3.1.2:compile
	[INFO]    javax.validation:validation-api:jar:1.1.0.Final:compile
	[INFO]    com.sun.jersey:jersey-client:jar:1.13:compile
	[INFO]    org.bouncycastle:bcprov-jdk15on:jar:1.51:compile
	[INFO]    com.google.code.gson:gson:jar:2.2.4:compile
	[INFO]    org.springframework.data:spring-data-jpa:jar:1.11.3.RELEASE:compile
	[INFO]    com.microsoft.sqlserver:mssql-jdbc:jar:6.1.0.jre8:compile
	[INFO]    org.apache.httpcomponents:httpcore:jar:4.3.3:compile
	[INFO]    org.codehaus.jackson:jackson-xc:jar:1.9.2:compile
	[INFO]    com.nimbusds:lang-tag:jar:1.4:compile
	[INFO]    org.slf4j:slf4j-api:jar:1.7.24:compile
	[INFO]    org.slf4j:jcl-over-slf4j:jar:1.7.24:runtime
	[INFO]    javax.xml.bind:jaxb-api:jar:2.2.2:compile
	[INFO]    javax.mail:mail:jar:1.4.5:compile
	[INFO]    org.aspectj:aspectjrt:jar:1.8.10:compile
	[INFO]    commons-lang:commons-lang:jar:2.6:compile
	[INFO]    javax.activation:activation:jar:1.1:compile
	[INFO]    net.minidev:json-smart:jar:1.1.1:compile
	[INFO]    ch.qos.logback:logback-core:jar:1.2.3:compile
	[INFO]    org.springframework:spring-orm:jar:4.3.8.RELEASE:compile
	[INFO]    commons-codec:commons-codec:jar:1.10:compile
	[INFO]    org.apache.commons:commons-lang3:jar:3.3.1:compile
	[INFO]    org.eclipse.persistence:commonj.sdo:jar:2.1.1:compile
	[INFO]    com.h2database:h2:jar:1.4.200:compile
	[INFO]    com.microsoft.azure:azure-core:jar:0.9.3:compile
	[INFO]    javax.inject:javax.inject:jar:1:compile
	[INFO]    com.sun.jersey:jersey-core:jar:1.13:compile
	[INFO]    org.springframework:spring-jdbc:jar:4.3.8.RELEASE:compile
	[INFO]    org.codehaus.jettison:jettison:jar:1.1:compile
	[INFO]    javax.xml.stream:stax-api:jar:1.0-2:compile
	[INFO]    org.springframework:spring-context:jar:4.3.8.RELEASE:compile
	[INFO]    org.springframework:spring-core:jar:4.3.8.RELEASE:compile
	[INFO]    commons-logging:commons-logging:jar:1.1.3:compile
	[INFO]    org.codehaus.jackson:jackson-core-asl:jar:1.9.2:compile
	[INFO]    stax:stax-api:jar:1.0.1:compile
