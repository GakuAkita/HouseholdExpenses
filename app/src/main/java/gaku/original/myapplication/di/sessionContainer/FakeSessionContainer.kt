package gaku.original.myapplication.di.sessionContainer

import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.FakeAppTimeZoneRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.category.FakeCategoryRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.emailConnect.FakeEmailConnectionRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.FakeExpenseRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.FakeMailboxExtractionRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.data.repository.repeatAdd.FakeRepeatAddRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository

open class FakeSessionContainer(
    override val expenseRepository: ExpenseRepository = FakeExpenseRepository(),
    override val categoryRepository: CategoryRepository = FakeCategoryRepository(),
    override val appTimeZoneRepository: AppTimeZoneRepository = FakeAppTimeZoneRepository(),
    override val repeatAddRepository: RepeatAddRepository = FakeRepeatAddRepository(),
    override val mailboxExtractionRepository: MailboxExtractionRepository =
        FakeMailboxExtractionRepository(),
    override val emailConnectionRepository: EmailConnectionRepository = FakeEmailConnectionRepository()
) : SessionContainer {
}