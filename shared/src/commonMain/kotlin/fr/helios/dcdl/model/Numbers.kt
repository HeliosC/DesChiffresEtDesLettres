package fr.helios.dcdl.model

import kotlinx.serialization.Serializable

enum class NumbersOperator(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    BY("/")
}

@Serializable
data class NumbersOperation(
    val number1: Int,
    val number2: Int,
    val operator: NumbersOperator
) {
    fun getResult(): Int {
        return when (operator) {
            NumbersOperator.PLUS -> number1 + number2
            NumbersOperator.MINUS -> number1 - number2
            NumbersOperator.TIMES -> number1 * number2
            NumbersOperator.BY -> number1 / number2
        }
    }
}