package org.jbpm.designer.repository;

/**
 * Reusable component managed by the repository
 */
public interface Widget<T> extends Item {
    /**
     * Returns location in the repository where this widget is stored
     * @return - widget location
     */
    String getWidgetLocation();

    /**
     * Returns type of the widget.
     * @return - widget type
     */
    String getWidgetType();

    /**
     * Returns actual content of this widget
     * @return - widget content
     */
    T getWidgetContent();
}
