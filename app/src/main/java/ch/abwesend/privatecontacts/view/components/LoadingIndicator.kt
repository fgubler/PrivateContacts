package ch.abwesend.privatecontacts.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicatorFullScreen(
    textAfterIndicator: (@Composable () -> String)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(200.dp)    // needs a fixed size vor alignment/arrangement
        )
        textAfterIndicator?.let { text ->
            Text(
                text = text(),
                modifier = Modifier.padding(top = 50.dp)
            )
        }
    }
}

@Preview
@Composable
fun Preview() {
    LoadingIndicatorFullScreen { "Loading data for you" }
}
