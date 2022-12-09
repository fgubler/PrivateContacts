package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationEntity

class ContactGroupRepository : RepositoryBase(), IContactGroupRepository {
    suspend fun getContactGroups(contactId: IContactIdInternal): List<ContactGroup> = withDatabase { database ->
        val contactGroupRelations = database.contactGroupRelationDao().getRelationsForContact(contactId.uuid)
        val contactGroupNames = contactGroupRelations.map { it.contactGroupName }
        val contactGroupEntities = database.contactGroupDao().getGroups(contactGroupNames)
        logger.debug("Found ${contactGroupEntities.size} contact groups for contact $contactId")
        contactGroupEntities.map { it.toContactGroup() }
    }

    suspend fun storeContactGroups(
        contactId: IContactIdInternal,
        contactGroups: List<IContactGroup>
    ): Unit = withDatabase { database ->
        database.contactGroupDao().createMissingContactGroups(contactGroups)
        database.contactGroupRelationDao().updateContactGroupRelations(contactId, contactGroups)
    }

    override suspend fun createMissingContactGroups(contactGroups: List<IContactGroup>) {
        bulkOperation(contactGroups) { database, groupsChunk ->
            database.contactGroupDao().createMissingContactGroups(groupsChunk)
        }
    }

    private suspend fun ContactGroupDao.createMissingContactGroups(contactGroups: Collection<IContactGroup>) {
        logger.debug("Creating missing contact groups")
        val uniqueGroups = contactGroups
            .filter { it.modelStatus in listOf(NEW, CHANGED) }
            .distinctBy { it.id }
            .map { it.toEntity() }
        upsertAll(uniqueGroups)
    }

    private suspend fun ContactGroupRelationDao.updateContactGroupRelations(
        contactId: IContactIdInternal,
        contactGroups: List<IContactGroup>
    ) {
        logger.debug("Updating contact group relations")
        val newRelations = contactGroups.map { contactGroup ->
            ContactGroupRelationEntity(contactId = contactId.uuid, contactGroupName = contactGroup.id.name)
        }
        deleteRelationsForContact(contactId.uuid)
        insertAll(newRelations)
    }
}
