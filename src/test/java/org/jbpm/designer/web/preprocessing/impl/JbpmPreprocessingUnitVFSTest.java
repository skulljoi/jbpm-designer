package org.jbpm.designer.web.preprocessing.impl;

import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.repository.vfs.VFSRepository;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.profile.impl.JbpmProfileImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JbpmPreprocessingUnitVFSTest {

    private static final String REPOSITORY_ROOT = System.getProperty("java.io.tmpdir")+"designer-repo";
    private static final String VFS_REPOSITORY_ROOT = "default://" + REPOSITORY_ROOT;
    private JbpmProfileImpl profile;

    @Before
    public void setup() {
        new File(REPOSITORY_ROOT).mkdir();
        profile = new JbpmProfileImpl();
        profile.setRepositoryId("vfs");
        profile.setRepositoryRoot(VFS_REPOSITORY_ROOT);
    }

    private void deleteFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteFiles(file);
            }
            file.delete();
        }
    }

    @After
    public void teardown() {
        File repo = new File(REPOSITORY_ROOT);
        if(repo.exists()) {
            deleteFiles(repo);
        }
        repo.delete();
    }
    @Test
    public void testProprocess() {
        Repository repository = new VFSRepository(profile);
        //prepare folders that will be used
        repository.storeDirectory("/myprocesses");
        repository.storeDirectory("/global");

        // prepare process asset that will be used to preprocess
        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("bpmn2 content")
                .type("bpmn2")
                .name("process")
                .location("/myprocesses");
        String uniqueId = repository.storeAsset(builder.getAsset());

        // create instance of preprocessing unit
        JbpmPreprocessingUnitVFS preprocessingUnitVFS = new JbpmPreprocessingUnitVFS(new TestServletContext());

        // setup parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("uuid", uniqueId);

        // run preprocess
        preprocessingUnitVFS.preprocess(new TestHttpServletRequest(params), null, new TestIDiagramProfile(repository), null);

        // validate results
        Collection<Asset> globalAssets = repository.listAssets("/global");
        assertNotNull(globalAssets);
        assertEquals(26, globalAssets.size());
        repository.assetExists("/global/backboneformsinclude.fw");
        repository.assetExists("/global/backbonejsinclude.fw");
        repository.assetExists("/global/cancelbutton.fw");
        repository.assetExists("/global/checkbox.fw");
        repository.assetExists("/global/customeditors.json");
        repository.assetExists("/global/div.fw");
        repository.assetExists("/global/dropdownmenu.fw");
        repository.assetExists("/global/fieldset.fw");
        repository.assetExists("/global/form.fw");
        repository.assetExists("/global/handlebarsinclude.fw");
        repository.assetExists("/global/htmlbasepage.fw");
        repository.assetExists("/global/image.fw");
        repository.assetExists("/global/jqueryinclude.fw");
        repository.assetExists("/global/jquerymobileinclude.fw");
        repository.assetExists("/global/link.fw");
        repository.assetExists("/global/mobilebasepage.fw");
        repository.assetExists("/global/orderedlist.fw");
        repository.assetExists("/global/passwordfield.fw");
        repository.assetExists("/global/radiobutton.fw");
        repository.assetExists("/global/script.fw");
        repository.assetExists("/global/submitbutton.fw");
        repository.assetExists("/global/table.fw");
        repository.assetExists("/global/textarea.fw");
        repository.assetExists("/global/textfield.fw");
        repository.assetExists("/global/themes.json");
        repository.assetExists("/global/unorderedlist.fw");

        Collection<Asset> defaultStuff = repository.listAssets("/myprocesses");
        assertNotNull(defaultStuff);
        assertEquals(5, defaultStuff.size());
        repository.assetExists("/myprocesses/defaultemailicon.gif");
        repository.assetExists("/myprocesses/defaultlogicon.gif");
        repository.assetExists("/myprocesses/defaultservicenodeicon.png");
        repository.assetExists("/myprocesses/WorkDefinitions.wid");
        // this is the process asset that was created for the test but let's check it anyway
        repository.assetExists("/myprocesses/process.bpmn2");

    }



    private class TestHttpServletRequest implements HttpServletRequest {

        private Map<String, String> parameters;

        TestHttpServletRequest(Map<String, String> params) {
            this.parameters = params;
        }

        public String getAuthType() {
            return null;  
        }

        public Cookie[] getCookies() {
            return new Cookie[0];  
        }

        public long getDateHeader(String name) {
            return 0;  
        }

        public String getHeader(String name) {
            return null;  
        }

        public Enumeration getHeaders(String name) {
            return null;  
        }

        public Enumeration getHeaderNames() {
            return null;  
        }

        public int getIntHeader(String name) {
            return 0;  
        }

        public String getMethod() {
            return null;  
        }

        public String getPathInfo() {
            return null;  
        }

        public String getPathTranslated() {
            return null;  
        }

        public String getContextPath() {
            return null;  
        }

        public String getQueryString() {
            return null;  
        }

        public String getRemoteUser() {
            return null;  
        }

        public boolean isUserInRole(String role) {
            return false;  
        }

        public Principal getUserPrincipal() {
            return null;  
        }

        public String getRequestedSessionId() {
            return null;  
        }

        public String getRequestURI() {
            return null;  
        }

        public StringBuffer getRequestURL() {
            return null;  
        }

        public String getServletPath() {
            return null;  
        }

        public HttpSession getSession(boolean create) {
            return null;  
        }

        public HttpSession getSession() {
            return null;  
        }

        public boolean isRequestedSessionIdValid() {
            return false;  
        }

        public boolean isRequestedSessionIdFromCookie() {
            return false;  
        }

        public boolean isRequestedSessionIdFromURL() {
            return false;  
        }

        public boolean isRequestedSessionIdFromUrl() {
            return false;  
        }

        public Object getAttribute(String name) {
            return null;  
        }

        public Enumeration getAttributeNames() {
            return null;  
        }

        public String getCharacterEncoding() {
            return null;  
        }

        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            
        }

        public int getContentLength() {
            return 0;  
        }

        public String getContentType() {
            return null;  
        }

        public ServletInputStream getInputStream() throws IOException {
            return null;  
        }

        public String getParameter(String name) {
            return this.parameters.get(name);
        }

        public Enumeration getParameterNames() {
            return null;  
        }

        public String[] getParameterValues(String name) {
            return new String[0];  
        }

        public Map getParameterMap() {
            return null;  
        }

        public String getProtocol() {
            return null;  
        }

        public String getScheme() {
            return null;  
        }

        public String getServerName() {
            return null;  
        }

        public int getServerPort() {
            return 0;  
        }

        public BufferedReader getReader() throws IOException {
            return null;  
        }

        public String getRemoteAddr() {
            return null;  
        }

        public String getRemoteHost() {
            return null;  
        }

        public void setAttribute(String name, Object o) {
            
        }

        public void removeAttribute(String name) {
            
        }

        public Locale getLocale() {
            return null;  
        }

        public Enumeration getLocales() {
            return null;  
        }

        public boolean isSecure() {
            return false;  
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return null;  
        }

        public String getRealPath(String path) {
            return null;  
        }

        public int getRemotePort() {
            return 0;  
        }

        public String getLocalName() {
            return null;  
        }

        public String getLocalAddr() {
            return null;  
        }

        public int getLocalPort() {
            return 0;  
        }
    }
    
    private class TestServletContext implements ServletContext {

        public String getContextPath() {
            return null;  
        }

        public ServletContext getContext(String uripath) {
            return null;  
        }

        public int getMajorVersion() {
            return 0;  
        }

        public int getMinorVersion() {
            return 0;  
        }

        public String getMimeType(String file) {
            return null;  
        }

        public Set getResourcePaths(String path) {
            return null;  
        }

        public URL getResource(String path) throws MalformedURLException {
            return null;  
        }

        public InputStream getResourceAsStream(String path) {
            return null;  
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return null;  
        }

        public RequestDispatcher getNamedDispatcher(String name) {
            return null;  
        }

        public Servlet getServlet(String name) throws ServletException {
            return null;  
        }

        public Enumeration getServlets() {
            return null;  
        }

        public Enumeration getServletNames() {
            return null;  
        }

        public void log(String msg) {
            
        }

        public void log(Exception exception, String msg) {
            
        }

        public void log(String message, Throwable throwable) {
            
        }

        public String getRealPath(String path) {
            return "src/main/webapp" + path;
        }

        public String getServerInfo() {
            return null;  
        }

        public String getInitParameter(String name) {
            return null;  
        }

        public Enumeration getInitParameterNames() {
            return null;  
        }

        public Object getAttribute(String name) {
            return null;  
        }

        public Enumeration getAttributeNames() {
            return null;  
        }

        public void setAttribute(String name, Object object) {
            
        }

        public void removeAttribute(String name) {
            
        }

        public String getServletContextName() {
            return null;  
        }
    }
    
    private class TestIDiagramProfile implements IDiagramProfile {

        private Repository repository;

        TestIDiagramProfile(Repository repository) {
            this.repository = repository;
        }

        public String getName() {
            return null; 
        }

        public String getTitle() {
            return null; 
        }

        public String getStencilSet() {
            return null; 
        }

        public Collection<String> getStencilSetExtensions() {
            return null; 
        }

        public String getSerializedModelExtension() {
            return null; 
        }

        public String getStencilSetURL() {
            return null; 
        }

        public String getStencilSetNamespaceURL() {
            return null; 
        }

        public String getStencilSetExtensionURL() {
            return null; 
        }

        public Collection<String> getPlugins() {
            return null; 
        }

        public IDiagramMarshaller createMarshaller() {
            return null; 
        }

        public IDiagramUnmarshaller createUnmarshaller() {
            return null; 
        }

        public String getRepositoryId() {
            return null; 
        }

        public String getRepositoryRoot() {
            return null; 
        }

        public String getRepositoryHost() {
            return null; 
        }

        public String getRepositoryProtocol() {
            return null; 
        }

        public String getRepositorySubdomain() {
            return null; 
        }

        public String getRepositoryUsr() {
            return null; 
        }

        public String getRepositoryPwd() {
            return null; 
        }

        public String getLocalHistoryEnabled() {
            return null; 
        }

        public String getLocalHistoryTimeout() {
            return null; 
        }

        public Repository getRepository(ServletContext context) {
            return this.repository;
        }
    }
}
