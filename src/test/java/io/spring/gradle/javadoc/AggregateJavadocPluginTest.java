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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AggregateJavadocPlugin.
 *
 * @author Rob Winch
 */
class AggregateJavadocPluginTest {

	@Test
	void classpathThenIncludesAggregatedModuleClasspath() {
		Project root = rootProject();

		Project module1 = projectWithPlugins("module1", root);
		addImplementationDependencies(module1, "io.projectreactor:reactor-core:3.3.5.RELEASE");

		Project module2 = projectWithPlugins("module2", root);
		addImplementationDependencies(module2, "org.slf4j:slf4j-api:1.7.30");

		Javadoc aggregateJavadoc = (Javadoc) root.getTasks()
				.findByName(AggregateJavadocPlugin.AGGREGATE_JAVADOC_TASK_NAME);

		FileCollection classpath = aggregateJavadoc.getClasspath();
		assertThat(classpath.getFiles()).extracting(File::getName).contains("reactor-core-3.3.5.RELEASE.jar");
		assertThat(classpath.getFiles()).extracting(File::getName).contains("slf4j-api-1.7.30.jar");
	}

	private void addImplementationDependencies(Project project, String... dependencies) {
		Configuration implementation = project.getConfigurations()
				.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
		for (String dependency : dependencies) {
			implementation.getDependencies().add(project.getDependencies().create(dependency));
		}
	}

	private void addMavenCentral(Project project) {
		project.getRepositories().mavenCentral();
	}

	private Project rootProject() {
		Project project = ProjectBuilder.builder().withName("root").build();
		PluginContainer plugins = project.getPlugins();
		plugins.apply(AggregateJavadocPlugin.class);
		addMavenCentral(project);
		return project;
	}

	private Project projectWithPlugins(String name, Project parent) {
		Project project = ProjectBuilder.builder().withParent(parent).withName(name).build();
		PluginContainer plugins = project.getPlugins();
		plugins.apply(JavaPlugin.class);
		plugins.apply(JavadocPlugin.class);
		addMavenCentral(project);
		return project;
	}

}
