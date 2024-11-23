package gaku.original.myapplication.interfaces

import android.util.Log
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.Expense
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ExpenseDBControl {
    //追加
    fun addExpense(Expense:Expense){
        DummyExpenses.expensesList.add(Expense)
    }

    //更新
    fun updateExpense(Expense:Expense){
        val index=DummyExpenses.expensesList.indexOfFirst { it.id==Expense.id }
        if(index!=-1){
            DummyExpenses.expensesList[index]=Expense
        }
        else{
            Log.d("DataBaseManage.kt","Unable to find the id from the list")
        }
    }

    //削除
    fun deleteExpense(id:String?){
        if(id==null)
        {
            Log.d("deleteExpense","id is null")
        }
        else
        {
            DummyExpenses.expensesList.removeIf { it.id == id }
        }
    }

    //idを生成
    //yyyymmddMMHHSS-(番号:同時に生成されてしまったとき)
    fun generateId(num:Int=0):String{
        val currentDateTime=LocalDateTime.now()
        val datetimeFormat= DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val datetimeStr=currentDateTime.format(datetimeFormat)
        val id=datetimeStr+"-"+"${num}"
        return id
    }

}