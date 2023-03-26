/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class ContactChangeError(@StringRes val label: Int) {
    UNABLE_TO_DELETE_CONTACT(R.string.unable_to_delete_contact),
    UNABLE_TO_RESOLVE_CONTACT(R.string.unable_to_resolve_contact),
    UNABLE_TO_SAVE_CONTACT(R.string.unable_to_save_contact),
    UNABLE_TO_RESOLVE_EXISTING_CONTACT(R.string.unable_to_resolve_existing_contact),

    UNABLE_TO_CREATE_CONTACT_WITH_NEW_TYPE(R.string.type_change_create_new_contact_error),
    UNABLE_TO_DELETE_CONTACT_WITH_OLD_TYPE(R.string.type_change_delete_old_contact_error),
    UNABLE_TO_CREATE_CONTACT_GROUP(R.string.unable_to_save_contact),

    NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS(R.string.not_yet_implemented_for_external_error),
    NOT_YET_IMPLEMENTED_FOR_INTERNAL_CONTACTS(R.string.not_yet_implemented_for_internal_error),

    UNKNOWN_ERROR(R.string.unknown_error),
}
