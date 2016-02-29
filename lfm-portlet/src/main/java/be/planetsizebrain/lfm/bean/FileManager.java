package be.planetsizebrain.lfm.bean;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.util.MimeTypes;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.util.PortalUtil;

import be.planetsizebrain.common.exception.UnexpectedLiferayException;
import be.planetsizebrain.lfm.service.FileService;

@Named("lfm")
@Scope("view")
public class FileManager implements Serializable {

	private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
	private static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=\"%s\"";
	private static final String ROOT_PLACEHOLDER = "[ROOT]";

	private String currentDirectory;
	private List<File> currentDirectoryFiles;
	private List<String> currentPathParts = Lists.newArrayListWithExpectedSize(10);
	private File selectedFile;
	private String newFileContents;
	private String newFileName;
	private boolean newFileIsFolder;

	@Inject private Props props;
	@Inject private MimeTypes mimeTypes;
	@Inject private FileService fileService;

	@PostConstruct
	public void init() {
		currentDirectory = props.get(PropsKeys.LIFERAY_HOME);
		getPathParts(currentDirectory);
		getFilesForDirectory(currentDirectory);
	}

	public void download(File file) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		PortletResponse portletResponse = (PortletResponse) facesContext.getExternalContext().getResponse();
		HttpServletResponse response = PortalUtil.getHttpServletResponse(portletResponse);

		String contentDisposition = String.format(CONTENT_DISPOSITION_VALUE, file.getName());
		String mimeType = mimeTypes.getContentType(file);

		try (OutputStream output = response.getOutputStream()) {
			response.reset();
			response.setContentType(mimeType);
			response.setHeader(CONTENT_DISPOSITION_HEADER, contentDisposition);

			response.flushBuffer();

			Files.copy(file.toPath(), output);
		} catch (IOException ioe) {
			throw new UnexpectedLiferayException(ioe);
		} finally {
			facesContext.responseComplete();
		}
	}

	public void delete(File file) {
		fileService.delete(file);
		refresh();
	}

	public void create() {
		try {
			File file = new File(currentDirectory + File.separator + newFileName);
			if (isNewFileIsFolder()) {
				Files.createDirectory(file.toPath());
			} else {
				Files.createFile(file.toPath());
			}

			refresh();
			newFileName = null;
		} catch (IOException ioe) {
			throw new UnexpectedLiferayException(ioe);
		}
	}

	public void touch(File file) {
		if (file.isFile()) {
			file.setLastModified(System.currentTimeMillis());
			getFilesForDirectory(currentDirectory);
		}
	}

	public void edit(File file) {
		this.selectedFile = file;
		setCurrentFile(fileService.getContents(file));
	}

	public void navigate(File file) {
		currentDirectory = file.getAbsolutePath();
		getPathParts(currentDirectory);
		getFilesForDirectory(currentDirectory);
	}

	public void changePath(String pathPart) {
		StringBuilder path = new StringBuilder(128);

		if (ROOT_PLACEHOLDER.equals(pathPart)) {
			path.append("/");
		} else {
			for (String part : currentPathParts) {
				path.append(File.separator).append(part);
				if (part.equals(pathPart)) {
					break;
				}
			}
		}

		currentDirectory = path.toString();
		getPathParts(currentDirectory);
		getFilesForDirectory(currentDirectory);
	}

	public String getCurrentFile() {
 		if (selectedFile != null) {
			try {
				return com.google.common.io.Files.toString(selectedFile, Charsets.UTF_8);
			} catch (IOException ioe) {
				throw new UnexpectedLiferayException(ioe);
			}
		}

		return StringPool.BLANK;
	}

	public void setCurrentFile(String currentFile) {
		newFileContents = currentFile;
	}

	public void save() {
		try {
			com.google.common.io.Files.write(newFileContents, selectedFile, Charsets.UTF_8);
			refresh();
		} catch (IOException ioe) {
			throw new UnexpectedLiferayException(ioe);
		}
	}

	public void handleUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try (InputStream inputStream = uploadedFile.getInputstream()) {
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);

			File targetFile = new File(currentDirectory + File.separator + uploadedFile.getFileName());
			com.google.common.io.Files.write(buffer, targetFile);
			refresh();
		} catch (Exception e) {
			throw new UnexpectedLiferayException(e);
		}
	}

	private void refresh() {
		selectedFile = null;
		newFileContents = "";
		getFilesForDirectory(currentDirectory);
	}

	private void getPathParts(String directory) {
		File dir = new File(directory);
		if (dir.exists()) {
			currentPathParts = Splitter.on(File.separator).omitEmptyStrings().splitToList(directory);
		}
	}

	private void getFilesForDirectory(String directory) {
		currentDirectoryFiles = fileService.getFilesDirectory(directory);
	}

	public List<String> getCurrentPathParts() {
		return currentPathParts;
	}

	public List<File> getCurrentDirectoryFiles() {
		return currentDirectoryFiles;
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public String getCurrentDirectory() {
		return currentDirectory;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

	public boolean isNewFileIsFolder() {
		return newFileIsFolder;
	}

	public void setNewFileIsFolder(boolean newFileIsFolder) {
		this.newFileIsFolder = newFileIsFolder;
	}
}