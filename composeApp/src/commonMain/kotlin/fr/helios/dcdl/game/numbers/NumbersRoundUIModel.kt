package fr.helios.dcdl.game.numbers

import fr.helios.dcdl.model.NumbersOperation
import fr.helios.dcdl.model.NumbersOperator
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.rules.NumbersRules

data class NumbersTileDataUI(
    val tile: Int,
    val isUsed: Boolean = false
)

data class NumbersOperationUI(
    val number1: Int? = null,
    val number2: Int? = null,
    val operator: NumbersOperator? = null,
    val isResultUsed: Boolean = false
) {
    companion object {
        fun List<NumbersOperationUI>.toAnswer(): RoundAnswer.Numbers {
            return RoundAnswer.Numbers(
                result = this.findLast { operation ->
                    operation.getResult() != null
                }?.getResult() ?: 0,
                operation = this.mapNotNull { operation ->
                    if (operation.number1 != null && operation.number2 != null && operation.operator != null) {
                        NumbersOperation(
                            number1 = operation.number1,
                            number2 = operation.number2,
                            operator = operation.operator,
                        )
                    } else {
                        null
                    }
                }
            )
        }
    }
    fun getResult(): Int? {
        return if (number1 != null && number2 != null && operator != null) {
            when (operator) {
                NumbersOperator.PLUS -> number1 + number2
                NumbersOperator.MINUS -> number1 - number2
                NumbersOperator.TIMES -> number1 * number2
                NumbersOperator.BY -> number1 / number2
            }
        } else {
            null
        }
    }

    fun isFull() =
        number1 != null && number2 != null && operator != null

    fun isEmpty() =
        number1 == null && number2 == null && operator == null

    fun addNumberIfSlotEmpty(number: Int): Pair<NumbersOperationUI, Boolean> {
        val candidate =
            if (number1 == null) {
                copy(number1 = number)
            } else if (number2 == null) {
                copy(number2 = number)
            } else {
                 return this to false
            }

        return if (candidate.checkResultRules()) {
            candidate to true
        } else {
            this to false
        }
    }

    fun editOperator(operator: NumbersOperator): Pair<NumbersOperationUI, Boolean> {
        val candidate = copy(operator = operator)

        return if (candidate.checkResultRules()) {
            candidate to true
        } else {
            this to false
        }
    }

    fun checkResultRules(): Boolean {
        val resultInt = getResult() ?: return true
        if (number1 == null || number2 == null) return true

        if (resultInt > NumbersRules.Result.MAX) return false
        if (resultInt < NumbersRules.Result.MIN) return false
        if (operator == NumbersOperator.BY) return number1 % number2 == 0

        return true
    }
}