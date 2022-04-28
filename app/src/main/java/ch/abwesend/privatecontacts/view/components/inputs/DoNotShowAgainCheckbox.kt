package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R

@Composable
fun DoNotShowAgainCheckbox(checked: Boolean, onCheckChanged: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 10.dp)
            .offset(x = (-12).dp)
            .clickable { onCheckChanged(!checked) }
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckChanged)
        Text(
            text = stringResource(id = R.string.do_not_show_again),
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}
