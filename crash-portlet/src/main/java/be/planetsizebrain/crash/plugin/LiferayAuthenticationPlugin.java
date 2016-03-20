package be.planetsizebrain.crash.plugin;

import java.util.Map;

import javax.inject.Named;

import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;

import com.google.common.collect.Maps;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

@Named
public class LiferayAuthenticationPlugin extends CRaSHPlugin<AuthenticationPlugin> implements AuthenticationPlugin<String> {

	private static final String PLUGIN_NAME = "liferay";
	private static final String ADMINISTRATOR_ROLE = "Administrator";

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

	@Override
	public boolean authenticate(String username, String password) throws Exception {
		long companyId = PortalUtil.getDefaultCompanyId();
		Map<String, String[]> headerMap = Maps.newHashMap();
		Map<String, String[]> parameterMap = Maps.newHashMap();
		Map<String, Object> resultsMap = Maps.newHashMap();

		int authResult = UserLocalServiceUtil.authenticateByScreenName(companyId, username, password, headerMap, parameterMap, resultsMap);

		if (Authenticator.SUCCESS == authResult) {
			User user = UserLocalServiceUtil.getUserByScreenName(companyId, username);
			long adminRoleId = RoleLocalServiceUtil.getRole(companyId, ADMINISTRATOR_ROLE).getRoleId();

			// TODO: maybe set threadlocals and allow more roles so the non-local services work?
			return RoleLocalServiceUtil.hasUserRole(user.getUserId(), adminRoleId);
		}

		return false;
	}

	@Override
	public Class<String> getCredentialType() {
		return String.class;
	}

	@Override
	public AuthenticationPlugin getImplementation() {
		return this;
	}
}