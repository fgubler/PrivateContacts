/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.sync

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.model.screencontext.ISettingsScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.SyncConflicts
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalContracts
object SyncConflictsScreen {
    @Composable
    fun Screen(screenContext: ISettingsScreenContext) {
        val viewModel = screenContext.settingsViewModel
        val conflicts by viewModel.syncConflicts.collectAsStateWithLifecycle()

        BaseScreen(screenContext = screenContext, selectedScreen = SyncConflicts) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(id = R.string.sync_conflicts_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (conflicts.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.sync_conflicts_empty),
                        fontStyle = FontStyle.Italic,
                    )
                } else {
                    conflicts.forEach { conflict ->
                        ConflictCard(
                            conflict = conflict,
                            onDeleteCopy = { viewModel.deleteConflictCopy(conflict) },
                            onMarkResolved = { viewModel.markConflictResolved(conflict.syncId) },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ConflictCard(
        conflict: SyncConflictUiModel,
        onDeleteCopy: () -> Unit,
        onMarkResolved: () -> Unit,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = conflict.originalName ?: stringResource(id = R.string.sync_conflict_original_label),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = conflict.copyName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDeleteCopy) {
                        Text(text = stringResource(id = R.string.sync_conflict_delete_copy))
                    }
                    TextButton(onClick = onMarkResolved) {
                        Text(text = stringResource(id = R.string.sync_conflict_resolve))
                    }
                }
            }
        }
    }
}
