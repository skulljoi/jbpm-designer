package org.jbpm.designer.web.server.integration;

import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.server.integration.base.AbstractGuvnorIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(Arquillian.class)
@RunAsClient
public class JbpmServiceRepositoryServletTest extends AbstractGuvnorIntegrationTest {

    private static final String jsonObjectName = JbpmServiceRepositoryServletTest.class.getSimpleName();
    
    private static MockHttpServletRequest fillRequest(ServletContext servletContext, 
            String action, String uuid, String taskName, String taskFormValue) {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setParameter("profile", "jbpm");
        request.setMethod("POST");
        
        request.setParameter("action", action);
        request.setParameter("uuid", uuid);
        request.setParameter("taskname", taskName);
        request.setParameter("tfvalue", taskFormValue);
        
        return request;
    }
    
    /**
     * Method to fill Guvnor with information needed to test methods.
     * @throws Exception When something goes wrong. 
     */
    protected static void setupGuvnor(URL guvnorUrl, IDiagramProfile profile) throws Exception {

    }
    
    @Test
    public void getCustomEditorsJSONTest() throws Exception { 
        walkGetCustomEditorsJSONTest(guvnorUrl, profile, servletContext);
    }
    
    public void walkGetCustomEditorsJSONTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) throws Exception { 
        // Setup Url
        setupGuvnor(guvnorUrl, profile);
        
        // Run test method
        //OCRAM: JbpmServiceRepositoryServletTest  
    }
    
}
