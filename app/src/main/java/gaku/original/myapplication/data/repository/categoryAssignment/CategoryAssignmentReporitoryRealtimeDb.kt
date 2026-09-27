package gaku.original.myapplication.data.repository.categoryAssignment

import com.google.firebase.database.DataSnapshot
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import kotlinx.coroutines.tasks.await

class CategoryAssignmentRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : CategoryAssignmentRepository {
    private val reference = realtimeDbReference.categoryAssignmentReference

    private val productReference = reference.child(CategoryAssignmentDto.Product().nodeName)
    private val storeReference = reference.child(CategoryAssignmentDto.Store().nodeName)

    override suspend fun getCategoryAssignments(): Map<String, CategoryAssignment> {
        val snapshot = reference.get().await()
        return snapshot.toCategoryAssignments()
    }

    override suspend fun addCategoryAssignment(assignment: CategoryAssignment) {
        when (assignment) {
            is CategoryAssignment.Product -> {
                val ref = productReference.push()
                val assignWithId = assignment.copy(
                    id = ref.key
                )
                /* Firebase用の型に変換 */
                val dto = assignWithId.toDto()
                ref.setValue(dto).await()
            }

            is CategoryAssignment.Store -> {
                val ref = storeReference.push()
                val assignWithId = assignment.copy(
                    id = ref.key
                )
                val dto = assignWithId.toDto()
                ref.setValue(dto).await()
            }
        }
    }

    override suspend fun updateCategoryAssignment(assignment: CategoryAssignment) {
        if (assignment.id == null) {
            throw CodingErrorException("assignment.id is null!")
        }

        when (assignment) {
            is CategoryAssignment.Product -> {
                val ref = productReference.child(assignment.id!!)
                val dto = assignment.toDto()
                ref.setValue(dto).await()
            }

            is CategoryAssignment.Store -> {
                val ref = storeReference.child(assignment.id!!)
                val dto = assignment.toDto()
                ref.setValue(dto).await()
            }
        }
    }

    override suspend fun deleteCategoryAssignment(assignment: CategoryAssignment) {
        if (assignment.id == null) {
            throw CodingErrorException("assignment.id is null!")
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

sealed interface CategoryAssignmentDto : HasId {
    val nodeName: String

    fun toDomain(): CategoryAssignment

    data class Product(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null, /* 店の名前や商品名 */
        val condition: String? = null, /* 完全一致なのか部分一致なのか */
        val regex: Boolean = false
    ) : CategoryAssignmentDto {
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
    ) : CategoryAssignmentDto {
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
        throw CodingErrorException("MatchCondition should not be null.")
    } else {
        /* For backward compatibility */
        if (this == "exact_match") {
            return MatchCondition.EXACT
        } else if (this == "contains") {
            return MatchCondition.CONTAINS
        }
        return MatchCondition.valueOf(this)
    }
}

fun CategoryAssignment.toDto(): CategoryAssignmentDto {
    when (this) {
        is CategoryAssignment.Product -> {
            return CategoryAssignmentDto.Product(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.name,
                regex = regex
            )
        }

        is CategoryAssignment.Store -> {
            return CategoryAssignmentDto.Store(
                id = id,
                categoryId = categoryId,
                name = name,
                condition = condition.name,
                regex = regex
            )
        }
    }
}


private fun DataSnapshot.toCategoryAssignments(): Map<String, CategoryAssignment> {
    return children.flatMap { child ->
        when (child.key) {
            CategoryAssignmentDto.Product().nodeName -> {
                child.children.map { item ->
                    item.key!! to item.getValue(CategoryAssignmentDto.Product::class.java)!!
                        .toDomain()
                }
            }

            CategoryAssignmentDto.Store().nodeName -> {
                child.children.map { item ->
                    item.key!! to item.getValue(CategoryAssignmentDto.Store::class.java)!!
                        .toDomain()
                }
            }

            else -> throw Exception("Unexpected node name: ${child.key}")
        }
    }.toMap()
}