package be.planetsizebrain.common.jsf;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacesMessageUtil {

	private static final Logger LOG = LoggerFactory.getLogger(FacesMessageUtil.class);

	private static ResourceBundle resourceBundle;

	public static void addErrorMessage(String clientId, String key, Object[] args) {
		addMessage(clientId, FacesMessage.SEVERITY_ERROR, key, args);
	}

	public static void addGlobalErrorMessage(String key, Object[] args) {
		String clientId = null;
		addErrorMessage(clientId, key, args);
	}

	public static void addGlobalInfoMessage( String key, Object[] args) {
		String clientId = null;
		addInfoMessage(clientId, key, args);
	}

	public static void addGlobalInfoMessage(String key, Object arg) {
		addGlobalInfoMessage(key, new Object[] { arg });
	}

	public static void addGlobalSuccessInfoMessage() {
		String key = "your-request-processed-successfully";
		Object[] args = null;
		addGlobalInfoMessage(key, args);
	}

	public static void addGlobalUnexpectedErrorMessage() {
		String key = "an-unexpected-error-occurred";
		Object[] args = null;
		addGlobalErrorMessage(key, args);
	}

	public static void addInfoMessage(String clientId, String key, Object[] args) {
		addMessage(clientId, FacesMessage.SEVERITY_INFO, key, args);
	}

	public static void addMessage(String clientId, FacesMessage.Severity severity,
		String key, Object[] args) {
		String message = getMessage(key, args);
		FacesMessage facesMessage = new FacesMessage(severity, message, message);
		FacesContext.getCurrentInstance().addMessage(clientId, facesMessage);
	}

	public static String getMessage(String key, Object[] args) {
		String message = key;

		try {
			if (resourceBundle == null) {
				resourceBundle = ResourceBundle.getBundle("Language");
			}

			message = resourceBundle.getString(key);

			if (args != null) {
				message = MessageFormat.format(message, args);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return message;
	}
}