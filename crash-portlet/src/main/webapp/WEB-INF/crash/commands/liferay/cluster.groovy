package liferay

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil
import org.crsh.cli.Command;
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext;

class cluster {

	@Usage("show Liferay clustering information")
	@Command
	Object main(InvocationContext<Map> context) {
		for (node in ClusterExecutorUtil.getClusterNodes()) {
			context.provide([nodeId: node.clusterNodeId, address : node.inetAddress, port: node.port])
		}
	}
}