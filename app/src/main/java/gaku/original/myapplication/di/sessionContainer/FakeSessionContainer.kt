package gaku.original.myapplication.di.sessionContainer

import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.FakeAppTimeZoneRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.FakeExpenseRepository

class FakeSessionContainer : SessionContainer {

    override val expenseRepository: ExpenseRepository = FakeExpenseRepository()

    override val appTimeZoneRepository: AppTimeZoneRepository = FakeAppTimeZoneRepository()
}