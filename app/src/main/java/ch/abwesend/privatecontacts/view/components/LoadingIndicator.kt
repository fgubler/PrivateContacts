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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicatorFullScreen(@StringRes textAfterIndicator: Int? = null) {
    LoadingIndicatorFullWidth(
        textAfterIndicator = textAfterIndicator,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoadingIndicatorFullWidth(
    modifier: Modifier = Modifier,
    loadingIndicatorSize: Dp = 150.dp,
    @StringRes textAfterIndicator: Int? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(loadingIndicatorSize) // needs a fixed size vor alignment/arrangement
        )
        textAfterIndicator?.let { stringRes ->
            Text(
                text = stringResource(stringRes),
                modifier = Modifier.padding(top = 50.dp)
            )
        }
    }
}
