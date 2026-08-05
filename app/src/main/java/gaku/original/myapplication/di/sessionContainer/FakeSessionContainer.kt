package gaku.original.myapplication.di.sessionContainer

import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.FakeExpenseRepository

class FakeSessionContainer : SessionContainer {

    override val expenseRepository: ExpenseRepository = FakeExpenseRepository()
}