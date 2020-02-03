package io.spring.gradle.testkit.junit

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.io.IOException
import java.nio.file.Paths

/**
 * @author Rob Winch
 */
class TestKit: AutoCloseable {
    val projectDir: File = createTempDir("testkit")

    fun withProjectResource(projectResourceName: String): GradleRunner {
        val classLoader = javaClass.classLoader
        val resourceUrl = classLoader.getResource(projectResourceName)
        if (resourceUrl == null) {
            throw IOException("Cannot find resource '$projectResourceName' with $classLoader")
        }
        val projectDir = Paths.get(resourceUrl.toURI()).toFile()
        return withProjectDir(projectDir)
    }

    fun withProjectDir(projectDir: File): GradleRunner {
        projectDir.copyRecursively(this.projectDir)
        return GradleRunner.create()
                .withProjectDir(this.projectDir)
                .withPluginClasspath()
    }

    override fun close() {
        projectDir.deleteRecursively()
    }
}