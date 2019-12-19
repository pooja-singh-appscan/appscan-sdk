/**
 * © Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scan;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.http.HttpsClient;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

public class ASEScanServiceProvider implements IScanServiceProvider, Serializable, CoreConstants{
	private IProgress m_progress;
	private IASEAuthenticationProvider m_authProvider;
	
	public ASEScanServiceProvider(IProgress progress, IAuthenticationProvider authProvider) {
		m_progress = progress;
		m_authProvider = (IASEAuthenticationProvider)authProvider;
	}

    @Override
    public String createAndExecuteScan(String type, Map<String, String> params) {
        String jobId=createJob(params);
        
    	if (jobId!=null) {
    		
    		// Starting URL
		    if(!params.get("startingURL").isEmpty())
			    updatescantJob(getStartingUrlParams(params.get("startingURL")),jobId);
		    
		    // Login Management - Automatic
		    if(!params.get("startingURL").isEmpty())
			    updatescantJob(getLoginAutoUserNameParams(params.get("username")),jobId);
		    if(!params.get("startingURL").isEmpty())
			    updatescantJob(getLoginAutoPasswordParams(params.get("password")),jobId);
		    
		    // Login Management - Recorded
		    if(!params.get("trafficFile").isEmpty())
			    updatetrafficJob(params,jobId,"login");
		    
		    // Explore Data
		    if(!params.get("startingURL").isEmpty())
			    updatetrafficJob(params,jobId,"add");
    	}
        if (jobId!=null && runScanJob(jobId)){
            return jobId;
        }
        return null;
    }
    
    private String createJob(Map<String, String> params) {
    	
        if(loginExpired())
           return null;
        
        Map<String, String> createJobParams = getcreateJobParams(params);        
        m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(CREATING_JOB)));
        
        // TODO : correct it .
        String templateId = createJobParams.get("templateId");
        createJobParams.remove("templateId");
		
        String request_url = m_authProvider.getServer() + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
        Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
        request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
        request_headers.put(CHARSET, UTF8);
        request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.postForm(request_url, request_headers, createJobParams);
			int status = response.getResponseCode();
		
			JSONObject json = (JSONObject) response.getResponseBodyAsJSON();
			
			if (status == HttpsURLConnection.HTTP_CREATED) {
				m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(CREATE_JOB_SUCCESS)));
				return json.getString(ASE_ID_ATTRIBUTE);
			}
			else if (json != null && json.has(MESSAGE))
				m_progress.setStatus(new Message(Message.ERROR, json.getString(MESSAGE)));
			else
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_CREATE_JOB, status)));
		} catch(IOException | JSONException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_CREATE_JOB, e.getLocalizedMessage())));
		}
		return null;
    }
    
    private Map<String,String> getcreateJobParams(Map<String,String> properties){
        Map<String,String> apiParams= new HashMap<>();
        apiParams.put("testPolicyId", properties.get("testPolicyId"));
        apiParams.put("folderId",properties.get("folderId"));
        apiParams.put("applicationId",properties.get("applicationId"));
        apiParams.put("name", properties.get("ScanName"));
        apiParams.put("templateId", properties.get("templateId"));
        return apiParams;
    }
    
    private String updatescantJob(Map<String, String> params, String jobId){
    	
        if(loginExpired())
    		return null;    	 		
    	
    	String request_url = m_authProvider.getServer() + String.format(ASE_UPDSCANT, jobId);
    	Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
    	request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
    	request_headers.put(CHARSET, UTF8);
    	request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
    	
        HttpsClient client = new HttpsClient();
    		
		try {
			HttpResponse response = client.postForm(request_url, request_headers, params);
			int status = response.getResponseCode();					
			if (status != HttpsURLConnection.HTTP_CREATED) {				
				// In the event update fails, stop the job
            }
        } catch(IOException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_UPDATE_JOB, e.getLocalizedMessage())));
		}
		return null;
    }
    
    private String updatetrafficJob(Map<String, String> params, String jobId, String action){
		
    	if(loginExpired())
			return null;

		String request_url = m_authProvider.getServer() + String.format(ASE_UPDTRAFFIC, jobId, action);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
		request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
		request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$

		 HttpsClient client = new HttpsClient();

		try {
			HttpResponse response = client.postForm(request_url, request_headers, params);
			int status = response.getResponseCode();			
			if (status != HttpsURLConnection.HTTP_CREATED) {				
				// In the event update fails, stop the job
            }		
		} catch(IOException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_UPDATE_JOB, e.getLocalizedMessage())));
		}
		return null;
	}
    
	private Map<String,String> getStartingUrlParams(String startingURL){
		Map<String,String> apiParams= new HashMap<>();
		apiParams.put("scantNodeXpath", "StartingUrl");
		apiParams.put("scantNodeNewValue", startingURL);
		//apiParams.put("encryptNodeValue", "false");
		//apiParams.put("allowExploreDataUpdate", "0");
		return apiParams;
	}
	
	private Map<String,String> getLoginAutoUserNameParams(String loginUsername){
		Map<String,String> apiParams= new HashMap<>();
		apiParams.put("scantNodeXpath", "LoginUsername");
		apiParams.put("scantNodeNewValue",loginUsername);
		//apiParams.put("encryptNodeValue", "false");
		//apiParams.put("allowExploreDataUpdate", "0");
		return apiParams;
	}
    
	private Map<String,String> getLoginAutoPasswordParams(String loginPassword){
		Map<String,String> apiParams= new HashMap<>();
		apiParams.put("scantNodeXpath", "LoginPassword");
		apiParams.put("scantNodeNewValue", loginPassword);
		apiParams.put("encryptNodeValue", "true");
		//apiParams.put("allowExploreDataUpdate", "0");
		return apiParams;
        }
    
    private boolean runScanJob(String jobId) {
      
       if(loginExpired())
			return false;
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTING_JOB)));
		
        String eTag = "";
        eTag = getEtag(jobId);                
		String request_url = m_authProvider.getServer() + String.format(ASE_RUN_JOB_ACTION, jobId);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
        request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
        request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
        request_headers.put("If-Match", eTag);
        Map<String ,String> params= new HashMap<>();
        params.put("type", "run");
		
		HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.postForm(request_url, request_headers, params);
			int status = response.getResponseCode();
			if (status == HttpsURLConnection.HTTP_OK) {
				m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTE_JOB_SUCCESS)));
				return true;
			}
			else
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_EXECUTE_JOB, status)));
		} catch(IOException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_EXECUTE_JOB, e.getLocalizedMessage())));
		}
		return false;
    }
    
    private String getEtag(String jobId) {

    	if(loginExpired())
			return null;		
		
		String request_url = m_authProvider.getServer() + String.format(ASE_GET_JOB, jobId);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
		request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
		request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.get(request_url, request_headers, null);
			int status = response.getResponseCode();
			if (status == HttpsURLConnection.HTTP_OK) {
				return response.getHeaderField("ETag");
			}
		} catch(IOException e) {
			return null;
		}
		return null;
    }
    
    private boolean loginExpired() {
        if(m_authProvider.isTokenExpired()) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_LOGIN_EXPIRED)));
			return true;
		}
		return false;
	}

    @Override
    public String submitFile(File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getScanDetails(String scanId) throws IOException, JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONArray getNonCompliantIssues(String scanId) throws IOException, JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IAuthenticationProvider getAuthenticationProvider() {
        return m_authProvider;
    }

    @Override
    public void setProgress(IProgress progress) {
        m_progress = progress;
    }
}