package gaku.original.myapplication.data.repository.amazonSubscribeItem

import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AmazonSubscribeItemRepositoryRealtimeDb(
    private val realtimeDbReference: RealtimeDbUserReference
) : AmazonSubscribeItemRepository {
    private val reference = realtimeDbReference.amazonSubscribeItemReference

    override suspend fun getAllAmazonSubscribeItems(): Map<String, AmazonSubscribeItem> {
        Timber.d("AmazonSubscribeItems reference = $reference")
        val snapshots = reference.get().await().children
        val amazonSubscribeItems = mutableMapOf<String, AmazonSubscribeItem>()
        for (snapshot in snapshots) {
            val amazonSubscribeItem = snapshot.getValue(AmazonSubscribeItem::class.java)
            if (amazonSubscribeItem != null) {
                amazonSubscribeItems[snapshot.key!!] = amazonSubscribeItem
            }
        }
        return amazonSubscribeItems
    }

    override suspend fun updateAmazonSubscribeItem(item: AmazonSubscribeItem) {
        /* AmazonSubscribe is automatically detected on the server. */
        if (item.id == null) {
            throw Exception("item.id is null. This is a programming error.")
        }

        reference.child(item.id!!).setValue(item).await()
    }
}