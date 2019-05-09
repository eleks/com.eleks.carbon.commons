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
import java.util.LinkedHashMap;
import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * simple groovy script deployer for axis2
 * by dlukyanov@ukr.net 
 */
@groovy.transform.CompileStatic
public class GroovyDeployer implements Deployer{
	static final Log log = LogFactory.getLog(GroovyDeployer.class);
	static final ConcurrentHashMap<String,DeployerScript> deployed = new ConcurrentHashMap();

	static final ExecutorService async = Executors.newFixedThreadPool(1, new TFactory());
	
	long compiledTime = -1;
	Class<DeployerScript> compiledScript = null;

	private ConfigurationContext config = null;
	private String extension=null;
	private String directory=null;

	private Map deployCtx = null;
	
	public void init(ConfigurationContext ctx){
		this.config = ctx;
	}

	public void setExtension(String extension){
		this.extension = extension;
	}

	public void setDirectory(String directory){
		this.directory = directory;
	}

	/**
	 * If `filename` as a groovy script then return it as a deployer, otherwise returns `Deployer.groovy` in the same directory as a deployer.
	 */
	protected DeployerScript compileScript(String filename)throws Exception{
		File file = file = new File(filename);
		DeployerScript script = null;
		if(filename.endsWith(".groovy")){
			//nothing to do. let's execute script as a deployer
		}else{
			file = new File(file.getParentFile(), "Deployer.groovy");
			if(compiledTime != file.lastModified())compiledScript = null;
		}

		if(compiledScript!=null){
			script = compiledScript.newInstance();
		}else{
			log.debug("compile  "+file);
			CompilerConfiguration conf = new CompilerConfiguration();
			conf.setDebug(true);
			conf.setScriptBaseClass( DeployerScript.class.getName() );

			GroovyShell shell = new GroovyShell(conf);
			script = (DeployerScript) shell.parse( file );
			if(!filename.endsWith(".groovy")){
				compiledTime = file.lastModified();
				compiledScript = (Class<DeployerScript>)script.getClass();
			}
			log.debug("compiled "+file);
		}

		Map ctx = new LinkedHashMap(7);
		ctx.put("file",new File(filename));
		ctx.put("config",this.config);

		//Map bindings = script.getBinding().getVariables();
		//bindings.clear();
		//bindings.put("ctx",ctx);
		//bindings.put("log",log);
		script.log = log;
		script.ctx = ctx;

		return script;
	}

	@Override
	public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
		try{
			String filename = deploymentFileData.getAbsolutePath();
			log.info("deploy   "+directory+"/"+new File(filename).getName());
			DeployerScript script = compileScript(filename);
			deployed.put(filename, script);
			new DeployRunner(script, "deploy").call();
			//script.invokeMethod("deploy", null);
			//log.info("deploy   "+directory+"/"+new File(filename).getName()+" SUCCESS");
		}catch(Throwable t){
			log.error(t,t);
			throw new DeploymentException("deploy failed: "+t, t);
		}
	}

	@Override
	public void undeploy(String filename) throws DeploymentException {
		try{
			log.info("undeploy "+directory+"/"+new File(filename).getName());
			DeployerScript script = deployed.remove(filename);
			if(script==null){
				log.warn("no deployed script for: "+filename);
			}else{
				new DeployRunner(script, "undeploy").call();
				//script.invokeMethod("undeploy", null);
				//log.info("undeploy "+directory+"/"+new File(filename).getName()+" SUCCESS");
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
	/*
	public static abstract class DeployerScript extends Script{
		Log log;
		Map<String,Object> ctx;
		boolean isAsync(){
			return false;
		}
	}
	*/
	static class TFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, GroovyDeployer.class.getName());
			t.setDaemon(true);
			log.debug("deploy thread created "+t);
			return t;
		}
	}

	static class DeployRunner implements Runnable{
		private DeployerScript script;
		private String         action;
		private String         tenantDomain = null;
		private long           delay = 0;
		private String         actionFmt;

		DeployRunner(DeployerScript script, String action){
			this.script=script;
			this.action=action;
			this.actionFmt = String.format("%-9s",action);
		}
		public void run(){
			try{
				File file = (File)script.ctx.get("file");
				if(delay>0){
					PrivilegedCarbonContext.startTenantFlow();
					PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain, true);
					Thread.sleep(delay);
				}
				script.invokeMethod(action, null);
				log.info(actionFmt+file.getParentFile().getName()+"/"+file.getName()+" SUCCESS");
			}catch(Throwable t){
				log.error(actionFmt+"failed: "+t,t);
			}finally{
				try{
					if(delay>0){
						PrivilegedCarbonContext.endTenantFlow();
					}
				}catch(Throwable ei){}
			}
		}
		public void call(){
			if(script.isAsync()){
				this.delay=123;
				this.tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
				File file = (File)script.ctx.get("file");
				log.info( actionFmt + file.getParentFile().getName() + "/" + file.getName() + " async << domain=" + this.tenantDomain );
				async.execute(this);
			}else{
				run();
			}
		}
	}
}

