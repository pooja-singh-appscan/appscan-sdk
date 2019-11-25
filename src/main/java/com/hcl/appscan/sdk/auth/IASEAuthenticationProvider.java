/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.auth;

import java.util.List;

/**
 *
 * @author anurag-s
 */
public interface IASEAuthenticationProvider extends IAuthenticationProvider{
    
    public void setCookies(List<String> cookies);
    
}
