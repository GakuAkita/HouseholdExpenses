package gaku.original.myapplication.data.repository.mailboxExtraction

import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

class MailboxExtractionRepositoryRealtimeDb(
    private val appUser: AppUser,
    private val firebaseRealtimeDb: FirebaseDatabase
) : MailboxExtractionRepository {
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