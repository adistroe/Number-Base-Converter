package converter

/**
 *  Contains all the messages displayed
 */
enum class Message(val text: String) {
    ENTER_TWO_NUMBERS("Enter two numbers in format: {source base} {target base} (To quit type /exit) "),
    ENTER_NUMBER("Enter number in base %s to convert to base %s (To go back type /back) "),
    CONVERSION_RESULT("Conversion result: %s")
}