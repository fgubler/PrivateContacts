package ch.abwesend.privatecontacts.domain.model.contactdata

interface GenericContactData<TValue, TThis : GenericContactData<TValue, TThis>> : ContactData {
    val value: TValue

    override fun changeType(type: ContactDataType): TThis
    override fun delete(): TThis

    fun changeValue(value: TValue): TThis

    /** string-representation for technical purposes (e.g. serialization) */
    fun serializedValue(): String
}
