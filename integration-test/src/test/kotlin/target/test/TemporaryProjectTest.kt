package target.test

import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class TemporaryProjectTest {

    @field:TempDir
    lateinit var tempFolder: File

    fun initializeTestProject(projectName: String) {
        val testProjectSrc = File("src/test/resources", projectName)

        testProjectSrc.copyRecursively(tempFolder, true)
    }

    fun persistFailedTestProject(projectName: String) {
        tempFolder.copyRecursively(
            File(
                "src/test/resources/failures",
                "$projectName-${System.currentTimeMillis()}"
            ),
            overwrite = true
        )
    }
}
