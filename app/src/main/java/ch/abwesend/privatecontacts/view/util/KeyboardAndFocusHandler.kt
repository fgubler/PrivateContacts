package ch.abwesend.privatecontacts.view.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController

@ExperimentalComposeUiApi
class KeyboardAndFocusHandler(
    private val keyboardController: SoftwareKeyboardController?,
    private val focusManager: FocusManager,
) : FocusManager by focusManager {
    fun closeKeyboardAndClearFocus() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }
}
