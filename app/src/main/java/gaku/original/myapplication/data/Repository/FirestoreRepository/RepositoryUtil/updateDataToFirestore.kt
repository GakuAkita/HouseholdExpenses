import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo

/**
 * ほぼsetDataと一緒。Collectionを渡してdataの中のidからDocumentReferenceを作るか、
 * 最初からDocumentReferenceを渡すかの違い。
 */
suspend fun <T : CommonProperty> updateDataToFirestore(
    data: T,
    reference: CollectionReference, // データ参照を取得するための関数
    timeout: Long = 3000,
): SuspendFuncStatusInfo {
    val funcName = "updateDataToFirestore"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "id is null or empty")
        return statusInfo
    }

    /* このdocumentRefをFirestoreReferenceに入れてしまってもいいな */
    val updateDocRef = reference.document(id)

    val statusInfo = setDataToFirestore(data, timeout, updateDocRef)

    return statusInfo
}
