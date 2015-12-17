/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mb.learningcurve.flinkdeploy.configurations;

import java.util.ArrayList;
import java.util.List;
import mb.learningcurve.flinkdeploy.Tools;
import mb.learningcurve.flinkdeploy.userprovided.Configuration;
import org.jclouds.scriptbuilder.domain.Statement;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

/**
 *
 * @author mb
 */
public class Hadoop {
    
    public static List<Statement> download(String hadoopRemoteLocation) {
        return Tools.download("~/", hadoopRemoteLocation, true, true, "hadoop", "hadoop.tar.gz");
    }
    
    public static List<Statement> configure(Configuration config, String nameNode, List<String> dataNodes, String userName) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~/hadoop/etc/hadoop/"));
                
		//st.add(exec("touch flink-conf.yaml"));
		//Add job manager rpc address
                //st.add(exec("sed -i \"s/jobmanager.rpc.address: .*/jobmanager.rpc.address: "+nameNode+"/g\" flink-conf.yaml"));
		
                //TODO: List of master nodes? For secondary namenode?
                st.add(exec("touch masters"));
                st.add(exec("echo \"" + nameNode + "\" >> masters"));
                
                st.add(exec("rm -rf slaves"));
                st.add(exec("touch slaves"));

                //TODO: Does it make sense to add namenode automatically as datanode or should we leave it to user?
                st.add(exec("echo \"" + nameNode + "\" >> slaves"));
                for (int i = 1; i <= dataNodes.size(); i++)
			st.add(exec("echo \"" + dataNodes.get(i-1) + "\" >> slaves"));
		
                //Configuration files
                st.add(exec("rm -rf core-site.xml"));
                st.add(exec("touch core-site.xml"));
                st.add(exec("echo \"<?xml version=\\\"1.0\\\"?>\n"+
                    "  <configuration>\n" +
                    "  <property>\n" +
                    "  <name>fs.default.name</name>\n" +
                    "  <value>hdfs://"+nameNode+":54310</value>\n" +
                    "  <description>The name of the default file system.  A URI whose\n" +
                    "  scheme and authority determine the FileSystem implementation.  The\n" +
                    "  uri's scheme determines the config property (fs.SCHEME.impl) naming\n" +
                    "  the FileSystem implementation class.  The uri's authority is used to\n" +
                    "  determine the host, port, etc. for a filesystem.</description>\n" +
                    "</property>"+
                        "</configuration>"+
                        "\" >> core-site.xml"));
                
                st.add(exec("rm -rf hdfs-site.xml"));
                st.add(exec("touch hdfs-site.xml"));
                String replication = "";
                if(config.getRawConfigValue("replication")==null){
                    replication = "1";
                }else{
                    replication = config.getRawConfigValue("replication");
                }
                //TODO: Change namenode IP to public?
                st.add(exec("echo \"<?xml version=\\\"1.0\\\"?>\n"+
                    "  <configuration>\n" +
                    "  <property>\n" +
                    "  <name>dfs.replication</name>\n" +
                    "  <value>"+ replication+"</value>\n" +
                    "  <description>Default block replication.\n" +
                    "  The actual number of replications can be specified when the file is created.\n" +
                    "  The default is used if replication is not specified in create time.\n" +
                    "  </description>\n" +
                    "</property>"+
                        "<property>\n" +
                    "<name>dfs.namenode.http-address</name>\n" +
                    "<value>"+nameNode+":50070</value>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "    <name>fs.default.name</name>\n" +
                    "    <value>hdfs://"+nameNode+":50040/</value>\n" +
                    "  </property>\n" +
                    "  <property>\n" +
                    "    <name>dfs.data.dir</name>\n" +
                    "    <value>/home/ubuntu/hdfs-data</value>\n" +
                    "  </property>"+
                        "</configuration>"+
                        "\" >> hdfs-site.xml"));
                
                //Add JAVA_HOME to hadoop-env.sh
                st.add(exec("sed -i \"s/export JAVA_HOME=.*/export JAVA_HOME=\\/usr\\/lib\\/jvm\\/java-7-openjdk-amd64/g\" hadoop-env.sh"));
                
		// Change owner of hadoop directory
		st.add(exec("chown -R " + userName + ":" + userName + " ~/hadoop"));
		
		// Add hadoop to execution PATH
		st.add(exec("echo \"export PATH=\\\"\\$HOME/hadoop/bin:\\$PATH\\\"\" >> ~/.bashrc"));
                
                //Adding known hosts
                st.add(exec("ssh-keyscan 127.0.0.1 >> ~/.ssh/known_hosts"));
                st.add(exec("ssh-keyscan 0.0.0.0 >> ~/.ssh/known_hosts"));
                st.add(exec("ssh-keyscan " + nameNode + " >> ~/.ssh/known_hosts"));
                for (int i = 1; i <= dataNodes.size(); i++)
			st.add(exec("ssh-keyscan " + dataNodes.get(i-1) + " >> ~/.ssh/known_hosts"));
                
                st.add(exec("chmod 600 ~/.ssh/id_rsa"));
                st.add(exec("chown -R " + userName + ":" + userName + " ~/.ssh"));
                
		return st;
	}
    
        /**
	 * Uses Monitor to restart daemon, if it stops
	 */
	public static List<Statement> startHDFSDaemonSupervision(String username) {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("cd ~"));
                st.add(exec("su -c 'case $(head -n 1 ~/daemons) in *MASTER*) ~/hadoop/bin/hdfs namenode -format' - " + username));
		st.add(exec("su -c 'case $(head -n 1 ~/daemons) in *MASTER*) java -cp ~/fdeploy/flink-deploy-1.jar mb.learningcurve.flinkdeploy.image.ProcessMonitor org.apache.flink.runtime.taskmanager.TaskManager ~/hadoop/sbin/start-dfs.sh ;; esac &' - " + username));
		return st;
	}
}
