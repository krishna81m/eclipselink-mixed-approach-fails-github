package com.mixed.spring.jpa;

import org.springframework.core.OverridingClassLoader;
import org.springframework.instrument.classloading.SimpleInstrumentableClassLoader;
import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;

/**
 * Weaver to ignore loading EclipseLink, entities and application classes by SimpleThrowAwayClassLoader.
 * <note>
 * Spring JPA uses SimpleThrowAwayClassLoader to load entities temporarily by design as entities and the Vendor adapter will instruct the Spring JPA
 * to not load their libraries by this loader. EclipseLink vendor adapter does not exclude itself and so EclipseLink is loaded by main loader - Tomcat
 * and entities by the SimpleThrowAwayClassLoader resulting in class cast exception on classes like TopLinkProject or entities.
 *
 * To avoid NPE at entity.getType().getPackage().getName()
 * spring jpaMappingContext NPE ClassGeneratingPropertyAccessorFactory
 *      if (entity.getType().getClassLoader() == null ||
 *              entity.getType().getPackage().getName().startsWith("java"))
 *
 * </note>
 */
public class CustomLoadTimeWeaver extends SimpleLoadTimeWeaver {


    /**
     * Exclude the EclipseLink and application packages.
     * @param overridingClassLoader
     */
    private static void excludePackages (OverridingClassLoader overridingClassLoader){

        overridingClassLoader.excludePackage("com.mixed");

        overridingClassLoader.excludePackage("org.eclipse");
        overridingClassLoader.excludePackage("com.inet"); //exclude loading db driver too.
    }

    /**
     * Customize the SimpleInstrumentableClassLoader to exclude packages.
     * @return
     */
    private static SimpleInstrumentableClassLoader getCustomizedInstrumentableClassLoader(){
        SimpleInstrumentableClassLoader icl = new SimpleInstrumentableClassLoader(Thread.currentThread().getContextClassLoader());
        excludePackages(icl);
        return icl;
    }

    /**
     * Customize the SimpleThrowawayClassLoader to exclude packages.
     * @return
     */
    private static SimpleThrowawayClassLoader getCustomizedSimpleThrowawayClassLoader(){
        SimpleThrowawayClassLoader tcl = new SimpleThrowawayClassLoader(Thread.currentThread().getContextClassLoader());
        excludePackages(tcl);
        return tcl;
    }

    /**
     * Constructor
     */
    public CustomLoadTimeWeaver(){
        super(getCustomizedInstrumentableClassLoader());
    }

    @Override
    public ClassLoader getThrowawayClassLoader() {
        return getCustomizedSimpleThrowawayClassLoader();
    }
}
