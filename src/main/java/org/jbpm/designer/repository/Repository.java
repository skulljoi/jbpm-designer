package org.jbpm.designer.repository;

import java.util.Collection;
import java.util.Map;

/**
 * Repository is responsible for managing its components that are as follows:
 * <ul>
 *     <li>Asset - component that can be of any type and is stored in a custom location</li>
 *     <li>Widget - component that is considered global and usually with reusable and is stored in predefined location</li>
 * </ul>
 */
public interface Repository {

    /**
     * Retrieves all directories stored under <code>startAt</code> location.
     * @param startAt - location where directories should be fetched from
     * @return - list of directories
     */
    Collection<String> listDirectories(String startAt);

    /**
     * Retrieves all directories stored under <code>startAt</code> location including all sub folders.
     * @param startAt - location where directories should be fetched from
     * @return - list of directories
     */
    Map<String, Collection<Asset>> listDirectoriesRecursively(String startAt);

    /**
     * Retrieves all assets stored in the given location.
     * NOTE: This will not load the actual content of the asset but only its meta data
     * @param location - location that assets should be collected from
     * @return - list of available assets
     */
    Collection<Asset> listAssets(String location);

    /**
     * Retrieves all assets stored in the given location.
     * NOTE: This will not load the actual content of the asset but only its meta data
     * @param location - location that assets should be collected from
     * @param filter - allows to defined filter criteria to fetch only assets of interest
     * @return - list of available assets
     */
    Collection<Asset> listAssets(String location, Filter filter);

    /**
     * Loads an asset given by the <code>assetUniqueId</code> including actual content of the asset.
     * @param assetUniqueId - unique identifier of the asset to load
     * @return return loaded asset including content
     * @throws AssetNotFoundException - throws in case of asset given by id does not exist
     */
    Asset loadAsset(String assetUniqueId) throws AssetNotFoundException;

    /**
     * Stores given asset in the repository. <code>asset</code> need to have all meta data and content available
     * for the operation to successfully complete.
     * @param asset - asset to be stored
     * @return returns asset unique identifier that can be used to locate it
     */
    String storeAsset(Asset asset);

    /**
     * Deletes asset from repository identified by <code>assetUniqueId</code>
     * @param assetUniqueId - unique identifier of the asset
     * @return return true if and only if operation completed successfully otherwise false
     * @throws AssetNotFoundException - throws in case of asset with given assetUniqueId does not exist
     */
    boolean deleteAsset(String assetUniqueId) throws AssetNotFoundException;

    /**
     * Examines repository if asset given by the <code>assetUniqueId</code> exists
     * @param assetUniqueId - unique identifier of the asset
     * @return true if and only if asset exists otherwise false
     */
    boolean assetExists(String assetUniqueId);

    /**
     * Retrieves all widgets
     * NOTE: this will not fetch the actual content of the widget but only its meta data
     * @return - returns list of available widgets in the repository
     */
    Collection<Widget> listWidgets();

    /**
     * Retrieves all widgets filtered by the given filter
     * NOTE: this will not fetch the actual content of the widget but only its meta data
     * @param filter -  allows to defined filter criteria to fetch only widgets of interest
     * @return - returns list of available widgets in the repository
     */
    Collection<Widget> listWidgets(Filter filter);

    /**
     * Loads widget given by <code>widgetUniqueId</code> including its content
     * @param widgetUniqueId - unique identifier of the widget
     * @return - loaded widget including its content
     * @throws WidgetNotFoundException - thrown in case Widget given by widgetUniqueId was not found
     */
    Widget loadWidget(String widgetUniqueId) throws WidgetNotFoundException;

    /**
     * Stores given widget in the repository. <code>widget</code> need to have all meta data and content available
     * for the operation to successfully complete.
     * @param widget - widget to be stored
     * @return - return unique widget identifier that can be used to locate it
     */
    String storeWidget(Widget widget);

    /**
     * Deletes widget from repository identified by <code>widgetUniqueId</code>
     * @param widgetUniqueId - unique identifier of the widget
     * @return - true if and only if operation was successful otherwise false
     * @throws WidgetNotFoundException - thrown in case widget given by widgetUniqueId was not found
     */
    boolean deleteWidget(String widgetUniqueId) throws WidgetNotFoundException;

    /**
     * Examines repository if widget given by <code>widgetUniqueId</code> exists
     * @param widgetUniqueId - unique identifier of the widget
     * @return true if and only if widget exists otherwise false
     */
    boolean widgetExists(String widgetUniqueId);
}
