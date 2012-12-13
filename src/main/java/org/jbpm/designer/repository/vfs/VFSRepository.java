package org.jbpm.designer.repository.vfs;

import org.apache.commons.codec.binary.Base64;
import org.jbpm.designer.repository.*;
import org.jbpm.designer.repository.impl.AbstractAsset;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.commons.java.nio.IOException;
import org.kie.commons.java.nio.file.*;
import org.kie.commons.java.nio.file.attribute.BasicFileAttributes;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import static org.kie.commons.io.FileSystemType.Bootstrap.BOOTSTRAP_INSTANCE;

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

    public Collection<Asset> listAssetsRecursively(String startAt, final Filter filter) {
        final Collection<Asset> foundAssets = new ArrayList<Asset>();
        Path path = Paths.get(repositoryRoot.toString() + startAt);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

           public FileVisitResult visitFile(Path paths, BasicFileAttributes basicFileAttributes) throws IOException {
               if (filter.accept(paths)) {
                   foundAssets.add(buildAsset(paths, false));
               }
               return FileVisitResult.CONTINUE;
           }

        });

        return foundAssets;
    }

    public String createDirectory(String location) {
        Path path = Paths.get(repositoryRoot.toString() + location);

        path = ioService.createDirectories(path);

        return path.toUri().toString();
    }

    public boolean directoryExists(String directory) {
        Path path = Paths.get(repositoryRoot.toString()+ directory);

        return ioService.exists(path);
    }

    public boolean deleteDirectory(String directory, boolean failIfNotEmpty) {

        try {
            Path path = Paths.get(repositoryRoot.toString() + directory);

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path paths, BasicFileAttributes basicFileAttributes) throws IOException {

                    ioService.delete(paths);

                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        ioService.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }

            });

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

    public Collection<Asset> listAssets(String location, final Filter filter) {
        Path path = Paths.get(repositoryRoot.toString() + location);
        DirectoryStream<Path> directories = ioService.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {

            public boolean accept( final Path entry ) throws IOException {

                return filter.accept(entry);
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

    public Asset loadAsset(String assetUniqueId) throws AssetNotFoundException {
        String uniqueId = decodeUniqueId(assetUniqueId);
        Path assetPath = Paths.get(URI.create(uniqueId));

        Asset asset = buildAsset(assetPath, true);

        return asset;
    }

    public Asset loadAssetFromPath(String location) throws AssetNotFoundException {
        Path path = Paths.get(repositoryRoot.toString() + location);

        if (ioService.exists(path)) {
            return loadAsset(path.toUri().toString());
        } else {
            throw new AssetNotFoundException();
        }

    }

    public String createAsset(Asset asset) {
        Path filePath = Paths.get(repositoryRootPath.toString() + (asset.getAssetLocation().equals("/")?"":asset.getAssetLocation()), asset.getFullName());
        if (!ioService.exists(filePath.getParent())) {
            ioService.createDirectories(filePath.getParent());
        }
        filePath = ioService.createFile(filePath, null);
        if(((AbstractAsset)asset).acceptBytes()) {
            ioService.write(filePath, ((Asset<byte[]>)asset).getAssetContent(), null);
        } else {
            ioService.write(filePath, asset.getAssetContent().toString().getBytes(), null);
        }

        return encodeUniqueId(filePath.toUri().toString());
    }

    public String updateAsset(Asset asset) throws AssetNotFoundException {
        String uniqueId = decodeUniqueId(asset.getUniqueId());
        Path filePath = Paths.get(URI.create(uniqueId));
        if (!ioService.exists(filePath)) {
            throw new AssetNotFoundException();
        }
        if(((AbstractAsset)asset).acceptBytes()) {
            ioService.write(filePath, ((Asset<byte[]>)asset).getAssetContent(), StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            ioService.write(filePath, asset.getAssetContent().toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }

        return asset.getUniqueId();
    }

    public boolean deleteAsset(String assetUniqueId) {
        String uniqueId = decodeUniqueId(assetUniqueId);
        try {
            return ioService.deleteIfExists(Paths.get(URI.create(uniqueId)));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteAssetFromPath(String path) {

        Path filePath = Paths.get(repositoryRoot.toString() + path);

        return deleteAsset(filePath.toUri().toString());
    }

    public boolean assetExists(String assetUniqueId) {
        String uniqueId = decodeUniqueId(assetUniqueId);
        try {
            return ioService.exists(Paths.get(URI.create(uniqueId)));
        } catch (Exception e) {
            return ioService.exists(Paths.get(this.repositoryRoot.toString() + assetUniqueId));
        }
    }

    protected Asset buildAsset(Path file, boolean loadContent) {

        String name = file.getName(file.getNameCount()-1).toString();
        String location = file.toString().replaceFirst(this.repositoryRootPath.toString(), "").replaceFirst(name, "");

        AssetBuilder assetBuilder = AssetBuilderFactory.getAssetBuilder(file.getFileName().toString());
        BasicFileAttributes attrs = ioService.readAttributes(file, BasicFileAttributes.class);
        assetBuilder.uniqueId(encodeUniqueId(file.toUri().toString()))
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

    private String decodeUniqueId(String uniqueId) {
        if (Base64.isBase64(uniqueId)) {
            byte[] decoded = Base64.decodeBase64(uniqueId);
            try {
                return new String(decoded, "UTF-8");
            } catch (UnsupportedEncodingException e) {

            }
        }

        return uniqueId;
    }

    private String encodeUniqueId(String uniqueId) {
        try {
            return Base64.encodeBase64URLSafeString(uniqueId.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
