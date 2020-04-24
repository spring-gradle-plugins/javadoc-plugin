package io.spring.gradle.javadoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
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

/**
 * Plugin used to indicate that a project is a memeber of the aggregate Javadoc.
 *
 * @author Rob Winch
 */
public class JavadocPlugin implements Plugin<Project> {

	private static final Attribute<String> DOCS_ELEMENTS_ATTRIBUTE = Attribute.of("org.gradle.docselements", String.class);

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class).all((plugin) -> createSourcesElements(project));
	}

	private void createSourcesElements(Project project) {
		project.getConfigurations().create("sourcesElements", (configuration) -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes((attributes) -> addAttributes(project, attributes));
			configuration.outgoing((publications) -> addOutgoing(project, publications));
		});
	}

	static void addAttributes(Project project, AttributeContainer attributes) {
		ObjectFactory objects = project.getObjects();
		attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
		attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
		attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, DocsType.SOURCES));
		attributes.attribute(DOCS_ELEMENTS_ATTRIBUTE, "sources");
	}

	static void addOutgoing(Project project, ConfigurationPublications publications) {
		JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet mainSrc = javaPlugin.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		mainSrc.getAllJava().getSrcDirs().forEach(publications::artifact);
	}

}
