package fr.helios.dcdl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform