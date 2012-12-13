package org.jbpm.designer.web.profile.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonParseException;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.emf.common.util.URI;
import org.jboss.drools.DroolsPackage;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.RepositoryManager;
import org.jbpm.designer.repository.guvnor.GuvnorRepository;
import org.jbpm.designer.repository.vfs.VFSRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jboss.drools.impl.DroolsFactoryImpl;
import org.jbpm.designer.bpmn2.impl.Bpmn2JsonMarshaller;
import org.jbpm.designer.bpmn2.impl.Bpmn2JsonUnmarshaller;
import org.jbpm.designer.bpmn2.resource.JBPMBpmn2ResourceFactoryImpl;
import org.jbpm.designer.bpmn2.resource.JBPMBpmn2ResourceImpl;
import org.jbpm.designer.web.plugin.IDiagramPlugin;
import org.jbpm.designer.web.plugin.impl.PluginServiceImpl;
import org.jbpm.designer.web.profile.IDiagramProfile;


/**
 * The implementation of the jBPM profile for Process Designer.
 * @author Tihomir Surdilovic
 */
public class JbpmProfileImpl implements IDiagramProfile {
    
    private static Logger _logger = LoggerFactory.getLogger(JbpmProfileImpl.class);
    
    private Map<String, IDiagramPlugin> _plugins = new LinkedHashMap<String, IDiagramPlugin>();

    private String _stencilSet;
    private String _localHistoryEnabled;
    private String _localHistoryTimeout;
    private String _repositoryId;
    private String _repositoryRoot;
    private String _repositoryHost;
    private String _repositoryProtocol;
    private String _repositorySubdomain;
    private String _repositoryUsr;
    private String _repositoryPwd;
    private String _repositoryGlobalDir;

    private boolean initialized = false;

    public JbpmProfileImpl(ServletContext servletContext) {
        this(servletContext, true, false);
    }

    public JbpmProfileImpl() {
        this(null, false, false);
    }
    
    public JbpmProfileImpl(ServletContext servletContext, boolean initializeLocalPlugins, boolean initializeRepository) {
        if(initializeLocalPlugins) {
            initializeLocalPlugins(servletContext);
        }
        if(initializeRepository) {
            initializeRepository();
        }
    }

    public String getTitle() {
        return "jBPM Process Designer";
    }

    public String getStencilSet() {
        return _stencilSet;
    }

    public Collection<String> getStencilSetExtensions() {
        return Collections.emptyList();
    }

    public Collection<String> getPlugins() {
        return Collections.unmodifiableCollection(_plugins.keySet());
    }
    
