package gaku.original.myapplication.repository.FirestoreRepository
//
//import android.util.Log
//import com.google.firebase.firestore.DocumentReference
//import gaku.original.myapplication.FirestoreReference
//import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
//import gaku.original.myapplication.data.FuncResultWithData
//import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
//import gaku.original.myapplication.data.SuspendFuncStatusInfo
//import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
//import gaku.original.myapplication.data.dataClass.getMailboxExtractionInternalClass
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withTimeout
//import javax.inject.Inject
//
//class MailboxExtractionFirestoreRepository @Inject constructor(
//    private val firestoreReference: FirestoreReference
//) {
//    val className = this::class.simpleName ?: "UnableToGetClassName"
//
//    /**
//     * @FIXME ん～本当はMailExtractionDocの内部のクラスかどうか判定したいんだけど、無理そうだな～
//     *
//     */
//    fun getMailboxExtractionMailTypeDocRef(type: MailboxExtractionCommon): DocumentReference? {
//        return firestoreReference.getMailboxExtractionMailTypeDocRef(type)
//    }
//
//    /**
//     * 引数はMailboxExtractionの内部data classのデータで、
//     * それをそのdata classのドキュメントにsetする
//     */
//    /**
//     * MailboxExtractionCommon を Firestore に set する
//     */
//    suspend fun setMailboxExtractionMailTypeSetting(
//        instance: MailboxExtractionCommon,
//        callback: (SuspendFuncStatusInfo) -> Unit
//    ): SuspendFuncStatusInfo {
//        val internal = getMailboxExtractionInternalClass(instance)
//        if (internal == null) {
//            val statusInfo = SuspendFuncStatusInfo(
//                status = SuspendFuncStatus.FAILED,
//                errorMessage = "MailboxExtractionのdata classに含まれていません\n開発者のミスです"
//            )
//            Log.d(className, statusInfo.errorMessage)
//            callback(statusInfo)
//            return statusInfo
//        }
//
//        val docRef = getMailboxExtractionMailTypeDocRef(instance)
//        if (docRef == null) {
//            val statusInfo = SuspendFuncStatusInfo(
//                status = SuspendFuncStatus.FAILED,
//                errorMessage = "MailboxExtraction/${instance.nodeName}を参照できませんでした"
//            )
//            callback(statusInfo)
//            return statusInfo
//        }
//
//        return setDataToFirestore(
//            data = instance,
//            reference = docRef,
//            callback = callback
//        )
//    }
//
//    suspend fun fetchMailboxExtractionMailTypeSetting(
//        instance: MailboxExtractionCommon,//インスタンス自体に何も入ってなくても良いから。nodeNameがほしい
//        timeout: Long = 3000,
//        callback: (SuspendFuncStatusInfo) -> Unit
//    ): FuncResultWithData<MailboxExtractionCommon> {
//        val internal = getMailboxExtractionInternalClass(instance)
//        if (internal == null) {
//            val fetchResult = FuncResultWithData<MailboxExtractionCommon>(
//                status = SuspendFuncStatus.FAILED,
//                errorMessage = "MailboxExtractionのdata classに含まれていません \n開発者のミスです"
//            )
//            callback(fetchResult.toSuspendFuncStatusInfo())
//            return fetchResult
//        }
//
//        val docRef = getMailboxExtractionMailTypeDocRef(instance)
//        if (docRef == null) {
//            val fetchResult = FuncResultWithData<MailboxExtractionCommon>(
//                status = SuspendFuncStatus.FAILED,
//                errorMessage = "MailboxExtraction/${instance.nodeName}を参照できませんでした"
//            )
//            callback(fetchResult.toSuspendFuncStatusInfo())
//            return fetchResult
//        }
//
//        var fetchResult: FuncResultWithData<MailboxExtractionCommon>
//        try {
//            withTimeout(timeout) {
//                val snapshot = docRef.get().await()
//                if (snapshot.exists()) {
//                    /* インスタンスと同じクラスに変換 */
//                    val typedInstance = snapshot.toObject(instance::class.java)
//                    if (typedInstance != null) {
//                        fetchResult = FuncResultWithData(
//                            status = SuspendFuncStatus.SUCCESS,
//                            data = typedInstance,
//                            errorMessage = ""
//                        )
//                    } else {
//                        fetchResult = FuncResultWithData(
//                            status = SuspendFuncStatus.FAILED,
//                            errorMessage = "変換に失敗しました"
//                        )
//                    }
//                } else {
//                    Log.d(
//                        className,
//                        "MailboxExtraction/${instance.nodeName}が存在しません\nエラーではありません"
//                    )
//                    fetchResult = FuncResultWithData(
//                        status = SuspendFuncStatus.SUCCESS,
//                        data = null,
//                        errorMessage = "MailboxExtraction/${instance.nodeName}が存在しません\nエラーではありません"
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            fetchResult = FuncResultWithData(
//                status = SuspendFuncStatus.FAILED,
//                errorMessage = "データ取得中にエラーが発生しました。:${e.message}"
//            )
//        }
//        callback(fetchResult.toSuspendFuncStatusInfo())
//        return fetchResult
//    }
//}