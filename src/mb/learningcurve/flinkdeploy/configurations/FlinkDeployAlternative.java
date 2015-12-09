package mb.learningcurve.flinkdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.scriptbuilder.domain.Statement;
import mb.learningcurve.flinkdeploy.Tools;

/**
 * Contains all methods to configure SnormDeployAlternative on remote node
 * 
 * @author Kasper Grud Skat Madsen
 */
public class FlinkDeployAlternative {

        // TODO Change this to do flink things
	public static List<Statement> download() {
		return Tools.download("~", "https://www.dropbox.com/s/lk61j2fwmbyo1xu/fdeploy.tar.gz?dl=1", true, true,"fdeploy.tar.gz");
	}
	
	/**
	 * Run memoryMonitor.
	 * 	Requires tools.jar from active jvm is on path. Is automatically searched and found if it exists in /usr/lib/jvm
	 */
	public static List<Statement> runMemoryMonitor(String username) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("su -c 'java -cp \"/home/"+username+"/fdeploy/flink-deploy-1.jar:$( find `ls -d /usr/lib/jvm/* | sort -k1 -r` -name tools.jar | head -1 )\" mb.learningcurve.flinkdeploy.image.MemoryMonitor &' - " + username));
		return st;
	}
	
	public static List<Statement> writeConfigurationFiles(String localConfigurationFile, String localCredentialFile) {	
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir ~/fdeploy/conf"));
                //TODO: Should this be configuration.yaml???
		st.addAll(Tools.echoFile(localConfigurationFile, "~/fdeploy/conf/configuration.yaml"));
		st.addAll(Tools.echoFile(localCredentialFile, "~/fdeploy/conf/credential.yaml"));
		return st;
	}
	
	public static List<Statement> writeLocalSSHKeys() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir ~/.ssh/"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + "id_rsa", "~/.ssh/id_rsa"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + "id_rsa.pub", "~/.ssh/id_rsa.pub"));
		return st;
	}
}