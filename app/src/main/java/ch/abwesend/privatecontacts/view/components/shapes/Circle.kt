package ch.abwesend.privatecontacts.view.components.shapes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun Circle(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val sizePx = size.toPx()
        drawCircle(
            color = color,
            radius = sizePx / 2f,
        )
    }
}
