/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.configuration;

import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;

/**
 *
 * @author anurag-s
 */
public class ConfigurationProviderFactory {
    
	public static IComponent getScanner(String type, IASEAuthenticationProvider provider) {
		IComponent comp = null;
		
		switch(type) {
		case "Folder":
			comp = new ASEFolderProvider(provider);
			break;
		case "TestPolicies":
			comp = new ASETestPoliciesProvider(provider);
			break;
		case "Agent":
			comp = new ASEAgentServerProvider(provider);
			break;
                case "Template":
			comp = new ASETemplateProvider(provider);
			break;
		default:
				break;
		}
		return comp;
	}
    
}
