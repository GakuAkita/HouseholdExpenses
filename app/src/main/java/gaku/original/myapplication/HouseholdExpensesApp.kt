package gaku.original.myapplication

import android.app.Application

class HouseholdExpensesApp:Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}