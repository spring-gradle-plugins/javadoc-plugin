package io.spring.gradle.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.util.function.Consumer;

/**
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
		aggregatedJavadoc(project,sourcesPath, aggregatedConfiguration);
	}

	private Configuration aggregatedConfiguration(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration aggregatedConfiguration = configurations
				.maybeCreate(AGGREGATE_JAVADOC_CLASSPATH_CONFIGURATION_NAME);
		configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(aggregatedConfiguration);
		aggregatedConfiguration.defaultDependencies(new Action<DependencySet>() {
			@Override
			public void execute(DependencySet defaultDependencies) {
				project.getGradle().getRootProject().subprojects(new Action<Project>() {
					@Override
					public void execute(Project subproject) {
						subproject.getPlugins().withType(JavadocPlugin.class, new Action<JavadocPlugin>() {
							@Override
							public void execute(JavadocPlugin javadoc) {
								Dependency dependency = project.getDependencies()
										.create(subproject);
								defaultDependencies.add(dependency);
							}
						});
					}
				});
			}
		});
		return aggregatedConfiguration;
	}

	private Configuration sourcesPath(Project project, Configuration aggregatedConfiguration) {
		ConfigurationContainer configurations = project.getConfigurations();
		return configurations.create("sourcesPath", new Action<Configuration>() {
			@Override
			public void execute(Configuration sourcesPath) {
				sourcesPath.setCanBeResolved(true);
				sourcesPath.setCanBeConsumed(false);
				sourcesPath.extendsFrom(aggregatedConfiguration);
				sourcesPath.attributes(new Action<AttributeContainer>() {
					@Override
					public void execute(AttributeContainer attributes) {
						ObjectFactory objects = project.getObjects();
						attributes.attribute(
								Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
						attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
						attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, DocsType.SOURCES));
						attributes.attribute(
								Attribute.of("org.gradle.docselements", String.class), "sources");
					}
				});
				sourcesPath.outgoing(new Action<ConfigurationPublications>() {
					@Override
					public void execute(
							ConfigurationPublications publications) {
						JavaPluginConvention javaPlugin = project.getConvention()
								.getPlugin(JavaPluginConvention.class);
						SourceSet mainSrc = javaPlugin.getSourceSets()
								.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
						mainSrc.getAllJava().getSrcDirs().forEach(new Consumer<File>() {
							@Override
							public void accept(File file) {
								publications.artifact(file);
							}
						});
					}
				});
			}
		});
	}

	private void aggregatedJavadoc(Project project, Configuration sourcesPath, Configuration aggregatedConfiguration) {
		project.getTasks().create(AGGREGATE_JAVADOC_TASK_NAME, Javadoc.class, new Action<Javadoc>() {
			@Override
			public void execute(Javadoc javadoc) {
				javadoc.setGroup("Documentation");
				javadoc.setDescription("Generates the aggregate Javadoc");
				ConfigurationContainer configurations = project.getConfigurations();
				javadoc.setSource(sourcesPath);
				javadoc.setClasspath(aggregatedConfiguration);
			}
		});
	}
}
