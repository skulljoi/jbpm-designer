package org.jbpm.designer.repository;

import java.util.HashMap;
import java.util.Map;

public class AssetTypeMapper {

    private static Map<String, String> mimeTypes = new HashMap<String, String>();

    static {
        mimeTypes.put("text", "text/plain");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("ftl", "text/plain");
        mimeTypes.put("flt", "text/plain");
        mimeTypes.put("xml", "text/xml");
        mimeTypes.put("json", "text/json");

    }

    public static String findMimeType(Asset asset) {
        if (mimeTypes.containsKey(asset.getAssetType().toLowerCase())) {
            return mimeTypes.get(asset.getAssetType().toLowerCase());
        }

        return "text/plain";
    }
}
