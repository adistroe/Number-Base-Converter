package converter

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow

/**
 *  Converts numbers between radixes [2-36].
 */
class Converter {

    private enum class Command(val text: String) {
        EXIT("/exit"),
        BACK("/back")
    }

    /**
     *  Prompts the user to enter a value and returns it.
     */
    private fun getUserInput(message: String): String {
        print(message)
        return readln()
    }

    /**
     *  Prints the conversion result to screen
     */
    private fun showConversionResult(result: String) {
        val message = String.format(Message.CONVERSION_RESULT.text, result)
        println("$message\n")
    }

    /**
     *  Converts integer number from radix [2-36] to base10.
     */
    private fun convertIntegerToBaseTen(number: String, sourceRadix: String): BigInteger {
        var result = BigInteger.ZERO
        //  reverse the digits, so we can use index when raising to power
        number.reversed().forEachIndexed { index, c ->
            //  decimal representation of digit (e.g. 'E' is 14)
            val digit = c.digitToInt(sourceRadix.toInt())
            result += digit.toBigInteger() * sourceRadix.toBigInteger().pow(index)
        }
        return result
    }

    /**
     *  Converts base10 integer number to another radix [2-36].
     */
    private fun convertIntegerFromBaseTen(number: BigInteger, targetRadix: String): CharSequence {
        var bigIntNumber = number
        //  stores the remainders
        val result = StringBuilder()
        do {
            val remainder = (bigIntNumber % targetRadix.toBigInteger()).toInt()
            //  store the digit representation of the remainder (e.g. 14 is stored as 'E')
            result.append(remainder.digitToChar(targetRadix.toInt()))
            //  bigIntNumber becomes the quotient
            bigIntNumber /= targetRadix.toBigInteger()
        } while (bigIntNumber > BigInteger.ZERO)
        //  the converted number is represented by the remainders in reversed order
        return result.reversed()
    }

    /**
     *  Converts the integer part of a number between source and target radixes [2-36]
     */
    private fun convertIntegerPart(number: String, source: String, target: String): CharSequence {
        //  convert number from source radix to decimal
        val sourceToDecimal = convertIntegerToBaseTen(number, source)
        //  convert number from decimal to target radix and return the result
        return convertIntegerFromBaseTen(sourceToDecimal, target)
    }

    /**
     *  Converts fractional part of number from radix [2-36] to base10.
     */
    private fun convertFractionalPartToBaseTen(fraction: String, sourceRadix: String): BigDecimal {
        var result = BigDecimal.ZERO
        val scale = fraction.length
        fraction.forEachIndexed { index, c ->
            //  decimal representation of digit (e.g. 'E' is 14)
            val digit = c.digitToInt(sourceRadix.toInt()).toBigDecimal()
            val divisor = sourceRadix.toDouble().pow(index + 1).toBigDecimal()
            result += digit * (BigDecimal.ONE.setScale(scale) / divisor)
        }
        return result
    }

    /**
     *  Converts base10 fractional part of number to another radix [2-36].
     */
    private fun convertFractionalPartFromBaseTen(fraction: BigDecimal, targetRadix: String): StringBuilder {
        var bigDecNumber = fraction
        val scale = fraction.scale()
        //  required by the assignment
        val maxDecimals = 5
        //  stores the remainders
        val result = StringBuilder()
        do {
            bigDecNumber *= targetRadix.toBigDecimal()
            //  the integer part will always be less in range of [0-35]
            val integerRemainder = bigDecNumber.toInt()
            //  store the digit representation of the remainder (e.g. 14 is stored as 'E')
            result.append(integerRemainder.digitToChar(targetRadix.toInt()))
            //  remove the integer part from bigDecNumber
            bigDecNumber -= integerRemainder.toBigDecimal()
            //  loop until fraction is zero, or we have at least 5 decimals
        } while (bigDecNumber % BigDecimal.ONE != BigDecimal.ZERO.setScale(scale)
            && result.length < maxDecimals
        )
        //  the converted fraction is represented by the integer remainders
        return result
    }

    /**
     *  Convert the fractional part of a number between source and target radixes [2-36]
     */
    private fun convertFractionalPart(number: String, source: String, target: String): StringBuilder {
        //  convert number from source radix to decimal
        val sourceToDecimal = convertFractionalPartToBaseTen(number, source)
        //  convert number from decimal to target radix and return the result
        return convertFractionalPartFromBaseTen(sourceToDecimal, target)
    }

    /**
     *  Converts number between two different radixes [2-36],
     *  like this: source radix --> decimal --> target radix
     */
    private fun convertNumber(source: String, target: String) {
        do {
            val message = String.format(Message.ENTER_NUMBER.text, source, target)
            val input = getUserInput(message)
            if (input != Command.BACK.text) {
                //  don't perform conversion if source and target radix are the same
                if (source == target) {
                    showConversionResult(input)
                } else {
                    //  number doesn't have fractional part
                    if (input.find { it == '.' } == null) {
                        showConversionResult(convertIntegerPart(input, source, target).toString())
                        //  number has fractional part
                    } else {
                        //  split number into integer and fractional parts
                        val integer = input.split('.').first()
                        val fractional = input.split('.').last()
                        val integerResult = convertIntegerPart(integer, source, target)
                        //  pad-right with zeroes, so have at least 5 digits
                        val fractionalResult =
                            convertFractionalPart(fractional, source, target)
                                .toString()
                                .padEnd(5, '0')
                        showConversionResult("$integerResult.$fractionalResult")
                    }
                }
            }
        } while (input != Command.BACK.text)
    }

    /**
     *  Prompts the user to choose a conversion option,
     *  performs the conversion and displays the new number.
     */
    fun run() {
        do {
            val input = getUserInput("\n${Message.ENTER_TWO_NUMBERS.text}").lowercase()
            if (input != Command.EXIT.text) {
                //  store source and target radixes
                val (source, target) = input.split(' ')
                convertNumber(source, target)
            }
        } while (input != Command.EXIT.text)
    }
}