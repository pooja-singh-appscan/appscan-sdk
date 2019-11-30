/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.scanners.ase;

import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.scan.ASEScanServiceProvider;
import com.hcl.appscan.sdk.scan.CloudScanServiceProvider;
import com.hcl.appscan.sdk.scan.IScan;
import com.hcl.appscan.sdk.scan.IScanFactory;
import com.hcl.appscan.sdk.scan.IScanServiceProvider;
import com.hcl.appscan.sdk.scanners.dynamic.DASTScan;
import java.util.Map;

/**
 *
 * @author anurag-s
 */
public class ASEScanFactory implements IScanFactory{

    @Override
    public IScan create(Map<String, String> properties, IProgress progress, IAuthenticationProvider authProvider) {
        IScanServiceProvider serviceProvider = new ASEScanServiceProvider(progress, authProvider);
		return new ASEScan(properties, progress, serviceProvider);
    }

    @Override
    public String getType() {
        return ASEConstants.ASE_DAST;
    }
    
}
