/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.scan;

import com.hcl.appscan.sdk.CoreConstants;
import static com.hcl.appscan.sdk.CoreConstants.API_SCANNER;
import static com.hcl.appscan.sdk.CoreConstants.APP_ID;
import static com.hcl.appscan.sdk.CoreConstants.CREATE_SCAN_SUCCESS;
import static com.hcl.appscan.sdk.CoreConstants.ERROR_INVALID_APP;
import static com.hcl.appscan.sdk.CoreConstants.ERROR_SUBMITTING_SCAN;
import static com.hcl.appscan.sdk.CoreConstants.EXECUTING_SCAN;
import static com.hcl.appscan.sdk.CoreConstants.ID;
import static com.hcl.appscan.sdk.CoreConstants.MESSAGE;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.app.ASEApplicationProvider;
import com.hcl.appscan.sdk.app.CloudApplicationProvider;
import com.hcl.appscan.sdk.app.IApplicationProvider;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.http.HttpsClient;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 *
 * @author anurag-s
 */
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
        if (jobId!=null && runScanJob(jobId)){
            return jobId;
        }
        return null;
        
    }
    
    private String createJob(Map<String, String> params){
        //if(loginExpired() || !verifyApplication(params.get("applicationId")))
                if(loginExpired())
			return null;
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTING_SCAN)));
                // TODO : correct it .
                String templateId=params.get("templateId");
                params.remove("templateId");
		
		//String request_url =  m_authProvider.getServer() + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
                String request_url = m_authProvider.getServer() + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
                request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
                request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
                
		
		      HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.postForm(request_url, request_headers, params);
			int status = response.getResponseCode();
		
			JSONObject json = (JSONObject) response.getResponseBodyAsJSON();
			
			if (status == HttpsURLConnection.HTTP_CREATED) {
				m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(CREATE_SCAN_SUCCESS)));
				return json.getString(ASE_ID_ATTRIBUTE);
			}
			else if (json != null && json.has(MESSAGE))
				m_progress.setStatus(new Message(Message.ERROR, json.getString(MESSAGE)));
			else
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, status)));
		} catch(IOException | JSONException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, e.getLocalizedMessage())));
		}
		return null;
        
    }
    
    private boolean runScanJob(String jobId){
        //if(loginExpired() || !verifyApplication(params.get("applicationId")))
                if(loginExpired())
			return false;
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTING_SCAN)));
                // TODO : correct it .
                String eTag=getEtag(jobId);
                
		//String request_url =  m_authProvider.getServer() + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
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
		
			//JSONObject json = (JSONObject) response.getResponseBodyAsJSON();
			
			if (status == HttpsURLConnection.HTTP_OK) {
				m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(CREATE_SCAN_SUCCESS)));
				return true;
			}
			//else if (json != null && json.has(MESSAGE))
			//	m_progress.setStatus(new Message(Message.ERROR, json.getString(MESSAGE)));
			else
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, status)));
		} catch(IOException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, e.getLocalizedMessage())));
		}
		return false;
        
    }
    
    private boolean loginExpired() {
		if(m_authProvider.isTokenExpired()) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_LOGIN_EXPIRED)));
			return true;
		}
		return false;
	}
    private boolean verifyApplication(String appId) {
		if(appId != null && !appId.trim().equals("")) { //$NON-NLS-1$
			IApplicationProvider provider = new ASEApplicationProvider(m_authProvider);
			if(provider.getApplications() != null && provider.getApplications().keySet().contains(appId))
				return true;
		}
		m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_INVALID_APP, appId)));
		return false;
	}

    @Override
    public String submitFile(File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getScanDetails(String jobId) throws IOException, JSONException {
                if(loginExpired())
			return null;
		String reportPackId=getReportPackId(jobId);
		String request_url = m_authProvider.getServer() + String.format(ASE_REPORTS, reportPackId);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
		
		HttpsClient client = new HttpsClient();
		HttpResponse response = client.get(request_url, request_headers, null);
		
		if (response.getResponseCode() == HttpsURLConnection.HTTP_OK || response.getResponseCode() == HttpsURLConnection.HTTP_CREATED)
			//return (JSONObject) response.getResponseBodyAsJSON();
                        return getResultJson(response);

		if (response.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST)
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_INVALID_JOB_ID, jobId)));
		
		return null;
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

    private String getEtag(String jobId) {
        //if(loginExpired() || !verifyApplication(params.get("applicationId")))
                if(loginExpired())
			return null;
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTING_SCAN)));
                String request_url = m_authProvider.getServer() + String.format(ASE_GET_JOB, jobId);
		Map<String, String> request_headers = m_authProvider.getAuthorizationHeader(true);
                request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
                request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		      HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.get(request_url, request_headers, null);
			int status = response.getResponseCode();
		
			JSONObject json = (JSONObject) response.getResponseBodyAsJSON();
			
			if (status == HttpsURLConnection.HTTP_OK) {
				m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(CREATE_SCAN_SUCCESS)));
				return response.getHeaderField("ETag");
			}
			else if (json != null && json.has(MESSAGE))
				m_progress.setStatus(new Message(Message.ERROR, json.getString(MESSAGE)));
			else
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, status)));
		} catch(IOException | JSONException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_SUBMITTING_SCAN, e.getLocalizedMessage())));
		}
		return null;
        
    }

    private String getReportPackId(String jobId) {
        return String.valueOf(Integer.parseInt(jobId)+1);
        // please uncomment the below code when you figure out how to parse the reponse.
        // currently the reponse is returned as array which makes no sense.
        /*IAuthenticationProvider authProvider = m_scanProvider.getAuthenticationProvider();
		if(authProvider.isTokenExpired()) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_LOGIN_EXPIRED)));
			return null;
		}
                
                
		String request_url = authProvider.getServer() + String.format(ASE_REPORTPACK, scanId);
		Map<String, String> request_headers = authProvider.getAuthorizationHeader(true);
                request_headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		request_headers.put(CHARSET, UTF8);
                request_headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient client = new HttpsClient();
		
            try {
                HttpResponse response = client.get(request_url, request_headers, null);
                if (response.getResponseCode() == HttpsURLConnection.HTTP_OK){
                    JSONObject object = (JSONObject) response.getResponseBodyAsJSON();
                    JSONArray array=object.getJSONArray("");
                    JSONObject obj=array.getJSONObject(0);
                    return obj.getString("reportPackId");
                }
                else {
                    m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_GETTING_RESULT)));
                }
            } catch (IOException |JSONException ex) {
                Logger.getLogger(ASEResultsProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
            */
    }

    private JSONObject getResultJson(HttpResponse response) {
        JSONObject result;
        
        try {
            JSONObject object=(JSONObject) response.getResponseBodyAsJSON();
            JSONObject reportsObject=object.getJSONObject("reports");
            JSONArray reports=reportsObject.getJSONArray("report");
            outer:
            for (Object obj:reports.toArray()){
                JSONObject reportObject=(JSONObject)obj;
                if (reportObject.getString("name").equalsIgnoreCase("Security Issues")){
                    result= new JSONObject();
                    JSONObject issueCountsSeverity=reportObject.getJSONObject("issue-counts-severity");
                    JSONArray issueCount=issueCountsSeverity.getJSONArray("issue-count");
                    int totalCount=0;
                    int count;
                    inner:
                    for (Object severity: issueCount.toArray()){
                        JSONObject severityCount=(JSONObject) severity;
                        JSONObject severityDetails=severityCount.getJSONObject("severity");
                        count=Integer.parseInt(severityCount.getString("count"));
                        switch(severityDetails.getString("name")){
                            case "High":
                                result.put("NHighIssues", count);
                                totalCount=totalCount+count;
                                break;
                            case "Medium":
                                result.put("NMediumIssues", count);
                                totalCount=totalCount+count;
                                break;
                            case "Low":
                                result.put("NLowIssues", count);
                                totalCount=totalCount+count;
                                break;
                            case "Information":
                                result.put("NInfoIssues", count);
                                totalCount=totalCount+count;
                                break;
                            default:
                                totalCount=totalCount+count;
                                break;
                        }
                    }
                    result.put("NIssuesFound", totalCount);
                    return result;
                }
            }
        } catch (IOException | JSONException ex) {
            Logger.getLogger(ASEScanServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
