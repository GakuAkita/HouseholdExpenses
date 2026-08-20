package gaku.original.myapplication.data.repository.mailboxExtraction

import EmailProvider
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

interface MailboxExtractionRepository {
    suspend fun getIsConnected(provider:EmailProvider): Boolean

    suspend fun getAllMailTypeSetting(): List<EmailTemplateType>

    suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType

    suspend fun saveMailTypeSetting(type: EmailTemplateType)
}