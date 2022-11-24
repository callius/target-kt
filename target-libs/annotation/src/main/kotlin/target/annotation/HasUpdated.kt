package target.annotation

import java.time.Instant

/**
 * Marks a [ModelTemplate] as having an updated field. Generated as:
 *
 * val updated: Instant
 */
@AddField(name = "updated", type = Instant::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasUpdated
