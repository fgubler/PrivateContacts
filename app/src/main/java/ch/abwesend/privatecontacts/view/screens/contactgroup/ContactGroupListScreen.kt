/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.abwesend.privatecontacts.view.model.screencontext.IContactGroupListScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.BaseScreen

object ContactGroupListScreen {
    @Composable
    fun Screen(screenContext: IContactGroupListScreenContext) {
        BaseScreen(screenContext = screenContext, selectedScreen = Screen.ContactGroups) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // TODO content follows in the next step
            }
        }
    }
}
