package org.jetbrains

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform