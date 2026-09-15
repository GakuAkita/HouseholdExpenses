package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType

class MailboxExtractionRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : MailboxExtractionRepository {
    private val reference = realtimeDbReference.emailTemplateSettingsReference

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        TODO("Not yet implemented")
    }

    override suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType {
    }

    override suspend fun saveMailTypeSetting(type: EmailTemplateType) {
        TODO("Not yet implemented")
    }

}


fun EmailTemplateType.getNodeName(): String =
    when (this) {
        is EmailTemplateType.AmazonKindle -> "amazon_kindle"
        is EmailTemplateType.AmazonItem -> "amazon_item"
        is EmailTemplateType.AmazonSubscribe -> "amazon_subscribe"
        is EmailTemplateType.RakutenPay -> "rakuten_pay"
        is EmailTemplateType.ShikokuElectricPower -> "shikoku_electric_power"
        is EmailTemplateType.Udemy -> "udemy"
        is EmailTemplateType.RakutenCardETC -> "rakuten_card_etc"
    }
