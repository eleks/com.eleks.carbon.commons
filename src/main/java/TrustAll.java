package com.eleks.carbon.commons;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.ServerInitializer;
import org.wso2.carbon.utils.ServerException;

import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class TrustAll implements ServerInitializer {
	@Override
	public void init(ConfigurationContext configurationContext) throws AxisFault, ServerException {
		try{
			SSLContext sslCtx = getNaiveSSLContext("TLS");
			SSLContext.setDefault(sslCtx);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
                public boolean verify(String hostname, SSLSession session) { 
                    return true; 
                }}); 
	        System.out.println("--------------------------- TrustAll initializer started ---------------------------");
        }catch(Exception e){
        	throw new ServerException("Can't start TrustAll initializer: "+e,e);
        }
	}

    public static SSLContext getNaiveSSLContext(String protocol)throws Exception{
        System.err.println("HTTP.getNaiveSSLContext() used. Must be disabled on prod!");
        KeyManager[] kms = new KeyManager[0];
        TrustManager[] trustCerts = new TrustManager[1];                
        trustCerts[0] = new X509TrustManager() {
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) { }
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) { }
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }    
        };
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, trustCerts, new SecureRandom());
        return sslContext;
    }

}
