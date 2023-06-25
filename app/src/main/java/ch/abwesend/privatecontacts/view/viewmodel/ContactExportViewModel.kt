/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import kotlinx.coroutines.launch

class ContactExportViewModel : ViewModel() {
    private val _sourceType: MutableState<ContactType> = mutableStateOf(ContactType.SECRET)
    val sourceType: State<ContactType> = _sourceType

    fun selectSourceType(contactType: ContactType) {
        _sourceType.value = contactType
    }

    fun exportContacts(targetFile: Uri?, sourceType: ContactType) {
        if (targetFile == null) {
            logger.warning("Trying to export to vcf file but no file is selected")
            return
        }
        logger.debugLocally("Exporting to vcf file from $sourceType")

        viewModelScope.launch {
            // TODO implement
        }
    }
}
