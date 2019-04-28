package org.phdeh.syncandrun.conventions

enum class Side(val isClient: Boolean = false, val isServer: Boolean = false) {
    SERVER(isServer = true),
    CLIENT(isClient = true),
    BOTH(true, true)
}