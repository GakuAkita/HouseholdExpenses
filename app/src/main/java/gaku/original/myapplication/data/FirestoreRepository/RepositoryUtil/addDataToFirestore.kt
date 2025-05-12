import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.addDataToFirestore
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
    val (addStatusInfo, docRef) =
        addDataToFirestore(data, reference, addTimeout)
    if (addStatusInfo.status != SuspendFuncStatus.SUCCESS) {
        callback(addStatusInfo)
        return addStatusInfo
    }

    /* addで生成したidをdataにいれて、もう一度setする */
    val id = docRef?.id
    if (id == null) {
        val errorInfo =
            SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "DocumentReferenceがnullです。")
        callback(errorInfo)
        return errorInfo
    }

    data.id = id

    val setStatus = setDataToFirestore(data, setTimeout, docRef, callback)

    return setStatus
}