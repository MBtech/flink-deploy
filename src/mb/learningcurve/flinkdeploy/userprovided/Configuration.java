package mb.learningcurve.flinkdeploy.userprovided;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mb.learningcurve.flinkdeploy.Tools;

/**
 * Class used to store all information for configuration.yaml, specific to cluster to deploy
 * 
 * @author MB (Code adapted from Storm deploy tool written by Kasper Grud Skat Madsen)
 */
public class Configuration {
	private static Logger log = LoggerFactory.getLogger(Configuration.class);
	HashMap<Integer, String> _nodeIdToInstanceTypeID = null;
	private String _imageID = null, _locationID = null;
	private ArrayList<String> _conf;
	
	private HashSet<String> _allConfigurationSettings = new HashSet<String>(Arrays.asList(
			"flink-version", 
			"scala-version",
                        "hadoop-version",
			"image","image-username",
			"region",
			"memory-monitor",
			"remote-exec-preconfig",
			"remote-exec-postconfig"));
	
	public static Configuration fromYamlFile(File f, String clustername) {
		return new Configuration(Tools.readYamlConf(f), clustername);
	}
	
	@SuppressWarnings("unchecked")
	public Configuration(HashMap<String, Object> conf, String clustername) {
		_conf = (ArrayList<String>) conf.get(clustername);
	}
	
	/**
	 * Returns true if no problems with configuration.
	 * Otherwise error message and false
	 */
	public boolean sanityCheck() {
		if (_conf == null) {
			log.error("Clustername not found in configuration.yaml");
			return false;
		}
				
		return true;
	}
	
	/**
	 * Get exec (pre config)
	 */
	public ArrayList<String> getRemoteExecPreConfig() {
		ArrayList<String> execPreConfig = new ArrayList<String>();
		for (int i = 0; i < _conf.size(); i++) {
			String key = _conf.get(i).substring(0, _conf.get(i).indexOf(" "));
			if (key.equals("remote-exec-preconfig")) {
				for (String command : _conf.get(i).substring(_conf.get(i).indexOf("{") + 1, _conf.get(i).indexOf("}")).split(","))
					execPreConfig.add(command.trim());
			}
		}
		return execPreConfig;
	}
	
	/**
	 * Get exec (post config)
	 */
	public ArrayList<String> getRemoteExecPostConfig() {
		ArrayList<String> execPostConfig = new ArrayList<String>();
		for (int i = 0; i < _conf.size(); i++) {
			String key = _conf.get(i).substring(0, _conf.get(i).indexOf(" "));
			if (key.equals("remote-exec-postconfig")) {
				for (String command : _conf.get(i).substring(_conf.get(i).indexOf("{") + 1, _conf.get(i).indexOf("}")).split(","))
					execPostConfig.add(command.trim());
			}
		}
		return execPostConfig;
	}
	
	/**
	 * Get user for logging on the image after boot
	 */
	public String getImageUsername() {
		String imageUsername = getRawConfigValue("image-username");
		
		// If no username is specifed, assume "ubuntu"
		if (imageUsername == null)
			return "ubuntu";
		
		return imageUsername;
	}
	
        //TODO: Set a default region
	/**
	 * Get region
	 */
	public String getDeploymentLocation() {
		if (_locationID != null) {
			return _locationID;
		}
		return getRawConfigValue("region");
	}
	
	/**
	 * Get whether memory monitor should be executed or not
	 * Default = false
	 */
	public boolean executeMemoryMonitor() {
		String memoryMonitor = getRawConfigValue("memory-monitor");
		if (memoryMonitor != null && memoryMonitor.trim().equalsIgnoreCase("true"))
			return true;
		return false;
	}
	
	/**
	 * Get image
	 */
	public String getDeploymentImage() {
		if (_imageID != null) {
			return _imageID;
		}
		return getRawConfigValue("image");
	}
	
	/**
	 * Get remote zk-location, based on requested version
	 */
        /*
	public String getZKLocation() {
		String version = getRawConfigValue("zk-version");
		if (version.equals("3.4.5")) {
			return "https://s3-eu-west-1.amazonaws.com/zk-releases/zookeeper-3.4.5.tar.gz";
		} else if (version.equals("3.4.6")) {
			return "https://s3-eu-west-1.amazonaws.com/zk-releases/zookeeper-3.4.6.tar.gz";
		} else {
			log.info("Zookeeper version not currently supported!");
		}
		return null;
	}*/
	
