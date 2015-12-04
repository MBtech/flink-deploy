package mb.learningcurve.stormdeploy;

import java.io.File;
import org.jclouds.compute.ComputeServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mb.learningcurve.stormdeploy.commands.Attach;
import mb.learningcurve.stormdeploy.commands.Deploy;
import mb.learningcurve.stormdeploy.commands.Kill;
import mb.learningcurve.stormdeploy.commands.ScaleOutCluster;
import mb.learningcurve.stormdeploy.userprovided.Configuration;
import mb.learningcurve.stormdeploy.userprovided.Credential;

/**
 * Main class for project
 * 
 * @author Kasper Grud Skat Madsen
 */
public class StormDeployAlternative {
	private static Logger log = LoggerFactory.getLogger(StormDeployAlternative.class);

	public static void main(String[] args) {
		if (args.length <= 1) {
			log.error("Wrong arguments provided, the following is supported:");
			log.error(" deploy CLUSTERNAME");
			log.error(" kill CLUSTERNAME");
			log.error(" attach CLUSTERNAME");
			log.error(" scaleout CLUSTERNAME #InstancesToAdd InstanceType");
			System.exit(0);
		}


		/**
		 * Parse
		 */
		String operation = args[0];
		String clustername = args[1].toLowerCase();
		Configuration config = Configuration.fromYamlFile(new File(Tools.getWorkDir() + "conf" + File.separator + "configuration.yaml"), clustername);
		Credential credentials = new Credential(new File(Tools.getWorkDir() + "conf" + File.separator + "credential.yaml"));


		/**
		 * Check configuration
		 */
		if (!config.sanityCheck()) {
			System.exit(0);
		}


		/**
		 * Check selected cloud provider is supported
		 */
		if (!Tools.getAllProviders().contains("aws-ec2")) {
			log.error("aws-ec2 not in supported list: " + Tools.getAllProviders());
			System.exit(0);
		}


		/**
		 * Check if file id_rsa and id_rsa.pub exists
		 */
		if (!new File(System.getProperty("user.home") + "/.ssh/id_rsa").exists() || !new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub").exists()) {
			log.error("Missing rsa ssh keypair. Please generate keypair, without password, by issuing: ssh-keygen -t rsa");
			System.exit(0);
		}


		/**
		 * Initialize connection to cloud provider
		 */
		ComputeServiceContext computeContext = Tools.initComputeServiceContext(config, credentials);
		log.info("Initialized cloud provider service");


		/**
		 * Execute specified operation now
		 */
		if (operation.trim().equalsIgnoreCase("deploy")) {

			Deploy.deploy(clustername, credentials, config, computeContext);

		} else if (operation.trim().equalsIgnoreCase("scaleout")) {

			try {
				int newNodes = Integer.valueOf(args[2]);
				String instanceType = args[3];
				ScaleOutCluster.AddWorkers(newNodes, clustername, instanceType, config, credentials, computeContext);
			} catch (Exception ex) {
				log.error("Error parsing arguments", ex);
				return;
			}

		} else if (operation.trim().equalsIgnoreCase("attach")) {

			Attach.attach(clustername, computeContext);

		} else if (operation.trim().equalsIgnoreCase("kill")) {

			Kill.kill(clustername, computeContext.getComputeService());

		} else {
			log.error("Unsupported operation " + operation);
		}
	}
}
