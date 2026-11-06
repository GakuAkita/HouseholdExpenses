package gaku.original.myapplication.data.repository.rakutenCardExclude

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.assertThrowsSuspend
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RakutenCardExcludeRepositoryRealtimeDbTest {

    private lateinit var reference: RealtimeDbUserReference
    private lateinit var repository: RakutenCardExcludeRepositoryRealtimeDb

    @Before
    fun setUp() {
        reference =
            FirebaseTestEnvironment.realtimeDbReference(FirebaseTestEnvironment.newTestUser())
        repository = RakutenCardExcludeRepositoryRealtimeDb(realtimeDbReference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun getExcludeItems_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getExcludeItems().isEmpty())
    }

    @Test
    fun addExcludeItem_savesWithGeneratedId() = runBlocking<Unit> {
        val item = RakutenCardExcludeItem(name = "テスト店", condition = MatchCondition.CONTAINS)

        repository.addExcludeItem(item)

        val (key, saved) = repository.getExcludeItems().entries.single()
        assertEquals(item.copy(id = key), saved)
    }

    @Test
    fun deleteExcludeItem_removesOnlyThatItem() = runBlocking<Unit> {
        repository.addExcludeItem(RakutenCardExcludeItem(name = "テスト店"))
        repository.addExcludeItem(RakutenCardExcludeItem(name = "別の店"))
        val toDelete = repository.getExcludeItems().values.first { it.name == "テスト店" }

        repository.deleteExcludeItem(toDelete)

        assertEquals(listOf("別の店"), repository.getExcludeItems().values.map { it.name })
    }

    @Test
    fun deleteExcludeItem_throwsWhenIdIsNull() = runBlocking<Unit> {
        assertThrowsSuspend<CodingErrorException> {
            repository.deleteExcludeItem(RakutenCardExcludeItem(name = "テスト店"))
        }
    }
}
