package ch.abwesend.privatecontacts.domain.model.contactgroup

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.WithModelStatus

interface IContactGroup : WithModelStatus {
    val id: IContactGroupId
    val notes: String

    fun changeName(newName: String): IContactGroup
    fun changeNotes(newNotes: String): IContactGroup
    fun changeStatus(newStatus: ModelStatus): IContactGroup
}

data class ContactGroup(
    override val id: IContactGroupId,
    override val notes: String,
    override val modelStatus: ModelStatus,
) : IContactGroup {
    override fun changeName(newName: String) = copy(id = id.changeName(newName))
    override fun changeNotes(newNotes: String) = copy(notes = newNotes)
    override fun changeStatus(newStatus: ModelStatus) = copy(modelStatus = newStatus)

    companion object {
        fun new(name: String) = ContactGroup(
            id = ContactGroupId(name = name, groupNo = null),
            notes = "",
            modelStatus = ModelStatus.NEW
        )
    }
}
