package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.WEBSITE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import com.alexstyl.contactstore.Label

fun ContactDataType.toLabel(category: ContactDataCategory, originalLabel: Label?): Label {
    // try avoiding translation-losses in the labels
    return if (originalLabel?.toContactDataType() == this) originalLabel
    else toLabel(category)
}

/**
 * Beware: when changing this method, you also need to change [toLabel]
 */
fun Label.toContactDataType(): ContactDataType =
    when (this) {
        Label.PhoneNumberMobile -> ContactDataType.Mobile
        Label.PhoneNumberCompanyMain -> ContactDataType.Business
        Label.PhoneNumberWorkMobile -> ContactDataType.MobileBusiness
        Label.Main -> ContactDataType.Main
        Label.LocationHome -> ContactDataType.Personal
        Label.LocationWork -> ContactDataType.Business
        Label.DateBirthday -> ContactDataType.Birthday
        Label.DateAnniversary -> ContactDataType.Anniversary
        Label.WebsiteHomePage -> ContactDataType.Main
        Label.Other -> ContactDataType.Other

        Label.RelationBrother -> ContactDataType.RelationshipBrother
        Label.RelationSister -> ContactDataType.RelationshipSister
        Label.RelationChild -> ContactDataType.RelationshipChild
        Label.RelationFather -> ContactDataType.RelationshipParent
        Label.RelationMother -> ContactDataType.RelationshipParent
        Label.RelationParent -> ContactDataType.RelationshipParent
        Label.RelationPartner -> ContactDataType.RelationshipPartner
        Label.RelationDomesticPartner -> ContactDataType.RelationshipPartner
        Label.RelationRelative -> ContactDataType.RelationshipRelative
        Label.RelationFriend -> ContactDataType.RelationshipFriend
        Label.RelationManager -> ContactDataType.RelationshipWork
        Label.RelationReferredBy -> ContactDataType.Other
        Label.RelationSpouse -> ContactDataType.RelationshipPartner

        is Label.Custom -> ContactDataType.CustomValue(customValue = label)
        else -> ContactDataType.Other
    }

/**
 * Beware: when changing this method, you also need to change [toContactDataType]
 */
private fun ContactDataType.toLabel(category: ContactDataCategory): Label =
    when (this) {
        ContactDataType.Anniversary -> Label.DateAnniversary
        ContactDataType.Birthday -> Label.DateBirthday
        ContactDataType.Business -> {
            if (category == ContactDataCategory.ADDRESS) Label.LocationWork else Label.PhoneNumberCompanyMain
        }
        ContactDataType.Custom -> Label.Custom("custom").also {
            logger.warning("This should never have happened: cannot have data-type 'Custom'")
        }
        is ContactDataType.CustomValue -> Label.Custom(customValue)
        ContactDataType.Main -> if (category == WEBSITE) Label.WebsiteHomePage else Label.Main
        ContactDataType.Mobile -> Label.PhoneNumberMobile
        ContactDataType.MobileBusiness -> Label.PhoneNumberWorkMobile
        ContactDataType.Other -> Label.Other
        ContactDataType.Personal -> Label.LocationHome
        ContactDataType.RelationshipChild -> Label.RelationChild
        ContactDataType.RelationshipFriend -> Label.RelationFriend
        ContactDataType.RelationshipParent -> Label.RelationParent
        ContactDataType.RelationshipPartner -> Label.RelationPartner
        ContactDataType.RelationshipRelative -> Label.RelationRelative
        ContactDataType.RelationshipBrother -> Label.RelationBrother
        ContactDataType.RelationshipSister -> Label.RelationSister
        ContactDataType.RelationshipSibling -> Label.RelationBrother // well who knows
        ContactDataType.RelationshipWork -> Label.RelationManager
    }
