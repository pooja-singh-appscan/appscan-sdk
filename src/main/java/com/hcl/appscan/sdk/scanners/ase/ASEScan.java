/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.scanners.ase;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;
import com.hcl.appscan.sdk.logging.DefaultProgress;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.results.ASEResultsProvider;
import com.hcl.appscan.sdk.results.CloudResultsProvider;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScan;
import com.hcl.appscan.sdk.scan.IScanServiceProvider;
import com.hcl.appscan.sdk.scanners.ScanConstants;
import static com.hcl.appscan.sdk.scanners.ScanConstants.ERROR_CREATING_SCAN;
import static com.hcl.appscan.sdk.scanners.dynamic.DASTConstants.STARTING_URL;
import com.hcl.appscan.sdk.utils.SystemUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author anurag-s
 */
public class ASEScan implements IScan, ScanConstants, Serializable{
    
    private static final long serialVersionUID = 1L;
        //TODO : figure out the report format which ASE supports
	private static final String REPORT_FORMAT = "pdf"; //$NON-NLS-1$
	
        private String m_target;
	private String m_scanId;
	private IProgress m_progress;
	private IScanServiceProvider m_serviceProvider;
	private Map<String, String> m_properties;
        
    public ASEScan(Map<String, String> properties, IScanServiceProvider provider) {
		this(properties, new DefaultProgress(), provider);
	}
	
	public ASEScan(Map<String, String> properties, IProgress progress, IScanServiceProvider provider) {
		m_properties = properties;
		m_progress = progress;
		m_serviceProvider = provider;
	}
        
        
    @Override
    public void run() throws ScannerException, InvalidTargetException {
        String target = getTarget();
        Map<String, String> params = getProperties();
        //params.put(STARTING_URL, target);
        
        setScanId(getServiceProvider().createAndExecuteScan(null, params));
		
		if(getScanId() == null)
			throw new ScannerException(Messages.getMessage(ERROR_CREATING_SCAN));
    }
    private Map<String, String> getProperties() {
		if(!m_properties.containsKey(CoreConstants.LOCALE))
			m_properties.put(CoreConstants.LOCALE, SystemUtil.getLocale());
		if(!m_properties.containsKey(CoreConstants.EMAIL_NOTIFICATION) ||
				!Boolean.parseBoolean(m_properties.get(CoreConstants.EMAIL_NOTIFICATION)))
			m_properties.put(CoreConstants.EMAIL_NOTIFICATION, Boolean.toString(false));
		return m_properties;
	}
    private Map<String,String> getParams(Map<String,String> properties){
        Map<String,String> apiParams= new HashMap<>();
        apiParams.put("testPolicyId", properties.get("testPolicyId"));
        apiParams.put("folderId",properties.get("testPolicyId"));
        apiParams.put("applicationId",properties.get("applicationId"));
        apiParams.put("name", properties.get("name"));
        apiParams.put("templateId", properties.get("templateId"));
        return apiParams;
    }
    
    private String getTarget() {
		return m_target;
	}
    
    private void setScanId(String id) {
		m_scanId = id;
	}

    @Override
    public String getScanId() {
        return m_scanId;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IResultsProvider getResultsProvider() {
        ASEResultsProvider provider = new ASEResultsProvider(m_scanId, getType(), m_serviceProvider, m_progress);
		provider.setReportFormat(getReportFormat());
		return provider;
    }

    @Override
    public String getName() {
        return m_properties.get(CoreConstants.SCAN_NAME);
    }

    @Override
    public IScanServiceProvider getServiceProvider() {
        return m_serviceProvider;
    }

    @Override
    public String getReportFormat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
