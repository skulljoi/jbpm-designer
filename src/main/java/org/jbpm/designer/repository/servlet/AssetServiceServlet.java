package org.jbpm.designer.repository.servlet;

import org.apache.log4j.Logger;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.server.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Servlet wraps Repository asset api.
 */
public class AssetServiceServlet extends HttpServlet {
    private static final Logger _logger = Logger.getLogger(AssetServiceServlet.class);
    private static final String ACTION_STORE_ASSET    = "storeasset";
    private static final String ACTION_DELETE_ASSET   = "deleteasset";
    private static final String ACTION_ASSET_EXISTS = "existsasset";
    private static final String ACTION_CREATE_DIRECTORY = "createdir";
    private static final String ACTION_DELETE_DIRECTORY = "deletedir";
    private static final String ACTION_DIRECTORY_EXISTS = "existsdir";
    private static final String ACTION_LIST_DIRECTORIES = "listdirs";
    private static final String ACTION_LIST_ASSETS = "listassets";
    private static final String ACTION_GET_ASSET_INFO = "getassetinfo";
    private static final String ACTION_GET_ASSET_SOURCE = "getassetsource";
    private static final String OPTION_BY_PATH = "optionbypath";
    private static final String OPTION_BY_ID = "optionbyid";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String profileName = req.getParameter("profile");
        String action = req.getParameter("action");
        String preprocessingData = req.getParameter("pp");
        String assetId = req.getParameter("assetid");
        String assetType = req.getParameter("assettype");
        String assetName = req.getParameter("assetname");
        String assetContent = req.getParameter("assetcontent");
        String assetLocation = req.getParameter("assetlocation");
        String loadoption = req.getParameter("loadoption");
        JSONObject returnObj = new JSONObject();
        JSONArray errorsArray = new JSONArray();

        try {
            IDiagramProfile profile = ServletUtil.getProfile(req, profileName, getServletContext());
            Repository repository = profile.getRepository();

            if(action != null && action.equals(ACTION_STORE_ASSET)) {
                try {
                    AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
                    builder.content(assetContent)
                            .type(assetType)
                            .name(assetName)
                            .location(assetLocation);
                    String id = repository.createAsset(builder.getAsset());
                    if(id == null) {
                        addError(errorsArray, "Unable to store asset");
                    }
                } catch (Exception e) {
                    addError(errorsArray, "Error storing asset: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_DELETE_ASSET)) {
                try {
                    Boolean ret = repository.deleteAsset(assetId);
                    if(!ret) {
                        addError(errorsArray, "Unable to delete asset");
                    }
                } catch (Exception e) {
                    addError(errorsArray, "Error deleting asset: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_ASSET_EXISTS)) {
                try {
                    Boolean ret = repository.assetExists(assetId);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_CREATE_DIRECTORY)) {
                String ret = repository.createDirectory(assetLocation);
                if(ret == null) {
                    addError(errorsArray, "Unable to create asset");
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_DELETE_DIRECTORY)) {
                try {
                    Boolean ret = repository.deleteDirectory(assetLocation, false);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_DIRECTORY_EXISTS)) {
                try {
                    Boolean ret = repository.directoryExists(assetLocation);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_LIST_DIRECTORIES)) {
                try {
                    Collection<String> dirCollection = repository.listDirectories(assetLocation);
                    if(dirCollection!= null) {
                        JSONArray dirListingArray = new JSONArray();
                        for(String dir : dirCollection) {
                            JSONObject dirObj = new JSONObject();
                            dirObj.put("name", dir);
                            dirListingArray.put(dirObj);
                        }
                        returnObj.put("answer", dirListingArray);
                    } else {
                        returnObj.put("answer", new JSONArray());
                    }
                } catch (Exception e) {
                    returnObj.put("answer", new JSONArray());
                    addError(errorsArray, "Error: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_LIST_ASSETS)) {
                try {
                    Collection<Asset> assetCollection = repository.listAssets(assetLocation);
                    if(assetCollection != null) {
                        JSONArray assetListingArray = new JSONArray();
                        for(Asset asset : assetCollection) {
                            JSONObject assetObj = new JSONObject();
                            assetObj.put("fullname", asset.getFullName());
                            assetObj.put("name", asset.getName());
                            assetObj.put("description", asset.getDescription());
                            assetObj.put("owner", asset.getOwner());
                            assetObj.put("version", asset.getVersion());
                            assetObj.put("id", asset.getUniqueId());
                            assetObj.put("location", asset.getAssetLocation());
                            assetObj.put("type", asset.getAssetType());
                            assetObj.put("created", asset.getCreationDate());
                            assetObj.put("modified", asset.getLastModificationDate());
                            assetListingArray.put(assetObj);
                        }
                        returnObj.put("answer" , assetListingArray);
                    } else {
                        returnObj.put("answer", new JSONArray());
                    }
                } catch (JSONException e) {
                    returnObj.put("answer", new JSONArray());
                    addError(errorsArray, "Error: " + e.getMessage());
                }
                returnJSONContent(returnObj, errorsArray, resp);
            } else if(action != null && action.equals(ACTION_GET_ASSET_SOURCE)) {
                Asset<String> asset;
                if(loadoption != null && loadoption.equals(OPTION_BY_ID)) {
                    asset = repository.loadAsset(assetId);
                } else if(loadoption != null && loadoption.equals(OPTION_BY_PATH)) {
                    asset = repository.loadAssetFromPath(assetLocation);
                } else {

                }

            } else if(action != null && action.equals(ACTION_GET_ASSET_INFO)) {
                // TODO FINISH
            } else {
                addError(errorsArray, "Invalid action specified");
                returnJSONContent(returnObj, errorsArray, resp);
            }
        } catch(Exception e) {
            _logger.error(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    private void addError(JSONArray errorArray, String errorMessage) {
        if(errorArray != null) {
            errorArray.put(errorMessage);
        }
    }

    private void returnJSONContent(JSONObject returnObj, JSONArray errorsArray, HttpServletResponse resp) throws Exception {
        returnObj.put("errors", errorsArray);
        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/json");
        resp.setCharacterEncoding("UTF-8");
        pw.write(returnObj.toString());
    }

    private void returnXMLContent(String content) {

    }

}
