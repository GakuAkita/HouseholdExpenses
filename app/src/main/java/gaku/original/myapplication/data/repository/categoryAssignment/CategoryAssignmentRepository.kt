package gaku.original.myapplication.data.repository.categoryAssignment

import gaku.original.myapplication.data.dataClass.CategoryAssignment

interface CategoryAssignmentRepository {
    suspend fun getCategoryAssignments(): Map<String, CategoryAssignment>

    suspend fun addCategoryAssignment(assignment: CategoryAssignment)

    suspend fun updateCategoryAssignment(assignment: CategoryAssignment)

    suspend fun deleteCategoryAssignment(assignment: CategoryAssignment)
}