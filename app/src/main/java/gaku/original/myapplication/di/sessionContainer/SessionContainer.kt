package gaku.original.myapplication.di.sessionContainer

import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository

interface SessionContainer {
    val expenseRepository: ExpenseRepository

    val categoryRepository: CategoryRepository

    val appTimeZoneRepository: AppTimeZoneRepository

    val repeatAddRepository: RepeatAddRepository

    val mailboxExtractionRepository: MailboxExtractionRepository

    val emailConnectionRepository: EmailConnectionRepository

    val amazonSubscribeItemRepository: AmazonSubscribeItemRepository
}