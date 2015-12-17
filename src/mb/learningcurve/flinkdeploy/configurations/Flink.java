package mb.learningcurve.flinkdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jclouds.scriptbuilder.domain.Statement;

import mb.learningcurve.flinkdeploy.Tools;


/**
 * Contains all methods to configure Flink on nodes
 * 
 * @author MB (Code adapted from Storm deploy tool written by Kasper Grud Skat Madsen)
 */
public class Flink {

	public static List<Statement> download(String flinkRemoteLocation) {
            return Tools.download("~/", flinkRemoteLocation, true, true, "flink", "flink.tar.gz");
	}
	
	/**
	 * Write flink/conf/flink-conf.yaml (basic settings only)
	 */
	public static List<Statement> configure(String jobManagerHostName, List<String> taskManagerHostNames, String userName) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~/flink/conf/"));
                
		//st.add(exec("touch flink-conf.yaml"));
		//Add job manager rpc address
                st.add(exec("sed -i \"s/jobmanager.rpc.address: .*/jobmanager.rpc.address: "+jobManagerHostName+"/g\" flink-conf.yaml"));
		
                st.add(exec("rm -rf slaves"));
                st.add(exec("touch slaves"));
                for (int i = 1; i <= taskManagerHostNames.size(); i++)
			st.add(exec("echo \"" + taskManagerHostNames.get(i-1) + "\" >> slaves"));
		
		// Change owner of flink directory
		st.add(exec("chown -R " + userName + ":" + userName + " ~/flink"));
		
		// Add flink to execution PATH
		st.add(exec("echo \"export PATH=\\\"\\$HOME/flink/bin:\\$PATH\\\"\" >> ~/.bashrc"));
                
                //Export Java home
                //st.add(exec("echo \"export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/\" >> ~/.bashrc"));
                
		return st;
	}

        /**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startJobManagerDaemonSupervision(String username) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~"));
		st.add(exec("su -c 'case $(head -n 1 ~/daemons) in *MASTER*) java -cp ~/fdeploy/flink-deploy-1.jar mb.learningcurve.flinkdeploy.image.ProcessMonitor org.apache.flink.runtime.jobmanager.JobManager ~/flink/bin/jobmanager.sh start cluster batch ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startTaskManagerDaemonSupervision(String username) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~"));
		st.add(exec("su -c 'case $(head -n 1 ~/daemons) in *WORKER*) java -cp ~/fdeploy/flink-deploy-1.jar mb.learningcurve.flinkdeploy.image.ProcessMonitor org.apache.flink.runtime.taskmanager.TaskManager ~/flink/bin/taskmanager.sh start batch ;; esac &' - " + username));
		return st;
	}
	
	/**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startUIDaemonSupervision(String username) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~"));
		st.add(exec("su -c 'case $(head -n 1 ~/daemons) in *UI*) java -cp ~/fdeploy/flink-deploy-1.jar mb.learningcurve.flinkdeploy.image.ProcessMonitor org.apache.flink.client.WebFrontend ~/flink/bin/start-webclient.sh ;; esac &' - " + username));
		return st;
	}
        
        //TODO: Write the configuration files to the client
}
