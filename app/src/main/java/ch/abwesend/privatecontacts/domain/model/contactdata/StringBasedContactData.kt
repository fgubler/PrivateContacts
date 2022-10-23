package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.domain.model.ModelStatus

interface StringBasedContactData : BaseGenericContactData<String> {
    /** Raw value */
    override val value: String
    /** Formatted for display */
    val formattedValue: String get() = value
    /** Formatted for automatic matching */
    val valueForMatching: String get() = value

    override val isEmpty: Boolean
        get() = value.isEmpty()

    override val displayValue: String
        get() = formattedValue

    override fun formatValueForSearch(): String = formatValueForSearch(value)

    companion object {
        fun formatValueForSearch(value: String): String = value
    }
}

/**
 * The generics are needed to make the functions return the dynamic type of "this"
 */
interface StringBasedContactDataGeneric<T : StringBasedContactDataGeneric<T>> : StringBasedContactData {
    fun changeValue(value: String): T
    override fun changeType(type: ContactDataType): T
    override fun overrideStatus(newStatus: ModelStatus): T
    override fun delete(): T
}
