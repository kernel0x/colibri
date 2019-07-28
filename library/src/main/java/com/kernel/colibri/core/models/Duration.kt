package com.kernel.colibri.core.models

import java.util.concurrent.TimeUnit

class Duration(val value: Long, val timeUnit: TimeUnit) : Comparable<Duration> {

    init {
        if (value < 0) {
            throw IllegalArgumentException("value must be >= 0, was $value")
        }
    }

    val timeUnitAsString: String
        get() = timeUnit.toString().toLowerCase()

    val valueAsMs: Long
        get() = if (value == NONE.toLong()) {
            value
        } else
            TimeUnit.MILLISECONDS.convert(value, timeUnit)

    val isForever: Boolean get() = this === FOREVER

    val isZero: Boolean get() = this == ZERO

    fun valueAs(timeUnit: TimeUnit) {
        timeUnit.convert(value, timeUnit)
    }

    operator fun plus(amount: Long): Duration {
        return Plus().apply(this, Duration(amount, timeUnit))
    }

    fun plus(amount: Long, timeUnit: TimeUnit): Duration {
        return Plus().apply(this, Duration(amount, timeUnit))
    }

    operator fun plus(duration: Duration): Duration {
        return Plus().apply(this, duration)
    }

    fun multiply(amount: Long): Duration {
        return Multiply().apply(this, Duration(amount, timeUnit))
    }

    fun divide(amount: Long): Duration {
        return Divide().apply(this, Duration(amount, timeUnit))
    }

    operator fun minus(amount: Long): Duration {
        return Minus().apply(this, Duration(amount, timeUnit))
    }

    fun minus(amount: Long, timeUnit: TimeUnit): Duration {
        return Minus().apply(this, Duration(amount, timeUnit))
    }

    operator fun minus(duration: Duration): Duration {
        return Minus().apply(this, duration)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val duration = other as Duration
        return valueAsMs == duration.valueAsMs
    }

    override fun hashCode(): Int {
        var result = (value xor value.ushr(32)).toInt()
        result = 31 * result + timeUnit.hashCode()
        return result
    }

    override fun toString(): String {
        return "Duration{" +
                "timeUnit=" + timeUnit +
                ", value=" + value +
                '}'.toString()
    }

    override fun compareTo(other: Duration): Int {
        val x = valueAsMs
        val y = other.valueAsMs
        return if (x < y) -1 else if (x == y) 0 else 1
    }

    private abstract class BiFunction {
        fun apply(lhs: Duration?, rhs: Duration?): Duration {
            if (lhs == null || rhs == null) {
                throw IllegalArgumentException("Duration cannot be null")
            }

            val specialDuration = handleSpecialCases(lhs, rhs)
            if (specialDuration != null) {
                return specialDuration
            }

            val newDuration: Duration
            newDuration = when {
                lhs.timeUnit.ordinal > rhs.timeUnit.ordinal -> {
                    val lhsConverted = rhs.timeUnit.convert(lhs.value, lhs.timeUnit)
                    Duration(apply(lhsConverted, rhs.value), rhs.timeUnit)
                }
                lhs.timeUnit.ordinal < rhs.timeUnit.ordinal -> {
                    val rhsConverted = lhs.timeUnit.convert(rhs.value, rhs.timeUnit)
                    Duration(apply(lhs.value, rhsConverted), lhs.timeUnit)
                }
                else ->
                    Duration(apply(lhs.value, rhs.value), lhs.timeUnit)
            }

            return newDuration
        }

        protected abstract fun handleSpecialCases(lhs: Duration, rhs: Duration): Duration?

        internal abstract fun apply(operand1: Long, operand2: Long): Long
    }

    private class Plus : BiFunction() {
        override fun handleSpecialCases(lhs: Duration, rhs: Duration): Duration? {
            if (ZERO == rhs) {
                return lhs
            } else if (ZERO == lhs) {
                return rhs
            } else if (lhs === FOREVER || rhs === FOREVER) {
                return FOREVER
            }
            return null
        }

        override fun apply(operand1: Long, operand2: Long): Long {
            return operand1 + operand2
        }
    }

    private class Minus : BiFunction() {
        override fun handleSpecialCases(lhs: Duration, rhs: Duration): Duration? {
            if (!lhs.isZero && rhs.isZero) {
                return lhs
            } else if (lhs === FOREVER) {
                return FOREVER
            } else if (rhs === FOREVER) {
                return ZERO
            } else if (FOREVER == rhs) {
                return ZERO
            }
            return null
        }

        override fun apply(operand1: Long, operand2: Long): Long {
            return operand1 - operand2
        }
    }

    private class Multiply : BiFunction() {
        override fun handleSpecialCases(lhs: Duration, rhs: Duration): Duration? {
            if (lhs.isZero || rhs.isZero) {
                return ZERO
            } else if (lhs === FOREVER || rhs === FOREVER) {
                return FOREVER
            }
            return null
        }

        override fun apply(operand1: Long, operand2: Long): Long {
            return operand1 * operand2
        }
    }

    private class Divide : BiFunction() {
        override fun handleSpecialCases(lhs: Duration, rhs: Duration): Duration? {
            if (lhs === FOREVER) {
                return FOREVER
            } else if (rhs === FOREVER) {
                throw IllegalArgumentException("Cannot divide by infinity")
            } else if (ZERO == lhs) {
                return ZERO
            }
            return null
        }

        override fun apply(operand1: Long, operand2: Long): Long {
            return operand1 / operand2
        }
    }

    companion object {
        val FOREVER = Duration(java.lang.Long.MAX_VALUE, TimeUnit.DAYS)
        val ZERO = Duration(0, TimeUnit.MILLISECONDS)
        val ONE_MILLISECOND = Duration(1, TimeUnit.MILLISECONDS)
        val ONE_HUNDRED_MILLISECONDS = Duration(100, TimeUnit.MILLISECONDS)
        val TWO_HUNDRED_MILLISECONDS = Duration(200, TimeUnit.MILLISECONDS)
        val FIVE_HUNDRED_MILLISECONDS = Duration(500, TimeUnit.MILLISECONDS)
        val ONE_SECOND = Duration(1, TimeUnit.SECONDS)
        val TWO_SECONDS = Duration(2, TimeUnit.SECONDS)
        val FIVE_SECONDS = Duration(5, TimeUnit.SECONDS)
        val TEN_SECONDS = Duration(10, TimeUnit.SECONDS)
        val ONE_MINUTE = Duration(60, TimeUnit.SECONDS)
        val TWO_MINUTES = Duration(120, TimeUnit.SECONDS)
        val FIVE_MINUTES = Duration(300, TimeUnit.SECONDS)
        val TEN_MINUTES = Duration(600, TimeUnit.SECONDS)
        private val NONE = -1
    }
}