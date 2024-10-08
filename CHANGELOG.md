## 0.5.0

Feature:

- Kotlin 2.0.20
- KSP 2.0.20-1.0.25
- Arrow 1.2.0
- Kotlinx-Datetime 0.6.0
- Dokka 1.9.20

Breaking:

- 

## 0.4.0

Feature:

- Kotlin 1.9.0
- KSP 1.9.0-1.0.11
- Arrow 1.1.5

Breaking:

- Changed enum value object validators to accept new `entries` property instead of `values` getter.

## 0.3.0

Feature:

- Multiplatform support.

Breaking:

- Removed the `target.core.valueobject` package as Kotlin value classes are only supported on the JVM. Validators were
  added for the removed value objects, e.g., `EmailAddress` -> `EmailAddressValidator`, to make them easy to
  reimplement.
- Removed the `idField` and `customId` fields from the `ModelTemplate` annotation as a consequence of removing
  value objects. The readme now details the recommended way to add an id field to a model template. TL;DR: create a new
  annotation annotated by `AddField`.

## 0.2.0

Feature:

- Replaced fail-fast with cumulative model validation in `annotation-processor`.

## 0.1.0

Initial implementation:

- A basic implementation of functional value objects and corresponding domain model generation tools.
