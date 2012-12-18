package org.jbpm.designer.web.server.integration;

import static junit.framework.Assert.*;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.server.ServletUtil;
import org.jbpm.designer.web.server.integration.base.AbstractGuvnorIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class ServletUtilTest extends AbstractGuvnorIntegrationTest {

    @Test
    @InSequence(0)
    public void setup() throws Exception { 
        // fill guvnor with packages and assets
        setupGuvnor(guvnorUrl, profile);
    }
    
    /**
     * Test ServletUtil.findPackageAndAssetInfo(origUuid, profile);
     * @throws Exception
     */
    @Test
    @InSequence(1)
    public void findPackageAndAssetInfoTest() {
        runFindPackageAndAssetInfoTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runFindPackageAndAssetInfoTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) {
        // Test method results (1 asset in package)
        String origPkg = packageNameList[1];
        String origUuid = packageToAssetUuidListMap.get(origPkg).get(0);

        String[] info = ServletUtil.findPackageAndAssetInfo(origUuid, profile);
        assertEquals("Retrieved package name is not equal.", origPkg, info[0]);
        assertEquals("Retrieved asset name is not equal.", uuidAssetMap.get(origUuid), info[1]);

        // Test method results (3 assets in package)
        origPkg = packageNameList[2];
        origUuid = packageToAssetUuidListMap.get(origPkg).get(1);

        info = ServletUtil.findPackageAndAssetInfo(origUuid, profile);
        assertEquals("Retrieved package name is not equal", origPkg, info[0]);
        assertEquals("Retrieved asset name is not equal.", uuidAssetMap.get(origUuid), info[1]);
    }

    /**
     * Test ServletUtil.assetExistsInGuvnor(packageName, assetName, profile);
     * @throws Exception
     */
    @Test
    @InSequence(2)
    public void assetExistsInGuvnorTest() { 
        runAssetExistsInGuvnorTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runAssetExistsInGuvnorTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) { 
       
        String packageName = packageNameList[1];
        String assetName = packageToAssetNameListMap.get(packageName).get(0);
        
        // test for false negative (says "NOT exists!" when it _does_ exist)
        boolean exists = ServletUtil.assetExistsInGuvnor(packageName, assetName, profile);
        assertTrue( assetName + " in pkg " + packageName + " exists!", exists );
        
        // test for false positive (says "existsi!" when it doesn't)
        exists = ServletUtil.assetExistsInGuvnor(packageName, UUID.randomUUID().toString(), profile);
        assertTrue( assetName + " in pkg " + packageName + " does not exist!", ! exists );
    }
    
    @Test
    @InSequence(3)
    public void getAllProcessesInPackageTest() { 
        runGetAllProcessesInPackageTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runGetAllProcessesInPackageTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) { 
        String pkgName = packageNameList[2];
        List<String> processAssetNames = ServletUtil.getAllProcessesInPackage(pkgName, profile);
        assertTrue( "Expected 3 processes, not " + processAssetNames.size(), processAssetNames.size() == 3);
       
        for( String retrievedProcess : processAssetNames ) { 
            assertTrue(packageToAssetNameListMap.get(pkgName).contains(retrievedProcess));
        }
    }
    
}
