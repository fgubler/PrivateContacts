/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.SettingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TestSettings(
    override val initialized: Flow<Boolean> = flow { emit(true) },
    override var isDarkTheme: Boolean = false,
    override var orderByFirstName: Boolean = true,
    override var showInitialAppInfoDialog: Boolean = true,
    override var defaultContactType: ContactType = ContactType.PRIVATE,
    override var requestIncomingCallPermissions: Boolean = true,
    override var showIncomingCallsOnLockScreen: Boolean = true,
    override var useBroadcastReceiverForIncomingCalls: Boolean = true,
) : SettingsProvider
