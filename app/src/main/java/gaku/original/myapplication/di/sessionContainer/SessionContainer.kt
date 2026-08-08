package gaku.original.myapplication.di.sessionContainer

import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository

interface SessionContainer {
    val expenseRepository: ExpenseRepository

    val appTimeZoneRepository: AppTimeZoneRepository
}