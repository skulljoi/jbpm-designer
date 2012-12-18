package org.jbpm.designer.web.server.menu.connector.commands;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractCommand {
    private static Logger logger = Logger.getLogger(AbstractCommand.class);

    public JSONObject getTree() {
        // TODO
        return null;
    }

    public JSONArray getCdc() {
        // TODO
        return null;
    }

    public JSONObject getCdw() {
        // TODO
        return null;
    }

    public JSONObject getParams() {
        // TODO
        return null;
    }

    public JSONArray getDisabled() {
        // TODO
        return null;
    }
}
