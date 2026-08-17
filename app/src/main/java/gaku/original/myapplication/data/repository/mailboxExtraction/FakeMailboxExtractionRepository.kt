package gaku.original.myapplication.data.repository.mailboxExtraction

import EmailProvider
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

class FakeMailboxExtractionRepository: MailboxExtractionRepository {
    override suspend fun getIsGmailToken(): Boolean {
        return true
    }

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        return listOf(
            EmailTemplateType.RakutenPay(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.AmazonKindle(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.AmazonItem(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.AmazonSubscribe(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.Udemy(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.RakutenCardETC(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            )
        )
    }

    override suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType {
        TODO("Not yet implemented")
    }

    override suspend fun saveMailTypeSetting(type: EmailTemplateType) {
        TODO("Not yet implemented")
    }
}