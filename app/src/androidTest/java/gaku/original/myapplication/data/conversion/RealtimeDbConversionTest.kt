package gaku.original.myapplication.data.conversion

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.categoryAssignment.toDto
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepositoryRealtimeDb
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailProvider
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Saves each data class to the Realtime Database emulator and reads it back with the app's conversion code.
 *
 * When you add a property to a data class, a "samples are fully populated" test fails first.
 * Set the new property in the sample below, then the round trip tests check its conversion.
 * When you add a subtype to a sealed type, add a sample for it in the same way.
 */
@RunWith(AndroidJUnit4::class)
class RealtimeDbConversionTest {

    private lateinit var reference: RealtimeDbUserReference

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.realtimeDbReference(FirebaseTestEnvironment.newTestUser())
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    /* ---------- AmazonSubscribeItem ---------- */

    @Test
    fun amazonSubscribeItem_sampleIsFullyPopulated() {
        assertFullyPopulated(sampleAmazonSubscribeItem)
    }

    @Test
    fun amazonSubscribeItem_roundTrip() = runBlocking<Unit> {
        val node = reference.amazonSubscribeItemReference.child(sampleAmazonSubscribeItem.id!!)
        node.setValue(sampleAmazonSubscribeItem).await()

        val actual = node.get().await().getValue(AmazonSubscribeItem::class.java)!!

        assertSameProperties(sampleAmazonSubscribeItem, actual)
        assertEquals(sampleAmazonSubscribeItem, actual)
    }

    /* ---------- CategoryAssignment ---------- */

    @Test
    fun categoryAssignment_samplesAreFullyPopulated() {
        assertCoversAllSubclasses(CategoryAssignment::class, categoryAssignmentSamples)
        categoryAssignmentSamples.forEach { assertFullyPopulated(it) }
    }

    @Test
    fun categoryAssignment_roundTrip() = runBlocking<Unit> {
        categoryAssignmentSamples.forEach { expected ->
            val dto = expected.toDto()
            val node = reference.categoryAssignmentReference
                .child(dto.nodeName)
                .child(expected.id!!)
            node.setValue(dto).await()

            val actual = node.get().await().getValue(dto.javaClass)!!.toDomain()

            assertSameProperties(expected, actual)
            assertEquals(expected, actual)
        }
    }

    /* ---------- EmailTemplateType ---------- */

    @Test
    fun emailTemplateType_samplesAreFullyPopulated() {
        assertCoversAllSubclasses(EmailTemplateType::class, emailTemplateTypeSamples)
        emailTemplateTypeSamples.forEach { assertFullyPopulated(it) }
    }

    @Test
    fun emailTemplateType_roundTrip() = runBlocking<Unit> {
        /* The conversion is private in the repository, so go through it. */
        val repository = MailboxExtractionRepositoryRealtimeDb(realtimeDbReference = reference)

        emailTemplateTypeSamples.forEach { expected ->
            repository.saveMailTypeSetting(expected)

            val actual = repository.getMailTypeSetting(expected)

            assertSameProperties(expected, actual)
            assertEquals(expected, actual)
        }
    }

    companion object {
        private val sampleAmazonSubscribeItem = AmazonSubscribeItem(
            id = "item1",
            productName = "Water",
            quantity = 2,
            price = 1980.5f,
            timestamp = 1_780_000_000_000L,
            enabled = false
        )

        private val categoryAssignmentSamples = listOf(
            CategoryAssignment.Product(
                id = "product1",
                categoryId = "category1",
                name = "coffee",
                condition = MatchCondition.CONTAINS,
                regex = true
            ),
            CategoryAssignment.Store(
                id = "store1",
                categoryId = "category2",
                name = "Seven Eleven",
                condition = MatchCondition.CONTAINS,
                regex = true
            ),
        )

        /* emailProvider uses a non-default value so that a dropped field is detected. */
        private val emailTemplateTypeSamples = listOf(
            EmailTemplateType.AmazonItem(enabled = true, emailProvider = EmailProvider.OUTLOOK),
            EmailTemplateType.RakutenPay(enabled = true, emailProvider = EmailProvider.OUTLOOK),
            EmailTemplateType.AmazonSubscribe(enabled = true, emailProvider = EmailProvider.OUTLOOK),
            EmailTemplateType.AmazonKindle(
                enabled = true,
                emailProvider = EmailProvider.OUTLOOK,
                categoryId = "category1"
            ),
            EmailTemplateType.ShikokuElectricPower(
                enabled = true,
                emailProvider = EmailProvider.OUTLOOK,
                categoryId = "category2"
            ),
            EmailTemplateType.Udemy(
                enabled = true,
                emailProvider = EmailProvider.OUTLOOK,
                categoryId = "category3"
            ),
            EmailTemplateType.RakutenCardETC(
                enabled = true,
                emailProvider = EmailProvider.OUTLOOK,
                categoryId = "category4"
            ),
        )
    }
}
