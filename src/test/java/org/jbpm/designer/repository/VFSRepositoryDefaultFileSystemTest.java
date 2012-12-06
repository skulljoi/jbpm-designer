package org.jbpm.designer.repository;

import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.repository.vfs.VFSRepository;
import org.jbpm.designer.web.profile.impl.JbpmProfileImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class VFSRepositoryDefaultFileSystemTest {

    // TODO change it to generic independent path
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
    public void testCreateDefaultVFSRepository() {


        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<String> directories = repository.listDirectories("/");
        assertNotNull(directories);
        assertEquals(0, directories.size());
    }

    @Test
    public void testCreateDirectory() {
        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/test");
        assertFalse(rootFolderExists);

        String directoryId = repository.storeDirectory("/test");
        assertNotNull(directoryId);

        rootFolderExists = repository.directoryExists("/test");
        assertTrue(rootFolderExists);
    }

    @Test
    public void testDeleteDirectory() {
        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/test");
        assertFalse(rootFolderExists);

        String directoryId = repository.storeDirectory("/test");
        assertNotNull(directoryId);

        rootFolderExists = repository.directoryExists("/test");
        assertTrue(rootFolderExists);

        boolean deleted = repository.deleteDirectory("/test", true);
        assertTrue(deleted);

        rootFolderExists = repository.directoryExists("/test");
        assertFalse(rootFolderExists);

    }

    @Test
    public void testDeleteNonEmptyDirectory() {
        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/test");
        assertFalse(rootFolderExists);

        String directoryId = repository.storeDirectory("/test/nested");
        assertNotNull(directoryId);

        rootFolderExists = repository.directoryExists("/test");
        assertTrue(rootFolderExists);

        rootFolderExists = repository.directoryExists("/test/nested");
        assertTrue(rootFolderExists);

        boolean deleted = repository.deleteDirectory("/test", false);
        assertTrue(deleted);

        rootFolderExists = repository.directoryExists("/test");
        assertFalse(rootFolderExists);

    }

    @Test
    public void testListAsset() {

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        try {
            new File(REPOSITORY_ROOT + "/" + "test.txt").createNewFile();
            new File(REPOSITORY_ROOT + "/" + "test.png").createNewFile();
        } catch (Exception e) {

        }

        assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(2, assets.size());
    }

    @Test
    public void testListSingleTextAsset() {

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        try {
            new File(REPOSITORY_ROOT + "/" + "test.txt").createNewFile();
        } catch (Exception e) {

        }

        assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(1, assets.size());
        Asset<String> asset = assets.iterator().next();

        assertEquals("txt", asset.getAssetType());
        assertEquals("test.txt", asset.getName());
        assertEquals("/", asset.getAssetLocation());
    }

    @Test
    public void testListSingleBinaryAsset() {

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        try {
            new File(REPOSITORY_ROOT + "/" + "test.png").createNewFile();
        } catch (Exception e) {

        }

        assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(1, assets.size());

        Asset<byte[]> asset = assets.iterator().next();

        assertEquals("png", asset.getAssetType());
        assertEquals("test.png", asset.getName());
        assertEquals("/", asset.getAssetLocation());
    }

    @Test
    public void testListNestedSingleTextAsset() {

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        String directoryId = repository.storeDirectory("/test/nested");
        assertNotNull(directoryId);

        Collection<Asset> assets = repository.listAssets("/test/nested");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        try {
            new File(REPOSITORY_ROOT + "/test/nested/" + "test.txt").createNewFile();
        } catch (Exception e) {

        }

        assets = repository.listAssets("/test/nested");
        assertNotNull(assets);
        assertEquals(1, assets.size());
        Asset<String> asset = assets.iterator().next();

        assertEquals("txt", asset.getAssetType());
        assertEquals("test.txt", asset.getName());
        assertEquals("/test/nested/", asset.getAssetLocation());
    }

    @Test
    public void testStoreSingleBinaryAsset() throws AssetNotFoundException{

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Byte);
        builder.content("simple content".getBytes())
                .type("png")
                .name("test.png")
                .location("/");

        String id = repository.storeAsset(builder.getAsset());

        assertNotNull(id);

        Asset<byte[]> asset = repository.loadAsset(id);

        assertEquals("png", asset.getAssetType());
        assertEquals("test.png", asset.getName());
        assertEquals("/", asset.getAssetLocation());
        assertFalse(asset.getAssetContent().length == 0);
    }

    @Test
    public void testStoreSingleTextAsset() throws AssetNotFoundException{

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("simple content")
                .type("txt")
                .name("test.txt")
                .location("/");

        String id = repository.storeAsset(builder.getAsset());

        assertNotNull(id);

        Asset<String> asset = repository.loadAsset(id);

        assertEquals("txt", asset.getAssetType());
        assertEquals("test.txt", asset.getName());
        assertEquals("/", asset.getAssetLocation());
        assertEquals("simple content\n", asset.getAssetContent());
    }

    @Test
    public void testAssetExists() throws AssetNotFoundException{

        Repository repository = new VFSRepository(profile);

        boolean rootFolderExists = repository.directoryExists("/");
        assertTrue(rootFolderExists);

        Collection<Asset> assets = repository.listAssets("/");
        assertNotNull(assets);
        assertEquals(0, assets.size());

        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("simple content")
                .type("txt")
                .name("test.txt")
                .location("/");

        String id = repository.storeAsset(builder.getAsset());

        assertNotNull(id);

        boolean assetExists = repository.assetExists(id);
        assertTrue(assetExists);
    }
}
