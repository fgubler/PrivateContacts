package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import com.alexstyl.contactstore.Label

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
        Label.RelationSister -> ContactDataType.RelationshipBrother
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
