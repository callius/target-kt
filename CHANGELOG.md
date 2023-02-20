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
