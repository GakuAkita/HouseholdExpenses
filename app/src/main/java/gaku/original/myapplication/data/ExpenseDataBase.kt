package gaku.original.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities=[Expense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract  class ExpenseDataBase:RoomDatabase() {
    abstract fun ExpenseDao():ExpenseDao
}