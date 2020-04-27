package io.spring.gradle.javadoc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AggregateJavadocPluginITest {

	@TempDir
	Path projectDir;

	@Test
	void aggregateJavadocWhenSimpleThenSuccess() throws Exception {
		runAggregateJavadocTask("simple");
		assertThat(aggregateJavadocPath("module1/M1")).exists();
		assertThat(aggregateJavadocPath("module2/M2")).exists();
		assertThat(aggregateJavadocPath("test")).doesNotExist();
	}

	@Test
	void aggregateJavadocWhenCustomProjectsThenSuccess() throws Exception {
		runAggregateJavadocTask("custom-projects");
		assertThat(aggregateJavadocPath("module1/M1")).exists();
		assertThat(aggregateJavadocPath("module2")).doesNotExist();
		assertThat(aggregateJavadocPath("module3/M3")).exists();
		assertThat(aggregateJavadocPath("test")).doesNotExist();
	}

	@Test
	void aggregateJavadocWhenExcludeThenSuccess() throws Exception {
		runAggregateJavadocTask("exclude");
		assertThat(aggregateJavadocPath("module1/M1")).exists();
		assertThat(aggregateJavadocPath("module2")).doesNotExist();
		assertThat(aggregateJavadocPath("test")).doesNotExist();
	}

	private void runAggregateJavadocTask(String project) throws IOException {
		copyToProjectDir(project);
		String task = ":aggregator:" + AggregateJavadocPlugin.AGGREGATE_JAVADOC_TASK_NAME;
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.projectDir.toFile())
				.withPluginClasspath()
				.withArguments(task)
				.forwardOutput().build();
		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	private void copyToProjectDir(String name) throws IOException {
		copyToProjectDir(Paths.get("src", "integTest", "resources", "javadoc", name));
	}

	private void copyToProjectDir(Path source) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {
				Files.createDirectories(resolve(dir));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.copy(file, resolve(file));
				return FileVisitResult.CONTINUE;
			}

			private Path resolve(Path file) {
				return projectDir.resolve(source.relativize(file));
			}
		});
	}

	private File aggregateJavadocPath(String path) {
		return new File(this.projectDir.toFile(), "aggregator/build/docs/javadoc/" + path + ".html");
	}

}
