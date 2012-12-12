package org.jbpm.designer.repository.servlet;

import org.apache.log4j.Logger;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.AssetNotFoundException;
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

import static org.junit.Assert.assertNotNull;

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
    private static final String ACTION_LOAD_ASSET = "loadasset";

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
        JSONObject returnObj = new JSONObject();
        JSONArray errorsArray = new JSONArray();

        try {
            IDiagramProfile profile = ServletUtil.getProfile(req, profileName, getServletContext());
            Repository repository = profile.getRepository(getServletContext());

            if(action != null && action.equals(ACTION_STORE_ASSET)) {
                try {
                    AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
                    builder.content(assetContent)
                            .type(assetType)
                            .name(assetName)
                            .location(assetLocation);
                    String id = repository.storeAsset(builder.getAsset());
                    if(id == null) {
                        addError(errorsArray, "Unable to store asset");
                    }
                } catch (Exception e) {
                    addError(errorsArray, "Error storing asset: " + e.getMessage());
                }
            } else if(action != null && action.equals(ACTION_DELETE_ASSET)) {
                try {
                    Boolean ret = repository.deleteAsset(assetId);
                    if(!ret) {
                        addError(errorsArray, "Unable to delete asset");
                    }
                } catch (AssetNotFoundException e) {
                    addError(errorsArray, "Error deleting asset: " + e.getMessage());
                }
            } else if(action != null && action.equals(ACTION_ASSET_EXISTS)) {
                try {
                    Boolean ret = repository.assetExists(assetId);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
            } else if(action != null && action.equals(ACTION_CREATE_DIRECTORY)) {
                String ret = repository.storeDirectory(assetLocation);
                if(ret == null) {
                    addError(errorsArray, "Unable to create asset");
                }
            } else if(action != null && action.equals(ACTION_DELETE_DIRECTORY)) {
                try {
                    Boolean ret = repository.deleteDirectory(assetLocation, false);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
            } else if(action != null && action.equals(ACTION_DIRECTORY_EXISTS)) {
                try {
                    Boolean ret = repository.directoryExists(assetLocation);
                    returnObj.put("answer", String.valueOf(ret));
                } catch (Exception e) {
                    returnObj.put("answer", "false");
                    addError(errorsArray, "Error: " + e.getMessage());
                }
            } else if(action != null && action.equals(ACTION_LIST_DIRECTORIES)) {
                Collection<Asset> dirCollection = repository.listAssets(assetLocation);
                if(dirCollection!= null) {
                    for(Asset dir : dirCollection) {
                        // TODO finish
                    }
                } else {

                }

            } else if(action != null && action.equals(ACTION_LIST_ASSETS)) {
                // TODO finish

            } else if(action != null && action.equals(ACTION_LOAD_ASSET)) {
                // TODO finish

            } else {
                addError(errorsArray, "Invalid action specified");
            }

            returnObj.put("errors", errorsArray);

            PrintWriter pw = resp.getWriter();
            resp.setContentType("text/json");
            resp.setCharacterEncoding("UTF-8");
            pw.write(returnObj.toString());
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
}
