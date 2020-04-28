/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.gradle.javadoc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

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

	private void runAggregateJavadocTask(String project) throws IOException, URISyntaxException {
		CopyUtils.fromResourceNameToDir("javadoc/aggregate/" + project, this.workingDir);
		String task = ":aggregator:" + AggregateJavadocPlugin.AGGREGATE_JAVADOC_TASK_NAME;
		// @formatter:off
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task)
				.forwardOutput()
				.build();
		// @formatter:on
		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	private File aggregateJavadocPath(String path) {
		return new File(this.workingDir, "aggregator/build/docs/javadoc/" + path + ".html");
	}

}
