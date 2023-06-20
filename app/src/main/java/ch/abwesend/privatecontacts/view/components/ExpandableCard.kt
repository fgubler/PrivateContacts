package ch.abwesend.privatecontacts.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R

/**
 * The [header] is always visible and contains the icon to expand/minimize the body.
 * The [content] is only show if [expanded] is true
 */
@Composable
fun ExpandableCard(
    expanded: Boolean,
    onToggleExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier.padding(all = 5.dp)) {
        Surface(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
            Column {
                ExpandableCardHeader(customHeader = header, expanded = expanded) {
                    onToggleExpanded(!expanded)
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun ExpandableCardHeader(
    customHeader: @Composable () -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val expandIcon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
    ) {
        Surface(modifier = Modifier.weight(1.0f)) {
            customHeader()
        }
        Icon(
            imageVector = expandIcon,
            contentDescription = stringResource(id = R.string.expand),
            modifier = Modifier
                .width(40.dp)
                .padding(start = 20.dp)
        )
    }
}
