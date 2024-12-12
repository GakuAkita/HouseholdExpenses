package gaku.original.myapplication

import gaku.original.myapplication.data.ExpenseRepository

class ListenerManager(private val expenseRepository: ExpenseRepository) {
    private val activeListeners = mutableSetOf<String>()

    fun addListener(userId: String) {
        if (userId !in activeListeners) {
            expenseRepository.observeExpenses(userId)
            activeListeners.add(userId)
        }
    }

    fun clearListeners(userId: String) {
        if (userId in activeListeners) {
            expenseRepository.clearListeners(userId)
            activeListeners.remove(userId)
        }
    }
}