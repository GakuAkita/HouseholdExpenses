package gaku.original.myapplication.data.datasource.room

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.datasource.room.dao.CategoryDao
import gaku.original.myapplication.data.datasource.room.entity.CategoryEntity
import gaku.original.myapplication.data.repository.inMemoryAppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setUp() {
        database = inMemoryAppDatabase()
        dao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getCategories_returnsOnlyGivenUsersCategories() = runBlocking<Unit> {
        val mine = entity(userId = "me", id = "1")
        val others = entity(userId = "other", id = "1")
        dao.insertCategories(listOf(mine, others))

        assertEquals(listOf(mine), dao.getCategories("me"))
    }

    @Test
    fun replaceCategories_removesOldCategoriesOfTheUserOnly() = runBlocking<Unit> {
        val old = entity(userId = "me", id = "1")
        val others = entity(userId = "other", id = "1")
        dao.insertCategories(listOf(old, others))

        val new = entity(userId = "me", id = "2")
        dao.replaceCategories("me", listOf(new))

        assertEquals(listOf(new), dao.getCategories("me"))
        assertEquals(listOf(others), dao.getCategories("other"))
    }

    private fun entity(userId: String, id: String) = CategoryEntity(
        userId = userId,
        id = id,
        timestamp = 1_770_000_000_000L,
        name = "食費",
        enabled = true
    )
}
