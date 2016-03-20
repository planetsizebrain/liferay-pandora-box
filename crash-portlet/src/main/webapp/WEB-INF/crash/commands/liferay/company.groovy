package liferay

import com.liferay.portal.service.CompanyLocalServiceUtil
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

class company {

	@Usage("show the default Liferay company")
	@Command
	Object main(InvocationContext<Map> context) {
		for (company in CompanyLocalServiceUtil.companies) {
			context.provide([companyId: company.companyId, name: company.name])
		}
	}
}

