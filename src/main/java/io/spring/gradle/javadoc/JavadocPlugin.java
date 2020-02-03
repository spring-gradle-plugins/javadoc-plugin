package io.spring.gradle.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author Rob Winch
 */
public class JavadocPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class).all((javaPlugin) -> {
			withSourcesElements(project);
		});
	}

	private void withSourcesElements(Project project) {
		project.getConfigurations().create("sourcesElements", new Action<Configuration>() {
			@Override
			public void execute(Configuration config) {
				config.setCanBeResolved(false);
				config.setCanBeConsumed(true);
				config.attributes(new Action<AttributeContainer>() {
					@Override
					public void execute(AttributeContainer attributes) {
						ObjectFactory objects = project.getObjects();
						attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
						attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
						attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, DocsType.SOURCES));
						attributes.attribute(Attribute.of("org.gradle.docselements", String.class), "sources");
					}
				});
				config.outgoing(new Action<ConfigurationPublications>() {
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
}
