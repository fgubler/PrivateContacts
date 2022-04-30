/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.infrastructure.room.database.IDatabaseFactory.Companion.DATABASE_NAME

interface IDatabaseFactory<out TDatabase> {
    fun createDatabase(context: Context): TDatabase

    companion object {
        const val DATABASE_NAME = "private_contacts_database"
    }
}

class DatabaseFactory : IDatabaseFactory<AppDatabase> {
    override fun createDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            // .fallbackToDestructiveMigration() // DO NOT USE ONCE PRODUCTIVE!
            .addMigrations(*DatabaseMigrations.allMigrations)
            .build()
    }
}
