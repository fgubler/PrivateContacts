/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgroup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactGroupEntity(
    @PrimaryKey val name: String,
    val notes: String,
)
