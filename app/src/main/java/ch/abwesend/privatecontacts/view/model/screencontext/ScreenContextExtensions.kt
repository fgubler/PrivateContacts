/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.screencontext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Beware: is only useful for the compiler: as - currently - there is only one implementation of ScreenContext,
 * the dynamic-type will always satisfy the type-check.
 *
 * @return true if navigation over the side-drawer is allowed, false if only back-navigation is allowed
 */
@ExperimentalContracts
fun isGenericNavigationAllowed(screenContext: IScreenContextBase): Boolean {
    contract {
        returns(true) implies (screenContext is IScreenContextWithGenericNavigation)
    }
    return screenContext is IScreenContextWithGenericNavigation
}
