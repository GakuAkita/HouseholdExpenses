import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore

suspend fun <T : CommonProperty> addDataWithIdToFirestore(
    data: T,
    reference: CollectionReference,
    timeout: Long = 3000,
): FuncResultWithData<T> {
    val funcName = "addDataWithIdToFirestore"

    data.timestamp = System.currentTimeMillis()

    /* ここでまずaddすることによって、idが生成される。 */
    val id = reference.document().id
    data.id = id

    val docRef = reference.document(id)

    val setStatus = setDataToFirestore(data, timeout, docRef)

    return if (setStatus.status == SuspendFuncStatus.SUCCESS) FuncResultWithData.Success(
        data = data
    ) else FuncResultWithData.Failure.GenericFailure(
        status = setStatus.status,
        errorMessage = setStatus.errorMessage,
    )
}