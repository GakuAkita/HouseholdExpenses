package gaku.original.myapplication.repository.RealtimeDBrepository.RepositoryUtil

/* たぶん使わない。使うときはTimeoutのときローカルキャッシュをチェックする機能を加えてください */
//suspend fun addSingleDataToRTDb(
//    data: Any, /* 文字列、数値、booleanのどれか */
//    keyName: String,/* キーの名前(childの名前) */
//    reference: DatabaseReference,
//    timeout: Long = 2000,
//    callback: (FuncStatusInfo) -> Unit = {}
//): FuncStatusInfo {
//    val funcName = "addSingleDataToRTDb"
//
//    //data型の確認
//    when (data) {
//        is String, is Boolean, is Number -> {
//            /* 特に問題ないので、次に行く */
//        }
//
//        else -> {
//            Log.d(funcName, "passed data's type is wrong.${data.javaClass.simpleName}")
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = "passed data's type is wrong.${data.javaClass.simpleName}"
//            )
//            callback(statusInfo)
//            return statusInfo
//        }
//    }
//
//    return try {
//        withTimeout(timeout) {
//            suspendCancellableCoroutine { continuation ->
//                reference.child(keyName).setValue(data)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Log.d(funcName, "Data added successfully")
//                            val statusInfo = FuncStatusInfo(
//                                status = FuncStatus.SUCCESS,
//                                errorMessage = ""
//                            )
//                            callback(statusInfo)
//                            continuation.resume(statusInfo) //  成功したので再開。これでSUCCESSが返るらしい
//                        } else {
//                            Log.e(funcName, "Failed to add data", task.exception)
//                            val statusInfo = FuncStatusInfo(
//                                status = FuncStatus.FAILED,
//                                errorMessage = task.exception?.message ?: "Unknown error"
//                            )
//                            callback(statusInfo)
//                            continuation.resume(statusInfo) //  失敗したので再開
//                        }
//                    }
//            }
//        }
//    } catch (e: TimeoutCancellationException) {
//        val statusInfo = FuncStatusInfo(
//            status = FuncStatus.TIMEOUT,
//            errorMessage = "Timeout occurred"
//        )
//        callback(statusInfo)
//        return statusInfo
//    } catch (e: Exception) {
//        val statusInfo = FuncStatusInfo(
//            status = FuncStatus.FAILED,
//            errorMessage = e.message ?: "Unknown error"
//        )
//        callback(statusInfo)
//        return statusInfo
//    }
//}
