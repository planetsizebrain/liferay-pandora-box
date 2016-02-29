package be.planetsizebrain.common.jsf;

import java.text.SimpleDateFormat;

import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;

import com.liferay.portal.kernel.portlet.LiferayPortletSession;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

public final class Functions {

	private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
	private static final String FILE_SIZE_FORMAT = "%.1f %sB";
	private static final String FILE_SIZE_PREFIXES = "kMGTPE";
	private static final String BYTE = " B";
	private static final int UNIT = 1024;

	private Functions() {
		// Not instanceable
	}

	// http://kofler.nonblocking.at/2012/06/using-liferay-6-permissions-within-a-jsf-portlet/
	public static boolean hasPermission(String actionId) {
		PortletRequest request = ((PortletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest()));

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

		String name = PortalUtil.getPortletId(request);
		String primKey = themeDisplay.getLayout().getPlid() + LiferayPortletSession.LAYOUT_SEPARATOR + name;
		long groupId = themeDisplay.getScopeGroupId();

		return permissionChecker.hasPermission(groupId, name, primKey, actionId);
	}

	public static String toHumanReadableSize(long length) {
		if (length < UNIT) return length + BYTE;
		int exp = (int) (Math.log(length) / Math.log(UNIT));
		char pre = FILE_SIZE_PREFIXES.charAt(exp - 1);
		return String.format(FILE_SIZE_FORMAT, length / Math.pow(UNIT, exp), pre);
	}

	public static String formatDate(long datetime) {
		return new SimpleDateFormat(DATE_FORMAT).format(datetime);
	}
}