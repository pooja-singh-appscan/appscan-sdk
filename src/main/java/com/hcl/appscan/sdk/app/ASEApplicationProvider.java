/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.app;

import com.hcl.appscan.sdk.CoreConstants;
import static com.hcl.appscan.sdk.CoreConstants.API_APPS;
import static com.hcl.appscan.sdk.CoreConstants.ID;
import static com.hcl.appscan.sdk.CoreConstants.NAME;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpsClient;
import com.hcl.appscan.sdk.http.HttpResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 *
 * @author anurag-s
 */
public class ASEApplicationProvider implements IApplicationProvider, CoreConstants {
    
    private Map<String, String> m_applications;
    private IASEAuthenticationProvider m_authProvider;
	
    public ASEApplicationProvider(IASEAuthenticationProvider provider) {
	m_authProvider = provider;
    }

    @Override
    public Map<String, String> getApplications() {
        if(m_applications == null)
		loadApplications();
		return m_applications;
        }

    @Override
    public String getAppName(String id) {
        return getApplications().get(id);
    }

    private void loadApplications() {
        if(m_authProvider.isTokenExpired())
			return;
		
		m_applications = new HashMap<String, String>();
		//String url =  m_authProvider.getServer() + ASE_APPS + "columns=name&sortBy=%2Bname"; //$NON-NLS-1$
                String url =  m_authProvider.getServer() + ASE_APPS+"?columns=name";
		Map<String, String> headers = m_authProvider.getAuthorizationHeader(true);
		headers.putAll(Collections.singletonMap("Range", "items=0-999999")); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.get(url, headers, null);
			
			if (!response.isSuccess())
				return;
		
			JSONArray array = (JSONArray)response.getResponseBodyAsJSON();
			if(array == null)
				return;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				String id = object.getString(ASE_ID_ATTRIBUTE);
				String name = object.getString(ASE_NAME_ATTRIBUTE);
				m_applications.put(id, name);
			}
		}
		catch(IOException | JSONException e) {
			m_applications = null;
		}
    }
    
}
