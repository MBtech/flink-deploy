package mb.learningcurve.flinkdeploy.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.scriptbuilder.domain.Statement;
import mb.learningcurve.flinkdeploy.Tools;
import mb.learningcurve.flinkdeploy.configurations.SystemTools.PACKAGE_MANAGER;
import mb.learningcurve.flinkdeploy.userprovided.Configuration;
import mb.learningcurve.flinkdeploy.userprovided.Credential;

/**
 * @author Kasper Grud Skat Madsen
 */
public class NodeConfiguration {
	
	public static List<Statement> getCommands(String clustername, Credential credentials, Configuration config, List<String> taskManagerHostNames, String jobManagerHostName, String uiHostname) {
		List<Statement> commands = new ArrayList<Statement>();
		
		// Install system tools
		commands.addAll(SystemTools.init(PACKAGE_MANAGER.APT));

		// Configure IAM credentials
		// FIXME: this is lame.  Want to use an IAM role for the machines
		// but jclouds doesn't support IAM yet.  Can probably make it works
		// using: https://github.com/jclouds/jclouds-labs-aws/blob/jclouds-labs-aws-1.8.1/iam/src/test/java/org/jclouds/iam/features/RolePolicyApiLiveTest.java
		// but there are no docs yet and I've wasted too much time messing with already.
		commands.addAll(AWSCredentials.configure(config.getDeploymentLocation(), credentials.get_ec2_identity(), credentials.get_ec2_credential()));
		
		// Install and configure s3cmd (to allow communication with Amazon S3)
		commands.addAll(S3CMD.install(PACKAGE_MANAGER.APT));
		commands.addAll(S3CMD.configure(credentials.get_ec2_identity(), credentials.get_ec2_credential()));
		
		// Install and configure ec2-ami-tools (only if optional x509 credentials have been defined)
		if (credentials.get_ec2_X509CertificatePath() != null && credentials.get_ec2_X509CertificatePath().length() > 0 && credentials.get_ec2_X509PrivateKeyPath() != null && credentials.get_ec2_X509PrivateKeyPath().length() > 0) {
			commands.addAll(EC2Tools.install(PACKAGE_MANAGER.APT));
			commands.addAll(EC2Tools.configure(credentials.get_ec2_X509CertificatePath(), credentials.get_ec2_X509PrivateKeyPath(), config.getDeploymentLocation(), clustername));
		}
		
		// Conditional - Download and configure ZeroMQ (including jzmq binding)
		//commands.addAll(ZeroMQ.download());
		//commands.addAll(ZeroMQ.configure());
		
		// Download and configure storm-deploy-alternative (before anything with supervision is started)
		commands.addAll(StormDeployAlternative.download());
		commands.addAll(StormDeployAlternative.writeConfigurationFiles(Tools.getWorkDir() + "conf" + File.separator + "configuration.yaml", Tools.getWorkDir() + "conf" + File.separator + "credential.yaml"));
		commands.addAll(StormDeployAlternative.writeLocalSSHKeys());
		
		// Download Flink
		commands.addAll(Flink.download(config.getFlinkRemoteLocation()));
		
		// Download Zookeeper
		//commands.addAll(Zookeeper.download(config.getZKLocation()));
		
		// Download Ganglia
		//commands.addAll(Ganglia.install());
		
		// Execute custom code, if user provided (pre config)
		if (config.getRemoteExecPreConfig().size() > 0)
			commands.addAll(Tools.runCustomCommands(config.getRemoteExecPreConfig()));
		
		// Configure Zookeeper (update configurationfiles)
		//commands.addAll(Zookeeper.configure(zookeeperHostnames));
		
		// Configure Flink (update configurationfiles)
		commands.addAll(Flink.configure(jobManagerHostName, taskManagerHostNames, config.getImageUsername()));
		
		// Configure Ganglia
		//commands.addAll(Ganglia.configure(clustername, uiHostname));
				
		// Execute custom code, if user provided (post config)
		if (config.getRemoteExecPostConfig().size() > 0)
			commands.addAll(Tools.runCustomCommands(config.getRemoteExecPostConfig()));
		
		
		/**
		 * Start daemons (only on correct nodes, and under supervision)
		 */
		commands.addAll(Zookeeper.startDaemonSupervision(config.getImageUsername()));
		commands.addAll(Flink.startJobManagerDaemonSupervision(config.getImageUsername()));
		commands.addAll(Flink.startTaskManagerDaemonSupervision(config.getImageUsername()));
		//commands.addAll(Flink.startUIDaemonSupervision(config.getImageUsername()));
		//commands.addAll(Flink.startDRPCDaemonSupervision(config.getImageUsername()));
		commands.addAll(Flink.startLogViewerDaemonSupervision(config.getImageUsername()));
		//commands.addAll(Ganglia.start());
		
		/**
		 * Start memory manager (to help share resources among Java processes)
		 * 	requires StormDeployAlternative is installed remotely
		 *  and user has specified he wants it running
		 */
		if (config.executeMemoryMonitor())
			commands.addAll(StormDeployAlternative.runMemoryMonitor(config.getImageUsername()));
		
		// Return commands
		return commands;
	}
}
