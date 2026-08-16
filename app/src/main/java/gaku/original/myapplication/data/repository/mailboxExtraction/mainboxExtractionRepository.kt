package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.data.dataClass.EmailTemplateType

interface mainboxExtractionRepository {

    suspend fun getMailTypeSetting(type: EmailTemplateType) {

    }

    suspend fun saveMailTypeSetting(type: EmailTemplateType) {

    }
}