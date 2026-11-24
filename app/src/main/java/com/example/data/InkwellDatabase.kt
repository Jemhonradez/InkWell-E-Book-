package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StoryEntity::class, ChapterEntity::class, CommentEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class InkwellDatabase : RoomDatabase() {
    abstract fun inkwellDao(): InkwellDao

    companion object {
        @Volatile
        private var INSTANCE: InkwellDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `email` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `handle` TEXT NOT NULL,
                        `bio` TEXT NOT NULL,
                        `passwordSalt` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)"
                )
            }
        }

        fun getInstance(context: Context): InkwellDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    InkwellDatabase::class.java,
                    "inkwell_stories.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
