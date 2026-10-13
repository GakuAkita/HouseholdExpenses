package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

interface MailboxExtractionRepository {
    suspend fun getAllMailTypeSetting(): List<EmailTemplateType>

    suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType

    suspend fun saveMailTypeSetting(type: EmailTemplateType)

    /** When the mail type was last extracted, in Unix milliseconds. Null before the first run. */
    suspend fun getLastExecMillis(type: EmailTemplateType): Long?
}