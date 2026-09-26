package gaku.original.myapplication.data.repository.categoryAssignment

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.assertThrowsSuspend
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryAssignmentRepositoryRealtimeDbTest {

    private lateinit var reference: RealtimeDbUserReference
    private lateinit var repository: CategoryAssignmentRepositoryRealtimeDb

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.realtimeDbReference(FirebaseTestEnvironment.newTestUser())
        repository = CategoryAssignmentRepositoryRealtimeDb(realtimeDbReference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun getCategoryAssignments_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getCategoryAssignments().isEmpty())
    }

    @Test
    fun addCategoryAssignment_savesProductAndStoreWithGeneratedIds() = runBlocking<Unit> {
        val product = sampleProduct()
        val store = sampleStore()

        repository.addCategoryAssignment(product)
        repository.addCategoryAssignment(store)

        val assignments = repository.getCategoryAssignments()
        assertEquals(2, assignments.size)
        assignments.forEach { (key, assignment) ->
            assertNotNull(assignment.id)
            assertEquals(key, assignment.id)
        }
        val savedProduct = assignments.values.filterIsInstance<CategoryAssignment.Product>().single()
        val savedStore = assignments.values.filterIsInstance<CategoryAssignment.Store>().single()
        assertEquals(product.copy(id = savedProduct.id), savedProduct)
        assertEquals(store.copy(id = savedStore.id), savedStore)
    }

    @Test
    fun updateCategoryAssignment_overwritesProduct() = runBlocking<Unit> {
        repository.addCategoryAssignment(sampleProduct())
        val saved = repository.getCategoryAssignments().values.single() as CategoryAssignment.Product
        val modified = saved.copy(name = "updated", condition = MatchCondition.CONTAINS, regex = true)

        repository.updateCategoryAssignment(modified)

        assertEquals(mapOf(saved.id to modified), repository.getCategoryAssignments())
    }

    @Test
    fun updateCategoryAssignment_overwritesStore() = runBlocking<Unit> {
        repository.addCategoryAssignment(sampleStore())
        val saved = repository.getCategoryAssignments().values.single() as CategoryAssignment.Store
        val modified = saved.copy(categoryId = "category2", condition = MatchCondition.EXACT)

        repository.updateCategoryAssignment(modified)

        assertEquals(mapOf(saved.id to modified), repository.getCategoryAssignments())
    }

    @Test
    fun deleteCategoryAssignment_removesOnlyTargetAssignment() = runBlocking<Unit> {
        repository.addCategoryAssignment(sampleProduct())
        repository.addCategoryAssignment(sampleStore())
        val assignments = repository.getCategoryAssignments().values
        val product = assignments.filterIsInstance<CategoryAssignment.Product>().single()
        val store = assignments.filterIsInstance<CategoryAssignment.Store>().single()

        repository.deleteCategoryAssignment(product)

        assertEquals(mapOf(store.id to store), repository.getCategoryAssignments())
    }

    @Test
    fun getCategoryAssignments_readsLegacyConditionNames() = runBlocking<Unit> {
        /* Older data stores condition as "exact_match" / "contains". */
        val productReference = reference.categoryAssignmentReference.child("productName")
        productReference.child("legacy1").setValue(
            mapOf(
                "id" to "legacy1",
                "categoryId" to "category1",
                "name" to "milk",
                "condition" to "contains",
                "regex" to false
            )
        ).await()
        productReference.child("legacy2").setValue(
            mapOf(
                "id" to "legacy2",
                "categoryId" to "category1",
                "name" to "bread",
                "condition" to "exact_match",
                "regex" to false
            )
        ).await()

        val assignments = repository.getCategoryAssignments()

        assertEquals(MatchCondition.CONTAINS, (assignments["legacy1"] as CategoryAssignment.Product).condition)
        assertEquals(MatchCondition.EXACT, (assignments["legacy2"] as CategoryAssignment.Product).condition)
    }

    @Test
    fun updateCategoryAssignment_throwsWhenIdIsNull() = runBlocking<Unit> {
        assertThrowsSuspend<CodingErrorException> {
            repository.updateCategoryAssignment(sampleProduct())
        }
    }

    @Test
    fun deleteCategoryAssignment_throwsWhenIdIsNull() = runBlocking<Unit> {
        assertThrowsSuspend<CodingErrorException> {
            repository.deleteCategoryAssignment(sampleStore())
        }
    }

    private fun sampleProduct() = CategoryAssignment.Product(
        categoryId = "category1",
        name = "coffee",
        condition = MatchCondition.CONTAINS,
        regex = false
    )

    private fun sampleStore() = CategoryAssignment.Store(
        categoryId = "category1",
        name = "Seven Eleven",
        condition = MatchCondition.EXACT,
        regex = false
    )
}
