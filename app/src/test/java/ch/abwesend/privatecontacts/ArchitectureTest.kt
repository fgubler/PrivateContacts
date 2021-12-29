package ch.abwesend.privatecontacts

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures

// packages
private const val ROOT_PACKAGE = "ch.abwesend.privatecontacts"

private const val APPLICATION_PACKAGE = "$ROOT_PACKAGE.application.."
private const val VIEW_PACKAGE = "$ROOT_PACKAGE.view.."
private const val DOMAIN_PACKAGE = "$ROOT_PACKAGE.domain.."
private const val DOMAIN_LIB_PACKAGE = "$DOMAIN_PACKAGE.lib.."
private const val INFRASTRUCTURE_PACKAGE = "$ROOT_PACKAGE.infrastructure.."

// layers
private const val APPLICATION_LAYER = "APPLICATION"
private const val VIEW_LAYER = "VIEW"
private const val DOMAIN_LAYER = "DOMAIN"
private const val INFRASTRUCTURE_LAYER = "INFRASTRUCTURE"

@Suppress("unused", "PropertyName")
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ArchitectureTest {
    @ArchTest
    val `view layer may only be accessed by application`: ArchRule = layers()
        .whereLayer(VIEW_LAYER)
        .mayOnlyBeAccessedByLayers(APPLICATION_LAYER)

    @ArchTest
    val `infrastructure layer may only be accessed by application`: ArchRule = layers()
        .whereLayer(INFRASTRUCTURE_LAYER)
        .mayOnlyBeAccessedByLayers(APPLICATION_LAYER)

    @ArchTest
    val `application layer may not be accessed by any other layers`: ArchRule = layers()
        .whereLayer(APPLICATION_LAYER)
        .mayNotBeAccessedByAnyLayer()

    @ArchTest
    val `only infrastructure layer may access entities`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_PACKAGE)
        .should().dependOnClassesThat().haveSimpleNameEndingWith("Entity")

    @ArchTest
    val `only infrastructure layer may access daos`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_PACKAGE)
        .should().dependOnClassesThat().haveSimpleNameEndingWith("Dao")

    private fun layers() = Architectures.layeredArchitecture()
        .layer(APPLICATION_LAYER).definedBy(APPLICATION_PACKAGE)
        .layer(VIEW_LAYER).definedBy(VIEW_PACKAGE)
        .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
        .layer(INFRASTRUCTURE_LAYER).definedBy(INFRASTRUCTURE_PACKAGE)
}
