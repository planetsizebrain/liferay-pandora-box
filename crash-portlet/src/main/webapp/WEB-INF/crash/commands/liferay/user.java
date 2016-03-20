package liferay;

import static com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.util.SimpleMap;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

@Usage("Different Liferay user related commands")
public class user extends BaseCommand {

	@Usage("get all users")
	@Command
	public void all(InvocationContext<User> context) throws Exception {
		UserLocalService userService = ((UserLocalService) ((SimpleMap) context.getAttributes().get("beans")).get(UserLocalServiceUtil.class.getName() + "#0"));
		for (User user : userService.getUsers(ALL_POS, ALL_POS)) {
			context.provide(user);
		}
	}

	@Usage("create user")
	@Command
	public Pipe<Map, User> create(InvocationContext<User> context) throws Exception {
		final UserLocalService userService = ((UserLocalService) ((SimpleMap) context.getAttributes().get("beans")).get(UserLocalServiceUtil.class.getName() + "#0"));

		return new Pipe<Map, User>() {

			@Override
			public void provide(Map userParameters) throws Exception {
				boolean autoPassword = GetterUtil.get(userParameters.get("autoPassword"), true);
				String password = GetterUtil.get(userParameters.get("password"), "");
				boolean autoScreenName = GetterUtil.get(userParameters.get("autoScreenName"), true);
				String screenName = GetterUtil.get(userParameters.get("screenName"), "");
				String emailAddress = GetterUtil.get(userParameters.get("emailAddress"), "");
				String firstName = GetterUtil.get(userParameters.get("firstName"), "");
				String lastName = GetterUtil.get(userParameters.get("lastName"), "");
				long facebookId = 0;
				String openid = "";

				User user = userService.addUser(
						userService.getDefaultUserId(PortalUtil.getDefaultCompanyId()),
						PortalUtil.getDefaultCompanyId(),
						autoPassword,
						password,
						password,
						autoScreenName,
						screenName,
						emailAddress,
						facebookId,
						openid,
						Locale.ENGLISH,
						firstName,
						"",
						lastName,
						0,
						0,
						true,
						1, 1, 1970,
						"",
						new long[] {},
						new long[] {},
						new long[] {},
						new long[] {},
						true,
						new ServiceContext()
				);

				context.provide(user);
			}
		};
	}
}