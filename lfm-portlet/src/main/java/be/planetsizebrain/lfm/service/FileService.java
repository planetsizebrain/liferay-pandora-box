package be.planetsizebrain.lfm.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.inject.Named;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.util.StringPool;

import be.planetsizebrain.common.exception.UnexpectedLiferayException;

@Named
public class FileService {

	public List<File> getFilesDirectory(String directory) {
		List<File> files = Lists.newArrayListWithExpectedSize(50);

		File dir = new File(directory);
		if (dir.exists()) {
			File[] directoryContents = dir.listFiles();
			if (directoryContents != null) {
				for (File file : directoryContents) {
					if (!file.isHidden()) {
						files.add(file);
					}
				}
			}
		}

		return files;
	}

	public void delete(File file) {
		try {
			if (file.isDirectory()) {
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						// try to delete the file anyway, even if its attributes
						// could not be read, since delete-only access is
						// theoretically possible
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						if (exc == null) {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						} else {
							// directory iteration failed; propagate exception
							throw exc;
						}
					}
				});
			} else {
				Files.delete(file.toPath());
			}
		} catch (IOException ioe) {
			throw new UnexpectedLiferayException(ioe);
		}
	}

	public String getContents(File file) {
		if (file != null) {
			try {
				return com.google.common.io.Files.toString(file, Charsets.UTF_8);
			} catch (IOException ioe) {
				throw new UnexpectedLiferayException(ioe);
			}
		}

		return StringPool.BLANK;
	}
}