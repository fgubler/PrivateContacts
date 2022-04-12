package ch.abwesend.privatecontacts.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.shapes.Circle

@Composable
fun BulletPointList(elements: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(elements) { item ->
            BulletPointListItem {
                Text(text = item)
            }
        }
    }
}

@Composable
fun BulletPointListItem(content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Circle(size = 5.dp, color = MaterialTheme.colors.onBackground)
        Surface(modifier = Modifier.padding(start = 10.dp)) {
            content()
        }
    }
}
