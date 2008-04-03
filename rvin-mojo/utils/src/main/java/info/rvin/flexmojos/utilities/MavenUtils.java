/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.rvin.flexmojos.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class MavenUtils {

	private MavenUtils() {
	}

	/**
	 * @param project
	 *            maven project
	 * @param sourceFile
	 *            sugested name on pom
	 * @return
	 */
	public static File resolveSourceFile(MavenProject project, String sourceFile) {

		File sourceDirectory = new File(project.getBuild().getSourceDirectory());

		if (sourceFile != null) {
			return new File(sourceDirectory, sourceFile);
		} else {
			File[] files = sourceDirectory.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (files.length == 1) {
				return files[0];
			}
			if (files.length > 1) {
				for (File file : files) {
					if (file.getName().equalsIgnoreCase("main.mxml")
							|| file.getName().equalsIgnoreCase("main.as")) {
						return file;
					}
				}
			}
			return null;
		}
	}

	/**
	 * @param project
	 * @param resolver
	 * @param localRepository
	 * @param remoteRepositories
	 * @param artifactMetadataSource
	 * @return all dependencies from the project
	 * @throws MojoExecutionException
	 */
	@SuppressWarnings("unchecked")
	public static Set<Artifact> getDependencyArtifacts(MavenProject project,
			ArtifactResolver resolver, ArtifactRepository localRepository,
			List remoteRepositories,
			ArtifactMetadataSource artifactMetadataSource)
			throws MojoExecutionException {
		ArtifactResolutionResult arr;
		try {
			arr = resolver
					.resolveTransitively(project.getDependencyArtifacts(),
							project.getArtifact(), remoteRepositories,
							localRepository, artifactMetadataSource);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		Set<Artifact> result = arr.getArtifacts();
		return result;
	}

	/**
	 * @param artifact
	 *            Artifact to be resolved
	 * @param resolver
	 * @param localRepository
	 * @param remoteRepositories
	 * @throws MojoExecutionException
	 */
	@SuppressWarnings("unchecked")
	public static void resolveArtifact(Artifact artifact,
			ArtifactResolver resolver, ArtifactRepository localRepository,
			List remoteRepositories) throws MojoExecutionException {
		try {
			resolver.resolve(artifact, remoteRepositories, localRepository);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static File[] getSourcePaths(Build build) {
		List<File> files = new ArrayList<File>();

		File source = new File(build.getSourceDirectory());
		if (source.exists()) {
			files.add(source);
		}

		List<Resource> resources = build.getResources();
		for (Resource resource : resources) {
			File resourceFile = new File(resource.getDirectory());
			if (resourceFile.exists()) {
				files.add(resourceFile);
			}
		}

		return files.toArray(new File[files.size()]);
	}

	public static File getConfigFile(Build build) throws MojoExecutionException {
		URL url = MavenUtils.class.getResource("/configs/config.xml");
		File configFile = new File(build.getDirectory(), "config.xml");
		try {
			FileUtils.copyURLToFile(url, configFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Error generating config file.", e);
		}
		return configFile;
	}

	public static File getFontsFile(Build build) throws MojoExecutionException {
		String os = System.getProperty("os.name").toLowerCase();
		URL url;
		if (os.contains("mac")) {
			url = MavenUtils.class.getResource("/fonts/macFonts.ser");
		} else {
			// TODO And linux?!
			// if(os.contains("windows")) {
			url = MavenUtils.class.getResource("/fonts/winFonts.ser");
		}
		File fontsSer = new File(build.getDirectory(), "fonts.ser");
		try {
			FileUtils.copyURLToFile(url, fontsSer);
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying fonts file.", e);
		}
		return fontsSer;
	}
}
