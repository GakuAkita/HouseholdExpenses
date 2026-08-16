package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

interface MailboxExtractionRepository {
    suspend fun getIsGmailToken(): Boolean

    suspend fun getAllMailTypeSetting(): List<EmailTemplateType>

    suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType

    suspend fun saveMailTypeSetting(type: EmailTemplateType)
}