package be.planetsizebrain.crash.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.portlet.*;

import org.springframework.context.annotation.Scope;

import be.planetsizebrain.common.jsf.FacesMessageUtil;
import be.planetsizebrain.crash.domain.ConsoleSize;

@Named("preferences")
@Scope("view")
public class CrashConsolePreferencesBean implements Serializable {

	private static final String CONSOLE_SIZE_KEY = "consoleSize";
	private static final String DEFAULT_CONSOLE_SIZE = "1024x768";

	private ConsoleSize selectedConsoleSize;

	@PostConstruct
	public void init() {
		PortletPreferences preferences = getPreferences();
		String resolution = preferences.getValue(CONSOLE_SIZE_KEY, DEFAULT_CONSOLE_SIZE);

		selectedConsoleSize = ConsoleSize.getByResolution(resolution);
	}

	public void submit() {
		try {
			PortletPreferences preferences = getPreferences();
			String resolution = selectedConsoleSize.getResolution();
			preferences.setValue(CONSOLE_SIZE_KEY, resolution);

			preferences.store();

			ActionResponse actionResponse = (ActionResponse) getExternalContext().getResponse();
			actionResponse.setPortletMode(PortletMode.VIEW);
			actionResponse.setWindowState(WindowState.NORMAL);

			FacesMessageUtil.addGlobalSuccessInfoMessage();
		} catch (Exception e) {
			FacesMessageUtil.addGlobalUnexpectedErrorMessage();
		}
	}

	public ConsoleSize[] getConsoleSizes() {
		return ConsoleSize.values();
	}

	public ConsoleSize getSelectedConsoleSize() {
		return selectedConsoleSize;
	}

	public void setSelectedConsoleSize(ConsoleSize selectedConsoleSize) {
		this.selectedConsoleSize = selectedConsoleSize;
	}

	private static ExternalContext getExternalContext() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getExternalContext();
	}

	private PortletPreferences getPreferences() {
		ExternalContext externalContext = getExternalContext();
		PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();

		return portletRequest.getPreferences();
	}
}