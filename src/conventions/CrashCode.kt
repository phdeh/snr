package conventions

enum class CrashCode {

    BAD_CONSOLE_KEYS,
    ARGUMENT_NUMBER_MISMATCH

    ;

    fun crash() {
        System.err.println(this.name)
        System.exit(this.ordinal + 1)
    }

}