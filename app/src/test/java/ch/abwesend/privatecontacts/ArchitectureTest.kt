/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.core.importer.Location
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchIgnore
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
private const val INFRASTRUCTURE_PACKAGE_PARTIAL = "$ROOT_PACKAGE.infrastructure"
private const val INFRASTRUCTURE_PACKAGE = "$INFRASTRUCTURE_PACKAGE_PARTIAL.."

private const val INFRASTRUCTURE_CONTACT_STORE_PACKAGE =
    "$INFRASTRUCTURE_PACKAGE_PARTIAL.repository.androidcontacts.."
private const val INFRASTRUCTURE_ADDRESS_FORMATTING_PACKAGE =
    "$INFRASTRUCTURE_PACKAGE_PARTIAL.service.addressformatting.."
private const val INFRASTRUCTURE_VCARD_PACKAGE =
    "$INFRASTRUCTURE_PACKAGE_PARTIAL.repository.vcard.."

private const val EXT_KOIN_PACKAGE = "org.koin.core.component.."
private const val EXT_CONTACT_STORE_PACKAGE = "com.alexstyl.contactstore.."
private const val EXT_EZ_VCARD_PACKAGE = "ezvcard"
private const val EXT_ADDRESS_LIBRARY_PACKAGE = "com.google.i18n.addressinput.."

// layers
private const val APPLICATION_LAYER = "APPLICATION"
private const val VIEW_LAYER = "VIEW"
private const val DOMAIN_LAYER = "DOMAIN"
private const val INFRASTRUCTURE_LAYER = "INFRASTRUCTURE"

@Suppress("unused", "PropertyName")
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [DoNotIncludeTests::class, DoNotIncludeKotlinTests::class]
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

    @ArchIgnore(reason = "The methods on KoinHelper are all inline-functions => cannot test this...")
    @ArchTest
    val `only Koin helper classes may access Koin`: ArchRule = noClasses()
        .that().haveSimpleNameNotContaining("Koin")
        .and().haveSimpleNameNotContaining("Application")
        .should().accessClassesThat().resideInAPackage(EXT_KOIN_PACKAGE)

    @ArchTest
    val `only androidcontacts package may access ContactStore library`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_CONTACT_STORE_PACKAGE)
        .and().resideOutsideOfPackage(APPLICATION_PACKAGE)
        .should().accessClassesThat().resideInAPackage(EXT_CONTACT_STORE_PACKAGE)

    @ArchTest
    val `only androidcontacts package may access itself`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_CONTACT_STORE_PACKAGE)
        .and().resideOutsideOfPackage(APPLICATION_PACKAGE)
        .should().accessClassesThat().resideInAPackage(INFRASTRUCTURE_CONTACT_STORE_PACKAGE)

    @ArchTest
    val `only vcard package may access ez-vcard library`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_VCARD_PACKAGE)
        .and().resideOutsideOfPackage(APPLICATION_PACKAGE)
        .should().accessClassesThat().resideInAPackage(EXT_EZ_VCARD_PACKAGE)

    @ArchTest
    val `only vcard package may access itself`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_VCARD_PACKAGE)
        .and().resideOutsideOfPackage(APPLICATION_PACKAGE)
        .should().accessClassesThat().resideInAPackage(INFRASTRUCTURE_VCARD_PACKAGE)

    @ArchTest
    val `only addressformatting package may access Google address library`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_ADDRESS_FORMATTING_PACKAGE)
        .should().accessClassesThat().resideInAPackage(EXT_ADDRESS_LIBRARY_PACKAGE)

    @ArchTest
    val `only addressformatting package may access itself`: ArchRule = noClasses()
        .that().resideOutsideOfPackage(INFRASTRUCTURE_ADDRESS_FORMATTING_PACKAGE)
        .and().resideOutsideOfPackage(APPLICATION_PACKAGE)
        .should().accessClassesThat().resideInAPackage(INFRASTRUCTURE_ADDRESS_FORMATTING_PACKAGE)

    private fun layers() = Architectures.layeredArchitecture().consideringAllDependencies()
        .layer(APPLICATION_LAYER).definedBy(APPLICATION_PACKAGE)
        .layer(VIEW_LAYER).definedBy(VIEW_PACKAGE)
        .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
        .layer(INFRASTRUCTURE_LAYER).definedBy(INFRASTRUCTURE_PACKAGE)
}

class DoNotIncludeKotlinTests : ImportOption {
    override fun includes(location: Location): Boolean {
        return !location.contains("Test/") && !location.contains("test/")
    }
}
