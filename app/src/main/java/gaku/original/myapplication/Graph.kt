package gaku.original.myapplication

import android.content.Context
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import gaku.original.myapplication.data.ExpenseDatabase
import gaku.original.myapplication.data.ExpenseRepository

object Graph {
    lateinit var database:ExpenseDatabase

    //The idea behind this is that you don't load everything at the start when you open up your application
    val expenseRepository by lazy {
        ExpenseRepository(
            expenseDao = database.expenseDao()
        )
    }

    fun provide(context: Context){
        database = databaseBuilder(context,ExpenseDatabase::class.java,"expenselist.db").build()
    }
}