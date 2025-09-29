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
        val destinationFolder = File("src/test/resources/failures", projectName)
        destinationFolder.delete()
        tempFolder.copyRecursively(destinationFolder, overwrite = true)
    }
}
