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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;

/**
the wrapper class to intercept input stream.
*/
@groovy.transform.CompileStatic
class WireHttpServletRequest {
	private String content4log = null;
	private ServletInputStream subst = null;
	private BufferedReader substR = null;
	private char mode = '0';
	@Delegate private HttpServletRequest delegate;
	
	WireHttpServletRequest(HttpServletRequest delegate){
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
	String getContent4Log(){
		if(content4log!=null)return content4log;
		byte []b=new byte[WireDumperFilter.MAX_LOG_LEN];
		InputStream orig = delegate.getInputStream();
		int len = orig.read(b);
		subst=new WireServletInputStream(new SequenceInputStream( new ByteArrayInputStream(b,0,len), orig ));
		content4log = new String(b,0,len, this.getCharset() );
		return content4log;
	}
	
	@Override
	ServletInputStream getInputStream() throws java.io.IOException{
		if(subst==null)throw new RuntimeException("DelegateHttpServletRequest not initialized with getContent4Log");
		if(mode=='r')throw new IllegalStateException("getReader() method has been called on this request");
		mode = 's';
		return subst;
	}
	
	@Override
	BufferedReader getReader() throws java.io.IOException{
		if(subst==null)throw new RuntimeException("DelegateHttpServletRequest not initialized with getContent4Log");
		if(mode=='s')throw new IllegalStateException("getInputStream() method has been called on this request");
		mode = 'r';
		if(substR==null)substR = new BufferedReader(new InputStreamReader(subst, this.getCharset()));
		return substR;
	}
	
}
