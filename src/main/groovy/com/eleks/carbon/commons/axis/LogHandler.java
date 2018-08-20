package com.eleks.carbon.commons.axis;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import javax.xml.namespace.QName;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.WSSecurityEngineResult;

import java.util.List;
import java.util.Vector;

import java.net.UnknownHostException;
import java.net.InetAddress;

import org.apache.axiom.soap.SOAPBody;

/*
import org.apache.axis2.context.*;
import org.apache.ws.security.*;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.transport.http.HTTPConstants;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.util.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.SynapseConstants;
import org.apache.axis2.description.Parameter;

import java.net.UnknownHostException;
import java.io.Serializable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Set;
import java.util.Map;
import org.apache.synapse.commons.datasource.DataSourceHelper;
import org.apache.synapse.commons.datasource.RepositoryBasedDataSourceFinder;
import java.sql.*;
import org.apache.axiom.om.xpath.AXIOMXPath;
import static org.apache.axis2.transport.TransportListener.HOST_ADDRESS;
*/


public class LogHandler extends AbstractHandler {
	private static final Logger log    = Logger.getLogger(LogHandler.class);
	private static String[] FLOW_NAMES = {null, "IN", "OUT", "IN-FAULT", "OUT-FAULT" };
	
	private static final String NS_WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final QName  QN_SECURITY =  new QName(NS_WSSE, "Security");
	private static final QName  QN_UTOKEN   =  new QName(NS_WSSE, "UsernameToken");
	private static final QName  QN_USERNAME =  new QName(NS_WSSE, "Username");
	private static String HOST_NAME;
	
	private static java.lang.reflect.Method esb48_RelayUtils_buildMessage=null;
	
	
	static {
		try {
			HOST_NAME = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			HOST_NAME = "-";
		}
		
		try {
			Class c=Class.forName("org.apache.synapse.transport.passthru.util.RelayUtils");
			esb48_RelayUtils_buildMessage = c.getMethod("buildMessage", MessageContext.class);
		}catch (Throwable t){}
	}
	

	public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
		int flow = 0;
		
		String flowName = null;
		String serviceName = null;
		String userName = null;
		String methodName = null;
		String msgId = null;
		String ip = null;
		long time = 0;
		
		flow = msgContext.getFLOW();
		if(flow>0 && flow<FLOW_NAMES.length)flowName = FLOW_NAMES[flow];
		
		try {
			serviceName=msgContext.getAxisService().getName();
		}catch(Exception e){}
		
		if(flow==msgContext.IN_FLOW){
			try {
				userName = getUserNameUT(msgContext);
			}catch(Exception e){}
		
			try {
				if(userName==null){
					userName = getUserName(msgContext);
				}
			}catch(Exception e){}
		}

		try {
			methodName = msgContext.getAxisOperation().getName().getLocalPart();
		}catch(Exception e){}
		
		if( methodName==null || methodName.length()==0 || "mediate".equals(methodName) ){
			try{
				methodName = getBody(msgContext).getFirstElement().getLocalName();
				if(methodName.endsWith("Request")){
					methodName = methodName.substring(0, methodName.length() - 7);
				}
			}catch(Exception e){}
		}
		if(msgId==null){ //???
			msgId="ctx"+Long.toHexString(java.util.UUID.randomUUID().getMostSignificantBits());
		}
		
		if(ip==null){
			try{
				ip = (String) msgContext.getProperty("REMOTE_ADDR");
			}catch(Exception e){}
		}
		
		time=System.currentTimeMillis();
			
		return InvocationResponse.CONTINUE;
	}
	private String getUserNameUT(MessageContext msgCtx){
		return msgCtx.getEnvelope().getHeader()
					.getFirstChildWithName(QN_SECURITY)
						.getFirstChildWithName(QN_UTOKEN)
							.getFirstChildWithName(QN_USERNAME).getText();
	}
	private String getUserName(MessageContext msgCtx){
		String user = null;
		List results = null;
		if ((results = (List)msgCtx.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
			throw new RuntimeException("No security results!!");
		} else {
			for (int i = 0; i < results.size(); i++) {
				//Get hold of the WSHandlerResult instance
				WSHandlerResult rResult = (WSHandlerResult) results.get(i);
				List<WSSecurityEngineResult> wsSecEngineResults = rResult.getResults();

				for (int j = 0; j < wsSecEngineResults.size(); j++) {
					//Get hold of the WSSecurityEngineResult instance
					WSSecurityEngineResult wser = wsSecEngineResults.get(j);
					user = wser.getPrincipal().getName();
				}
			}
		}
		return user;	
	}
	private static SOAPBody getBody(MessageContext msgContext){
		if(esb48_RelayUtils_buildMessage!=null){
			//force build message for esb 4.8+
			try {
				esb48_RelayUtils_buildMessage.invoke(null,msgContext);
			}catch(Throwable e){
				log.error(e,e);
			}
		}
		return msgContext.getEnvelope().getBody();
	}
}
