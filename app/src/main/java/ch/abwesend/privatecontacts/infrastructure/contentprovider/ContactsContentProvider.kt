/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import ch.abwesend.privatecontacts.application.KoinInitializer
import ch.abwesend.privatecontacts.application.PrivateContactsApplication
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.logging.LoggerFactory
import kotlinx.coroutines.runBlocking

/**
 * This content-provider may be started outside of [PrivateContactsApplication]
 * => cannot use Dependency-Injection (and therefore the logger) before Koin is initialized.
 */
class ContactsContentProvider : ContentProvider() {
    private val contactLoadService: ContactLoadService by injectAnywhere()
    private val safeLogger: ILogger? by lazy {
        context?.let { LoggerFactory(it).createDefault(javaClass) }
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "contacts", CONTACTS)
        addURI(AUTHORITY, "contacts/#", CONTACT_ID)
        addURI(AUTHORITY, "phone/*", PHONE_LOOKUP)
    }

    companion object {
        // Define the authority for this content provider
        const val AUTHORITY = "ch.abwesend.privatecontacts.provider"

        // Define the content URIs
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/contacts")
        val PHONE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/phone")

        // URI matcher paths
        const val CONTACTS = 1
        const val CONTACT_ID = 2
        const val PHONE_LOOKUP = 3

        // Define column names for the contacts cursor
        const val COLUMN_ID = ContactsContract.Contacts._ID
        const val COLUMN_DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_MIDDLE_NAME = "middle_name"
        const val COLUMN_NAME_PREFIX = "name_prefix"
        const val COLUMN_NAME_SUFFIX = "name_suffix"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER

        // Define column names for the phone cursor
        const val COLUMN_PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        const val COLUMN_PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER
        const val COLUMN_PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE
        const val COLUMN_PHONE_LABEL = ContactsContract.CommonDataKinds.Phone.LABEL

        // Define the projection (columns) for the contacts cursor
        val DEFAULT_PROJECTION = arrayOf(
            COLUMN_ID,
            COLUMN_DISPLAY_NAME,
            COLUMN_FIRST_NAME,
            COLUMN_LAST_NAME,
            COLUMN_NICKNAME,
            COLUMN_MIDDLE_NAME,
            COLUMN_NAME_PREFIX,
            COLUMN_NAME_SUFFIX,
            COLUMN_NOTES,
            COLUMN_HAS_PHONE_NUMBER
        )

        // Define the projection for the phone cursor
        val PHONE_PROJECTION = arrayOf(
            COLUMN_PHONE_CONTACT_ID,
            COLUMN_PHONE_NUMBER,
            COLUMN_PHONE_TYPE,
            COLUMN_PHONE_LABEL
        )
    }

    override fun onCreate(): Boolean {
        safeLogger?.info("Creating ContactsContentProvider")
        context?.let { KoinInitializer.initializeKoin(context = it) }
        logger.info("Koin initialized for ContactsContentProvider")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        logger.info("Querying ContactsContentProvider with URI: $uri")
        return when (uriMatcher.match(uri)) {
            CONTACTS -> queryContacts(projection)
            CONTACT_ID -> {
                val contactId = uri.lastPathSegment
                queryContactById(contactId, projection)
            }
            PHONE_LOOKUP -> {
                val phoneNumber = uri.lastPathSegment
                queryContactByPhoneNumber(phoneNumber, projection)
            }
            else -> null
        }
    }

    private fun queryContacts(projection: Array<out String>?): Cursor {
        // Use the provided projection or fall back to the default projection
        val actualProjection = projection ?: DEFAULT_PROJECTION

        // Load secret contacts
        val contacts = runBlocking {
            contactLoadService.loadFullContactsByType(ContactType.SECRET)
        }

        // Create a cursor with the requested columns
        val cursor = MatrixCursor(actualProjection)

        // Add each contact as a row in the cursor
        for (contact in contacts) {
            addContactToCursor(cursor, contact, actualProjection)
        }

        return cursor
    }

    private fun queryContactById(contactId: String?, projection: Array<out String>?): Cursor? {
        if (contactId == null) return null

        // Use the provided projection or fall back to the default projection
        val actualProjection = projection ?: DEFAULT_PROJECTION

        // Load secret contacts
        val contacts = runBlocking {
            contactLoadService.loadFullContactsByType(ContactType.SECRET)
        }

        // Find the contact with the specified ID
        val contact = contacts.find { getContactIdString(it.id) == contactId } ?: return null

        // Create a cursor with the requested columns
        val cursor = MatrixCursor(actualProjection)

        // Add the contact as a row in the cursor
        addContactToCursor(cursor, contact, actualProjection)

        return cursor
    }

    private fun queryContactByPhoneNumber(phoneNumber: String?, projection: Array<out String>?): Cursor? {
        if (phoneNumber == null) return null

        // Use the provided projection or fall back to the phone projection
        val actualProjection = projection ?: PHONE_PROJECTION

        // Load secret contacts
        val contacts = runBlocking {
            contactLoadService.loadFullContactsByType(ContactType.SECRET)
        }

        // Create a cursor with the requested columns
        val cursor = MatrixCursor(actualProjection)

        // Find contacts with the specified phone number
        for (contact in contacts) {
            val phoneData = contact.contactDataSet.filter { 
                it.category == ContactDataCategory.PHONE_NUMBER && 
                it.displayValue.replace(Regex("[^0-9+]"), "") == phoneNumber.replace(Regex("[^0-9+]"), "")
            }

            if (phoneData.isNotEmpty()) {
                addPhoneDataToCursor(cursor, contact, phoneData, actualProjection)
            }
        }

        return cursor
    }

    private fun addContactToCursor(cursor: MatrixCursor, contact: IContact, projection: Array<out String>) {
        val rowBuilder = cursor.newRow()

        // Map contact properties to cursor columns
        for (column in projection) {
            when (column) {
                COLUMN_ID -> rowBuilder.add(getContactIdString(contact.id))
                COLUMN_DISPLAY_NAME -> rowBuilder.add(contact.displayName)
                COLUMN_FIRST_NAME -> rowBuilder.add(contact.firstName)
                COLUMN_LAST_NAME -> rowBuilder.add(contact.lastName)
                COLUMN_NICKNAME -> rowBuilder.add(contact.nickname)
                COLUMN_MIDDLE_NAME -> rowBuilder.add(contact.middleName)
                COLUMN_NAME_PREFIX -> rowBuilder.add(contact.namePrefix)
                COLUMN_NAME_SUFFIX -> rowBuilder.add(contact.nameSuffix)
                COLUMN_NOTES -> rowBuilder.add(contact.notes)
                COLUMN_HAS_PHONE_NUMBER -> rowBuilder.add(hasPhoneNumber(contact))
                // Add more columns as needed
            }
        }
    }

    private fun hasPhoneNumber(contact: IContact): Int {
        return if (contact.contactDataSet.any { it.category == ContactDataCategory.PHONE_NUMBER }) 1 else 0
    }

    private fun getContactIdString(contactId: ContactId): String {
        return when (contactId) {
            is IContactIdInternal -> contactId.uuid.toString()
            is IContactIdExternal -> contactId.contactNo.toString()
        }
    }

    private fun addPhoneDataToCursor(
        cursor: MatrixCursor, 
        contact: IContact, 
        phoneData: List<ContactData>, 
        projection: Array<out String>
    ) {
        for (data in phoneData) {
            val rowBuilder = cursor.newRow()

            // Map phone data properties to cursor columns
            for (column in projection) {
                when (column) {
                    COLUMN_PHONE_CONTACT_ID -> rowBuilder.add(getContactIdString(contact.id))
                    COLUMN_PHONE_NUMBER -> rowBuilder.add(data.displayValue)
                    COLUMN_PHONE_TYPE -> rowBuilder.add(mapContactDataTypeToPhoneType(data.type))
                    COLUMN_PHONE_LABEL -> rowBuilder.add(data.type.key.name)
                    // Add more columns as needed
                }
            }
        }
    }

    private fun mapContactDataTypeToPhoneType(type: ContactDataType): Int {
        return when (type.key) {
            ContactDataType.Key.MOBILE -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            ContactDataType.Key.PERSONAL -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
            ContactDataType.Key.BUSINESS -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
            ContactDataType.Key.MOBILE_BUSINESS -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE
            ContactDataType.Key.MAIN -> ContactsContract.CommonDataKinds.Phone.TYPE_MAIN
            ContactDataType.Key.CUSTOM -> ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM
            else -> ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
        }
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            CONTACTS -> "vnd.android.cursor.dir/vnd.ch.abwesend.privatecontacts.contact"
            CONTACT_ID -> "vnd.android.cursor.item/vnd.ch.abwesend.privatecontacts.contact"
            PHONE_LOOKUP -> "vnd.android.cursor.dir/vnd.ch.abwesend.privatecontacts.phone"
            else -> "vnd.android.cursor.dir/vnd.ch.abwesend.privatecontacts.contact"
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // TODO implement inserting
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // TODO implement deleting
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // TODO implement update
        return 0
    }
}
