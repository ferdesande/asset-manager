package org.fsg.assetmanager.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;

import java.util.Objects;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.fsg.assetmanager", importOptions = {ImportOption.DoNotIncludeTests.class})
@SuppressWarnings("unused")
class ArchitectureTest {
    private static final String PROJECT_ROOT = "org.fsg.assetmanager";
    private static final String DOMAIN_LAYER = PROJECT_ROOT + ".domain";
    private static final String INFRASTRUCTURE_LAYER = PROJECT_ROOT + ".infrastructure";

    private static final String ADAPTER_IN = INFRASTRUCTURE_LAYER + ".adapter.in";
    private static final String ADAPTER_OUT = INFRASTRUCTURE_LAYER + ".adapter.out";
    private static final String PACKAGES_ARCH_FILE = "/architecture.puml";

    // TODO: all allowEmptyShould MUST be removed when they are not needed
    // Basic rules
    @ArchTest
    void architectureShouldAdhereToPlantUmlDiagram(JavaClasses classes) {
        var plantUmlDiagram = ArchitectureTest.class.getResource(PACKAGES_ARCH_FILE);
        classes()
                .should(PlantUmlArchCondition.adhereToPlantUmlDiagram(
                        Objects.requireNonNull(plantUmlDiagram, PACKAGES_ARCH_FILE + " not found in resources"),
                        PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInAnyPackage(PROJECT_ROOT + "..")
                ))
                .as("Architecture should adhere to PlantUML diagram")
                .check(classes);
    }

    @ArchTest
    static final ArchRule noAccessToStandardStreams =
            noClasses()
                    .should(GeneralCodingRules.ACCESS_STANDARD_STREAMS)
                    .as("No class should access standard streams")
                    .because("Use proper logging instead of System.out/err");

    @ArchTest
    void architectureShouldBeFreeOfCycles(JavaClasses classes) {
        SlicesRuleDefinition.slices()
                .matching(PROJECT_ROOT + ".(*)..")
                .should()
                .beFreeOfCycles()
                .as("Architecture should be free of cycles between packages")
                .check(classes);
    }

    // Naming convention rules
    @ArchTest
    static final ArchRule restControllersShouldBeSuffixed =
            classes()
                    .that().resideInAPackage(ADAPTER_IN + ".rest..")
                    .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().haveSimpleNameEndingWith("Controller")
                    .allowEmptyShould(true)
                    .as("REST controllers should be suffixed with 'Controller'")
                    .because("REST controllers should follow naming convention");

    @ArchTest
    static final ArchRule springRepositoriesShouldBeSuffixed =
            classes()
                    .that().resideInAPackage(ADAPTER_OUT + ".persistence..")
                    .and().areInterfaces()
                    .and().areAssignableTo(Repository.class)
                    .should().haveSimpleNameEndingWith("Repository")
                    .as("Spring data repositories should be suffixed with 'Repository'");

    @ArchTest
    static final ArchRule jpaEntitiesShouldBeSuffixed =
            classes()
                    .that().resideInAPackage(ADAPTER_OUT + ".persistence..")
                    .and().areAnnotatedWith(Entity.class)
                    .should().haveSimpleNameEndingWith("Entity")
                    .as("JPA entities should be suffixed with 'Entity'")
                    .because("Persistence adapters should follow naming convention");

    // Other rules
    @ArchTest
    static final ArchRule allPortsShouldBeInterfaces =
            classes()
                    .that().resideInAPackage(DOMAIN_LAYER + ".port..")
                    .should().beInterfaces()
                    .orShould().beRecords()
                    .as("All ports should be interfaces (use cases, repositories) or records (DTOs)")
                    .because("All ports must define contracts and DTOs");

    @ArchTest
    static final ArchRule domainShouldBeIndependent =
            noClasses()
                    .that().resideInAPackage(DOMAIN_LAYER + "..")
                    .should().dependOnClassesThat()
                    .resideOutsideOfPackages(DOMAIN_LAYER + "..", "java..", "lombok..")
                    .as("Domain should have no external dependencies except Java standard libraries and lombok to " +
                            "reduce boilerplate")
                    .because("Domain is the core and should not depend on anything");
}
