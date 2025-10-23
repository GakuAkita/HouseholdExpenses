package gaku.original.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import gaku.original.myapplication.data.local.dao.CategoryDao
import gaku.original.myapplication.data.local.entity.CategoryEntity

/**
 * Roomデータベースのメインクラス
 * アプリのローカルデータベースを定義
 */
@Database(
    entities = [CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "household_expenses_db"
    }
}

