package gaku.original.myapplication.data.repository.mailboxExtraction

import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailProvider
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType
import kotlinx.coroutines.tasks.await

class MailboxExtractionRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : MailboxExtractionRepository {
    private val reference = realtimeDbReference.emailTemplateSettingsReference

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        TODO("Not implemented")
    }

    override suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType {
        TODO("Not yet implemented")
    }

    override suspend fun saveMailTypeSetting(type: EmailTemplateType) {
        val firebaseType: EmailTemplateTypeFirebase = when (type) {
            is EmailTemplateType.AmazonItem -> {
                EmailTemplateTypeFirebase.AmazonItem(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.RakutenPay -> {
                EmailTemplateTypeFirebase.RakutenPay(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.AmazonKindle -> {
                EmailTemplateTypeFirebase.AmazonKindle(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.AmazonSubscribe -> {
                EmailTemplateTypeFirebase.AmazonSubscribe(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.ShikokuElectricPower -> {
                EmailTemplateTypeFirebase.ShikokuElectricPower(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.Udemy -> {
                EmailTemplateTypeFirebase.Udemy(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.RakutenCardETC -> {
                EmailTemplateTypeFirebase.RakutenCardETC(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }
        }
        val node = reference.child(firebaseType.nodeName)
        node.setValue(firebaseType).await()
    }

}

/**
 * Intentionally separate the data class used for Firestore and the one used for this App.
 * It's because by defining Firestore-specific data class, I can use toObject()..
 */
sealed interface EmailTemplateTypeFirebase {
    /* all parameters should simple type *//* To use doc.toObject, all properties should be nullable. */
    val enabled: Boolean?
    val emailProvider: String?

    fun toDomain(): EmailTemplateType

    val nodeName: String

    fun emailProviderToDomain(): EmailProvider? {
        if (emailProvider == null) {/* This is normal */
            return null
        } else {
            val provider = EmailProvider.fromString(emailProvider!!)
            if (provider == null) {
                throw Exception("Unable to convert to EmailProvider. Some bad parameter is in Firestore as EmailProvider. value = ${emailProvider}")
            } else {
                return provider
            }
        }
    }

    data class RakutenPay(
        override val enabled: Boolean? = null, override val emailProvider: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName get() = "rakuten_pay"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.RakutenPay(
                enabled = enabled ?: false
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class AmazonKindle(
        override val enabled: Boolean? = null,
        override val emailProvider: String? = null,
        val categoryId: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName: String get() = "amazon_kindle"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.AmazonKindle(
                enabled = enabled ?: false, categoryId = categoryId
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class AmazonSubscribe(
        override val enabled: Boolean? = null,
        override val emailProvider: String? = null,
    ) : EmailTemplateTypeFirebase {
        override val nodeName get() = "amazon_subscribe"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.AmazonSubscribe(
                enabled = enabled ?: false
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class AmazonItem(
        override val enabled: Boolean? = null, override val emailProvider: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName get() = "amazon_item"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.AmazonItem(
                enabled = enabled ?: false
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean? = null,
        override val emailProvider: String? = null,
        val categoryId: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName get() = "shikoku_electric_power"
        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.ShikokuElectricPower(
                enabled = enabled ?: false, categoryId = categoryId
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class Udemy(
        override val enabled: Boolean? = null,
        override val emailProvider: String? = null,
        val categoryId: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName: String
            get() = "udemy"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.Udemy(
                enabled = enabled ?: false, categoryId = categoryId
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }

    data class RakutenCardETC(
        override val enabled: Boolean? = null,
        override val emailProvider: String? = null,
        val categoryId: String? = null
    ) : EmailTemplateTypeFirebase {
        override val nodeName: String
            get() = "rakuten_card_etc"

        override fun toDomain(): EmailTemplateType {
            val _emailProvider = EmailTemplateType.RakutenCardETC(
                enabled = enabled ?: false, categoryId = categoryId
            )
            val provider = emailProviderToDomain()
            if (provider == null) {
                return _emailProvider
            } else {
                return _emailProvider.copy(
                    emailProvider = provider
                )
            }
        }
    }
}