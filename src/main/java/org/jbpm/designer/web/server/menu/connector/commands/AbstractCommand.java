package org.jbpm.designer.web.server.menu.connector.commands;

import org.apache.log4j.Logger;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.AssetTypeMapper;
import org.jbpm.designer.repository.Directory;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public abstract class AbstractCommand {
    private static Logger logger = Logger.getLogger(AbstractCommand.class);

    public JSONObject listContent(IDiagramProfile profile, String target, String current, boolean tree) throws Exception {
        try {
            if(target == null || target.length() < 1) {
                target = "/";
            } else if(!target.startsWith("/")) {
                target = "/" + target;
            }
            JSONObject retObj = new JSONObject();
            retObj.put("cwd", getCwd(profile, target, tree));
            retObj.put("cdc", getCdc(profile, target, tree));
            if(target == "/") {
                retObj.put("tree", getTree(profile, target, tree));
            }
            addParams(retObj);
            return retObj;
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return new JSONObject();
        }
    }

    public JSONObject makeDirectory(IDiagramProfile profile, String current, String name, boolean tree) throws Exception {
        if(current == null || current.length() < 1) {
            current = "/";
        } else if(!current.startsWith("/")) {
            current = "/" + current;
        }

        Directory newDir = null;
        try {
            newDir = profile.getRepository().createDirectory(current + "/" + name);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        JSONObject retObj = new JSONObject();
        retObj.put("cwd", getCwd(profile, current, tree));
        retObj.put("cdc", getCdc(profile, current, tree));
        retObj.put("tree", getTree(profile, "/", tree));
        retObj.put("select", newDir == null ? "" : newDir.getName());

        return retObj;
    }

    public JSONObject makeFile(IDiagramProfile profile, String current, String name, boolean tree) throws Exception {
        if(current == null || current.length() < 1) {
            current = "/";
        } else if(!current.startsWith("/")) {
            current = "/" + current;
        }

        if(name.endsWith(".bpmn2")) {
            name = name.substring(0, name.length() - 6);
        } else if(name.endsWith(("bpmn"))) {
            name = name.substring(0, name.length() - 5);
        }
        System.out.println("************* NEW NAME: " + name);
        String fullName = name + ".bpmn2";

        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("")
                .type("bpmn2")
                .name(name)
                .location(current);

        String newFileId = profile.getRepository().createAsset(builder.getAsset());
        if(newFileId == null) {
            logger.error("Unable to create asset: " + current + "/" + fullName);
        }

        JSONObject retObj = new JSONObject();
        retObj.put("cwd", getCwd(profile, current, tree));
        retObj.put("cdc", getCdc(profile, current, tree));
        retObj.put("tree", getTree(profile, "/", tree));
        retObj.put("select", newFileId == null ? "" : current + "/" + fullName);

        return retObj;
    }

    public Map<String, Object> getTree(IDiagramProfile profile, String path, boolean tree) throws Exception {
        String qname = "";
        if(path != "/") {
            String[] pathParts = path.split("/");
            qname = pathParts[pathParts.length - 1];
        } else {
            qname = path;
        }

        Map<String, Object> info = new HashMap<String, Object>();
        info.put("hash", path);
        info.put("name", qname);
        info.put("read", "true");
        info.put("write", "true");

        Collection<Directory> subdirs = profile.getRepository().listDirectories(path);
        List<Object> dirs = new ArrayList<Object>();
        if(subdirs != null) {
            for(Directory sub : subdirs) {
                dirs.add(getTree(profile, path.endsWith("/") ? path+sub.getName() : path + "/" + sub.getName(), tree));
            }
        }

        info.put("dirs", dirs);
        return info;
    }

    public List<Map<String, Object>> getCdc(IDiagramProfile profile, String path, boolean tree) throws Exception {
        List<Map<String, Object>> cdcinfo = new ArrayList<Map<String, Object>>();
        System.out.println("***************** PATH: " + path);
        Collection<Asset> assets = profile.getRepository().listAssets(path);
        Collection<Directory> dirs = profile.getRepository().listDirectories(path);

        if(assets != null) {
            for(Asset asset : assets) {
                cdcinfo.add(getAssetInfo(profile, asset));
            }
        }
        if(dirs != null) {
            for(Directory dir : dirs) {
                cdcinfo.add(getDirectoryInfo(profile,dir));
            }
        }
        return cdcinfo;
    }

    public Map<String, Object> getCwd(IDiagramProfile profile, String path, boolean tree) throws Exception {
        Map<String, Object> cwdinfo = new HashMap<String, Object>();
        cwdinfo.put("hash", path);
        cwdinfo.put("name", path);
        cwdinfo.put("mime", "directory");
        cwdinfo.put("rel", path);
        cwdinfo.put("size", "0");
        cwdinfo.put("date", ""); // TODO fix
        cwdinfo.put("read", true);
        cwdinfo.put("write", true);
        cwdinfo.put("rm", false);
        return cwdinfo;
    }

    public void addParams(JSONObject retObj) throws Exception {
        JSONObject paramsObj = new JSONObject();
        paramsObj.put("dotFiles", "true");
        paramsObj.put("archives", new JSONArray());
        paramsObj.put("uplMaxSize", "100M");
        paramsObj.put("url", "");
        paramsObj.put("extract", new JSONArray());
        retObj.put("params", paramsObj);
        retObj.put("disabled", new JSONArray());
    }

    protected Map<String, Object> getDirectoryInfo(IDiagramProfile profile, Directory dir) {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("name", dir.getName());
        info.put("hash", dir.getLocation() + "/" + dir.getName());
        info.put("mime", "directory");
        info.put("date", "");
        info.put("size", "");
        info.put("read", true);
        info.put("write", true);
        info.put("rm",  true);
        info.put("url",  "");
        info.put("tmb", "");

        return info;
    }

    protected Map<String, Object> getAssetInfo(IDiagramProfile profile, Asset asset) {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("name", asset.getFullName());
        info.put("hash", asset.getAssetLocation() + "/" + asset.getFullName());
        info.put("mime", AssetTypeMapper.findMimeType(asset));
        info.put("date", asset.getLastModificationDate());
        info.put("size", "");
        info.put("read", true);
        info.put("write", true);
        info.put("rm",  true);
        info.put("url", asset.getAssetType() + "|" + asset.getUniqueId());
        return info;
    }
}
