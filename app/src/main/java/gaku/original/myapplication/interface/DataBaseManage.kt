package gaku.original.myapplication.`interface`

import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.ExpenseClass
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ExpenseDBControl {
    //追加
    fun addExpense(Expense:ExpenseClass){
        DummyExpenses.expensesList.add(Expense)
    }

    //更新
    fun updateExpense(Expense:ExpenseClass){
        for(i in DummyExpenses.expensesList.indices){
            if(DummyExpenses.expensesList[i].id==Expense.id){
                DummyExpenses.expensesList[i]=Expense
            }
        }
    }

    //削除
    fun deleteExpense(id:String){
        for(i in DummyExpenses.expensesList.indices){
            if(DummyExpenses.expensesList[i].id==id){
                DummyExpenses.expensesList.removeAt(i)
            }
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