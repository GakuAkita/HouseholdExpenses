package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

interface mainboxExtractionRepository {

    suspend fun getMailTypeSetting(type: EmailTemplateType) {

    }

    suspend fun saveMailTypeSetting(type: EmailTemplateType) {

    }
}