package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.filterShouldUpsert
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationEntity
import kotlinx.coroutines.flow.map

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

    override suspend fun createMissingContactGroups(contactGroups: List<IContactGroup>): ContactSaveResult =
        try {
            bulkOperation(contactGroups) { database, groupsChunk ->
                database.contactGroupDao().createMissingContactGroups(groupsChunk)
            }
            ContactSaveResult.Success
        } catch (e: Exception) {
            logger.error("Failed to create contact groups as batch-operation", e)
            ContactSaveResult.Failure(UNABLE_TO_CREATE_CONTACT_GROUP)
        }

    override suspend fun loadAllContactGroups(): ResourceFlow<List<IContactGroup>> =
        withDatabase { database ->
            database.contactGroupDao().getAllAsFlow()
                .map { entities -> entities.map { it.toContactGroup() } }
                .toResourceFlow()
        }

    private suspend fun ContactGroupDao.createMissingContactGroups(contactGroups: Collection<IContactGroup>) {
        logger.debug("Creating missing contact groups")
        val uniqueGroups = contactGroups
            .filterShouldUpsert()
            .distinctBy { it.id }
            .map { it.toEntity() }
        upsertAll(uniqueGroups)
    }

    private suspend fun ContactGroupRelationDao.updateContactGroupRelations(
        contactId: IContactIdInternal,
        contactGroups: List<IContactGroup>
    ) {
        logger.debug("Updating contact group relations")

        if (contactGroups.all { it.modelStatus == ModelStatus.UNCHANGED }) {
            logger.debug("No contact group relations to update")
            return
        }

        val newRelations = contactGroups
            .filterNot { group -> group.modelStatus == ModelStatus.DELETED }
            .map { contactGroup ->
                ContactGroupRelationEntity(contactId = contactId.uuid, contactGroupName = contactGroup.id.name)
            }
        deleteRelationsForContact(contactId.uuid)
        insertAll(newRelations)
    }
}
