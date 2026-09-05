package gaku.original.myapplication.data.repository.categoryAssignment

import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class FakeCategoryAssignmentRepository : CategoryAssignmentRepository {

    private val categoryAssignments = mutableMapOf<String, CategoryAssignment>(
        "1" to CategoryAssignment.Store(
            id = "1",
            categoryId = "1",
            name = "テスト店",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "2" to CategoryAssignment.Product(
            id = "2",
            categoryId = "2",
            name = "テスト商品",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "3" to CategoryAssignment.Product(
            id = "3",
            categoryId = "2",
            name = "テスト商品",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "4" to CategoryAssignment.Product(
            id = "4",
            categoryId = "2",
            name = "テスト商品4",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "5" to CategoryAssignment.Product(
            id = "5",
            categoryId = "2",
            name = "テスト商品5",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "6" to CategoryAssignment.Product(
            id = "6",
            categoryId = "2",
            name = "テスト商品6",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "7" to CategoryAssignment.Product(
            id = "7",
            categoryId = "2",
            name = "テスト商品7",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "8" to CategoryAssignment.Store(
            id = "8",
            categoryId = "2",
            name = "テスト商品7",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "9" to CategoryAssignment.Store(
            id = "9",
            categoryId = "2",
            name = "テスト商品7",
            condition = MatchCondition.EXACT,
            regex = false
        ),
        "10" to CategoryAssignment.Store(
            id = "10",
            categoryId = "2",
            name = "テスト商品7",
            condition = MatchCondition.EXACT,
            regex = false
        ),
    )

    override suspend fun getCategoryAssignments(): Map<String, CategoryAssignment> {
        delay(2000)
        return categoryAssignments
    }

    override suspend fun addCategoryAssignment(assignment: CategoryAssignment) {
        val id = UUID.randomUUID().toString()
        val dataWithId = when (assignment) {
            is CategoryAssignment.Store -> {
                assignment.copy(
                    id = id
                )
            }

            is CategoryAssignment.Product -> {
                assignment.copy(
                    id = id
                )
            }
        }
        categoryAssignments[id] = dataWithId
        return
    }

    override suspend fun updateCategoryAssignment(assignment: CategoryAssignment) {
        delay(3000.milliseconds)
        when (assignment) {
            is CategoryAssignment.Store -> {
                categoryAssignments[assignment.id!!] = assignment
            }

            is CategoryAssignment.Product -> {
                categoryAssignments[assignment.id!!] = assignment
            }
        }
        return
    }

    override suspend fun deleteCategoryAssignment(assignment: CategoryAssignment) {
        delay(5000.milliseconds)
        when (assignment) {
            is CategoryAssignment.Store -> {
                categoryAssignments.remove(assignment.id!!)
            }

            is CategoryAssignment.Product -> {
                categoryAssignments.remove(assignment.id!!)
            }
        }
        return
    }
}