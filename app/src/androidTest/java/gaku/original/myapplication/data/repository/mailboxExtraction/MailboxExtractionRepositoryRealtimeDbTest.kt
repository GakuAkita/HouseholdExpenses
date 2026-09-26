package gaku.original.myapplication.data.repository.mailboxExtraction

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MailboxExtractionRepositoryRealtimeDbTest {

    private lateinit var reference: RealtimeDbUserReference
    private lateinit var repository: MailboxExtractionRepositoryRealtimeDb

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.realtimeDbReference(FirebaseTestEnvironment.newTestUser())
        repository = MailboxExtractionRepositoryRealtimeDb(realtimeDbReference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun getAllMailTypeSetting_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getAllMailTypeSetting().isEmpty())
    }

    @Test
    fun getMailTypeSetting_returnsDefaultWhenNotSaved() = runBlocking<Unit> {
        defaultTypes.forEach { type ->
            assertEquals(type, repository.getMailTypeSetting(type))
        }
    }

    @Test
    fun saveMailTypeSetting_canBeLoadedForEveryType() = runBlocking<Unit> {
        savedTypes.forEach { repository.saveMailTypeSetting(it) }

        savedTypes.forEach { type ->
            /* The argument only selects the type. The saved values should be returned. */
            val default = defaultTypes.single { it.javaClass == type.javaClass }
            assertEquals(type, repository.getMailTypeSetting(default))
        }
    }

    @Test
    fun saveMailTypeSetting_overwritesPreviousSetting() = runBlocking<Unit> {
        repository.saveMailTypeSetting(EmailTemplateType.Udemy(enabled = true, categoryId = "category1"))

        repository.saveMailTypeSetting(EmailTemplateType.Udemy(enabled = false, categoryId = null))

        assertEquals(
            EmailTemplateType.Udemy(enabled = false, categoryId = null),
            repository.getMailTypeSetting(EmailTemplateType.Udemy())
        )
    }

    @Test
    fun getAllMailTypeSetting_returnsEverySavedType() = runBlocking<Unit> {
        savedTypes.forEach { repository.saveMailTypeSetting(it) }

        val settings = repository.getAllMailTypeSetting()

        assertEquals(savedTypes.toSet(), settings.toSet())
        assertEquals(savedTypes.size, settings.size)
    }

    @Test
    fun getAllMailTypeSetting_ignoresUnknownNodes() = runBlocking<Unit> {
        val rakutenPay = EmailTemplateType.RakutenPay(enabled = true)
        repository.saveMailTypeSetting(rakutenPay)
        reference.emailTemplateSettingsReference.child("unknown_template")
            .setValue(mapOf("enabled" to true)).await()

        assertEquals(listOf(rakutenPay), repository.getAllMailTypeSetting())
    }

    companion object {
        /* When a new EmailTemplateType is added, add it to both lists. */
        private val defaultTypes = listOf(
            EmailTemplateType.AmazonItem(),
            EmailTemplateType.RakutenPay(),
            EmailTemplateType.Udemy(),
            EmailTemplateType.RakutenCardETC(),
            EmailTemplateType.AmazonSubscribe(),
            EmailTemplateType.AmazonKindle(),
            EmailTemplateType.ShikokuElectricPower(),
        )

        private val savedTypes = listOf(
            EmailTemplateType.AmazonItem(enabled = true),
            EmailTemplateType.RakutenPay(enabled = true),
            EmailTemplateType.Udemy(enabled = true, categoryId = "category1"),
            EmailTemplateType.RakutenCardETC(enabled = true, categoryId = "category2"),
            EmailTemplateType.AmazonSubscribe(enabled = true),
            EmailTemplateType.AmazonKindle(enabled = true, categoryId = "category3"),
            EmailTemplateType.ShikokuElectricPower(enabled = true, categoryId = "category4"),
        )
    }
}
