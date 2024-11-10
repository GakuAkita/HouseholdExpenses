package gaku.original.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities=[ExpenseClass::class],
    version = 1,
    exportSchema = false
)
abstract  class ExpenseDataBase:RoomDatabase() {
    abstract fun ExpenseDao():ExpenseDao
}