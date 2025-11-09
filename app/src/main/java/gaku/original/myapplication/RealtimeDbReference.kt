package gaku.original.myapplication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.utility.LogException
import gaku.original.myapplication.utility.LogTimeout
import gaku.original.myapplication.utility.sanitizeEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val database = if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
        // DEBUGモードかつUSE_FIREBASE_EMULATOR=trueのときはエミュレータを使用
        FirebaseDatabase.getInstance().also {
            // Androidエミュレータからは "10.0.2.2" を使用（ホストマシンの127.0.0.1にアクセス）
            // 実機でテストする場合は、local.propertiesでFIREBASE_EMULATOR_HOSTを設定
            val emulatorHost = BuildConfig.FIREBASE_EMULATOR_HOST // local.propertiesから読み込まれる
            Log.d(className, "Using Realtime Database emulator: $emulatorHost:9000")
            it.useEmulator(emulatorHost, 9000)
        }.reference
    } else {
        // 本番環境のRealtime Databaseを使用
        FirebaseDatabase
            .getInstance("https://householdexpenses2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference
    }//users配下にそれぞれのuserIdが存在

    private val currentUserId: String?
        get() = if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
            // エミュレータ使用時はtestUserを使用（functions/src/local_emulator.tsと同じ）
            "testUser"
        } else {
            firebaseAuth.currentUser?.uid
        }

    private val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    suspend fun getUserRef(): FuncResultWithData<DatabaseReference> {
        val funcName = ::getUserRef.name
        return try {
            withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    val userId: String = currentUserId ?: throw Exception("userId is null")

                    val ref: DatabaseReference = database.child("users").child(userId)
                    FuncResultWithData.Success(ref)
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            LogException(className, funcName, e)
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
        }
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    private suspend fun getUserChildrenRef(
        childrenPath: List<String>,/* たどり着きたい順に名前をいれていく */
        funcName: String
    ): FuncResultWithData<DatabaseReference> {
        val userRefRet = getUserRef()

        if (userRefRet !is FuncResultWithData.Success) {
            return userRefRet
        }

        val userRef = userRefRet.data

        //シーケンスみたい。ある処理が終えたら次をスタートして、、みたいな。
        return try {
            val finalRef = withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    var tmpRef = userRef
                    for (childName in childrenPath) {
                        tmpRef = tmpRef.child(childName)
                            ?: throw Exception("tmpRef became null at child: $childName")
                    }
                    tmpRef
                }
            }
            val result = FuncResultWithData.Success(finalRef)
            result
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val result = FuncResultWithData.Failure.Timeout("Timeout: ${e.message}")
            result
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            result
        }
    }

