/*-----------------------------------*/
package com.eleks.carbon.commons.deployer;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.context.ConfigurationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * simple groovy script deployer for axis2
 * by dlukyanov@ukr.net 
 */
@groovy.transform.CompileStatic
public class GroovyDeployer implements Deployer{
	static final Log log = LogFactory.getLog(GroovyDeployer.class);
	static final ConcurrentHashMap<String,Script> deployed = new ConcurrentHashMap();

	private ConfigurationContext config = null;
	private String extension=null;
	private String directory=null;
	
	public void init(ConfigurationContext ctx){
		this.config = ctx;
	}

	public void setExtension(String extension){
		this.extension = extension;
	}

	public void setDirectory(String directory){
		this.directory = directory;
	}

	private Map buildCtx(String fileName){
		Map ctx = new HashMap(5);
		ctx.put("log",log);
		ctx.put("file",new File(fileName));
		ctx.put("config",this.config);
		return ctx;
	}


	private Script compileScript(String scriptFile)throws Exception{
		CompilerConfiguration conf = new CompilerConfiguration();
		conf.setDebug(true);
		GroovyShell shell = new GroovyShell(conf);
		Script script = shell.parse( new File(scriptFile) );
		Map bindings = script.getBinding().getVariables();
		bindings.clear();
		bindings.put("log",log);
		return script;
	}


	public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
		try{
			String filename = deploymentFileData.getAbsolutePath();
			log.info("deploy "+filename);
			Script script = compileScript(filename);
			deployed.put(filename, script);
			script.invokeMethod("deploy", buildCtx(filename));
		}catch(Throwable t){
			log.error(t,t);
			throw new DeploymentException("deploy failed: "+t, t);
		}
	}

	public void undeploy(String filename) throws DeploymentException {
		try{
			log.info("undeploy "+filename);
			Script script = deployed.remove(filename);
			if(script==null){
				log.warn("no deployed script for: "+filename);
			}else{
				script.invokeMethod("undeploy", buildCtx(filename));
			}
		}catch(Throwable t){
			log.error(t,t);
			throw new DeploymentException("undeploy failed: "+t, t);
		}
	}

	public void cleanup() throws DeploymentException {
		// Deployers which require cleaning up should override this method
		//do we need this?
	}
}

