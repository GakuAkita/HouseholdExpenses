package gaku.original.myapplication.data.repository.mailboxExtraction

import EmailProvider
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType
import kotlinx.coroutines.delay

class FakeMailboxExtractionRepository: MailboxExtractionRepository {
    override suspend fun getIsConnected(provider: EmailProvider): Boolean {
        return true
    }

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        return listOf(
            EmailTemplateType.RakutenPay(
                enabled = true,
                emailProvider = EmailProvider.GMAIL
            ),
            EmailTemplateType.AmazonKindle(
                enabled = false,
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
        delay(5000)
    }
}