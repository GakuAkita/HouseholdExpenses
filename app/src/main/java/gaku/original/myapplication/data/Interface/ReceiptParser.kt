package gaku.original.myapplication.data.Interface

import com.google.mlkit.vision.text.Text
import gaku.original.myapplication.data.dataClass.Expense

interface ReceiptParser {
    fun parse(text: Text?): Expense
}