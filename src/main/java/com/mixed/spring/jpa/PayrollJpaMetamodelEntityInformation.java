package com.mixed.spring.jpa;


import org.springframework.beans.BeanWrapper;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Set;

/**
 *  Extends from JpaMetamodelEntityInformation to customize isNew api which
 *  check new Object by compare id with 0l value.
 */
public class PayrollJpaMetamodelEntityInformation<T, ID extends Serializable> extends JpaMetamodelEntityInformation<T, ID> {
    private final SingularAttribute<? super T, ?> versionAttribute;

    public PayrollJpaMetamodelEntityInformation(Class<T> domainClass, Metamodel metamodel) {
        super(domainClass, metamodel);
        ManagedType<T> type = metamodel.managedType(domainClass);
        IdentifiableType<T> identifiableType = (IdentifiableType<T>) type;
        this.versionAttribute = findVersionAttribute(identifiableType, metamodel);
    }

    /**
     * Returns the version attribute of the given {@link ManagedType} or {@literal null} if none available.
     *
     * @param type must not be {@literal null}.
     * @param metamodel must not be {@literal null}.
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> SingularAttribute<? super T, ?> findVersionAttribute(IdentifiableType<T> type,
                                                                            Metamodel metamodel) {

        try {
            return type.getVersion(Object.class);
        } catch (IllegalArgumentException o_O) {
            // Needs workarounds as the method is implemented with a strict type check on e.g. Hibernate < 4.3
        }

        Set<SingularAttribute<? super T, ?>> attributes = type.getSingularAttributes();

        for (SingularAttribute<? super T, ?> attribute : attributes) {
            if (attribute.isVersion()) {
                return attribute;
            }
        }

        Class<?> superType = type.getJavaType().getSuperclass();

        try {

            ManagedType<?> managedSuperType = metamodel.managedType(superType);

            if (!(managedSuperType instanceof IdentifiableType)) {
                return null;
            }

            return (SingularAttribute<? super T, ?>) findVersionAttribute((IdentifiableType<T>) managedSuperType, metamodel);

        } catch (IllegalArgumentException o_O) {
            return null;
        }
    }

    @Override
    public boolean isNew(T entity) {

        if (versionAttribute == null || versionAttribute.getJavaType().isPrimitive()) {
            ID id = getId(entity);
            Class<ID> idType = getIdType();

            if (id instanceof Number) {
                return ((Number) id).longValue() == 0L;
            }

            throw new IllegalArgumentException(String.format("Unsupported primitive id type %s!", idType));
        }

        BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(entity);
        Object versionValue = wrapper.getPropertyValue(versionAttribute.getName());

        return versionValue == null;
    }
}