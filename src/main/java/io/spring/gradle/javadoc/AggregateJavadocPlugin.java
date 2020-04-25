package io.spring.gradle.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;

/**
 * Plugin used to generate aggregated Javadoc.
 *
 * @author Rob Winch
 */
public class AggregateJavadocPlugin implements Plugin<Project> {

	public static final String AGGREGATE_JAVADOC_TASK_NAME = "aggregateJavadoc";

	public static final String AGGREGATE_JAVADOC_CLASSPATH_CONFIGURATION_NAME = "aggregateJavadocClasspath";

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);
		Configuration aggregatedConfiguration = aggregatedConfiguration(project);
		Configuration sourcesPath = sourcesPath(project, aggregatedConfiguration);
		project.getTasks().create(AGGREGATE_JAVADOC_TASK_NAME, Javadoc.class,
				new JavadocTask(sourcesPath, aggregatedConfiguration));
	}

	private Configuration aggregatedConfiguration(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration aggregatedConfiguration = configurations.maybeCreate(AGGREGATE_JAVADOC_CLASSPATH_CONFIGURATION_NAME);
		configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(aggregatedConfiguration);
		aggregatedConfiguration.defaultDependencies(new AggregatedDependencies(project)::apply);
		return aggregatedConfiguration;
	}

	private Configuration sourcesPath(Project project, Configuration aggregatedConfiguration) {
		ConfigurationContainer configurations = project.getConfigurations();
		return configurations.create("sourcesPath", (sourcesPath) -> {
			sourcesPath.setCanBeResolved(true);
			sourcesPath.setCanBeConsumed(false);
			sourcesPath.extendsFrom(aggregatedConfiguration);
			sourcesPath.attributes(attributes -> JavadocPlugin.addAttributes(project, attributes));
			sourcesPath.outgoing(publications -> JavadocPlugin.addOutgoing(project, publications));
		});
	}

	private static class JavadocTask implements Action<Javadoc>{

		private final Configuration sourcesPath;

		private final Configuration aggregatedConfiguration;

		JavadocTask(Configuration sourcesPath, Configuration aggregatedConfiguration) {
			this.sourcesPath = sourcesPath;
			this.aggregatedConfiguration=aggregatedConfiguration;
		}

		@Override
		public void execute(Javadoc javadoc) {
			javadoc.setGroup("Documentation");
			javadoc.setDescription("Generates the aggregate Javadoc");
			javadoc.setSource(sourcesPath);
			javadoc.setClasspath(aggregatedConfiguration);
		}

	}

	private static class AggregatedDependencies {

		private final Project project;

		public AggregatedDependencies(Project project) {
			this.project = project;
		}

		public void apply(DependencySet defaultDependencies) {
			project.getGradle().getRootProject().subprojects(subproject -> apply(defaultDependencies, subproject));
		}

		private void apply(DependencySet defaultDependencies, Project subproject) {
			subproject.getPlugins().withType(JavadocPlugin.class, (javadocPlugin) -> {
				Dependency subprojectDependency = this.project.getDependencies().create(subproject);
				defaultDependencies.add(subprojectDependency);
			});
		}

	}

}
