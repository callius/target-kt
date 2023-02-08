package target.annotation

import kotlinx.datetime.Instant

/**
 * Marks a [ModelTemplate] as having an updated field. Generated as:
 *
 * ```kotlin
 * val updated: Instant
 * ```
 */
@AddField(name = "updated", type = Instant::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HasUpdated
