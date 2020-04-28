package com.mixed.domain.data;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.OneToManyMapping;

public class SampleCompany extends SampleBasicCompany {

    public static void addToDescriptor(ClassDescriptor descriptor) {
        // Only read object attributes of type company
        OneToManyMapping mapping = (OneToManyMapping) descriptor.getMappingForAttributeName("m_Employees");
        // Expression exp = new ExpressionBuilder(); // << Current Legacy TopLinkProject approach appends to mapping.buildSelectionCriteria() with this new Expression

        // Expression exp = new ExpressionBuilder(mapping.getReferenceClass()); // << Suggested approach to include mapping reference class
        // this.queryClass = queryClass;
        // this.wasQueryClassSetInternally = false;

        // Create new Expression based on original mapping criteria builder
        Expression previousCriteria = mapping.buildSelectionCriteria();
        ExpressionBuilder exp = previousCriteria.getBuilder();
        // explicitly set the reference class as it is not set using previousCriteria.getBuilder()
        exp.setQueryClass(mapping.getReferenceClass());

        // complex EL selection criteria
        mapping.setSelectionCriteria(previousCriteria
            .and(exp.get("firstName")
                .equal("First1")
                    .or(
                        exp.get("lastName").equal("Last1")
                        .or(exp.get("middleInitial").isNull())
                    )
                    .and(
                        exp.get("version").isNull().not())
                    )
            );
    }

}
