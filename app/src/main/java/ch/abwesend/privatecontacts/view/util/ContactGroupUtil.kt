/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.util.Constants

fun IContact.joinFilteredGroupsToString(): String =
    contactGroups
        .asSequence()
        .filter { it.modelStatus != ModelStatus.DELETED }
        .map { it.id.name }
        .filter { it.isNotEmpty() }
        .sorted()
        .joinToString(separator = Constants.linebreak)