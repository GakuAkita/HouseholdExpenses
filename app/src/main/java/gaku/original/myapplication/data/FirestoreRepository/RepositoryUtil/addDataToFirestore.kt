import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo

suspend fun <T : CommonProperty> addDataWithIdToFirestore(
    data: T,
    reference: CollectionReference,
    addTimeout: Long = 3000,
    setTimeout: Long = 3000,
    callback: (SuspendFuncStatusInfo) -> Unit = {}
): SuspendFuncStatusInfo {
    val funcName = "addDataWithIdToFirestore"

    data.timestamp = System.currentTimeMillis()

    /* ここでまずaddすることによって、idが生成される。 */
    val id = reference.document().id
    data.id = id

    val docRef = reference.document(id)

    val setStatus = setDataToFirestore(data, setTimeout, docRef, callback)

    return setStatus
}