/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.buttons

import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.view.routing.AppRouter

@Composable
fun MenuBackButton(router: AppRouter) {
    BackIconButton { router.navigateUp() }
}