	/**
	 * Get remote location of Flink, based on requested versions of flink, hadoop and scala
	 */
	public String getFlinkRemoteLocation() {
		String flinkVersion = getRawConfigValue("flink-version");
                String scalaVersion = getRawConfigValue("scala-version");
                String hadoopVersion = getRawConfigValue("hadoop-version");
                String downloadURL = "http://www.us.apache.org/dist/flink/flink-" + flinkVersion + "/flink-" + flinkVersion + "-bin-";
                if (scalaVersion != null) {
                if (hadoopVersion.equals("1.2.1")) {
                    return downloadURL + "hadoop1-scala_" + scalaVersion + ".tgz";
                } else if (hadoopVersion.equals("2.3.0")) {
                    return downloadURL + "hadoop2-scala_" + scalaVersion + ".tgz";
                } else if (hadoopVersion.equals("2.4.1")) {
                    return downloadURL + "hadoop24-scala_" + scalaVersion + ".tgz";
                } else if (hadoopVersion.equals("2.6.0")) {
                    return downloadURL + "hadoop26-scala_" + scalaVersion + ".tgz";
                } else if (hadoopVersion.equals("2.7.0")) {
                    return downloadURL + "hadoop27-scala_" + scalaVersion + ".tgz";
                } else {
                    log.info("Hadoop version " + hadoopVersion + " not currently supported!");
                }
                }else{
                    if (hadoopVersion.equals("1.2.1")) {
                    return downloadURL + "hadoop1.tgz";
                } else if (hadoopVersion.equals("2.3.0")) {
                    return downloadURL + "hadoop2.tgz";
                } else if (hadoopVersion.equals("2.4.1")) {
                    return downloadURL + "hadoop24.tgz";
                } else if (hadoopVersion.equals("2.6.0")) {
                    return downloadURL + "hadoop26.tgz";
                } else if (hadoopVersion.equals("2.7.0")) {
                    return downloadURL + "hadoop27.tgz";
                } else {
                    log.info("Hadoop version " + hadoopVersion + " not currently supported!");
                }
                }
		
		return null;
	}
	
	private String getRawConfigValue(String k) {
		for (int i = 0; i < _conf.size(); i++) {
			String key = _conf.get(i).substring(0, _conf.get(i).indexOf(" "));
			if (k.equals(key))
				return _conf.get(i).substring(_conf.get(i).indexOf(" ")).replaceAll("\"", "").toLowerCase().trim();
		}
		return null;
	}
	
	/**
	 * Get map{node id, instanceType}
	 */
	public HashMap<Integer, String> getNodeIdToInstanceType() {
		if (_nodeIdToInstanceTypeID != null) {
			return _nodeIdToInstanceTypeID;
		}
		
		// Create nodeid to instancetype
		HashMap<Integer, String> nodeIdToInstanceTypeID = new HashMap<Integer, String>();
		for (int nodeId = 0; nodeId < _conf.size(); nodeId++) {
			if (_allConfigurationSettings.contains(_conf.get(nodeId).substring(0, _conf.get(nodeId).indexOf(" "))))
				continue;

			String instance = _conf.get(nodeId).substring(0, _conf.get(nodeId).indexOf(" "));
			nodeIdToInstanceTypeID.put(nodeId, instance);
		}
		return nodeIdToInstanceTypeID;
	}
	
	//TODO: Not being used (it was in Deploy.java)
	/**
	 * Get map{node id, zkid}
	 */
	public HashMap<Integer, Integer> getNodeIdToZkId() {
		int zkid = 1;
		HashMap<Integer, Integer> nodeIdToZkId = new HashMap<Integer, Integer>();
		for (int nodeId = 0; nodeId < _conf.size(); nodeId++) {
			if (_allConfigurationSettings.contains(_conf.get(nodeId).substring(0, _conf.get(nodeId).indexOf(" "))))
				continue;
			
			String daeamons = _conf.get(nodeId).substring(_conf.get(nodeId).indexOf("{") + 1, _conf.get(nodeId).indexOf("}"));
			if (daeamons.contains("ZK"))
				nodeIdToZkId.put(nodeId, zkid++);
		}
		return nodeIdToZkId;
	}
	
	
	/**
	 * Get map{arr[daemons], arr[node ids]}
	 */
	public HashMap<ArrayList<String>, ArrayList<Integer>> getDaemonsToNodeIds() {
		HashMap<String, ArrayList<Integer>> deamonsToNodeIds = new HashMap<String, ArrayList<Integer>>();
		for (int i = 0; i < _conf.size(); i++) {
			if (_allConfigurationSettings.contains(_conf.get(i).substring(0, _conf.get(i).indexOf(" "))))
				continue;
			
			String deamons = _conf.get(i).substring(_conf.get(i).indexOf("{") + 1, _conf.get(i).indexOf("}"));
			if (!deamonsToNodeIds.containsKey(deamons))
				deamonsToNodeIds.put(deamons, new ArrayList<Integer>());
			deamonsToNodeIds.get(deamons).add(i);
		}
		
		// Convert to arr[daemons], arr[node ids]
		HashMap<ArrayList<String>, ArrayList<Integer>> ret = new HashMap<ArrayList<String>, ArrayList<Integer>>();
		for (Entry<String, ArrayList<Integer>> e : deamonsToNodeIds.entrySet()) {
			ArrayList<String> daemons = new ArrayList<String>();
			for (String daemon : e.getKey().split(","))
				daemons.add(daemon.trim());
			ret.put(daemons, e.getValue());
		}
		return ret;
	}
}
