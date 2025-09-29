package target.test

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class GenerateModelTest : TemporaryProjectTest() {

    @Test
    fun `generates a model successfully`() {
        val projectName = "generate-model"
        initializeTestProject(projectName)

        val gradleRunner = GradleRunner.create().withProjectDir(tempFolder)

        try {
            gradleRunner.withArguments("compileKotlin").build()
        } catch (e: Exception) {
            persistFailedTestProject(projectName)
            throw e
        }

        persistFailedTestProject(projectName)
    }
}
