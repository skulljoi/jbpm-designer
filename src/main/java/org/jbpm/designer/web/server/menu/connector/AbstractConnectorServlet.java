package org.jbpm.designer.web.server.menu.connector;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.server.ServletUtil;
import org.jbpm.designer.web.server.menu.connector.commands.MakeDirCommand;
import org.jbpm.designer.web.server.menu.connector.commands.MakeFileCommand;
import org.jbpm.designer.web.server.menu.connector.commands.OpenCommand;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public abstract class AbstractConnectorServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(AbstractConnectorServlet.class);

    private Map<String, Object> requestParams;
    private List<FileItemStream> listFiles;
    private List<ByteArrayOutputStream> listFileStreams;
    private boolean initialized = false;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void initializeDefaultRepo(IDiagramProfile profile, Repository repository, HttpServletRequest request) throws Exception {
        String sampleBpmn2 = getServletContext().getRealPath("/defaults/SampleProcess.bpmn2");
        createAssetIfNotExisting(repository, "/defaultPackage", "BPMN2-SampleProcess", "bpmn2", getBytesFromFile(new File(sampleBpmn2)));
        if(profile.getRepositoryGlobalDir() != null) {
            createDirectoryIfNotExist(repository, profile.getRepositoryGlobalDir());
        }
    }

    /**
     * Processing a new request from ElFinder client.
     * @param request
     * @param response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        parseRequest(request, response);
        IDiagramProfile profile = ServletUtil.getProfile(request, "jbpm", getServletContext());
        Repository repository = profile.getRepository();
        if(!initialized) {
            try {
                initializeDefaultRepo(profile, repository, request);
                initialized = true;
            } catch (Exception e) {
                logger.error("Unable to initialize repository: " + e.getMessage());
            }
        }
        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("*********************************** COMMAND: " + requestParams.get("cmd"));
            Iterator<String> keys = requestParams.keySet().iterator();
            while(keys.hasNext()) {
                String key = keys.next();
                System.out.println("******************************* PARAM: " + key + " -- val: " + requestParams.get(key));
            }

            String cmd = (String) requestParams.get("cmd");
            if(cmd != null && cmd.equals("open")) {
                OpenCommand command = new OpenCommand();
                command.init(request, response, profile, repository, requestParams);
                output(response, false, command.execute());
            } else if(cmd != null && cmd.equals("mkdir")) {
                MakeDirCommand command = new MakeDirCommand();
                command.init(request, response, profile, repository, requestParams);
                output(response, false, command.execute());
            } else if(cmd != null && cmd.equals("mkfile")) {
                MakeFileCommand command = new MakeFileCommand();
                command.init(request, response, profile, repository, requestParams);
                output(response, false, command.execute());
            } else if(cmd != null && cmd.equals("rename")) {
                // TODO FINISH
                // name is new name
                // target is full path of current dir to be renamed
                // current is current "parent" dir
            } else if(cmd != null && cmd.equals("paste")) {
                // TODO FINISH
            } else if(cmd != null && cmd.equals("paste")) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            putResponse(returnJson, "error", e.getMessage());

            // output the error
            try {
                output(response, false, returnJson);
            } catch (Exception ee) {
                logger.error("", ee);
            }
        }
    }

    protected static void output(HttpServletResponse response, boolean isResponseTextHtml, JSONObject json) {
        if (isResponseTextHtml) {
            response.setContentType("text/html; charset=UTF-8");
        } else {
            response.setContentType("application/json; charset=UTF-8");
        }
        System.out.println("******************* RESPONSE\n: " + json.toString());
        try {
            json.write(response.getWriter());
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Parse request parameters and files.
     * @param request
     * @param response
     */
    protected void parseRequest(HttpServletRequest request, HttpServletResponse response) {
        requestParams = new HashMap<String, Object>();
        listFiles = new ArrayList<FileItemStream>();
        listFileStreams = new ArrayList<ByteArrayOutputStream>();

        // Parse the request
        if (ServletFileUpload.isMultipartContent(request)) {
            // multipart request
            try {
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        requestParams.put(name, Streams.asString(stream));
                    } else {
                        String fileName = item.getName();
                        if (fileName != null && !"".equals(fileName.trim())) {
                            listFiles.add(item);

                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            IOUtils.copy(stream, os);
                            listFileStreams.add(os);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error parsing multipart content", e);
            }
        } else {
            // not a multipart
            for (Object mapKey : request.getParameterMap().keySet()) {
                String mapKeyString = (String) mapKey;

                if (mapKeyString.endsWith("[]")) {
                    // multiple values
                    String values[] = request.getParameterValues(mapKeyString);
                    List<String> listeValues = new ArrayList<String>();
                    for (String value : values) {
                        listeValues.add(value);
                    }
                    requestParams.put(mapKeyString, listeValues);
                } else {
                    // single value
                    String value = request.getParameter(mapKeyString);
                    requestParams.put(mapKeyString, value);
                }
            }
        }
    }

    /**
     * Instanciate command implementation and prepare it before execution.
     * @param commandStr
     * @param request
     * @param response
     * @param config
     * @return
     */
//    protected AbstractCommand prepareCommand(String commandStr, HttpServletRequest request, HttpServletResponse response, AbstractConnectorConfig config) {
//        if (commandStr != null) {
//            commandStr = commandStr.trim();
//        }
//
//        if (commandStr == null && "POST".equals(request.getMethod())) {
//            putResponse("error", "Data exceeds the maximum allowed size");
//        }
//
//        if (!config.isCommandAllowed(commandStr)) {
//            putResponse("error", "Permission denied");
//        }
//
//        AbstractCommand command = null;
//        if (commandStr != null) {
//            command = instanciateCommand(commandStr);
//            if (command == null) {
//                putResponse("error", "Unknown command");
//            }
//        } else {
//            String current = (String) request.getParameterMap().get("current");
//            if (current != null) {
//                command = new OpenCommand();
//            } else {
//                command = new ContentCommand();
//            }
//        }
//
//        command.setRequest(request);
//        command.setResponse(response);
//        command.setJson(json);
//        command.setRequestParameters(requestParams);
//        command.setListFiles(listFiles);
//        command.setListFileStreams(listFileStreams);
//        command.setConfig(config);
//
//        command.init();
//
//        return command;
//    }

    /**
     * Instanciate a command from its name.
     * @param commandName
     * @return
     */
//    protected AbstractCommand instanciateCommand(String commandName) {
//        AbstractCommand instance = null;
//        try {
//            Class<AbstractCommand> clazz = getCommandClass(commandName);
//            if (clazz != null) {
//                instance = clazz.newInstance();
//                if (instance == null) {
//                    throw new Exception("Command not found : " + commandName);
//                }
//            }
//        } catch (Exception e) {
//            // instance will be null
//            logger.error("Could not instance connector configuration", e);
//        }
//        return instance;
//    }

    /**
     * Get command class for a command name.
     * @param commandName
     * @return
     */
//    protected Class<AbstractCommand> getCommandClass(String commandName) {
//        // do we have override for command?
//        Class<AbstractCommand> clazz = getCommandClassOverride(commandName);
//        if (clazz == null) {
//            // no override, use the default command
//            clazz = getCommandClassDefault(commandName);
//        }
//        return clazz;
//    }

    /**
     * Get default implementation class for a command.
     * @param commandName
     * @return
     */
//    @SuppressWarnings("unchecked")
//    protected Class<AbstractCommand> getCommandClassDefault(String commandName) {
//        String className = AbstractConnectorServlet.class.getPackage().getName() + ".commands." + StringUtils.capitalize(commandName) + "Command";
//        Class<AbstractCommand> clazz = null;
//        try {
//            clazz = (Class<AbstractCommand>) Class.forName(className);
//        } catch (ClassNotFoundException e) {
//            // not found
//        }
//        return clazz;
//    }

    /**
     * Get override implementation class for a command.
     * @param commandName
     * @return
     */
//    @SuppressWarnings("unchecked")
//    protected Class<AbstractCommand> getCommandClassOverride(String commandName) {
//        String className = AbstractConnectorServlet.class.getPackage().getName() + ".commands." + StringUtils.capitalize(commandName) + "CommandOverride";
//        Class<AbstractCommand> clazz = null;
//        try {
//            clazz = (Class<AbstractCommand>) Class.forName(className);
//        } catch (ClassNotFoundException e) {
//            // not found
//        }
//        return clazz;
//    }

    /**
     * Append data to JSON response.
     * @param param
     * @param value
     */
    protected void putResponse(JSONObject json, String param, Object value) {
        try {
            json.put(param, value);
        } catch (JSONException e) {
            logger.error("json write error", e);
        }
    }

    private void createDirectoryIfNotExist(Repository repository, String location) throws Exception {
        if(!repository.directoryExists(location)) {
            repository.createDirectory(location);
        }
    }

    private String createAssetIfNotExisting(Repository repository, String location, String name, String type, byte[] content) {
        try {
            boolean assetExists = repository.assetExists(location + "/" + name + "." + type);
            if (!assetExists) {
                // create theme asset
                AssetBuilder assetBuilder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Byte);
                assetBuilder.content(content)
                        .location(location)
                        .name(name)
                        .type(type)
                        .version("1.0");

                Asset<byte[]> customEditorsAsset = assetBuilder.getAsset();

                return repository.createAsset(customEditorsAsset);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = null;
        is = new FileInputStream(file);
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            is.close();
            return null; // File is too large
        }

        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }
}
