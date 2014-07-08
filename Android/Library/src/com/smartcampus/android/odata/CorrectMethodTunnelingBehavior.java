/*
Copyright (c) 2014, Aalborg University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.smartcampus.android.odata;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.format.Entry;

import com.smartcampus.webclient.ConnectionInfo;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;

public class CorrectMethodTunnelingBehavior implements OClientBehavior {
	
    private final String[] methodsToTunnel;

    public CorrectMethodTunnelingBehavior(String... methodsToTunnel) {
        this.methodsToTunnel = methodsToTunnel;
    }
    
    @Override
    public void modify(ClientConfig clientConfig) { }

    @Override
    public ODataClientRequest transform(ODataClientRequest request) {
        String method = request.getMethod();
        for(String methodToTunnel : methodsToTunnel) {
        	if (method.equals(methodToTunnel)) { 
	        	if(methodToTunnel.equals("DELETE"))
	        		return request.entryPayload(generateRandomEntry()).header(ODataConstants.Headers.X_HTTP_METHOD, method).method("POST");
	        	else if(methodToTunnel.equals("PUT"))
	        		return request.header(ODataConstants.Headers.X_HTTP_METHOD, method).method("POST");
        	}
        }
        return request;
    }
    
    
    /*
     * Yes you are right, you are looking at the greatest hack in the world. However, it is necessary to construct an entry
     * which is explicitly set for the request (maybe a less "hackish" solution is possible(?)) to FORCE the content-length attribute 
     * to be set in the HTTP header. This is to preserve the (limited) API. Otherwise, the request does not contain any body (see odataclient class of odata4j).
     * In case the HTTP body is empty, the %#�%/%&#"!"��&#& Jersey lib will not specify the content-length in the HTTP header... which is
     * completely insane when considering this is in fact a POST request. Furthermore "#%"#%"%#""!% Jersey does not allow the header to
     * be manually set as is possible with the X-HTTP-METHOD: DELETE in modify() (which allows for naively inserting header content).
     * When trying to set content-length manually, &#"#"%&#& Jersey overwrites this and substitutes it with nothing thereby violating the
     * HTTP standard. This IS a bug with Jersey - apparently a special-case which it does not account for. 
     */
    private Entry generateRandomEntry() {
    	
    	//String serviceURI = "http://smartcampus.cloudapp.net/RadioMapService.svc/";
    	String serviceURI = ConnectionInfo.SMARTCAMPUS_SERVICE_ROOT_URI;
    	String entityName = "Edges";
    	
    	OCreateRequest<OEntity> req = ODataConsumer.create(serviceURI).createEntity(entityName);
    	return new randomEntry(req.get());
    }
    
    private static class randomEntry implements Entry {

    	private OEntity entity;
    	
    	public randomEntry(OEntity entity) {
    		this.entity = entity;
    	}
    	
		@Override
		public String getUri() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getETag() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OEntity getEntity() {
			return entity;
		}
    	
    }

	@Override
	public void modifyClientFilters(Filterable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyWebResourceFilters(Filterable arg0) {
		// TODO Auto-generated method stub
		
	}
}
