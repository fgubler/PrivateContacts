package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.filterShouldUpsert
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_UPDATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.ContactGroupDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toContactGroup
import ch.abwesend.privatecontacts.infrastructure.room.contactgroup.toEntity
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationDao
import ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation.ContactGroupRelationEntity
import kotlinx.coroutines.CancellationException

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

    override suspend fun loadAllContactGroups(ignoreEmptyGroups: Boolean): List<IContactGroup> =
        withDatabase { database ->
            val allGroups = database.contactGroupDao().getAll().map { it.toContactGroup() }.toList()
            if (ignoreEmptyGroups) {
                val nonEmptyGroupNames = database.contactGroupRelationDao().getGroupNamesWithContacts().toSet()
                allGroups.filter { it.id.name in nonEmptyGroupNames }
            } else allGroups
        }

    override suspend fun getContactIdsInGroups(groupNames: Collection<String>): Set<IContactIdInternal> {
        val relations = bulkLoadingOperation(groupNames) { database, groupNamesChunk ->
            database.contactGroupRelationDao().getRelationsForContactGroups(groupNamesChunk)
        }
        logger.debug("Found ${relations.size} contact group relations for ${groupNames.size} groups")
        return relations.map { ContactIdInternal(it.contactId) }.toSet()
    }

    override suspend fun loadNumberOfContactsPerGroup(): Map<String, Int> = withDatabase { database ->
        database.contactGroupRelationDao().getNumberOfContactsPerGroup()
            .associate { it.contactGroupName to it.numberOfContacts }
    }

    override suspend fun updateContactGroup(oldGroup: IContactGroup, newGroup: IContactGroup): ContactSaveResult =
        try {
            val oldName = oldGroup.id.name
            val newName = newGroup.id.name
            withDatabaseTransaction { database ->
                if (oldName == newName) {
                    database.contactGroupDao().update(newGroup.toEntity())
                } else {
                    // the name is the primary key: create the new group, move the relations, then delete the old one
                    database.contactGroupDao().insert(newGroup.toEntity())
                    database.contactGroupRelationDao().renameContactGroup(oldGroupName = oldName, newGroupName = newName)
                    database.contactGroupDao().delete(oldGroup.toEntity())
                }
            }
            logger.debug("Updated contact group '$oldName' (new name '$newName')")
            ContactSaveResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to update contact group '${oldGroup.id.name}'", e)
            ContactSaveResult.Failure(UNABLE_TO_UPDATE_CONTACT_GROUP)
        }

    override suspend fun deleteContactGroup(contactGroup: IContactGroup): ContactDeleteResult =
        try {
            withDatabase { database ->
                // the relations are deleted by cascade-delete
                database.contactGroupDao().delete(contactGroup.toEntity())
            }
            logger.debug("Deleted contact group '${contactGroup.id.name}'")
            ContactDeleteResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to delete contact group '${contactGroup.id.name}'", e)
            ContactDeleteResult.Failure(UNABLE_TO_DELETE_CONTACT_GROUP)
        }

    private suspend fun ContactGroupDao.createMissingContactGroups(contactGroups: Collection<IContactGroup>) {
        logger.debug("Creating missing contact groups")
        val uniqueGroups = contactGroups
            .filterShouldUpsert()
            .distinctBy { it.id }
            .map { it.toEntity() }
        insertMissing(uniqueGroups)
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
