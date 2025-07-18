import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.removeDocument
import gaku.original.myapplication.data.SuspendFuncStatusInfo

suspend fun <T : CommonProperty> removeDataFromFirestore(
    data: T,
    reference: CollectionReference,
    timeout: Long = 3000,
): SuspendFuncStatusInfo {
    val funcName = "removeDataFromFirestore"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "id is null or empty")
        return statusInfo// 即終了
    }

    val docRef = reference.document(id)

    val statusInfo = removeDocument(docRef, timeout)

    return statusInfo
}
