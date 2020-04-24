package io.spring.gradle.javadoc

import io.spring.gradle.javadoc.AggregateJavadocPlugin.AGGREGATE_JAVADOC_TASK_NAME
import io.spring.gradle.testkit.junit.TestKit
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Rob Winch
 */
internal class AggregateJavadocPluginITest {
    @Test
    fun aggregateJavadocWhenSimpleThenSuccess() {
        TestKit().use { testKit ->
            val task = ":aggregator:$AGGREGATE_JAVADOC_TASK_NAME"
            val build = testKit
                    .withProjectResource(projectResource("simple"))
                    .withArguments(task)
                    .forwardOutput()
                    .build()
            assertThat(build.task(task)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            val m1 =  File(testKit.projectDir, aggregateJavadocPath("module1/M1"))
            assertThat(m1).exists()
            val m2 = File(testKit.projectDir, aggregateJavadocPath("module2/M2"))
            assertThat(m2).exists()
            val test = File(testKit.projectDir, aggregateJavadocPath("test"))
            assertThat(test).doesNotExist()
        }
    }

    @Test
    fun aggregateJavadocWhenCustomProjectsThenSuccess() {
        TestKit().use { testKit ->
            val task = ":aggregator:$AGGREGATE_JAVADOC_TASK_NAME"
            val build = testKit
                    .withProjectResource(projectResource("custom-projects"))
                    .withArguments(task)
                    .forwardOutput()
                    .build()
            assertThat(build.task(task)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            val m1 =  File(testKit.projectDir, aggregateJavadocPath("module1/M1"))
            assertThat(m1).exists()
            val m2 = File(testKit.projectDir, aggregateJavadocPath("module2"))
            assertThat(m2).doesNotExist()
            val m3 = File(testKit.projectDir, aggregateJavadocPath("module3/M3"))
            assertThat(m3).exists()
            val test = File(testKit.projectDir, aggregateJavadocPath("test"))
            assertThat(test).doesNotExist()
        }
    }

    @Test
    fun aggregateJavadocWhenExcludeThenSuccess() {
        TestKit().use { testKit ->
            val task = ":aggregator:$AGGREGATE_JAVADOC_TASK_NAME"
            val build = testKit
                    .withProjectResource(projectResource("exclude"))
                    .withArguments(task)
                    .forwardOutput()
                    .build()
            assertThat(build.task(task)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            val m1 =  File(testKit.projectDir, aggregateJavadocPath("module1/M1"))
            assertThat(m1).exists()
            val m2 = File(testKit.projectDir, aggregateJavadocPath("module2"))
            assertThat(m2).doesNotExist()
            val test = File(testKit.projectDir, aggregateJavadocPath("test"))
            assertThat(test).doesNotExist()
        }
    }

    private fun aggregateJavadocPath(path: String) = "aggregator/build/docs/javadoc/${path}.html"

    private fun projectResource(name: String) = "javadoc/${name}"
}