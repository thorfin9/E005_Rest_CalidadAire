package com.ipartek.auxiliar;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class ClienteHttp {

	
	public static RestClient crear() {
		try {
			TrustManager[] confiarEnTodo = { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] c, String a) {
				}

				public void checkServerTrusted(X509Certificate[] c, String a) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} };
			SSLContext ssl = SSLContext.getInstance("TLS");
			ssl.init(null, confiarEnTodo, new SecureRandom());

			HttpClient http = HttpClient.newBuilder().sslContext(ssl).build();
			return RestClient.builder().requestFactory(new JdkClientHttpRequestFactory(http)).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}