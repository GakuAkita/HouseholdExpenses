package gaku.original.myapplication.data.FirestoreRepository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailAutoExtractionCommon
import gaku.original.myapplication.data.dataClass.getMailAutoExtractionInternalClass
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class MailAutoExtractionFirestoreRepository(
    private val firestoreReference: FirestoreReference
) {
    val className = this::class.simpleName ?: "UnableToGetClassName"

    /**
     * @FIXME ん～本当はMailExtractionDocの内部のクラスかどうか判定したいんだけど、無理そうだな～
     *
     */
    fun getMailAutoExtractionInternalDocRef(type: MailAutoExtractionCommon): DocumentReference? {
        return firestoreReference.getMailAutoExtractionInternalDocRef(type)
    }

    /**
     * 引数はMailAutoExtractionの内部data classのデータで、
     * それをそのdata classのドキュメントにsetする
     */
    /**
     * MailAutoExtractionCommon を Firestore に set する
     */
    suspend fun setMailAutoExtractionInternalType(
        instance: MailAutoExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val internal = getMailAutoExtractionInternalClass(instance)
        if (internal == null) {
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "MailAutoExtractionのdata classに含まれていません\n開発者のミスです"
            )
            Log.d(className, statusInfo.errorMessage)
            callback(statusInfo)
            return statusInfo
        }

        val docRef = getMailAutoExtractionInternalDocRef(instance)
        if (docRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "MailAutoExtraction/${instance.documentName}を参照できませんでした"
            )
            callback(statusInfo)
            return statusInfo
        }

        return setDataToFirestore(
            data = instance,
            reference = docRef,
            callback = callback
        )
    }

    suspend fun fetchMailAutoExtractionData(
        instance: MailAutoExtractionCommon,//インスタンス自体に何も入ってなくても良いから。documentNameがほしい
        timeout: Long = 3000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<MailAutoExtractionCommon> {
        val internal = getMailAutoExtractionInternalClass(instance)
        if (internal == null) {
            val fetchResult = FetchResult<MailAutoExtractionCommon>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "MailAutoExtractionのdata classに含まれていません \n開発者のミスです"
            )
            callback(fetchResult.toSuspendFuncStatusInfo())
            return fetchResult
        }

        val docRef = getMailAutoExtractionInternalDocRef(instance)
        if (docRef == null) {
            val fetchResult = FetchResult<MailAutoExtractionCommon>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "MailAutoExtraction/${instance.documentName}を参照できませんでした"
            )
            callback(fetchResult.toSuspendFuncStatusInfo())
            return fetchResult
        }

        var fetchResult: FetchResult<MailAutoExtractionCommon>
        try {
            withTimeout(timeout) {
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    /* インスタンスと同じクラスに変換 */
                    val typedInstance = snapshot.toObject(instance::class.java)
                    if (typedInstance != null) {
                        fetchResult = FetchResult(
                            status = SuspendFuncStatus.SUCCESS,
                            data = typedInstance,
                            errorMessage = ""
                        )
                    } else {
                        fetchResult = FetchResult(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "変換に失敗しました"
                        )
                    }
                } else {
                    Log.d(
                        className,
                        "MailAutoExtraction/${instance.documentName}が存在しません\nエラーではありません"
                    )
                    fetchResult = FetchResult(
                        status = SuspendFuncStatus.SUCCESS,
                        data = null,
                        errorMessage = "MailAutoExtraction/${instance.documentName}が存在しません\nエラーではありません"
                    )
                }
            }
        } catch (e: Exception) {
            fetchResult = FetchResult(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "データ取得中にエラーが発生しました。:${e.message}"
            )
        }
        callback(fetchResult.toSuspendFuncStatusInfo())
        return fetchResult
    }
}