package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

class FakeMailboxExtractionRepository: MailboxExtractionRepository {
    override suspend fun getIsGmailToken(): Boolean {
        return true
    }

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        TODO("Not yet implemented")
    }

    override suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType {
        TODO("Not yet implemented")
    }

    override suspend fun saveMailTypeSetting(type: EmailTemplateType) {
        TODO("Not yet implemented")
    }
}