package org.jbpm.designer.repository.vfs;

import org.jbpm.designer.repository.*;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import org.jbpm.designer.repository.impl.AbstractAsset;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.commons.java.nio.IOException;
import org.kie.commons.java.nio.file.*;
import org.kie.commons.java.nio.file.attribute.BasicFileAttributes;
import org.kie.commons.java.nio.file.attribute.FileAttribute;

import static org.kie.commons.io.FileSystemType.Bootstrap.*;
import static org.kie.commons.validation.PortablePreconditions.*;

public class VFSRepository implements Repository {

    private final IOService ioService = new IOServiceDotFileImpl();

    private URI repositoryRoot;
    private Path repositoryRootPath;

    public VFSRepository(IDiagramProfile profile) {
        // TODO build env from profile params?
        Map<String, Object> env = new HashMap<String, Object>();
        this.repositoryRoot = URI.create(profile.getRepositoryRoot());
        this.repositoryRootPath = Paths.get(this.repositoryRoot);

        if ( ioService.getFileSystem( this.repositoryRoot ) == null ) {

            ioService.newFileSystem( this.repositoryRoot, env, BOOTSTRAP_INSTANCE );
            ioService.createDirectories(Paths.get(this.repositoryRoot));
        }
    }
    
    public Collection<String> listDirectories(String startAt) {
        Path path = Paths.get(repositoryRoot.toString() + startAt);
        DirectoryStream<Path> directories = ioService.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {

            public boolean accept( final Path entry ) throws IOException {
                if ( Files.isDirectory(entry) ) {
                    return true;
                }
                return false;
            }
        });
        Collection<String> foundDirectories = new ArrayList<String>();
        Iterator<Path> it = directories.iterator();
        while (it.hasNext()) {
            foundDirectories.add(it.next().getFileName().toString());
        }

        return foundDirectories;
    }

    public Map<String, Collection<Asset>> listDirectoriesRecursively(String startAt) {
        throw new UnsupportedOperationException();
    }

    public String storeDirectory(String location) {
        Path path = Paths.get(repositoryRoot.toString() + location);

        path = ioService.createDirectories(path);

        return path.toUri().toString();
    }

    public boolean directoryExists(String directory) {
        Path path = Paths.get(repositoryRoot.toString()+ directory);

        return ioService.exists(path);
    }

    public boolean deleteDirectory(String directory, boolean failIfNotEmpty) {
        // TODO improve as soon as walkFileTree methods are provided on ioService
        // before it gets use of walkFileTree if could have unexpected results for different file system providers
        try {
            Path path = Paths.get(repositoryRoot.toString() + directory);
            File file = path.toFile();
            if (failIfNotEmpty && file.listFiles().length > 0) {
                throw new IllegalArgumentException("Directory " + directory + " is not empty");
            }
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteDirectory(f.getAbsolutePath().replaceFirst(repositoryRoot.getPath(), ""), failIfNotEmpty);
                }
            }
            ioService.delete(path);

            return true;
        } catch (Exception e)  {
            return false;
        }
    }

    public Collection<Asset> listAssets(String location) {
        Path path = Paths.get(repositoryRoot.toString() + location);
        DirectoryStream<Path> directories = ioService.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {

            public boolean accept( final Path entry ) throws IOException {
                if (!Files.isDirectory(entry)) {
                    return true;
                }
                return false;
            }
        });
        Collection<Asset> foundDirectories = new ArrayList<Asset>();
        Iterator<Path> it = directories.iterator();
        while (it.hasNext()) {
            Asset asset = buildAsset(it.next(), false);
            foundDirectories.add(asset);
        }

        return foundDirectories;
    }

    public Collection<Asset> listAssets(String location, Filter filter) {
        throw new UnsupportedOperationException();
    }

    public Asset loadAsset(String assetUniqueId) throws AssetNotFoundException {

        Path assetPath = Paths.get(URI.create(assetUniqueId));

        Asset asset = buildAsset(assetPath, true);

        return asset;
    }

    public String storeAsset(Asset asset) {
        Path filePath = Paths.get(repositoryRootPath.toString() + (asset.getAssetLocation().equals("/")?"":asset.getAssetLocation()), asset.getName());

        filePath = ioService.createFile(filePath, null);
        if(((AbstractAsset)asset).acceptBytes()) {
            ioService.write(filePath, ((Asset<byte[]>)asset).getAssetContent(), null);
        } else {
            ioService.write(filePath, asset.getAssetContent().toString().getBytes(), null);
        }

        return filePath.toUri().toString();
    }

    public boolean deleteAsset(String assetUniqueId) throws AssetNotFoundException {

        try {
            ioService.delete(Paths.get(URI.create(assetUniqueId)));
            return true;
        } catch (NoSuchFileException fe) {
            throw new AssetNotFoundException();
        }   catch (Exception e) {
            return false;
        }
    }

    public boolean assetExists(String assetUniqueId) {
        return ioService.exists(Paths.get(URI.create(assetUniqueId)));
    }

    protected Asset buildAsset(Path file, boolean loadContent) {

        String name = file.getName(file.getNameCount()-1).toString();
        String location = file.toString().replaceFirst(this.repositoryRootPath.toString(), "").replaceFirst(name, "");

        AssetBuilder assetBuilder = AssetBuilderFactory.getAssetBuilder(file.getFileName().toString());
        BasicFileAttributes attrs = ioService.readAttributes(file, BasicFileAttributes.class);
        assetBuilder.uniqueId(file.toUri().toString())
                    .name(name)
                    .location(location)
                    .creationDate(attrs.creationTime() == null ? "" : attrs.creationTime().toString())
                    .lastModificationDate(attrs.lastModifiedTime() == null ? "" : attrs.lastModifiedTime().toString())
                    // TODO some provider specific details
                    .description("")
                    .owner("");

        if (loadContent) {
            if (((AbstractAsset)assetBuilder.getAsset()).acceptBytes()) {
                assetBuilder.content(ioService.readAllBytes(file));
            } else {
                assetBuilder.content(ioService.readAllString(file, Charset.forName("UTF-8")));
            }
        }

        return assetBuilder.getAsset();
    }
}
