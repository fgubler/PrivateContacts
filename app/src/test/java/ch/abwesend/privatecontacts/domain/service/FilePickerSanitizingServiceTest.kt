/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.os.Environment
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class FilePickerSanitizingServiceTest : TestBase() {
    private val rootDirectory = "/my/root/directory/"

    @InjectMockKs
    private lateinit var underTest: TestFilePickerSanitizingService

    override fun setup() {
        super.setup()
        mockkStatic(Environment::class)
        every { Environment.getExternalStorageDirectory().absolutePath } returns rootDirectory
    }

    @Test
    fun `should return the file if it exists`() {
        val path = "${rootDirectory}random/path/validFile.txt"
        underTest.fileExists = true

        val result = underTest.getValidFileOrNull(path)

        assertThat(result).isNotNull
        val resultPath = result!!.absolutePath
            .replace(oldValue = "\\", newValue = "/") // only necessary for Windows
        assertThat(resultPath).endsWith(path)
    }

    @Test
    fun `should return null if the file does not exist`() {
        val path = "${rootDirectory}random/path/fileIsNotThere.txt"
        underTest.fileExists = false

        val result = underTest.getValidFileOrNull(path)

        assertThat(result).isNull()
    }

    @Test
    fun `should leave path starting with root directory alone`() {
        val path = "${rootDirectory}random/path/file.txt"

        val result = underTest.sanitize(path)

        assertThat(result).isEqualTo(path)
    }

    @Test
    fun `should remove any prefix before the root directory`() {
        val path = "randomStuff/whatever : x ${rootDirectory}random/path/file.txt"

        val result = underTest.sanitize(path)

        assertThat(result).isNotEqualTo(path)
        assertThat(result).startsWith(rootDirectory)
    }

    @Test
    fun `should always remove file-picker prefix`() {
        val realPath = "/random/path/file.txt"
        val path = "$FILE_PICKER_URI_PATH_PREFIX$realPath"

        val result = underTest.sanitize(path)

        assertThat(result).isNotEqualTo(path)
        assertThat(result).startsWith(realPath)
    }
}

internal class TestFilePickerSanitizingService : FilePickerSanitizingService() {
    var fileExists: Boolean = false
    override fun fileExists(file: File): Boolean = fileExists

    fun sanitize(filePath: String): String = sanitizeFilePath(filePath)
}
