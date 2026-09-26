package gaku.original.myapplication.data.repository.amazonSubscribeItem

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.assertThrowsSuspend
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AmazonSubscribeItemRepositoryRealtimeDbTest {

    private lateinit var reference: RealtimeDbUserReference
    private lateinit var repository: AmazonSubscribeItemRepositoryRealtimeDb

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.realtimeDbReference(FirebaseTestEnvironment.newTestUser())
        repository = AmazonSubscribeItemRepositoryRealtimeDb(realtimeDbReference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun getAllAmazonSubscribeItems_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getAllAmazonSubscribeItems().isEmpty())
    }

    @Test
    fun getAllAmazonSubscribeItems_returnsItemsSavedByServer() = runBlocking<Unit> {
        /* Items are created on the server side, so write them directly. */
        val water = sampleItem(id = "item1", productName = "Water")
        val coffee = sampleItem(id = "item2", productName = "Coffee")
        saveItem(water)
        saveItem(coffee)

        val items = repository.getAllAmazonSubscribeItems()

        assertEquals(mapOf("item1" to water, "item2" to coffee), items)
    }

    @Test
    fun updateAmazonSubscribeItem_overwritesExistingItem() = runBlocking<Unit> {
        val item = sampleItem(id = "item1", productName = "Water")
        saveItem(item)
        val modified = item.copy(quantity = 3, price = 2500f, enabled = false)

        repository.updateAmazonSubscribeItem(modified)

        assertEquals(mapOf("item1" to modified), repository.getAllAmazonSubscribeItems())
    }

    @Test
    fun updateAmazonSubscribeItem_throwsWhenIdIsNull() = runBlocking<Unit> {
        assertThrowsSuspend<Exception> {
            repository.updateAmazonSubscribeItem(sampleItem(id = null, productName = "Water"))
        }
    }

    private suspend fun saveItem(item: AmazonSubscribeItem) {
        reference.amazonSubscribeItemReference.child(item.id!!).setValue(item).await()
    }

    private fun sampleItem(id: String?, productName: String) = AmazonSubscribeItem(
        id = id,
        productName = productName,
        quantity = 1,
        price = 1980f,
        timestamp = 1_780_000_000_000L,
        enabled = true
    )
}
