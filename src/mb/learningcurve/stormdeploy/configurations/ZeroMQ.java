package mb.learningcurve.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.scriptbuilder.domain.Statement;
import mb.learningcurve.stormdeploy.Tools;

/**
 * Contains all methods to configure ZeroMQ on nodes
 * 
 * @author Kasper Grud Skat Madsen
 */
public class ZeroMQ {
	private static String _condZmq = "$(find /usr/* -name 'libzmq.a' | wc -l) -eq 0";
	private static String _condJzmq = "$(find /usr/* -name 'zmq.jar' | wc -l) -eq 0";
	
	public static List<Statement> download() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec(Tools.conditionalExec(_condZmq, "cd ~")));
		st.add(exec(Tools.conditionalExec(_condZmq, "wget http://download.zeromq.org/zeromq-2.1.7.tar.gz")));
		st.add(exec(Tools.conditionalExec(_condZmq, "tar -zxf zeromq-2.1.7.tar.gz")));
		st.add(exec(Tools.conditionalExec(_condZmq, "rm zeromq-2.1.7.tar.gz")));
		return st;
	}
	
	public static ArrayList<Statement> configure() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec(Tools.conditionalExec(_condZmq, "cd zeromq-2.1.7")));
		st.add(exec(Tools.conditionalExec(_condZmq, "./configure")));
		st.add(exec(Tools.conditionalExec(_condZmq, "make")));
		st.add(exec(Tools.conditionalExec(_condZmq, "make install")));
		st.add(exec(Tools.conditionalExec(_condZmq, "ldconfig")));
		
		st.add(exec(Tools.conditionalExec(_condJzmq, "git clone https://github.com/nathanmarz/jzmq.git")));
		st.add(exec(Tools.conditionalExec(_condJzmq, "cd jzmq")));
		st.add(exec(Tools.conditionalExec(_condJzmq, "./autogen.sh")));
		st.add(exec(Tools.conditionalExec(_condJzmq, "./configure")));
		st.add(exec(Tools.conditionalExec(_condJzmq, "make")));
		st.add(exec(Tools.conditionalExec(_condJzmq, "make install")));
		return st;
	}
}