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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utilities for copying directories.
 *
 * @author Rob Winch
 */
final class CopyUtils {

	private CopyUtils() {
	}

	static void fromResourceNameToDir(String projectResourceName, File toDir) throws IOException, URISyntaxException {
		toDir.mkdirs();
		ClassLoader classLoader = CopyUtils.class.getClassLoader();
		URL resourceUrl = classLoader.getResource(projectResourceName);
		if (resourceUrl == null) {
			throw new IOException("Cannot find resource '" + projectResourceName + "' with " + classLoader);
		}
		Path source = Paths.get(resourceUrl.toURI());
		copyRecursively(source, toDir.toPath());
	}

	private static void copyRecursively(Path source, Path destination) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
					throws IOException {
				Files.createDirectories(resolve(dir));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.copy(file, resolve(file));
				return FileVisitResult.CONTINUE;
			}

			private Path resolve(Path file) {
				return destination.resolve(source.relativize(file));
			}
		});
	}

}
