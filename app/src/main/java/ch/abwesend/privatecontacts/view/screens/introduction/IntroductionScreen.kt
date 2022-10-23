package ch.abwesend.privatecontacts.view.screens.introduction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.text.SectionTitle
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.Introduction
import ch.abwesend.privatecontacts.view.screens.BaseScreen

object IntroductionScreen {
    @Composable
    fun Screen(screenContext: ScreenContext) {
        BaseScreen(screenContext = screenContext, selectedScreen = Introduction) { padding ->
            Column(modifier = Modifier.padding(padding).padding(10.dp)) {
                SectionTitle(titleRes = R.string.screen_introduction, addTopPadding = false)
                Text(text = stringResource(id = R.string.app_introduction_description))

                SectionTitle(titleRes = R.string.permissions)
                Text(text = stringResource(id = R.string.show_caller_information_text))
            }
        }
    }
}