    private void initializeLocalPlugins(ServletContext context) {
        Map<String, IDiagramPlugin> registry = PluginServiceImpl.getLocalPluginsRegistry(context);
        FileInputStream fileStream = null;
        try {
            try {
                fileStream = new FileInputStream(new StringBuilder(context.getRealPath("/")).append("/").
                        append("/").append("profiles").append("/").append("jbpm.xml").toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(fileStream, "UTF-8");
            while(reader.hasNext()) {
                if (reader.next() == XMLStreamReader.START_ELEMENT) {
                    if ("profile".equals(reader.getLocalName())) {
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("stencilset".equals(reader.getAttributeLocalName(i))) {
                                _stencilSet = reader.getAttributeValue(i);
                            }
                        }
                    } else if ("plugin".equals(reader.getLocalName())) {
                        String name = null;
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("name".equals(reader.getAttributeLocalName(i))) {
                                name = reader.getAttributeValue(i);
                            }
                        }
                        _plugins.put(name, registry.get(name));
                    } else if ("repository".equals(reader.getLocalName())) {
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("id".equals(reader.getAttributeLocalName(i))) {
                                String repositoryId = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryId)) {
                                    _repositoryId = repositoryId;
                                } else {
                                    _logger.info("Invalid repository id specified");
                                }
                            }
                            if ("globaldir".equals(reader.getAttributeLocalName(i))) {
                                String repositoryGlobalDir = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryGlobalDir)) {
                                    _repositoryGlobalDir = repositoryGlobalDir;
                                } else {
                                    _repositoryGlobalDir = "repository";
                                }
                            }
                            if ("root".equals(reader.getAttributeLocalName(i))) {
                                String repositoryRoot = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryRoot)) {
                                    _repositoryRoot = repositoryRoot;
                                }
                            }
                            if ("protocol".equals(reader.getAttributeLocalName(i))) {
                                String repositoryProtocol = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryProtocol)) {
                                    _repositoryProtocol = repositoryProtocol;
                                }
                            }
                            if ("host".equals(reader.getAttributeLocalName(i))) {
                                String repositoryHost = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryHost)) {
                                    _repositoryHost = repositoryHost;
                                }
                            }
                            if ("subdomain".equals(reader.getAttributeLocalName(i))) {
                                String repositorySubdomain = reader.getAttributeValue(i);
                                if(!isEmpty(repositorySubdomain)) {
                                    if(repositorySubdomain.startsWith("/")) {
                                        repositorySubdomain = repositorySubdomain.substring(1);
                                    } 
                                    if(repositorySubdomain.endsWith("/")) {
                                        repositorySubdomain = repositorySubdomain.substring(0, repositorySubdomain.length() - 1);
                                    }
                                    _repositorySubdomain = repositorySubdomain;
                                }
                            }
                            if ("usr".equals(reader.getAttributeLocalName(i))) {
                                String repositoryUsr = reader.getAttributeValue(i);
                                if(!isEmpty(repositoryUsr)) {
                                    _repositoryUsr = repositoryUsr;
                                }
                            }
                            if ("pwd".equals(reader.getAttributeLocalName(i))) {
                                // allow any value for pwd
                                _repositoryPwd = reader.getAttributeValue(i);
                            }
                        }
                    } else if ("localhistory".equals(reader.getLocalName())) {
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("enabled".equals(reader.getAttributeLocalName(i))) {
                                String localhistoryenabled = reader.getAttributeValue(i);
                                if(!isEmpty(localhistoryenabled)) {
                                    _localHistoryEnabled = localhistoryenabled;
                                } else {
                                    _logger.info("Invalid local history enabled");
                                }
                            }
                            if ("timeout".equals(reader.getAttributeLocalName(i))) {
                                String localhistorytimeout = reader.getAttributeValue(i);
                                if(!isEmpty(localhistorytimeout)) {
                                    _localHistoryTimeout = localhistorytimeout;
                                } else {
                                    _logger.info("Invalid local history timeout");
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e); // stop initialization
        } finally {
            if (fileStream != null) { try { fileStream.close(); } catch(IOException e) {}};
        }
    }

    private void initializeRepository() {

        RepositoryManager.getInstance().registerRepository("repository-guvnor", new GuvnorRepository(this));
        RepositoryManager.getInstance().registerRepository("repository-vfs", new VFSRepository(this));
        initialized = true;
    }

    public String getName() {
        return "jbpm";
    }

    public String getSerializedModelExtension() {
        return "bpmn";
    }

    public String getRepositoryId() {
        return _repositoryId;
    }

    public String getRepositoryRoot() {
        return _repositoryRoot;
    }

    public String getRepositoryHost() {
        return _repositoryHost;
    }

    public String getRepositoryProtocol() {
        return _repositoryProtocol;
    }

    public String getRepositorySubdomain() {
        return _repositorySubdomain;
    }

    public String getRepositoryUsr() {
        return _repositoryUsr;
    }

    public String getRepositoryPwd() {
        return _repositoryPwd;
    }

    public String getLocalHistoryEnabled() {
        return _localHistoryEnabled;
    }

    public String getLocalHistoryTimeout() {
        return _localHistoryTimeout;
    }

    public String getRepositoryGlobalDir() {
        return _repositoryGlobalDir;
    }

    public void setRepositoryId(String _repositoryId) {
        this._repositoryId = _repositoryId;
    }


    public void setRepositoryRoot(String _repositoryRoot) {
        this._repositoryRoot = _repositoryRoot;
    }

    public void setRepositoryHost(String _repositoryHost) {
        this._repositoryHost = _repositoryHost;
    }

    public void setRepositoryProtocol(String _repositoryProtocol) {
        this._repositoryProtocol = _repositoryProtocol;
    }

    public void setRepositorySubdomain(String _repositorySubdomain) {
        this._repositorySubdomain = _repositorySubdomain;
    }

    public void setRepositoryUsr(String _repositoryUsr) {
        this._repositoryUsr = _repositoryUsr;
    }

    public void setRepositoryPwd(String _repositoryPwd) {
        this._repositoryPwd = _repositoryPwd;
    }

    public void setREpositoryGlobalDir(String repositoryGlobalDir) {
        this._repositoryGlobalDir = repositoryGlobalDir;
    }

    public Repository getRepository() {
        if (!initialized) {
            initializeRepository();

        }
        return RepositoryManager.getInstance().getRepository("repository-" + _repositoryId);
    }
    
    public IDiagramMarshaller createMarshaller() {
        return new IDiagramMarshaller() {
            public String parseModel(String jsonModel, String preProcessingData) {
                DroolsFactoryImpl.init();
                Bpmn2JsonUnmarshaller unmarshaller = new Bpmn2JsonUnmarshaller();
                JBPMBpmn2ResourceImpl res;
                try {
                    res = (JBPMBpmn2ResourceImpl) unmarshaller.unmarshall(jsonModel, preProcessingData);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    res.save(outputStream, new HashMap<Object, Object>());
                    return StringEscapeUtils.unescapeHtml(outputStream.toString("UTF-8"));
                } catch (JsonParseException e) {
                    _logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    _logger.error(e.getMessage(), e);
                }
                return "";
            }

			public Definitions getDefinitions(String jsonModel,
					String preProcessingData) {
				try {
					Bpmn2JsonUnmarshaller unmarshaller = new Bpmn2JsonUnmarshaller();
					JBPMBpmn2ResourceImpl res = (JBPMBpmn2ResourceImpl) unmarshaller.unmarshall(jsonModel, preProcessingData);
					return (Definitions) res.getContents().get(0);
				} catch (JsonParseException e) {
					_logger.error(e.getMessage(), e);
				} catch (IOException e) {
					_logger.error(e.getMessage(), e);
				}
				return null;
			}
			
			public Resource getResource(String jsonModel, String preProcessingData) {
				try {
					Bpmn2JsonUnmarshaller unmarshaller = new Bpmn2JsonUnmarshaller();
					return (JBPMBpmn2ResourceImpl) unmarshaller.unmarshall(jsonModel, preProcessingData);
				} catch (JsonParseException e) {
					_logger.error(e.getMessage(), e);
				} catch (IOException e) {
					_logger.error(e.getMessage(), e);
				}
				return null;
			}
        };
    }

    public IDiagramUnmarshaller createUnmarshaller() {
        return new IDiagramUnmarshaller() {
            public String parseModel(String xmlModel, IDiagramProfile profile, String preProcessingData) {
                DroolsFactoryImpl.init();
                Bpmn2JsonMarshaller marshaller = new Bpmn2JsonMarshaller();
                marshaller.setProfile(profile);
                try {
                    return marshaller.marshall(getDefinitions(xmlModel), preProcessingData);
                } catch (Exception e) {
                    _logger.error(e.getMessage(), e);
                }
                return "";
            }
        };
    }
    
    public Definitions getDefinitions(String xml) {
        try {
        	DroolsFactoryImpl.init();
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new JBPMBpmn2ResourceFactoryImpl());
            resourceSet.getPackageRegistry().put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
            resourceSet.getPackageRegistry().put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);

            JBPMBpmn2ResourceImpl resource = (JBPMBpmn2ResourceImpl) resourceSet.createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));
            resource.getDefaultLoadOptions().put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
            resource.setEncoding("UTF-8");
            Map<String, Object> options = new HashMap<String, Object>();
            options.put( JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8" );
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            resource.load(is, options);

            EList<Diagnostic> warnings = resource.getWarnings();
            
            if (warnings != null && !warnings.isEmpty()){
                for (Diagnostic diagnostic : warnings) {
                    System.out.println("Warning: "+diagnostic.getMessage());
                }
            }
            
            EList<Diagnostic> errors = resource.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (Diagnostic diagnostic : errors) {
                    System.out.println("Error: "+diagnostic.getMessage());
                }
                throw new IllegalStateException("Error parsing process definition");
            }

            return ((DocumentRoot) resource.getContents().get(0)).getDefinitions();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public String getStencilSetURL() {
        return "/designer/stencilsets/bpmn2.0jbpm/bpmn2.0jbpm.json";
    }

    public String getStencilSetNamespaceURL() {
        return "http://b3mn.org/stencilset/bpmn2.0#";
    }

    public String getStencilSetExtensionURL() {
        return "http://oryx-editor.org/stencilsets/extensions/bpmncosts-2.0#";
    }
    
    private boolean isEmpty(final CharSequence str) {
        if ( str == null || str.length() == 0 ) {
            return true;
        }
        for ( int i = 0, length = str.length(); i < length; i++ ){
            if ( str.charAt( i ) != ' ' ) {
                return false;
            }
        }
        return true;
    }
}

