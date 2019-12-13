/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scanners.ase;

import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;
import com.hcl.appscan.sdk.logging.DefaultProgress;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.results.ASEResultsProvider;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScanServiceProvider;
import com.hcl.appscan.sdk.scanners.ASoCScan;
import com.hcl.appscan.sdk.scanners.ScanConstants;
import java.util.HashMap;
import java.util.Map;

public class ASEScan extends ASoCScan implements ScanConstants{
    
    private static final long serialVersionUID = 1L;
    
    //TODO : figure out the report format which ASE supports
	private static final String REPORT_FORMAT = "pdf"; //$NON-NLS-1$
	
        
    public ASEScan(Map<String, String> properties, IScanServiceProvider provider) {
		super(properties, new DefaultProgress(), provider);
	}
	
	public ASEScan(Map<String, String> properties, IProgress progress, IScanServiceProvider provider) {
		super(properties, progress, provider);
	}        
        
    @Override
    public void run() throws ScannerException, InvalidTargetException {
        
        Map<String, String> params = getParams(getProperties());
        String id=getServiceProvider().createAndExecuteScan(null, params);        
        setScanId(id);
		if(getScanId() == null)
			throw new ScannerException(Messages.getMessage(ERROR_CREATING_SCAN));
    }
    
    private Map<String,String> getParams(Map<String,String> properties){
        Map<String,String> apiParams= new HashMap<>();
        apiParams.put("testPolicyId", properties.get("testPolicyId"));
        apiParams.put("folderId",properties.get("folderId"));
        apiParams.put("applicationId",properties.get("applicationId"));
        apiParams.put("name", properties.get("ScanName"));
        apiParams.put("templateId", properties.get("templateId"));
        return apiParams;
    }

    @Override
    public String getType() {
        return ASEConstants.ASE_DAST;
    }

    @Override
    public IResultsProvider getResultsProvider() {
        ASEResultsProvider provider = new ASEResultsProvider(getScanId(), getType(), getServiceProvider(), getProgress());
		provider.setReportFormat(getReportFormat());
		return provider;
    }

    @Override
    public String getReportFormat() {
        return REPORT_FORMAT;
    }
}