//    // userId配下のexpenses
//    suspend fun getUserExpenseRef(): FuncResultWithData<DatabaseReference> {
//        val funcName = ::getUserExpenseRef.name
//        Log.d(className, "${funcName} was called.")
//        val childrenPath = listOf("data", "expenses")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    //userId配下のcategory
//    suspend fun getUserCategoryRef(): FuncResultWithData<DatabaseReference> {
//        val funcName = ::getUserCategoryRef.name
//        Log.d(className, "${funcName} was called")
//        val childrenPath = listOf("data", "categories")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    suspend fun getUserSettingsRef(): FuncResultWithData<DatabaseReference> {
//        val funcName = ::getUserSettingsRef.name
//        Log.d(className, "${funcName} was called")
//        val childrenPath = listOf("settings")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    suspend fun getUserRepeatAddRef(): FuncResultWithData<DatabaseReference> {
//        val funcName = ::getUserRepeatAddRef.name
//        Log.d(className, "${funcName} was called")
//        val baseRefRet = getUserSettingsRef()
//        if (baseRefRet !is FuncResultWithData.Success) {//拡張関数を使うと、スマートキャストが効かない
//            return baseRefRet
//        }
//
//        val baseRef = baseRefRet.data
//        val newRef = baseRef.child("repeatAdd")
//        val result = FuncResultWithData.Success(newRef)
//        return result
//    }

    /* カテゴリー割当て */
    /**
     * カテゴリー割当てはメール抽出だけではなくて
     * PayPayOCRの場合とかも使うからReferenceもuserId直下にある
     */
    suspend fun getCategoryAssignmentDataRef(): FuncResultWithData<DatabaseReference> {
        val funcName = ::getCategoryAssignmentDataRef.name
        val childrenPath = listOf("category_assignment_data")

        val ret = getUserChildrenRef(childrenPath, funcName)
        return ret
    }

    /**
     * CategoryAssignmentDataのプロパティ名とノード名を一致させておく
     * そうすると、RepositoryでGetするときに一括変換できる
     */
    suspend fun getProductNameCategoryAssignmentRef(): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getCategoryAssignmentDataRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FuncResultWithData.Success(
            baseRef.child("productName")
        )
        return result
    }

    suspend fun getStoreNameCategoryAssignmentRef(): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getCategoryAssignmentDataRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FuncResultWithData.Success(
            baseRef.child("storeName")
        )
        return result
    }

    /* MailboxExtraction配下 */
    private suspend fun getMailboxExtractionRef(): FuncResultWithData<DatabaseReference> {
        val funcName = ::getMailboxExtractionRef.name
        /* Log.d(className, "${funcName} was called") */
        val childrenPath = listOf("mailbox_extraction")

        val ret = getUserChildrenRef(childrenPath, funcName)//callbackは中で実行される
        return ret
    }

    private suspend fun getMailboxExtractionEmailTemplateSettingsRef(): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FuncResultWithData.Success(
            baseRef.child("email_template_settings")
        )
        return result
    }

    suspend fun getMailboxExtractionEmailTemplateSettingSingleRef(
        type: EmailTemplateType
    ): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getMailboxExtractionEmailTemplateSettingsRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data

        val result = FuncResultWithData.Success(
            baseRef.child(type.nodeName)
        )
        return result
    }

    /**
     * トークンがあるかないかを取得する
     * */
    suspend fun getMailboxExtractionGmailTokensRef(
    ): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data

        val result = FuncResultWithData.Success(
            baseRef.child("gmail_tokens")
        )
        return result
    }

    /**
     * 基本的にメールアドレスに対して一個だが、
     * 将来的に複数のメールアドレスから取得したいとなったときに。
     */
    suspend fun getMailboxExtractionGmailTokenSingleRef(
        email: String? = currentUserEmail
    ): FuncResultWithData<DatabaseReference> {
        if (email == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "email is null"
            )
        }
        val baseRefRet = getMailboxExtractionGmailTokensRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val sanitizedEmail = sanitizeEmail(email)/* @や.などを変換する */
        val result = FuncResultWithData.Success(
            baseRef.child(sanitizedEmail)
        )
        return result
    }

    suspend fun getMailboxExtractionLastExecRef(
        type: EmailTemplateType
    ): FuncResultWithData<DatabaseReference> {
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data

        val result = FuncResultWithData.Success(
            baseRef.child("last_exec").child(type.nodeName)
        )

        return result
    }

    /**
     * Amazon定期便モニターの参照を取得
     * users/{userId}/mailbox_extraction/amazon_subscribe_monitor
     */
    suspend fun getAmazonSubscribeMonitorRef(): FuncResultWithData<DatabaseReference> {
        val funcName = ::getAmazonSubscribeMonitorRef.name
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data
        val result = FuncResultWithData.Success(
            baseRef.child("amazon_subscribe_monitor")
        )
        return result
    }

    /**
     * Amazon定期便アイテムの参照を取得
     * users/{userId}/mailbox_extraction/amazon_subscribe_monitor/subscribe
     */
    suspend fun getAmazonSubscribeMonitorItemsRef(): FuncResultWithData<DatabaseReference> {
        val funcName = ::getAmazonSubscribeMonitorItemsRef.name
        val baseRefRet = getAmazonSubscribeMonitorRef()
        if (baseRefRet !is FuncResultWithData.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data
        val result = FuncResultWithData.Success(
            baseRef.child("subscribe_items")
        )
        return result
    }
}