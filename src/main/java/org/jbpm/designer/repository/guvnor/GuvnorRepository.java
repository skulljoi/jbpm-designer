package org.jbpm.designer.repository.guvnor;

import org.jbpm.designer.repository.*;

import java.util.Collection;
import java.util.Map;

/**
 * Repository implementation that is using Guvnor as a storage
 */
public class GuvnorRepository implements Repository {

    public Collection<String> listDirectories(String startAt) {
        throw new UnsupportedOperationException();
    }

    public Map<String, Collection<Asset>> listDirectoriesRecursively(String startAt) {
        throw new UnsupportedOperationException();
    }

    public Collection<Asset> listAssets(String location) {
        throw new UnsupportedOperationException();
    }

    public Collection<Asset> listAssets(String location, Filter filter) {
        throw new UnsupportedOperationException();
    }

    public Asset loadAsset(String assetUniqueId) throws AssetNotFoundException {
        throw new UnsupportedOperationException();
    }

    public String storeAsset(Asset asset) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteAsset(String assetUniqueId) throws AssetNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean assetExists(String assetUniqueId) {
        throw new UnsupportedOperationException();
    }

    public Collection<Widget> listWidgets() {
        throw new UnsupportedOperationException();
    }

    public Collection<Widget> listWidgets(Filter filter) {
        throw new UnsupportedOperationException();
    }

    public Widget loadWidget(String widgetUniqueId) throws WidgetNotFoundException {
        throw new UnsupportedOperationException();
    }

    public String storeWidget(Widget widget) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteWidget(String widgetUniqueId) throws WidgetNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean widgetExists(String widgetUniqueId) {
        throw new UnsupportedOperationException();
    }
}
