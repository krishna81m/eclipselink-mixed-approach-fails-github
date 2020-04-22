package com.mixed.domain.data;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.OneToManyMapping;

public class SampleCompany extends SampleBasicCompany {

    public static void addToDescriptor(ClassDescriptor descriptor) {
        ExpressionBuilder exp;
        OneToManyMapping mapping;

        // Only read object attributes of type company
        mapping = (OneToManyMapping) descriptor.getMappingForAttributeName("m_Employees");
        exp = new ExpressionBuilder();

        // complex EL selection criteria
        mapping.setSelectionCriteria(mapping.buildSelectionCriteria()
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
