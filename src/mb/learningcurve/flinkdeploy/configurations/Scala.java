/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mb.learningcurve.flinkdeploy.configurations;

import java.util.ArrayList;
import java.util.List;
import mb.learningcurve.flinkdeploy.userprovided.Configuration;
import org.jclouds.scriptbuilder.domain.Statement;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

/**
 *
 * @author mb
 */
public class Scala {

    public static List<Statement> configure(Configuration config, String userName) {
        ArrayList<Statement> st = new ArrayList<Statement>();
		    //Install scala 2.10.x 
        //TODO: Should be installing scala version ask by user
        String scalaVersion = config.getRawConfigValue("scala-version");
        String scalaDeb = "scala-2.10.4.deb";
        if(scalaVersion.equals("2.10")){
            scalaDeb = "scala-2.10.4.deb";
        }else if (scalaVersion.equals("2.11")){
            scalaDeb = "scala-2.11.4.deb";
        }
        st.add(exec("wget www.scala-lang.org/files/archive/"+scalaDeb));
        st.add(exec("dpkg -i "+scalaDeb));
        st.add(exec("apt-get install -f -y"));
        return st;
    }
}
