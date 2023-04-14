/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

interface DropDownOption<out T> {
    val value: T
    @Composable
    fun getLabel(): String
}

data class DynamicStringDropDownOption<T>(
    private val labelProvider: @Composable () -> String,
    override val value: T,
) : DropDownOption<T> {
    @Composable
    override fun getLabel() = labelProvider()
}

data class StringDropDownOption<T>(private val label: String, override val value: T) : DropDownOption<T> {
    @Composable
    override fun getLabel() = label
}

data class ResDropDownOption<T>(@StringRes private val labelRes: Int, override val value: T) : DropDownOption<T> {
    @Composable
    override fun getLabel() = stringResource(id = labelRes)
}
