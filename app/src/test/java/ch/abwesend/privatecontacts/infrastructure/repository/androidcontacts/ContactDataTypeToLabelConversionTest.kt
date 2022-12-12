/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.ADDRESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.COMPANY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EMAIL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EVENT_DATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.PHONE_NUMBER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.RELATIONSHIP
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.WEBSITE
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toContactDataType
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toLabel
import ch.abwesend.privatecontacts.testutil.TestBase
import com.alexstyl.contactstore.Label
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactDataTypeToLabelConversionTest : TestBase() {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `converting a label there and back should avoid errors in normal cases`(compareToOriginal: Boolean) {
        val labelsWithCategory = listOf(
            Label.PhoneNumberMobile to PHONE_NUMBER,
            Label.PhoneNumberCompanyMain to PHONE_NUMBER,
            Label.PhoneNumberWorkMobile to PHONE_NUMBER,
            Label.Main to PHONE_NUMBER,
            Label.Main to EMAIL,
            Label.Main to ADDRESS,
            Label.Main to COMPANY,
            Label.LocationHome to ADDRESS,
            Label.LocationWork to ADDRESS,
            Label.WebsiteHomePage to WEBSITE,
            Label.DateBirthday to EVENT_DATE,
            Label.DateAnniversary to EVENT_DATE,
            Label.Other to PHONE_NUMBER,
            Label.Other to EMAIL,
            Label.Other to WEBSITE,
            Label.Other to RELATIONSHIP,
            Label.Other to COMPANY,
            Label.Custom(label = "someCustomLabelText") to PHONE_NUMBER,
            Label.Custom(label = "someCustomLabelText") to EMAIL,
            Label.Custom(label = "someCustomLabelText") to WEBSITE,
            Label.Custom(label = "someCustomLabelText") to RELATIONSHIP,
            Label.Custom(label = "someCustomLabelText") to COMPANY,
            Label.RelationBrother to RELATIONSHIP,
            Label.RelationSister to RELATIONSHIP,
            Label.RelationChild to RELATIONSHIP,
            Label.RelationParent to RELATIONSHIP,
            Label.RelationPartner to RELATIONSHIP,
            Label.RelationRelative to RELATIONSHIP,
            Label.RelationFriend to RELATIONSHIP,
            Label.RelationManager to RELATIONSHIP,
            // exception: all the others which are not supported in a 1:1 mapping (see test below)
        )
        val labels = labelsWithCategory.map { it.first }

        val contactDataTypes = labelsWithCategory.map { (label, category) ->
            Triple(label, label.toContactDataType(), category)
        }
        val resultingLabels = contactDataTypes.map { (label, type, category) ->
            type.toLabel(category, originalLabel = label.takeIf { compareToOriginal })
        }

        assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            assertThat(resultingLabels[index]).isEqualTo(labels[index])
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `labels without 1-1 mapping should only be overwritten if changed`(labelUnchanged: Boolean) {
        val labels = listOf(
            LabelConversionErrorData(Label.Main, WEBSITE, Label.WebsiteHomePage),
            LabelConversionErrorData(Label.RelationFather, RELATIONSHIP, Label.RelationParent),
            LabelConversionErrorData(Label.RelationMother, RELATIONSHIP, Label.RelationParent),
            LabelConversionErrorData(Label.RelationDomesticPartner, RELATIONSHIP, Label.RelationPartner),
            LabelConversionErrorData(Label.RelationSpouse, RELATIONSHIP, Label.RelationPartner),
            LabelConversionErrorData(Label.RelationReferredBy, RELATIONSHIP, Label.Other),
        )

        val contactDataTypes = labels.map { labelData ->
            labelData to labelData.originalLabel.toContactDataType()
        }
        val resultingLabels = contactDataTypes.map { (labelData, type) ->
            type.toLabel(labelData.category, originalLabel = labelData.originalLabel.takeIf { labelUnchanged })
        }

        assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            if (labelUnchanged) {
                assertThat(resultingLabels[index]).isEqualTo(labels[index].originalLabel)
            } else {
                assertThat(resultingLabels[index]).isEqualTo(labels[index].resultingLabel)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `unsupported labels mapped to OTHER should only be overwritten if changed`(labelUnchanged: Boolean) {
        val labels = listOf(
            LabelConversionErrorData(Label.PhoneNumberPager, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberCar, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberFaxWork, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberFaxHome, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberCallback, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberIsdn, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberOtherFax, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberRadio, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberTelex, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberTtyTdd, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberWorkPager, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberAssistant, PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberMms, PHONE_NUMBER, Label.Other),

            LabelConversionErrorData(Label.WebsiteBlog, WEBSITE, Label.Other),
            LabelConversionErrorData(Label.WebsiteFtp, WEBSITE, Label.Other),
            LabelConversionErrorData(Label.WebsiteProfile, WEBSITE, Label.Other),
        )

        val contactDataTypes = labels.map { labelData ->
            labelData to labelData.originalLabel.toContactDataType()
        }
        val resultingLabels = contactDataTypes.map { (labelData, type) ->
            type.toLabel(labelData.category, originalLabel = labelData.originalLabel.takeIf { labelUnchanged })
        }

        assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            if (labelUnchanged) {
                assertThat(resultingLabels[index]).isEqualTo(labels[index].originalLabel)
            } else {
                assertThat(resultingLabels[index]).isEqualTo(labels[index].resultingLabel)
            }
        }
    }
}

private data class LabelConversionErrorData(
    val originalLabel: Label,
    val category: ContactDataCategory,
    val resultingLabel: Label,
)
