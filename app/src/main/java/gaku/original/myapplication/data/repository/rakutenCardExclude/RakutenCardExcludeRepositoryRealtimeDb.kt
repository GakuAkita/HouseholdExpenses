package gaku.original.myapplication.data.repository.rakutenCardExclude

import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.categoryAssignment.toMatchCondition
import kotlinx.coroutines.tasks.await

class RakutenCardExcludeRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : RakutenCardExcludeRepository {
    private val reference = realtimeDbReference.rakutenCardExcludeItemReference

    override suspend fun getExcludeItems(): Map<String, RakutenCardExcludeItem> {
        return reference.get().await().children.associate { item ->
            item.key!! to item.getValue(RakutenCardExcludeItemDto::class.java)!!.toDomain()
        }
    }

    override suspend fun addExcludeItem(item: RakutenCardExcludeItem) {
        val ref = reference.push()
        ref.setValue(item.copy(id = ref.key).toDto()).await()
    }

    override suspend fun deleteExcludeItem(item: RakutenCardExcludeItem) {
        val id = item.id ?: throw CodingErrorException("item.id is null!")
        reference.child(id).removeValue().await()
    }
}

/* Cloud Functions read this too: see RakutenCardExcludeItem in functions/src/type/Mailbox.ts */
data class RakutenCardExcludeItemDto(
    val id: String? = null,
    val name: String? = null,
    val condition: String? = null
) {
    fun toDomain(): RakutenCardExcludeItem {
        return RakutenCardExcludeItem(
            id = id,
            name = name ?: "",
            condition = condition.toMatchCondition()
        )
    }
}

fun RakutenCardExcludeItem.toDto(): RakutenCardExcludeItemDto {
    return RakutenCardExcludeItemDto(
        id = id,
        name = name,
        condition = condition.name
    )
}
