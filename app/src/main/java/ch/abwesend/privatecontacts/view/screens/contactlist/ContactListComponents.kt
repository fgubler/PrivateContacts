/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.theme.primaryOnLight
import ch.abwesend.privatecontacts.view.theme.selectedElement
import ch.abwesend.privatecontacts.view.util.color
import kotlinx.coroutines.launch

private const val EASTER_EGG_LOVE = "love"
private val alphabet = 'A'..'Z'

@ExperimentalFoundationApi
@Composable
fun ContactList(
    contacts: List<IContactBase>,
    selectedContacts: Set<ContactId>,
    scrollingState: LazyListState,
    alphabeticScrollbarWidth: Dp,
    showTypeIcons: Boolean,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    if (contacts.isEmpty()) NoResults()
    else ListOfContacts(
        contacts = contacts,
        selectedContacts = selectedContacts,
        scrollingState = scrollingState,
        alphabeticScrollbarWidth = alphabeticScrollbarWidth,
        showTypeIcons = showTypeIcons,
        onContactClicked = onContactClicked,
        onContactLongClicked = onContactLongClicked,
    )
}

@Composable
private fun NoResults() {
    FullScreenError(errorMessage = R.string.no_contacts_found)
}

@ExperimentalFoundationApi
@Composable
private fun ListOfContacts(
    contacts: List<IContactBase>,
    selectedContacts: Set<ContactId>,
    scrollingState: LazyListState,
    alphabeticScrollbarWidth: Dp,
    showTypeIcons: Boolean,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    val contactsWithSubtitle = contacts.withFirstLetterSubtitles()
    val contactList = contactsWithSubtitle + (null to null)

    Row {
        LazyColumn(
            state = scrollingState,
            modifier = Modifier
                .fillMaxHeight()
                .padding(10.dp)
                .weight(1f)
        ) {
            items(
                items = contactList,
                contentType = { (subtitle, _) -> if (subtitle.isNullOrEmpty()) "Contact" else "Contact with Subtitle" },
                itemContent = { (subtitle, contact) ->
                    if (contact == null) {
                        Spacer(modifier = Modifier.height(50.dp)) // padding at the end for the action-button
                    } else {
                        val selected = selectedContacts.contains(contact.id)
                        subtitle?.let { Subtitle(text = it) }
                        Contact(
                            contact = contact,
                            selected = selected,
                            showTypeIcon = showTypeIcons,
                            onClicked = onContactClicked,
                            onLongClicked = onContactLongClicked,
                        )
                    }
                }
            )
        }
        AlphabeticScrollbar(contacts, scrollingState, alphabeticScrollbarWidth)
    }
}

@Composable private fun AlphabeticScrollbar(
    contacts: List<IContactBase>,
    scrollingState: LazyListState,
    scrollbarWidth: Dp,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollbarBackground = primaryOnLight.copy(alpha = 0.3f)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(scrollbarWidth)
            .background(scrollbarBackground)
    ) {
        alphabet.forEach { letter ->
            Text(
                text = letter.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable {
                        val index = contacts.indexOfFirst { it.displayName.startsWith(letter, ignoreCase = true) }
                        if (index >= 0) {
                            coroutineScope.launch {
                                scrollingState.animateScrollToItem(index)
                            }
                        }
                    }
            )
        }
    }
}

private fun List<IContactBase>.withFirstLetterSubtitles(): List<Pair<String?, IContactBase>> =
    mapIndexed { index, contact ->
        val previousContact = if (index > 0) get(index - 1) else null
        val firstLetter = contact.displayName.firstOrNull()
        val hasSameFirstLetter = firstLetter == previousContact?.displayName?.firstOrNull()
        val subtitle = if (hasSameFirstLetter) null else firstLetter?.uppercase()
        subtitle to contact
    }

@Composable
private fun Subtitle(text: String) {
    Text(text = text, color = AppColors.greyText)
}

/** Beware: Need to draw the row even for null, otherwise, loading new pages does not work properly */
@ExperimentalFoundationApi
@Composable
private fun Contact(
    contact: IContactBase,
    selected: Boolean,
    showTypeIcon: Boolean,
    onClicked: (IContactBase) -> Unit,
    onLongClicked: (IContactBase) -> Unit,
) {
    val background =
        if (selected) MaterialTheme.colors.selectedElement
        else MaterialTheme.colors.background

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .combinedClickable(
                onClick = { onClicked(contact) },
                onLongClick = { onLongClicked(contact) }
            )
    ) {
        val name = contact.displayName
        val contactIcon = when {
            selected -> Icons.Default.TaskAlt
            name.lowercase().contains(EASTER_EGG_LOVE) -> Icons.Filled.Favorite
            else -> Icons.Filled.AccountCircle
        }

        Icon(
            imageVector = contactIcon,
            contentDescription = name,
            modifier = if (showTypeIcon) Modifier.padding(start = 10.dp)
            else Modifier.padding(start = 10.dp, end = 20.dp)
        )
        if (showTypeIcon) {
            Icon(
                imageVector = contact.type.icon,
                contentDescription = stringResource(id = contact.type.label),
                modifier = Modifier.padding(start = 5.dp, end = 20.dp),
                tint = contact.type.color,
            )
        }
        Text(text = name)
    }
    Spacer(modifier = Modifier.height(6.dp))
}
