package ch.abwesend.privatecontacts.domain.model.contactdata

data class PhoneNumber(
    val value: String,
    val type: PhoneNumberType,
    val isMainNumber: Boolean = false,
)

sealed interface PhoneNumberType {
    val value: String

    object Mobile : PhoneNumberType {
        override val value: String = "MOBILE"
    }

    object Private : PhoneNumberType {
        override val value: String = "PRIVATE"
    }

    object Business : PhoneNumberType {
        override val value: String = "BUSINESS"
    }

    class Custom(val customValue: String) : PhoneNumberType {
        override val value: String = "CUSTOM"
    }
}
