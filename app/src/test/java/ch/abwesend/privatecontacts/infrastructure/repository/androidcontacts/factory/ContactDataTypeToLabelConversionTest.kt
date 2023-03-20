/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.testutil.TestBase
import com.alexstyl.contactstore.Label
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions
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
            Label.PhoneNumberMobile to ContactDataCategory.PHONE_NUMBER,
            Label.PhoneNumberCompanyMain to ContactDataCategory.PHONE_NUMBER,
            Label.PhoneNumberWorkMobile to ContactDataCategory.PHONE_NUMBER,
            Label.Main to ContactDataCategory.PHONE_NUMBER,
            Label.Main to ContactDataCategory.EMAIL,
            Label.Main to ContactDataCategory.ADDRESS,
            Label.Main to ContactDataCategory.COMPANY,
            Label.LocationHome to ContactDataCategory.ADDRESS,
            Label.LocationWork to ContactDataCategory.ADDRESS,
            Label.WebsiteHomePage to ContactDataCategory.WEBSITE,
            Label.DateBirthday to ContactDataCategory.EVENT_DATE,
            Label.DateAnniversary to ContactDataCategory.EVENT_DATE,
            Label.Other to ContactDataCategory.PHONE_NUMBER,
            Label.Other to ContactDataCategory.EMAIL,
            Label.Other to ContactDataCategory.WEBSITE,
            Label.Other to ContactDataCategory.RELATIONSHIP,
            Label.Other to ContactDataCategory.COMPANY,
            Label.Custom(label = "someCustomLabelText") to ContactDataCategory.PHONE_NUMBER,
            Label.Custom(label = "someCustomLabelText") to ContactDataCategory.EMAIL,
            Label.Custom(label = "someCustomLabelText") to ContactDataCategory.WEBSITE,
            Label.Custom(label = "someCustomLabelText") to ContactDataCategory.RELATIONSHIP,
            Label.Custom(label = "someCustomLabelText") to ContactDataCategory.COMPANY,
            Label.RelationBrother to ContactDataCategory.RELATIONSHIP,
            Label.RelationSister to ContactDataCategory.RELATIONSHIP,
            Label.RelationChild to ContactDataCategory.RELATIONSHIP,
            Label.RelationParent to ContactDataCategory.RELATIONSHIP,
            Label.RelationPartner to ContactDataCategory.RELATIONSHIP,
            Label.RelationRelative to ContactDataCategory.RELATIONSHIP,
            Label.RelationFriend to ContactDataCategory.RELATIONSHIP,
            Label.RelationManager to ContactDataCategory.RELATIONSHIP,
            // exception: all the others which are not supported in a 1:1 mapping (see test below)
        )
        val labels = labelsWithCategory.map { it.first }

        val contactDataTypes = labelsWithCategory.map { (label, category) ->
            Triple(label, label.toContactDataType(), category)
        }
        val resultingLabels = contactDataTypes.map { (label, type, category) ->
            type.toLabel(category, originalLabel = label.takeIf { compareToOriginal })
        }

        Assertions.assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            Assertions.assertThat(resultingLabels[index]).isEqualTo(labels[index])
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `labels without 1-1 mapping should only be overwritten if changed`(labelUnchanged: Boolean) {
        val labels = listOf(
            LabelConversionErrorData(Label.Main, ContactDataCategory.WEBSITE, Label.WebsiteHomePage),
            LabelConversionErrorData(Label.RelationFather, ContactDataCategory.RELATIONSHIP, Label.RelationParent),
            LabelConversionErrorData(Label.RelationMother, ContactDataCategory.RELATIONSHIP, Label.RelationParent),
            LabelConversionErrorData(
                Label.RelationDomesticPartner,
                ContactDataCategory.RELATIONSHIP,
                Label.RelationPartner
            ),
            LabelConversionErrorData(Label.RelationSpouse, ContactDataCategory.RELATIONSHIP, Label.RelationPartner),
            LabelConversionErrorData(Label.RelationReferredBy, ContactDataCategory.RELATIONSHIP, Label.Other),
        )

        val contactDataTypes = labels.map { labelData ->
            labelData to labelData.originalLabel.toContactDataType()
        }
        val resultingLabels = contactDataTypes.map { (labelData, type) ->
            type.toLabel(labelData.category, originalLabel = labelData.originalLabel.takeIf { labelUnchanged })
        }

        Assertions.assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            if (labelUnchanged) {
                Assertions.assertThat(resultingLabels[index]).isEqualTo(labels[index].originalLabel)
            } else {
                Assertions.assertThat(resultingLabels[index]).isEqualTo(labels[index].resultingLabel)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `unsupported labels mapped to OTHER should only be overwritten if changed`(labelUnchanged: Boolean) {
        val labels = listOf(
            LabelConversionErrorData(Label.PhoneNumberPager, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberCar, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberFaxWork, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberFaxHome, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberCallback, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberIsdn, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberOtherFax, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberRadio, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberTelex, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberTtyTdd, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberWorkPager, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberAssistant, ContactDataCategory.PHONE_NUMBER, Label.Other),
            LabelConversionErrorData(Label.PhoneNumberMms, ContactDataCategory.PHONE_NUMBER, Label.Other),

            LabelConversionErrorData(Label.WebsiteBlog, ContactDataCategory.WEBSITE, Label.Other),
            LabelConversionErrorData(Label.WebsiteFtp, ContactDataCategory.WEBSITE, Label.Other),
            LabelConversionErrorData(Label.WebsiteProfile, ContactDataCategory.WEBSITE, Label.Other),
        )

        val contactDataTypes = labels.map { labelData ->
            labelData to labelData.originalLabel.toContactDataType()
        }
        val resultingLabels = contactDataTypes.map { (labelData, type) ->
            type.toLabel(labelData.category, originalLabel = labelData.originalLabel.takeIf { labelUnchanged })
        }

        Assertions.assertThat(resultingLabels).hasSameSizeAs(labels)
        resultingLabels.indices.forEach { index ->
            if (labelUnchanged) {
                Assertions.assertThat(resultingLabels[index]).isEqualTo(labels[index].originalLabel)
            } else {
                Assertions.assertThat(resultingLabels[index]).isEqualTo(labels[index].resultingLabel)
            }
        }
    }
}

private data class LabelConversionErrorData(
    val originalLabel: Label,
    val category: ContactDataCategory,
    val resultingLabel: Label,
)
