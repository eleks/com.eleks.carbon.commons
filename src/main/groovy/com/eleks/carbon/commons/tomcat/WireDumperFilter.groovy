/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eleks.carbon.commons.tomcat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>Implementation of a Filter that logs interesting contents from the
 * specified Request (before processing) and the corresponding Response
 * (after processing).  It is especially useful in debugging problems
 * related to headers and cookies.</p>
 */
@groovy.transform.CompileStatic
public class WireDumperFilter implements Filter {
	protected static final int MAX_LOG_LEN = 1024*8; //bytes to log from request/response...

	/**
	 * The logger for this class.
	 */
	protected static final Logger log = Logger.getLogger(WireDumperFilter.class);


	/**
	 * Log the interesting request parameters, invoke the next Filter in the
	 * sequence, and log the interesting response parameters.
	 *
	 * @param request  The servlet request to be processed
	 * @param response The servlet response to be created
	 * @param chain    The filter chain being processed
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet error occurs
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hRequest = null;
		HttpServletResponse hResponse = null;

		if (request instanceof HttpServletRequest) {
			hRequest = (HttpServletRequest) request;
		}
		if (response instanceof HttpServletResponse) {
			hResponse = (HttpServletResponse) response;
		}
		
		if(log.isInfoEnabled() && hRequest!=null && hResponse!=null){
			//init parameters
			long ts = System.currentTimeMillis(); //request time
			
			String msgId = hRequest.getHeader("X-MessageId");
			if(msgId==null){
				msgId = hRequest.getHeader("X-Message-ID");
				if(msgId==null){
					msgId = Long.toHexString(java.util.UUID.randomUUID().getMostSignificantBits());
				}
			}
			if(msgId.length()>16)msgId.substring(0,16);
			String method = hRequest.getMethod();
			String url = hRequest.getRequestURL();
			
			String content = null;
			if(log.isDebugEnabled()){
				//wrap request/response
				hRequest = new WireHttpServletRequest(hRequest);
				hResponse= new WireHttpServletResponse(hResponse);
				content = ((WireHttpServletRequest)hRequest).getContent4Log();
			}
			StringBuilder out=new StringBuilder();
			out.append("REQ<< ").append(msgId).append(" ").append(method).append(" ").append(url);
			//add headers
			out.append(" [");
			out.append( hRequest.getHeaderNames().collect{String h-> h+"="+hRequest.getHeader(h) }.join(", ") );
			out.append( "] " );
			if(content!=null){
				out.append(" content:\n");
				out.append(content);
				content = null;
			}
			log.info(out.toString());
			out.setLength(0);
			
			// Perform the request
			chain.doFilter(hRequest, hResponse);
			
			out.append("RES>> ").append(msgId).append(" ").append(method).append(" ").append(url);
			out.append(" -> ").append(hResponse.getStatus());
			out.append(" ").append((System.currentTimeMillis()-ts)/1000).append("s");
			//add headers
			out.append(" [");
			out.append( hResponse.getHeaderNames().collect{ ""+it+"="+hResponse.getHeader(it) }.join(", ") );
			out.append( "] " );
			
			//!!!
			if(log.isDebugEnabled() && hResponse instanceof WireHttpServletResponse){
				//wrap request/response
				content = ((WireHttpServletResponse)hResponse).getContent4Log();
			}
			if(content!=null){
				out.append(" content:\n");
				out.append(content);
				content=null;
			}
			log.info(out.toString());
			out=null;
		}else{
			// Perform the plain request
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	@Override
	public void destroy() {
		// NOOP
	}

}
