/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.configuration;

import java.util.Map;

/**
 *
 * @author anurag-s
 */
public interface IComponent {
    
    /**
	 * Gets the available components for a component type.
	 * @return A Map of components, keyed by the component id.
	 */
	public Map<String, String> getComponents();
	
	/**
	 * Gets the name of the component with the given id. 
	 * @param id The id of the component.
	 * @return The component name.
	 */
	public String getComponentName(String id);
    
}
