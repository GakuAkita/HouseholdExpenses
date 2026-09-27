package gaku.original.myapplication.data.repository.mailboxExtraction

import com.google.firebase.database.DataSnapshot
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailProvider
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType
import kotlinx.coroutines.tasks.await

class MailboxExtractionRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : MailboxExtractionRepository {
    private val reference = realtimeDbReference.emailTemplateSettingsReference

    override suspend fun getAllMailTypeSetting(): List<EmailTemplateType> {
        return reference.get().await().children.mapNotNull { it.toMailTypeSetting() }
    }

    private suspend fun <T : EmailTemplateTypeDto> getSetting(
        default: T, clazz: Class<T>
    ): EmailTemplateType {
        val snapshot = reference.child(default.nodeName).get().await()
        return snapshot.getValue(clazz)?.toDomain() ?: default.toDomain()
    }

    override suspend fun getMailTypeSetting(type: EmailTemplateType): EmailTemplateType {/* I'm not sure this is the best way. *//* By constructing Unit tests, we might be able to confirm the data can be saved and loaded properly */

        val domain = when (type) {
            is EmailTemplateType.AmazonItem -> {
                getSetting(
                    EmailTemplateTypeDto.AmazonItem(),
                    EmailTemplateTypeDto.AmazonItem::class.java
                )
            }

            is EmailTemplateType.RakutenPay -> {
                getSetting(
                    EmailTemplateTypeDto.RakutenPay(),
                    EmailTemplateTypeDto.RakutenPay::class.java
                )
            }

            is EmailTemplateType.Udemy -> {
                getSetting(
                    EmailTemplateTypeDto.Udemy(), EmailTemplateTypeDto.Udemy::class.java
                )
            }

            is EmailTemplateType.RakutenCardETC -> {
                getSetting(
                    EmailTemplateTypeDto.RakutenCardETC(),
                    EmailTemplateTypeDto.RakutenCardETC::class.java
                )
            }

            is EmailTemplateType.AmazonSubscribe -> {
                getSetting(
                    EmailTemplateTypeDto.AmazonSubscribe(),
                    EmailTemplateTypeDto.AmazonSubscribe::class.java
                )
            }

            is EmailTemplateType.AmazonKindle -> {
                getSetting(
                    EmailTemplateTypeDto.AmazonKindle(),
                    EmailTemplateTypeDto.AmazonKindle::class.java
                )
            }

            is EmailTemplateType.ShikokuElectricPower -> {
                getSetting(
                    EmailTemplateTypeDto.ShikokuElectricPower(),
                    EmailTemplateTypeDto.ShikokuElectricPower::class.java
                )
            }
        }


        if (domain.javaClass != type.javaClass) {
            throw CodingErrorException(
                "expected ${type.javaClass.simpleName}, " + "but got ${domain.javaClass.simpleName}"
            )
        }

        return domain
    }

    override suspend fun saveMailTypeSetting(type: EmailTemplateType) {
        val dto: EmailTemplateTypeDto = when (type) {
            is EmailTemplateType.AmazonItem -> {
                EmailTemplateTypeDto.AmazonItem(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.RakutenPay -> {
                EmailTemplateTypeDto.RakutenPay(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.AmazonKindle -> {
                EmailTemplateTypeDto.AmazonKindle(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.AmazonSubscribe -> {
                EmailTemplateTypeDto.AmazonSubscribe(
                    enabled = type.enabled, emailProvider = type.emailProvider.name
                )
            }

            is EmailTemplateType.ShikokuElectricPower -> {
                EmailTemplateTypeDto.ShikokuElectricPower(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.Udemy -> {
                EmailTemplateTypeDto.Udemy(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }

            is EmailTemplateType.RakutenCardETC -> {
                EmailTemplateTypeDto.RakutenCardETC(
                    enabled = type.enabled,
                    emailProvider = type.emailProvider.name,
                    categoryId = type.categoryId
                )
            }
        }
        val node = reference.child(dto.nodeName)
        node.setValue(dto).await()
    }
}

/**
 * When new type is added, it should be added here.!!!!!!!!
 */
private fun DataSnapshot.toMailTypeSetting(): EmailTemplateType? {
    return when (key) {
        EmailTemplateTypeDto.AmazonItem().nodeName -> getValue(EmailTemplateTypeDto.AmazonItem::class.java)?.toDomain()

        EmailTemplateTypeDto.RakutenPay().nodeName -> getValue(EmailTemplateTypeDto.RakutenPay::class.java)?.toDomain()

        EmailTemplateTypeDto.RakutenCardETC().nodeName -> getValue(EmailTemplateTypeDto.RakutenCardETC::class.java)?.toDomain()

        EmailTemplateTypeDto.AmazonKindle().nodeName -> getValue(EmailTemplateTypeDto.AmazonKindle::class.java)?.toDomain()
        EmailTemplateTypeDto.AmazonSubscribe().nodeName -> getValue(EmailTemplateTypeDto.AmazonSubscribe::class.java)?.toDomain()

        EmailTemplateTypeDto.ShikokuElectricPower().nodeName -> getValue(
            EmailTemplateTypeDto.ShikokuElectricPower::class.java
        )?.toDomain()

        EmailTemplateTypeDto.Udemy().nodeName -> getValue(EmailTemplateTypeDto.Udemy::class.java)?.toDomain()

        else -> null
    }
}

/**
 * Intentionally separate the data class stored in the database and the one used for this App.
 * It's because by defining a storage-specific data class, I can use getValue().
 */
sealed interface EmailTemplateTypeDto {
    /* all parameters should simple type *//* To use getValue(), all properties should be nullable. */
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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
    ) : EmailTemplateTypeDto {
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