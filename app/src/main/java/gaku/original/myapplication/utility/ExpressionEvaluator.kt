package gaku.original.myapplication.utility

fun evalExpression(expression: String): Double {
    val tokens = expression.replace("×", "*").replace("÷", "/")
    return ExpressionEvaluator(tokens).parse()
}

class ExpressionEvaluator(private val input: String) {
    private var pos = -1
    private var ch: Char = ' '

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < input.length) throw RuntimeException("Unexpected: $ch")
        return x
    }

    private fun nextChar() {
        pos++
        ch = if (pos < input.length) input[pos] else '\u0000'
    }

    private fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            x = when {
                eat('+') -> x + parseTerm()
                eat('-') -> x - parseTerm()
                else -> return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            x = when {
                eat('*') -> x * parseFactor()
                eat('/') -> x / parseFactor()
                else -> return x
            }
        }
    }

    private fun parseFactor(): Double {
        if (eat('+')) return parseFactor()
        if (eat('-')) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('(')) {
            x = parseExpression()
            eat(')')
        } else if (ch in '0'..'9' || ch == '.') {
            while (ch in '0'..'9' || ch == '.') nextChar()
            x = input.substring(startPos, pos).toDouble()
        } else {
            throw RuntimeException("Unexpected: $ch")
        }

        return x
    }
}