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
import javax.servlet.ServletOutputStream;
import java.nio.ByteBuffer;

/**
the wrapper class to intercept output stream.
*/
@groovy.transform.CompileStatic
class WireServletOutputStream extends ServletOutputStream{
	private byte[] intercept;
	private int interceptlen;
	@Delegate private OutputStream delegate;
	
	WireServletOutputStream(OutputStream delegate){
		this.delegate = delegate;
		this.intercept = new byte[WireDumperFilter.MAX_LOG_LEN];
		this.interceptlen=0;
	}
	
	public ByteBuffer getIntercept(){
		return ByteBuffer.wrap(intercept, 0, interceptlen);
	}
	
	private void intercept(byte[] b, int off, int len){
		if(interceptlen<intercept.length && len>0){
			int i=intercept.length - interceptlen;
			if(len<i)i=len;
			System.arraycopy(b, off, intercept, interceptlen, i);
			interceptlen+=i;
		}
	}
	
	private void intercept(byte b){
		if(interceptlen<intercept.length){
			intercept[interceptlen]=b;
			interceptlen++;
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException{
		intercept(b, 0, b.length);
		delegate.write(b);
	}
	@Override
	public void write(byte[] b,int off,int len) throws IOException{
		intercept(b, off, len);
		delegate.write(b,off,len);
	}
	
	@Override
	public void write(int b) throws IOException{
		intercept((byte)b);
		delegate.write(b);
	}
}
