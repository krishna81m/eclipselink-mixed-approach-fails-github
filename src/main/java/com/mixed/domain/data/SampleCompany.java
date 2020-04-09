package com.mixed.domain.data;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.OneToManyMapping;

public class SampleCompany extends SampleBasicCompany {

    public static void addToDescriptor(ClassDescriptor descriptor) {
        ExpressionBuilder exp;
        OneToManyMapping mapping;

        // Only read object attributes of type company
        exp = new ExpressionBuilder();
        mapping = (OneToManyMapping) descriptor.getMappingForAttributeName("m_Employees");

        // complex EL selection criteria
        mapping.setSelectionCriteria(mapping.buildSelectionCriteria()
            .and(exp.get("firstName")
                .equal("I")
                    .or(
                        exp.get("lastName").equal("AM")
                        .or(exp.get("middleInitial").isNull())
                    )
                    .and(
                        exp.get("version").isNull().not())
                    )
                );

    }

}
