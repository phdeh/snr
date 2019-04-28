package org.phdeh.syncandrun.conventions

enum class CrashCode {

    EXECUTION_COMPLETE,
    BAD_CONSOLE_KEYS,
    ARGUMENT_NUMBER_MISMATCH,
    MESSAGE_ENUMERATION_FILLED_INCORRECTLY

    ;

    fun crash() {
        if (this.ordinal != 0)
            System.err.println(this.name)
        System.exit(this.ordinal)
    }

}