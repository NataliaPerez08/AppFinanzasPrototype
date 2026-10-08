package com.appfinanzas.prototype.security

data class AutoLockOption(val label: String, val millis: Long)

object LockPolicy {

    const val DEFAULT_AUTO_LOCK_MILLIS = 60_000L

    val AUTO_LOCK_OPTIONS = listOf(
        AutoLockOption("Inmediatamente", 0L),
        AutoLockOption("30 segundos", 30_000L),
        AutoLockOption("1 minuto", 60_000L),
        AutoLockOption("5 minutos", 300_000L),
        AutoLockOption("15 minutos", 900_000L),
    )

    fun cooldownFor(failedAttempts: Int): Long = when {
        failedAttempts < 5 -> 0L
        failedAttempts == 5 -> 30_000L
        failedAttempts == 6 -> 60_000L
        failedAttempts == 7 -> 300_000L
        else -> 900_000L
    }

    fun remainingCooldown(failedAttempts: Int, lastFailureAt: Long, now: Long): Long {
        if (lastFailureAt <= 0L) return 0L
        return (cooldownFor(failedAttempts) - (now - lastFailureAt)).coerceAtLeast(0L)
    }

    fun shouldLock(elapsedMillis: Long, timeoutMillis: Long): Boolean =
        timeoutMillis == 0L || elapsedMillis >= timeoutMillis
}
