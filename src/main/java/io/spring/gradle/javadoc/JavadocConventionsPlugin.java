package io.spring.gradle.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.JavadocOutputLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Rob Winch
 */
public class JavadocConventionsPlugin implements Plugin<Project> {

	public static final String STYLESHEET_RESOURCE_NAME = "/io/spring/gradle/javadoc/internal/stylesheet.css";

	static final String STYLESHEET_FILE_NAME = "build/io.spring.gradle.javadoc-conventions/stylesheet.css";

	@Override
	public void apply(Project project) {
		TaskProvider<Sync> syncJavadocStylesheet = project.getTasks().register("syncJavadocStylesheet", Sync.class, sync -> {
			sync.setGroup("Documentation");
			sync.setDescription("Syncs the javadoc stylesheet");

			URL resource = getClass().getResource(STYLESHEET_RESOURCE_NAME);
			sync.from(resource);
			String relativeToPath = project.relativeProjectPath(new File(STYLESHEET_FILE_NAME).getParent());
			sync.into(relativeToPath);
		});

		project.getTasks().withType(Javadoc.class, javadoc -> {
			javadoc.dependsOn(syncJavadocStylesheet);
			StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
			String title = title(project);
			options.setAuthor(true);
			options.setDocTitle(title);
			options.setEncoding(StandardCharsets.UTF_8.name());
			options.setMemberLevel(JavadocMemberLevel.PROTECTED);
			options.setOutputLevel(JavadocOutputLevel.QUIET);
			options.splitIndex(true);
			options.setStylesheetFile(project.file(STYLESHEET_FILE_NAME));
			options.setUse(true);
			options.setWindowTitle(title);
		});
	}

	/**
	 * Obtains the Javadoc title from the root project by stripping off "-build",
	 * replacing all "-" with " ", and capitalizing each word.
	 *
	 * @param project
	 * @return
	 */
	private String title(Project project) {
		String BUILD = "-build";
		String title = project.getRootProject().getName();
		if (title != null && title.endsWith(BUILD)) {
			title = title.substring(0, title.length() - BUILD.length());
		}
		title = title.replace("-", " ");
		char[] chars = title.toCharArray();
		boolean capitalizeNext = true;
		for (int i=0;i<chars.length;i++) {
			if (capitalizeNext) {
				chars[i] = Character.toUpperCase(chars[i]);
				capitalizeNext = false;
			}
			if (chars[i] == ' ') {
				capitalizeNext = true;
			}
		}
		return new String(chars) + " API";
	}
}
