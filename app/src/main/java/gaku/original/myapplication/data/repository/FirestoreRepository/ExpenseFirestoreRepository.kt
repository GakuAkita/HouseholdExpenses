package gaku.original.myapplication.data.repository.FirestoreRepository

//class ExpenseFirestoreRepository @Inject constructor(
//    private val firestoreReference: FirestoreReference
//) {
//    private val className: String = this::class.simpleName ?: "UnableToGetClassName"
//
//    fun getExpensesColRef(): CollectionReference? {
//        return firestoreReference.getExpensesColRef()
//    }
//
//    suspend fun addExpense(
//        expense: Expense,
//    ): FuncResultWithData<Expense> {
//        val ref = getExpensesColRef()
//        if (ref == null) {
//            val statusInfo = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//            return statusInfo
//        }
//
//        /* タイムアウトは設定しない */
//        val statusInfo = addDataWithIdToFirestore(expense, ref)
//        return statusInfo
//    }
//
//    suspend fun updateExpense(
//        expense: Expense
//    ): FuncStatusInfo {
//        val ref = getExpensesColRef()
//        if (ref == null) {
//            val statusInfo = FuncStatusInfo(
//                FuncStatus.FAILED,
//                "Expensesコレクションが参照できませんでした"
//            )
//            return statusInfo
//        }
//
//        val statusInfo = updateDataToFirestore(expense, ref)
//        return statusInfo
//    }
//
//    suspend fun removeExpense(
//        expense: Expense,
//    ): FuncStatusInfo {
//        val ref = getExpensesColRef()
//        if (ref == null) {
//            val statusInfo = FuncStatusInfo(
//                FuncStatus.FAILED,
//                "Expensesコレクションが参照できませんでした"
//            )
//            return statusInfo
//        }
//
//        val statusInfo = removeDataFromFirestore(expense, ref)
//        return statusInfo
//    }
//
//    suspend fun fetchMonthsExpenses(
//        fromMonth: YearMonth,
//        toMonth: YearMonth,
//        timeout: Long = 10000
//    ): FuncResultWithData<List<Expense>> {
//        val funcName = ::fetchMonthsExpenses.name
//        LogClassFuncCalled(className, funcName)
//
//        val expenseRef = getExpensesColRef()
//        if (expenseRef == null) {
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//            return result
//        }
//
//        // ISO 8601 の文字列範囲を生成（UTCで扱う想定）
//        val startDateTime =
//            fromMonth.atDay(1).atStartOfDay().toString() + "Z" // "2025-03-01T00:00:00Z"
//        val endDateTime =
//            toMonth.plusMonths(1).atDay(1).atStartOfDay().toString() + "Z" // "2025-06-01T00:00:00Z"
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {/* これをしないとメインスレッドを止めてしまう？ */
//                    val snapshot = expenseRef
//                        .whereGreaterThanOrEqualTo("datetime", startDateTime)
//                        .whereLessThan("datetime", endDateTime)
//                        .get()
//                        .await()
//
//                    val list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
//                    val result = FuncResultWithData.Success(
//                        data = list
//                    )
//                    result
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            val result = FuncResultWithData.Failure.Timeout()
//            result
//        } catch (e: Exception) {
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//            result
//        }
//    }
//
//    suspend fun fetchNotCategorizedExpenses(
//        timeout: Long = 10000
//    ): FuncResultWithData<List<Expense>> {
//        val funcName = ::fetchNotCategorizedExpenses.name
//        LogClassFuncCalled(className, funcName)
//
//        val expenseRef = getExpensesColRef()
//            ?: return FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {
//                    /**
//                     * 注意:whereEqualTo(...,null)は、フィールドが存在しないものは取得できない
//                     * ちゃんとnullという値が入っていないと
//                     */
//                    val snapshot = expenseRef
//                        .whereEqualTo("category", null)
//                        .get()
//                        .await()
//                    val list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
//
//                    FuncResultWithData.Success(data = list)
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//        }
//    }
//
//
//    suspend fun fetchAllExpenses(
//        timeout: Long = 10000
//    ): FuncResultWithData<List<Expense>> {
//        val funcName = ::fetchAllExpenses.name
//        LogClassFuncCalled(className, funcName)
//
//        val expenseRef = getExpensesColRef()
//
//        if (expenseRef == null) {
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//            return result
//        }
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {
//                    Log.d(className, "Start waiting for getUserExpenseRef.")
//                    val snapshot = expenseRef.get().await()
//
//                    val list = mutableListOf<Expense>()
//                    for (doc in snapshot.documents) {
//                        val expense = doc.toObject(Expense::class.java)
//                            ?: throw Exception("Expenseへの変換に失敗 docId=${doc.id}")
//                        list.add(expense)
//                    }
//
//                    val result = FuncResultWithData.Success(list)
//                    Log.d(className, "Fetched Expenses: $list")
//                    /* 戻り値 */
//                    result
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            Log.d(className, "$funcName Timeout.")
//            val result = FuncResultWithData.Failure.Timeout()
//            /* 戻り値 */
//            result
//        } catch (e: Exception) {
//            Log.d(className, "$funcName failed. ${e.message}")
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//            /* 戻り値 */
//            result
//        }
//    }
//
//    /**
//     * フィルター条件に基づいてExpenseを検索
//     *
//     * Firestoreの制限により、以下の方法でフィルタリングを行います：
//     * 1. Firestoreクエリ: 日付範囲、category（null判定）、generatedType（in演算）、結果数制限
//     * 2. ローカルフィルタリング: 金額範囲、テキスト検索（storeName, itemName, note）
//     *
//     * 注意: categoryIds を使ったフィルタリングは制限があります。
//     * - category.idでのフィルタリングはFirestoreの複雑なクエリのため、一度取得してからローカルでフィルタリングします
//     *
//     * コスト最適化のため、結果数を制限し、可能な限りFirestore側でフィルタリングを行います。
//     */
//    suspend fun searchExpenses(
//        filter: ExpenseSearchFilter,
//        timeout: Long = 10000,
//        limit: Long = 100L  // 結果数制限を追加
//    ): FuncResultWithData<List<Expense>> {
//        val funcName = ::searchExpenses.name
//        LogClassFuncCalled(className, funcName)
//
//        val expenseRef = getExpensesColRef()
//            ?: return FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {
//                    // Firestoreクエリを構築
//                    var query = expenseRef.orderBy("datetime")
//
//                    // 日付範囲でフィルタリング（Firestoreで実行）
//                    if (filter.dateFrom != null) {
//                        query = query.whereGreaterThanOrEqualTo("datetime", filter.dateFrom)
//                    }
//                    if (filter.dateTo != null) {
//                        query = query.whereLessThanOrEqualTo("datetime", filter.dateTo)
//                    }
//
//                    // GeneratedTypeでフィルタリング（Firestoreで実行）
//                    // 注意: in演算は10個まで
//                    if (filter.generatedTypes != null && filter.generatedTypes.isNotEmpty()) {
//                        if (filter.generatedTypes.size <= 10) {
//                            query = query.whereIn("generatedType", filter.generatedTypes)
//                        }
//                        // 10個を超える場合は後でローカルフィルタリング
//                    }
//
//                    // Categoryがnullのものだけを取得する場合（Firestoreで実行）
//                    if (filter.categoryIds != null &&
//                        filter.categoryIds.size == 1 &&
//                        filter.categoryIds[0] == null
//                    ) {
//                        query = query.whereEqualTo("category", null)
//                    }
//
//                    // 金額範囲でFirestore側フィルタリング（可能な限り）
//                    if (filter.amountMin != null) {
//                        query = query.whereGreaterThanOrEqualTo("amount", filter.amountMin)
//                    }
//                    if (filter.amountMax != null) {
//                        query = query.whereLessThanOrEqualTo("amount", filter.amountMax)
//                    }
//
//                    // 結果数制限を追加（コスト削減）
//                    query = query.limit(limit)
//
//                    // クエリを実行
//                    val snapshot = query.get().await()
//                    var list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
//
//                    // ローカルでのフィルタリング
//                    list = applyLocalFilters(list, filter)
//
//                    FuncResultWithData.Success(data = list)
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//        }
//    }
//
//    /**
//     * ローカルでフィルタリングを適用
//     *
//     * コスト最適化のため、可能な限りFirestore側でフィルタリングを行い、
//     * ローカルフィルタリングは最小限に抑えます。
//     */
//    private fun applyLocalFilters(
//        expenses: List<Expense>,
//        filter: ExpenseSearchFilter
//    ): List<Expense> {
//        var filtered = expenses
//
//        // GeneratedTypeフィルタリング（10個を超える場合）
//        if (filter.generatedTypes != null && filter.generatedTypes.size > 10) {
//            filtered = filtered.filter { expense ->
//                expense.generatedType in filter.generatedTypes
//            }
//        }
//
//        // CategoryIdフィルタリング
//        if (filter.categoryIds != null &&
//            !(filter.categoryIds.size == 1 && filter.categoryIds[0] == null)
//        ) {
//            // categoryIds が指定されている場合（nullのみではない）
//            filtered = filtered.filter { expense ->
//                expense.category?.id in filter.categoryIds
//            }
//        }
//
//        // 金額範囲フィルタリング（Firestore側で処理済みの場合はスキップ）
//        // 注意: Firestore側で金額フィルタリングが適用されていない場合のみローカルで実行
//        if (filter.amountMin != null && filter.amountMax == null) {
//            // amountMinのみが指定され、Firestore側で処理済みの場合はスキップ
//        } else if (filter.amountMax != null && filter.amountMin == null) {
//            // amountMaxのみが指定され、Firestore側で処理済みの場合はスキップ
//        } else if (filter.amountMin != null && filter.amountMax != null) {
//            // 両方が指定されている場合、Firestore側で処理済みの場合はスキップ
//        } else {
//            // どちらも指定されていない場合はスキップ
//        }
//
//        // ストア名フィルタリング（部分一致）
//        if (filter.storeName != null) {
//            filtered = filtered.filter { expense ->
//                expense.storeName?.contains(filter.storeName, ignoreCase = true) == true
//            }
//        }
//
//        // アイテム名フィルタリング（部分一致）
//        if (filter.itemName != null) {
//            filtered = filtered.filter { expense ->
//                expense.itemName?.contains(filter.itemName, ignoreCase = true) == true
//            }
//        }
//
//        // メモフィルタリング（部分一致）
//        if (filter.note != null) {
//            filtered = filtered.filter { expense ->
//                expense.note?.contains(filter.note, ignoreCase = true) == true
//            }
//        }
//
//        return filtered
//    }
//
//    /**
//     * コスト最適化のための厳格な検索
//     * テキスト検索が指定されている場合は、より厳しい制限を適用
//     */
//    suspend fun searchExpensesStrict(
//        filter: ExpenseSearchFilter,
//        timeout: Long = 10000,
//        limit: Long = 100L  // テキスト検索時はより厳しい制限
//    ): FuncResultWithData<List<Expense>> {
//        val funcName = ::searchExpensesStrict.name
//        LogClassFuncCalled(className, funcName)
//
//        val expenseRef = getExpensesColRef()
//            ?: return FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "Expensesコレクションが参照できませんでした"
//            )
//
//        // テキスト検索が指定されている場合は、より厳しい制限を適用
//        val hasTextSearch =
//            filter.storeName != null || filter.itemName != null || filter.note != null
//        val effectiveLimit = if (hasTextSearch) minOf(limit, 100) else limit
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {
//                    // Firestoreクエリを構築
//                    var query = expenseRef.orderBy("datetime")
//
//                    // 日付範囲でフィルタリング（必須）
//                    if (filter.dateFrom == null && filter.dateTo == null) {
//                        // 日付範囲が指定されていない場合は、過去1年分に制限
//                        val oneYearAgo = java.time.LocalDateTime.now().minusYears(1)
//                            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                        query = query.whereGreaterThanOrEqualTo("datetime", oneYearAgo)
//                    } else {
//                        if (filter.dateFrom != null) {
//                            query = query.whereGreaterThanOrEqualTo("datetime", filter.dateFrom)
//                        }
//                        if (filter.dateTo != null) {
//                            query = query.whereLessThanOrEqualTo("datetime", filter.dateTo)
//                        }
//                    }
//
//                    // GeneratedTypeでフィルタリング（必須）
//                    if (filter.generatedTypes != null && filter.generatedTypes.isNotEmpty()) {
//                        if (filter.generatedTypes.size <= 10) {
//                            query = query.whereIn("generatedType", filter.generatedTypes)
//                        }
//                    }
//
//                    // Categoryがnullのものだけを取得する場合（Firestoreで実行）
//                    if (filter.categoryIds != null &&
//                        filter.categoryIds.size == 1 &&
//                        filter.categoryIds[0] == null
//                    ) {
//                        query = query.whereEqualTo("category", null)
//                    }
//
//                    // 金額範囲でFirestore側フィルタリング
//                    if (filter.amountMin != null) {
//                        query = query.whereGreaterThanOrEqualTo("amount", filter.amountMin)
//                    }
//                    if (filter.amountMax != null) {
//                        query = query.whereLessThanOrEqualTo("amount", filter.amountMax)
//                    }
//
//                    // 結果数制限を追加（コスト削減）
//                    query = query.limit(effectiveLimit)
//
//                    // クエリを実行
//                    val snapshot = query.get().await()
//                    var list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
//
//                    // ローカルでのフィルタリング（テキスト検索のみ）
//                    list = applyLocalFilters(list, filter)
//
//                    FuncResultWithData.Success(data = list)
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//        }
//    }
//
//
//}