package io.spring.gradle.javadoc;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.JavadocOutputLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 */
class JavadocConventionsPluginTest {

	@Test
	void options() {
		Project project = projectWithPlugins("spring-security-build");
		Javadoc javadoc = (Javadoc) project.getTasks().findByPath(":javadoc");
		StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

		assertThat(options.isAuthor()).isTrue();
		assertThat(options.getDocTitle()).isEqualTo("Spring Security API");
		assertThat(options.getEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
		assertThat(options.getMemberLevel()).isEqualTo(JavadocMemberLevel.PROTECTED);
		assertThat(options.getOutputLevel()).isEqualTo(JavadocOutputLevel.QUIET);
		assertThat(options.isSplitIndex()).isTrue();
		File stylesheetFile = project.file(JavadocConventionsPlugin.STYLESHEET_FILE_NAME);
		assertThat(options.getStylesheetFile()).isEqualTo(stylesheetFile);
		assertThat(options.isUse()).isTrue();
		assertThat(options.getWindowTitle()).isEqualTo("Spring Security API");
	}

	@Test
	void titleWhenNoBuild() {
		Project project = projectWithPlugins("spring-security");
		Javadoc javadoc = (Javadoc) project.getTasks().findByPath(":javadoc");
		StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

		assertThat(options.isAuthor()).isTrue();
		assertThat(options.getDocTitle()).isEqualTo("Spring Security API");
		assertThat(options.getWindowTitle()).isEqualTo("Spring Security API");
	}

	private Project projectWithPlugins(String name) {
		Project project = ProjectBuilder.builder()
				.withName(name)
				.build();
		PluginContainer plugins = project.getPlugins();
		plugins.apply(JavaPlugin.class);
		plugins.apply(JavadocPlugin.class);
		plugins.apply(JavadocConventionsPlugin.class);
		return project;
	}
}