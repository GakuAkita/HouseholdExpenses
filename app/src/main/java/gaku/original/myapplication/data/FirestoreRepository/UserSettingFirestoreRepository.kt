package gaku.original.myapplication.data.FirestoreRepository

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.AppTimeZone
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.mergeDataToFirestore
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class UserSettingsFirestoreRepository(
    private val firebaseAuth: FirebaseAuth,/* まあ、FirestoreReference内で持っているけどこっちでも持っている */
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    val timeZoneKey: String = "TimeZone"

//    fun getUserSettingsColRef(): CollectionReference? {
//        return firestoreReference.getUserSettingsColRef()
//    }
//
//    fun getUserDocRef(): DocumentReference? {
//        return firestoreReference.getUserDocRef()
//    }

    /* emailの追加もここでやってしまう。 */
    //SignUp後にやる操作
    suspend fun addUserInitialData(
        email: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)

        val userRef = firestoreReference.getUserDocRef()
        if (userRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空でユーザーDocを取得できませんでした。"
            )
            callback(statusInfo)
            return statusInfo
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空です。"
            )
            callback(statusInfo)
            return statusInfo
        }
        val newMap = mapOf("email" to email, "id" to uid, timeZoneKey to "Asia/Tokyo")

        val statusInfo = setDataToFirestore(newMap, reference = userRef, callback = callback)
        return statusInfo
    }

    /**
     * タイムゾーンを設定する。idやemailと同じ領域に保存する。
     */
    suspend fun setUserTimeZone(
        zoneId: String = "Asia/Tokyo",
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = firestoreReference.getUserDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Userドキュメントが参照できませんでした"
            )
            return statusInfo
        }

        val newMap = mapOf(timeZoneKey to zoneId)
        val statusInfo = mergeDataToFirestore(newMap, reference = ref, callback = {
            if (it.status == SuspendFuncStatus.SUCCESS) {
                /* ローカル側のタイムゾーンにセットする */
                AppTimeZone.updateStrZoneId(zoneId)
            } else {
                callback(it)
            }
        })
        return statusInfo
    }

    suspend fun getUserPreferences(
        timeout: Long = 3000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName = ::getUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val docRef = firestoreReference.getUserPreferencesDocRef()
        if (docRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Userドキュメントが参照できませんでした"
            )
            callback(statusInfo)
            return statusInfo
        }

        return try {
            withTimeout(timeout) {

            }
        } catch (e: TimeoutCancellationException) {

        } catch (e: Exception) {

        }

    }

//    suspend fun getUserTimeZone(
//        timeout: Long = 3000,
//        callback: (SuspendFuncStatusInfo) -> Unit
//    ): SuspendFuncStatusInfo {
//        val funcName = ::getUserTimeZone.name
//        LogClassFuncCalled(className, funcName)
//
//        val ref = firestoreReference.getUserDocRef()
//        if (ref == null) {
//            val statusInfo = SuspendFuncStatusInfo(
//                SuspendFuncStatus.FAILED,
//                "Userドキュメントが参照できませんでした"
//            )
//            return statusInfo
//        }
//
//        return try {
//            withTimeout(timeout) {
//                val snapshot = ref.get().await()
//            }
//
//        } catch (e: TimeoutCancellationException) {
//            Log.d(className, "$funcName Timeout.")
//            val statusInfo =
//                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
//            callback(statusInfo)
//
//            statusInfo
//        } catch (e: Exception) {
//            Log.d(className, "$funcName failed. ${e.message}")
//            val statusInfo =
//                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
//            callback(statusInfo)
//
//            FetchResult(statusInfo.status, statusInfo.errorMessage)
//        }
//
//        return statusInfo
//    }

}