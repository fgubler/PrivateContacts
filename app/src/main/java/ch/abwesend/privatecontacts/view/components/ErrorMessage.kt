/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig

@Composable
fun FullScreenError(
    @StringRes errorMessage: Int,
    buttonConfig: ButtonConfig? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(id = errorMessage),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        buttonConfig?.let { config ->
            Button(onClick = config.onClick) {
                Text(text = stringResource(id = config.label))
                config.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = config.label),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
    }
}
