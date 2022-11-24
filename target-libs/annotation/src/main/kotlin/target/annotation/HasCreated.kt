package target.annotation

import java.time.Instant

/**
 * Marks a [ModelTemplate] as having a created field. Generated as:
 * val created: Instant
 */
@AddField(name = "created", type = Instant::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HasCreated
