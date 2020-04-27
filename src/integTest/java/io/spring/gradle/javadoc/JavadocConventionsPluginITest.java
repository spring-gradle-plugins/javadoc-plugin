package io.spring.gradle.javadoc;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 */
class JavadocConventionsPluginITest {

	@TempDir
	File workingDir;

	@Test
	void syncJavadocStylesheet() throws Exception {

		CopyUtils.fromResourceNameToDir("javadoc/conventions/simple", this.workingDir);
		String task = ":syncJavadocStylesheet";
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task)
				.forwardOutput()
				.build();
		assertThat(buildResult.task(task).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		File stylesheet = styleSheet();
		assertThat(stylesheet).exists();
		assertThat(stylesheet).isFile();
	}

	@Test
	void javadoc() throws Exception {
		CopyUtils.fromResourceNameToDir("javadoc/conventions/simple", this.workingDir);
		String task = ":javadoc";
		BuildResult buildResult = GradleRunner.create()
				.withProjectDir(this.workingDir)
				.withPluginClasspath()
				.withArguments(task, "--stacktrace")
				.forwardOutput()
				.withDebug(true)
				.build();
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