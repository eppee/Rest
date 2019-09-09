package se.perkodar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient
{
	private List<Pair> params;
	private List<Pair> headers;

	private String url;
	private Object post;

	private int responseCode;
	private String message;

	private String response;
	private Header[] responseHeaders;

	public String getResponse()
	{
		return response;
	}

	public String getErrorMessage()
	{
		return message;
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public RestClient(String url) {
		this.url = url;
		params = new ArrayList<Pair>();
		headers = new ArrayList<Pair>();
	}

	public void addParam(String name, String value)
	{
		params.add(new Pair(name, value));
	}

	public void addHeader(String name, String value)
	{
		headers.add(new Pair(name, value));
	}

	public void execute(RequestMethod method) throws Exception
	{
		switch (method)
		{
		case GET:
		{
			// add parameters
			String combinedParams = "";
			if (!params.isEmpty())
			{
				combinedParams += "";
				for (Pair p : params)
				{
					String paramString = p.getName() + "" + URLEncoder.encode(p.getValue(),"UTF-8");
					if (combinedParams.length() > 1)
					{
						combinedParams += "&" + paramString;
					}
					else
					{
						combinedParams += paramString;
					}
				}
			}

			HttpGet request = new HttpGet(url + combinedParams);

			// add headers
			for (Pair h : headers)
			{
				request.addHeader(h.getName(), h.getValue());
			}

			executeRequest(request, url);
			break;
		}
		case POST:
		case PUT:
		case DELETE:
		{
			HttpPost request = new HttpPost(url);

			// add headers
			for (Pair h : headers)
			{
				request.addHeader(h.getName(), h.getValue());
			}

			if (post != null)
			{
				request.setEntity(new StringEntity(getPostAsString(),"UTF-8"));
			}

			executeRequest(request, url);
			break;
		}
		}
	}

	public String getPostAsString() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(post);
	}

	private void executeRequest(HttpUriRequest request, String url) throws Exception
	{

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,15000);
		HttpConnectionParams.setSoTimeout(httpParameters, 15000);
		HttpClient client = new DefaultHttpClient(httpParameters);

		HttpResponse httpResponse;

		httpResponse = client.execute(request);
		responseCode = httpResponse.getStatusLine().getStatusCode();
		message = httpResponse.getStatusLine().getReasonPhrase();

		HttpEntity entity = httpResponse.getEntity();
		setResponseHeaders(httpResponse.getAllHeaders());

		if (entity != null)
		{

			InputStream instream = entity.getContent();
			response = convertStreamToString(instream);

			instream.close();
		}


	}

	private static String convertStreamToString(InputStream is)
	{

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	public InputStream getInputStream(){
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,15000);
		HttpConnectionParams.setSoTimeout(httpParameters, 15000);
		HttpClient client = new DefaultHttpClient(httpParameters);

		HttpResponse httpResponse;

		try
		{

			HttpPost request = new HttpPost(url);

			httpResponse = client.execute(request);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null)
			{

				InputStream instream = entity.getContent();
				return instream;
			}

		}
		catch (ClientProtocolException e)
		{
			client.getConnectionManager().shutdown();
			e.printStackTrace();
		}
		catch (IOException e)
		{
			client.getConnectionManager().shutdown();
			e.printStackTrace();
		}
		return null;
	}
	public Object getPost() {
		return post;
	}

	public void setPost(Object post) {
		this.post = post;
	}
	public Header[] getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(Header[] responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	public enum RequestMethod
	{
		GET,
		POST,
		DELETE, PUT
	}
}
