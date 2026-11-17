package org.ikigaidigital;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Hexagonal architecture rules, enforced by the build: dependencies point inward (adapters → application → domain),
 * and the core stays free of frameworks.
 */
@AnalyzeClasses(packages = "org.ikigaidigital", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "org.ikigaidigital.domain..";
    private static final String APPLICATION = "org.ikigaidigital.application..";
    private static final String INBOUND_ADAPTERS = "org.ikigaidigital.adapter.in..";
    private static final String OUTBOUND_ADAPTERS = "org.ikigaidigital.adapter.out..";

    /** TimeDeposit and TimeDepositCalculator must stay in the root package (no breaking changes), but belong to the domain. */
    private static final DescribedPredicate<JavaClass> DOMAIN_CLASSES = resideInAnyPackage(DOMAIN)
            .or(equivalentTo(TimeDeposit.class))
            .or(equivalentTo(TimeDepositCalculator.class))
            .as("domain classes");

    private static final DescribedPredicate<JavaClass> CORE_CLASSES = DOMAIN_CLASSES
            .or(resideInAnyPackage(APPLICATION))
            .as("core classes (domain + application)");

    @ArchTest
    static final ArchRule coreDoesNotDependOnFrameworks = noClasses().that(CORE_CLASSES)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..")
            .because("the core must stay framework-free; Spring wiring lives in config");

    @ArchTest
    static final ArchRule coreDoesNotDependOnAdaptersOrConfig = noClasses().that(CORE_CLASSES)
            .should().dependOnClassesThat().resideInAnyPackage("org.ikigaidigital.adapter..", "org.ikigaidigital.config..")
            .because("dependencies point inward: adapters depend on the core, never the other way round");

    @ArchTest
    static final ArchRule domainDoesNotDependOnApplication = noClasses().that(DOMAIN_CLASSES)
            .should().dependOnClassesThat().resideInAnyPackage(APPLICATION)
            .because("the domain is the innermost layer");

    @ArchTest
    static final ArchRule inboundAdaptersUseOnlyInPorts = noClasses().that().resideInAnyPackage(INBOUND_ADAPTERS)
            .should().dependOnClassesThat().resideInAnyPackage(OUTBOUND_ADAPTERS, "org.ikigaidigital.application.port.out..")
            .orShould().dependOnClassesThat().haveSimpleName("TimeDepositService")
            .because("controllers talk to use-case interfaces (in-ports) only");

    @ArchTest
    static final ArchRule outboundAdaptersDoNotDependOnInboundAdapters = noClasses().that().resideInAnyPackage(OUTBOUND_ADAPTERS)
            .should().dependOnClassesThat().resideInAnyPackage(INBOUND_ADAPTERS)
            .because("adapters are independent of each other");
}
