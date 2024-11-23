package gaku.original.myapplication

import android.content.Context
import androidx.room.Room
import gaku.original.myapplication.data.ExpenseDao
import gaku.original.myapplication.data.ExpenseDataBase
import gaku.original.myapplication.data.ExpenseRepository

object Graph {
    lateinit var database: ExpenseDataBase

    val expenseRepository by lazy {/*必要になったときだけロードする*/
        ExpenseRepository(ExpenseDao=database.ExpenseDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, ExpenseDataBase::class.java, "Expenselist.db").build()
    }
}