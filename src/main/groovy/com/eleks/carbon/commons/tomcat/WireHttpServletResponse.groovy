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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

/**
the wrapper class to intercept input stream.
*/
@groovy.transform.CompileStatic
class WireHttpServletResponse {
	private String content4log = null;
	private WireServletOutputStream subst = null;
	private PrintWriter substW = null;
	private char mode = '0';
	@Delegate private HttpServletResponse delegate;
	
	WireHttpServletResponse(HttpServletResponse delegate){
		this.delegate = delegate;
	}
	
	private Charset getCharset(){
		try{
			return Charset.forName(delegate.getCharacterEncoding());
		}catch(Throwable t){
			WireDumperFilter.log.warn(t.toString());
			return java.nio.charset.StandardCharsets.UTF_8;
		}
	}
	//assume this method called after target servlet started writing.
	String getContent4Log(){
		if(content4log!=null)return content4log;
		if(subst==null)return null;
		content4log = getCharset().decode(subst.getIntercept()).toString();
		return content4log;
	}
	
	@Override
	ServletOutputStream getOutputStream() throws java.io.IOException{
		if(mode=='w')throw new IllegalStateException("getWriter() method has been called on this response");
		mode = 's';
		if(subst!=null)return subst;
		ServletOutputStream orig = delegate.getOutputStream();
		//could it be null???
		if(orig==null)throw new IOException("Can't obtain output stream: null"); 
		subst = new WireServletOutputStream(orig);
		return subst;
	}
	
	@Override
	PrintWriter getWriter() throws java.io.IOException{
		if(mode=='s')throw new IllegalStateException("getOutputStream() method has been called on this request");
		if(substW!=null)return substW;
		if(subst==null)this.getOutputStream();  //init subst
		mode = 'w';
		substW = new PrintWriter(new OutputStreamWriter(subst, this.getCharset()), true);
		return substW;
	}
	
}
