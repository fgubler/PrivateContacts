package ch.abwesend.privatecontacts.view.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun getSafeAreaPadding(): PaddingValues = WindowInsets.safeContent.asPaddingValues()

@Composable
fun getSafeAreaPaddingTop(safeAreaPadding: PaddingValues = getSafeAreaPadding()): Dp =
    safeAreaPadding.calculateTopPadding()

@Composable
fun getSafeAreaPaddingStart(safeAreaPadding: PaddingValues = getSafeAreaPadding()): Dp =
    safeAreaPadding.calculateStartPadding(LocalLayoutDirection.current)

@Composable
fun getSafeAreaPaddingEnd(safeAreaPadding: PaddingValues = getSafeAreaPadding()): Dp =
    safeAreaPadding.calculateEndPadding(LocalLayoutDirection.current)

@Composable
fun getSafeAreaPaddingBottom(safeAreaPadding: PaddingValues = getSafeAreaPadding()): Dp =
    safeAreaPadding.calculateBottomPadding()

@Composable
fun Modifier.setTopBarSafeAreaPadding(
    invertTopAndBottomBars: Boolean,
    safeArea: PaddingValues = getSafeAreaPadding()
): Modifier {
    return background(MaterialTheme.colors.primarySurface)
        .padding(
        top = if (invertTopAndBottomBars) 0.dp else getSafeAreaPaddingTop(safeArea),
        start = getSafeAreaPaddingStart(safeArea),
        end = getSafeAreaPaddingEnd(safeArea),
        bottom = if (invertTopAndBottomBars) getSafeAreaPaddingBottom(safeArea) else 0.dp
    )
}