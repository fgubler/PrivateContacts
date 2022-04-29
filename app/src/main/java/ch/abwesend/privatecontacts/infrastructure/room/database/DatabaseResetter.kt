/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.infrastructure.room.database.IDatabaseFactory.Companion.DATABASE_NAME
import java.io.File

class DatabaseResetter {
    fun resetDatabase(context: Context): Boolean {
        return (deleteSqLiteDatabase(context) || deleteDatabaseFromFileSystem(context)).also {
            logger.debug("database deletion ${successText(it)}")
        }
    }

    private fun deleteSqLiteDatabase(context: Context): Boolean = try {
        context.deleteDatabase(DATABASE_NAME).also {
            "SqLite database deletion ${successText(it)}"
        }
    } catch (t: Throwable) {
        logger.error("failed to delete database with error", t)
        false
    }

    private fun deleteDatabaseFromFileSystem(context: Context): Boolean = try {
        val databasesDir = File(context.databaseDirectoryPath)
        File(databasesDir, "$DATABASE_NAME.db").delete().also {
            logger.debug("database file deletion ${successText(it)}")
        }
    } catch (t: Throwable) {
        logger.error("failed to delete database from filesystem", t)
        false
    }

    private fun successText(success: Boolean): String = if (success) "successful" else "failed"

    private val Context.databaseDirectoryPath: String
        get() = applicationInfo.dataDir.toString() + "/databases"
}
