package org.jbpm.designer.web.server;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.AssetNotFoundException;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.profile.IDiagramProfileService;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/** 
 * 
 * Interaction with task forms for inline editor.
 * 
 * @author Tihomir Surdilovic
 */
public class TaskFormsEditorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(TaskFormsEditorServlet.class);
	private static final String TASKFORM_FILE_EXTENSION = "ftl";
    private static final String FORMMODELER_FILE_EXTENSION = "form";
	private static final String TASKFORM_NAME_EXTENSION = "-taskform";
	private static final String ACTION_LOAD = "load";
	private static final String ACTION_SAVE = "save";

    private IDiagramProfile profile;
    // this is here just for unit testing purpose
    public void setProfile(IDiagramProfile profile) {
        this.profile = profile;
    }

    @Inject
    private IDiagramProfileService _profileService = null;

	 @Override
	 public void init(ServletConfig config) throws ServletException {
		 super.init(config);
	 }
	 
	 @Override
	 protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		 String action = req.getParameter("action");
	     String uuid = req.getParameter("uuid");
	     String profileName = req.getParameter("profile");
	     String taskName = req.getParameter("taskname");
	     String taskFormValue = req.getParameter("tfvalue");
         String formType = req.getParameter("formtype");

         if (profile == null) {
	        profile = _profileService.findProfile(req, profileName);
         }
         Repository repository = profile.getRepository();

         Asset<String> processAsset = null;
         try {
             processAsset = repository.loadAsset(uuid);


             if(action != null && action.equals(ACTION_LOAD)) {
                 resp.setContentType("text/html");
                 resp.setCharacterEncoding("UTF-8");
                 PrintWriter pw = resp.getWriter();
                 String taskResponse = getTaskFormFromRepository(formType, taskName, processAsset.getAssetLocation(), repository);
                 pw.write(taskResponse);
             } else if(action != null && action.equals(ACTION_SAVE)) {
                 resp.setContentType("text/plain");
                 resp.setCharacterEncoding("UTF-8");
                 PrintWriter pw = resp.getWriter();
                 try {
                    pw.write(storeTaskFormInRepository(formType, taskName, processAsset.getAssetLocation(), taskFormValue, repository));
                } catch (Exception e) {
                    pw.write("error: " + e.getMessage());
                }
             }
         } catch (Exception e) {
             PrintWriter pw = resp.getWriter();
             pw.write("error: " + e.getMessage());
         }
	 }
	 
	 private String storeTaskFormInRepository(String formType, String taskName, String packageName, String formValue, Repository repository) throws Exception{

        repository.deleteAssetFromPath(packageName + taskName + TASKFORM_NAME_EXTENSION + "." + formType);

        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Byte);
        builder.location(packageName)
                .name(taskName + TASKFORM_NAME_EXTENSION)
                .type(formType)
                .content(formValue.getBytes("UTF-8"));

        repository.createAsset(builder.getAsset());
        return "ok";
	 }
	 
	 private String getTaskFormFromRepository(String formType, String taskName, String packageName, Repository repository) {
         try {
             Asset<String> formAsset = repository.loadAssetFromPath(packageName + "/" + taskName + TASKFORM_NAME_EXTENSION + "." + formType);

             if(formType.equals(FORMMODELER_FILE_EXTENSION)) {
                 String uniqueId = formAsset.getUniqueId();
                 if (Base64.isBase64(uniqueId)) {
                     byte[] decoded = Base64.decodeBase64(uniqueId);
                     try {
                         uniqueId =  new String(decoded, "UTF-8");
                     } catch (UnsupportedEncodingException e) {
                         e.printStackTrace();
                     }
                 }
                 return formAsset.getName() + "." + formAsset.getAssetType() + "|" + uniqueId;
             } else {
                 return formAsset.getAssetContent();
             }
         } catch (AssetNotFoundException anfe) {
             try {
                 // create empty
                 String formValue = "";
                 AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Byte);
                 builder.location(packageName)
                         .name(taskName + TASKFORM_NAME_EXTENSION)
                         .type(formType)
                         .content(formValue.getBytes("UTF-8"));
                 repository.createAsset(builder.getAsset());

                 Asset<String> newFormAsset = repository.loadAssetFromPath(packageName + "/" + taskName + TASKFORM_NAME_EXTENSION + "." + formType);

                 if(formType.equals(FORMMODELER_FILE_EXTENSION)) {
                     String uniqueId = newFormAsset.getUniqueId();
                     if (Base64.isBase64(uniqueId)) {
                         byte[] decoded = Base64.decodeBase64(uniqueId);
                         try {
                             uniqueId =  new String(decoded, "UTF-8");
                         } catch (UnsupportedEncodingException e) {
                             e.printStackTrace();
                         }
                     }

                     return newFormAsset.getName() + "." + newFormAsset.getAssetType() + "|" + uniqueId;
                 } else {
                    return formValue;
                 }
             } catch(Exception e) {
                 e.printStackTrace();
                 _logger.error(e.getMessage());
             }
         }
         return "false";
     }
}
