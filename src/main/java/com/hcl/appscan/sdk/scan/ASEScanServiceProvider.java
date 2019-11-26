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
import java.util.Map;
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
        //if(loginExpired() || !verifyApplication(params.get("applicationId")))
                if(loginExpired())
			return null;
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXECUTING_SCAN)));
                // TODO : correct it .
                String templateId=params.get("templateId");
                params.remove("templateId");
		
		//String request_url =  m_authProvider.getServer() + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
                String request_url = "https://ap-asc-win47.nonprod.hclpnp.com:9443/ase" + String.format(ASE_CREATEJOB_TEMPLATE_ID, templateId);
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
				return json.getString(ID);
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
