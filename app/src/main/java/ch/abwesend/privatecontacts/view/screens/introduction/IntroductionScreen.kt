package ch.abwesend.privatecontacts.view.screens.introduction

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.Introduction
import ch.abwesend.privatecontacts.view.screens.BaseScreen

object IntroductionScreen {
    @Composable
    fun Screen(screenContext: ScreenContext) {
        BaseScreen(screenContext = screenContext, selectedScreen = Introduction) {
            Column(modifier = Modifier.padding(10.dp)) {
                SectionTitle(titleRes = R.string.app_name)
                Text(text = stringResource(id = R.string.app_introduction_description))
            }
        }
    }

    @Composable
    private fun SectionTitle(@StringRes titleRes: Int) {
        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.h5,
        )
    }
}
