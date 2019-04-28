package conventions

enum class CrashCode {

    BAD_CONSOLE_KEYS,
    ARGUMENT_NUMBER_MISMATCH,
    MESSAGE_ENUMERATION_FILLED_INCORRECTLY

    ;

    fun crash() {
        System.err.println(this.name)
        System.exit(this.ordinal + 1)
    }

}