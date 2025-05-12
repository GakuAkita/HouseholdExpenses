import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.addDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo

suspend fun <T : CommonProperty> addDataWithIdToFirestore(
    data: T,
    reference: CollectionReference,
    addTimeout: Long = 3000,
    setTimeout: Long = 3000,
    callback: (SuspendFuncStatusInfo) -> Unit = {}
): SuspendFuncStatus {
    val funcName = "addDataWithIdToFirestore"

    data.timestamp = System.currentTimeMillis()

    val (addStatusInfo, docRef) =
        addDataToFirestore(data, reference, addTimeout)
    if (addStatusInfo.status != SuspendFuncStatus.SUCCESS) {
        callback(addStatusInfo)
        return addStatusInfo.status
    }

    
}