package io.spring.gradle.javadoc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AggregateJavadocPluginITest {

	@TempDir
	File workingDir;

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

	private void runAggregateJavadocTask(String project) throws IOException,
			URISyntaxException {
		CopyUtils.fromResourceNameToDir("javadoc/aggregate/" + project, this.workingDir);
		String task = ":aggregator:" + AggregateJavadocPlugin.AGGREGATE_JAVADOC_TASK_NAME;
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task)
				.forwardOutput().build();
		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	private File aggregateJavadocPath(String path) {
		return new File(this.workingDir, "aggregator/build/docs/javadoc/" + path + ".html");
	}

}
