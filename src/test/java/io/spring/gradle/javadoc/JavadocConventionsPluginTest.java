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
import java.nio.charset.StandardCharsets;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.JavadocOutputLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JavadocConventionsPlugin.
 *
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
		Project project = ProjectBuilder.builder().withName(name).build();
		PluginContainer plugins = project.getPlugins();
		plugins.apply(JavaPlugin.class);
		plugins.apply(JavadocPlugin.class);
		plugins.apply(JavadocConventionsPlugin.class);
		return project;
	}

}
