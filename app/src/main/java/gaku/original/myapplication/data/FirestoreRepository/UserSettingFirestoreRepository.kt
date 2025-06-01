package gaku.original.myapplication.data.FirestoreRepository

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.AppTimeZone
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.mergeDataToFirestore
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.UserPreferences
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
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
     * タイムゾーンを設定する。
     * これはdata classでsetするのではなくて、zoneId単体でセットする
     */
    suspend fun setUserTimeZone(
        zoneId: String = "Asia/Tokyo",
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Userドキュメントが参照できませんでした"
            )
            return statusInfo
        }

        val newMap = mapOf(UserPreferences.FIELD_TIME_ZONE to zoneId)
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

    /**
     * UserPreferencesを全部取ってきて、
     * その中からタイムゾーンだけ取り出す
     */
    suspend fun getUserTimeZone(
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<String> {
        val fetchResult = fetchUserPreferences()
        if (fetchResult.status != SuspendFuncStatus.SUCCESS) {
            val statusInfo = SuspendFuncStatusInfo(
                fetchResult.status,
                fetchResult.errorMessage ?: "UserPreferencesの取得に失敗しました"
            )
            callback(statusInfo)
            return FetchResult(statusInfo.status, statusInfo.errorMessage)
        }

        /* 取得はできたけどnullだったら、、 */
        val userPreferences = fetchResult.data
        if (userPreferences == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "UserPreferencesデータがnullです"
            )
            callback(statusInfo)
            return FetchResult(statusInfo.status, statusInfo.errorMessage)
        }

        AppTimeZone.updateStrZoneId(userPreferences.timeZone)
        val statusInfo = SuspendFuncStatusInfo(
            SuspendFuncStatus.SUCCESS,
            ""
        )
        callback(statusInfo)
        return FetchResult(statusInfo.status, statusInfo.errorMessage, userPreferences.timeZone)
    }

    /**
     * UserPreferencesを全部取ってくる。特定の項目だけではなく。
     */
    suspend fun fetchUserPreferences(
        timeout: Long = 3000,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<UserPreferences> {
        val funcName = ::fetchUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "UserPreferencesドキュメントが参照できませんでした"
            )
            callback(statusInfo)
            return FetchResult(statusInfo.status, statusInfo.errorMessage)
        }

        return try {
            withTimeout(timeout) {
                val snapshot = ref.get().await()
                if (snapshot.exists()) {
                    val preferences = snapshot.toObject(UserPreferences::class.java)
                    if (preferences != null) {
                        val statusInfo = SuspendFuncStatusInfo(
                            SuspendFuncStatus.SUCCESS,
                            ""
                        )
                        callback(statusInfo)
                        FetchResult(statusInfo.status, statusInfo.errorMessage, preferences)
                    } else {
                        val statusInfo = SuspendFuncStatusInfo(
                            SuspendFuncStatus.FAILED,
                            "UserPreferencesデータの変換に失敗しました"
                        )
                        callback(statusInfo)
                        FetchResult(statusInfo.status, statusInfo.errorMessage)
                    }
                } else {
                    val statusInfo = SuspendFuncStatusInfo(
                        SuspendFuncStatus.FAILED,
                        "ドキュメントが存在しません"
                    )
                    callback(statusInfo)
                    FetchResult(statusInfo.status, statusInfo.errorMessage)
                }
            }
        } catch (e: TimeoutCancellationException) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.TIMEOUT,
                "タイムアウトしました"
            )
            callback(statusInfo)
            FetchResult(statusInfo.status, statusInfo.errorMessage)
        } catch (e: Exception) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                e.message ?: "不明なエラー"
            )
            callback(statusInfo)
            FetchResult(statusInfo.status, statusInfo.errorMessage)
        }
    }
}