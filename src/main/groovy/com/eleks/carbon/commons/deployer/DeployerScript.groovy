/*-----------------------------------*/
package com.eleks.carbon.commons.deployer;
import org.apache.commons.logging.Log;

/**
 * base class for deploy scripts to be able to use CompileStatic
 */
@groovy.transform.CompileStatic
public abstract class DeployerScript extends Script{
	public Log log;
	public Map<String,Object> ctx;
	public boolean isAsync(){ return false; }
}
