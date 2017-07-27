/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package com.eleks.carbon.commons.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.concurrent.TimeUnit;

/**
 * carbon helper 
 */
@groovy.transform.CompileStatic
public class CarbonUtil{
	static final Log log = LogFactory.getLog(CarbonUtil.class);
	private static boolean serverStarted=false;
	private static long    STARTDETECTDELAY=7373;
	private static java.util.concurrent.LinkedBlockingQueue<Runnable> startRunners= new java.util.concurrent.LinkedBlockingQueue();
	
	static{
		init();
	}
	
	
	private static void init(){
        Thread t = new Thread() {
        	boolean startDetected=false;
            @Override
            public void run() {
                log.debug("carbon on-start listener initialized");
                while (true) {
                	if(isServerStarted()){
                		if(!startDetected){
							log.debug("carbon start detected");
                			startDetected=true;
                		}
						try {
							log.debug("poll for the next runnable...");
							Runnable r = startRunners.poll(STARTDETECTDELAY, TimeUnit.MILLISECONDS);
							if(r==null){
								log.debug("no runners in on-start queue. finalize on-start listener...");
								return;
							}
							log.debug("runner: "+r);
							r.run();
						} catch (Exception ex) {
							log.error("Error running onServerStart: "+ex,ex);
						}
					}else{
						log.debug("server not started yet");
						try{
							this.sleep(STARTDETECTDELAY);
						}catch(Exception ei){}
					}
                }
            }
        };
        t.setDaemon(true);
        t.start();
	}



	public static String getCarbonHome(){
		return System.getProperty("carbon.home");
	}
	
	public static boolean isServerStarted(){
		if(serverStarted)return true;
		try{
			String s;
			s=System.getProperty("wso2carbon.start.time");
			if(s==null)return false;
			
			s=System.getProperty("setup");
			if(s!=null)return false;
			
			s=org.wso2.carbon.core.ServerStatus.getCurrentStatus();
			if(!"RUNNING".equals(s))return false;

			serverStarted=true;
		}catch(Throwable e){
			log.info(e.toString());
		}
		return serverStarted;
	}

	//executes code when server starts or right now if it's already started
	public static void onServerStart(Runnable r){
		if(isServerStarted()){
			r.run();
		}else{
			startRunners.add(r);
		}
	}
}
