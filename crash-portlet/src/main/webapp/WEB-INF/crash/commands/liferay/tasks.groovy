package liferay

import org.crsh.cli.Command
import org.crsh.cli.Usage
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelperUtil
import org.crsh.command.InvocationContext

class tasks {

	@Usage("show the current task queue")
	@Command
	Object main(InvocationContext<Map> context) {
		for (job in SchedulerEngineHelperUtil.getScheduledJobs()) {
			context.provide([
		        name : job.jobName,
				description : job.description,
				group : job.groupName
			])
		}
	}
}