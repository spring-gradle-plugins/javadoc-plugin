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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for JavadocConventionsPlugin.
 *
 * @author Rob Winch
 */
class JavadocConventionsPluginITest {

	@TempDir
	File workingDir;

	@Test
	void syncJavadocStylesheet() throws Exception {

		CopyUtils.fromResourceNameToDir("javadoc/conventions/simple", this.workingDir);
		String task = ":syncJavadocStylesheet";

		// @formatter:off
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task)
				.forwardOutput()
				.build();
		// @formatter:on

		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		File stylesheet = styleSheet();
		assertThat(stylesheet).exists();
		assertThat(stylesheet).isFile();
	}

	@Test
	void javadoc() throws Exception {
		CopyUtils.fromResourceNameToDir("javadoc/conventions/simple", this.workingDir);
		String task = ":javadoc";
		// @formatter:off
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task, "--stacktrace")
				.forwardOutput()
				.build();
		// @formatter:on
		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(styleSheet()).exists();
		assertThat(buildJavadocFile("stylesheet.css")).hasDigest("MD5", "c99e8f025fbd2a04f67a43eb7df9cd0a");
	}

	private File buildJavadocFile(String path) {
		return new File(this.workingDir, "build/docs/javadoc/" + path);
	}

	private File styleSheet() {
		return new File(this.workingDir, JavadocConventionsPlugin.STYLESHEET_FILE_NAME);
	}

}
