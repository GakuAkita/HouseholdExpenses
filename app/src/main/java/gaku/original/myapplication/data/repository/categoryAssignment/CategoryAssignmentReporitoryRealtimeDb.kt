package gaku.original.myapplication.data.repository.categoryAssignment

import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import kotlinx.coroutines.tasks.await

class CategoryAssignmentRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : CategoryAssignmentRepository {
    private val reference = realtimeDbReference.categoryAssignmentReference

    private val productReference = reference.child(CategoryAssignmentFirebase.Product().nodeName)
    private val storeReference = reference.child(CategoryAssignmentFirebase.Store().nodeName)

    override suspend fun getCategoryAssignments(): Map<String, CategoryAssignment> {
        val snapshot = reference.get().await()
        val children = snapshot.children

    }

    override suspend fun addCategoryAssignment(assignment: CategoryAssignment) {
        when (assignment) {
            is CategoryAssignment.Product -> {
                val ref = productReference.push()
                val assignWithId = assignment.copy(
                    id = ref.key
                )
                /* Firebase用の型に変換 */
                val assignmentFirebase = assignWithId.toFirebase()
                ref.setValue(assignmentFirebase).await()
            }

            is CategoryAssignment.Store -> {
                val ref = storeReference.push()
                val assignWithId = assignment.copy(
                    id = ref.key
                )
                val assignmentFirebase = assignWithId.toFirebase()
                ref.setValue(assignmentFirebase).await()
            }
        }
    }

    override suspend fun updateCategoryAssignment(assignment: CategoryAssignment) {
        if (assignment.id == null) {
            throw Exception("Coding Error: assignment.id is null!")
        }

        when (assignment) {
            is CategoryAssignment.Product -> {
                val ref = productReference.child(assignment.id!!)
                val assignmentFirebase = assignment.toFirebase()
                ref.setValue(assignmentFirebase).await()
            }

            is CategoryAssignment.Store -> {
                val ref = storeReference.child(assignment.id!!)
                val assignmentFirebase = assignment.toFirebase()
                ref.setValue(assignmentFirebase).await()
            }
        }
    }

    override suspend fun deleteCategoryAssignment(assignment: CategoryAssignment) {
        if (assignment.id == null) {
            throw Exception("Coding Error: assignment.id is null!")
        }
        when (assignment) {
            is CategoryAssignment.Product -> {
                val ref = productReference.child(assignment.id!!)
                ref.removeValue().await()
            }

            is CategoryAssignment.Store -> {
                val ref = storeReference.child(assignment.id!!)
                ref.removeValue().await()
            }
        }
    }
}

sealed interface CategoryAssignmentFirebase : HasId {
    val nodeName: String

    fun toDomain(): CategoryAssignment

    data class Product(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null, /* 店の名前や商品名 */
        val condition: String? = null, /* 完全一致なのか部分一致なのか */
        val regex: Boolean = false
    ) : CategoryAssignmentFirebase {
        override val nodeName: String = "productName"

        override fun toDomain(): CategoryAssignment {
            return CategoryAssignment.Product(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.toMatchCondition(),
                regex = regex
            )
        }
    }

    data class Store(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null,
        val condition: String? = null,
        val regex: Boolean = false,
    ) : CategoryAssignmentFirebase {
        override val nodeName: String = "storeName"

        override fun toDomain(): CategoryAssignment {
            return CategoryAssignment.Store(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.toMatchCondition(),
                regex = regex
            )
        }
    }
}

fun String?.toMatchCondition(): MatchCondition {
    if (this == null) {
        /* When already saved in Firestore once, this should not be null. */
        throw Exception("Coding Error: MatchCondition should not be null.")
    } else {
        return MatchCondition.valueOf(this)
    }
}

fun CategoryAssignment.toFirebase(): CategoryAssignmentFirebase {
    when (this) {
        is CategoryAssignment.Product -> {
            return CategoryAssignmentFirebase.Product(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.name,
                regex = regex
            )
        }

        is CategoryAssignment.Store -> {
            return CategoryAssignmentFirebase.Store(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.name,
                regex = regex
            )
        }
    }
}