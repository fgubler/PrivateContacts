/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view

import android.net.Uri
import android.os.Environment
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.FILE_PICKER_URI_PATH_PREFIX
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.getFilePathForDisplay
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class FilePathViewExtensionsTest : TestBase() {
    private val rootDirectory = "/my/root/directory/"

    @MockK
    private lateinit var uri: Uri

    override fun setup() {
        super.setup()
        mockkStatic(Environment::class)
        every { Environment.getExternalStorageDirectory().absolutePath } returns rootDirectory
    }

    @Test
    fun `should return empty for null Uri`() {
        val nullUri: Uri? = null

        val result = nullUri.getFilePathForDisplay()

        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty for null path`() {
        every { uri.path } returns ""

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEmpty()
    }

    @Test
    fun `should just return the path if it does not fit the expected scheme`() {
        val path = "Something else completely"
        every { uri.path } returns path

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEqualTo(path)
    }

    @Test
    fun `should trim the path`() {
        val subPath = "Something else completely"
        val path = " $subPath "
        every { uri.path } returns path

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEqualTo(subPath)
    }

    @Test
    fun `should just remove the root directory if it is the beginning`() {
        val subPath = "random/path/file.txt"
        val path = "$rootDirectory$subPath"
        every { uri.path } returns path

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEqualTo(subPath)
    }

    @Test
    fun `should remove any prefix before the root directory`() {
        val subPath = "random/path/file.txt"
        val path = "randomStuff/whatever : x $rootDirectory$subPath"
        every { uri.path } returns path

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEqualTo(subPath)
    }

    @Test
    fun `should always remove file-picker prefix`() {
        val subPath = "/random/path/file.txt"
        val path = "$FILE_PICKER_URI_PATH_PREFIX$subPath"
        every { uri.path } returns path

        val result = uri.getFilePathForDisplay()

        assertThat(result).isEqualTo(subPath)
    }
}
