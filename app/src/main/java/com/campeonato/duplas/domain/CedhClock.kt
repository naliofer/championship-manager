package com.campeonato.duplas.domain

/** Wall-clock anchors survive process death; a paused timer never consumes time. */
data class CedhClock(val elapsed: Long = 0, val since: Long? = null) {
    fun elapsedAt(now: Long) = elapsed + (since?.let { (now - it).coerceAtLeast(0) } ?: 0)
    fun start(now: Long) = if (since == null) copy(since = now) else this
    fun pause(now: Long) = CedhClock(elapsedAt(now))
}
