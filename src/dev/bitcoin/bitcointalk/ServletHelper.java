package dev.bitcoin.bitcointalk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.entity.mime.MultipartEntity;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;

public class ServletHelper {
	public static void addMultipartBodyToRequest(MultipartEntity entity, HTTPRequest req) throws IOException{

	    /*
	     * turn Entity to byte[] using ByteArrayOutputStream
	     */
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    entity.writeTo(bos);
	    byte[] body = bos.toByteArray();

	    /*
	     * extract multipart boundary (body starts with --boundary\r\n)
	     */
	    String boundary = new BufferedReader(new StringReader(new String(body))).readLine();
	    boundary = boundary.substring(2, boundary.length());

	    /*
	     * add multipart header and body
	     */
	    req.addHeader(new HTTPHeader("Content-type", "multipart/form-data; boundary=" + boundary));
	    req.setPayload(body);
	}
}
