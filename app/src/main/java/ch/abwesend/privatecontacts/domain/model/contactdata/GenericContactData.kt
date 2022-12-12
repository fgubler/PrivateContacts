package ch.abwesend.privatecontacts.domain.model.contactdata

sealed interface BaseGenericContactData<TValue> : ContactData {
    override val value: TValue
}

interface GenericContactData<TValue, TThis : GenericContactData<TValue, TThis>> : BaseGenericContactData<TValue> {
    override fun changeType(type: ContactDataType): TThis
    override fun delete(): TThis

    fun changeValue(value: TValue): TThis

    /** string-representation for technical purposes (e.g. serialization) */
    fun serializedValue(): String
}
