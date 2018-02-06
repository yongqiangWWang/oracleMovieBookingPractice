package com.mbp.view.utils.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;

import javax.servlet.http.HttpServletResponse;

import oracle.adf.model.binding.DCIteratorBinding;

import oracle.binding.BindingContainer;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;


/**
 * Utils for webpages.
 *
 */
public class JSFUtils {

    private static final String NO_RESOURCE_FOUND = "Missing resource: ";

    private static Map<String, SelectItem> selectItems =
        new HashMap<String, SelectItem>();

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @param expression
     * @return Managed object
     */
    public static Object resolveExpression(String expression) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();
        ValueBinding bind = app.createValueBinding(expression);
        return bind.getValue(ctx);
    }

    /**
     * Convenience method for resolving a reference to a managed bean by name
     * rather than by expression.
     * @param beanName
     * @return Managed object
     */
    public static Object getManagedBeanValue(String beanName) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        return resolveExpression(buff.toString());
    }

    /**
     * Method for setting a new object into a JSF managed bean.
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean
     * @param expression
     * @param newValue
     */
    public static void setExpressionValue(String expression, Object newValue) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();
        ValueBinding bind = app.createValueBinding(expression);
        Class bindClass = bind.getType(ctx);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
            bind.setValue(ctx, newValue);
        }
    }

    public static void setManagedBeanValue(String beanName, Object newValue) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        setExpressionValue(buff.toString(), newValue);
    }


    public static void storeOnSession(String key, Object object) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        sessionState.put(key, object);
    }

    public static Object getFromSession(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        return sessionState.get(key);
    }

    public static Object getSession() {
        return FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    }

    public static Object getFromRequest(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map reqMap = ctx.getExternalContext().getRequestMap();
        return reqMap.get(key);
    }


    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return getStringSafely(bundle, key, null);
    }


    public static FacesMessage getMessageFromBundle(String key,
                                                    FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key, null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message = new FacesMessage(summary, detail);
        message.setSeverity(severity);
        return message;
    }

    public static void addFacesErrorMessage(String msg) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    public static void addFacesInfoMessage(String msg) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    public static void addFacesErrorMessage(String attrName, String msg) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    public static String getRootViewId() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getViewRoot().getViewId();
    }

    public static String getRootViewComponentId() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getViewRoot().getId();
    }

    private static ResourceBundle getBundle() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIViewRoot uiRoot = ctx.getViewRoot();
        Locale locale = uiRoot.getLocale();
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(),
                                        locale, ldr);
    }

    private static String getStringSafely(ResourceBundle bundle, String key,
                                          String defaultValue) {
        String resource = null;
        try {
            resource = bundle.getString(key);
        } catch (MissingResourceException mrex) {
            if (defaultValue != null) {
                resource = defaultValue;
            } else {
                resource = NO_RESOURCE_FOUND + key;
            }
        }
        return resource;
    }

    public static SelectItem getSelectItem(String value) {
        SelectItem item = selectItems.get(value);
        if (item == null) {
            item = createNewSelectItem(value, value);
            selectItems.put(value, item);
        }
        return item;
    }

    public static SelectItem createNewSelectItem(String label, String value) {
        return new SelectItem(value, label);
    }

    public static HttpServletResponse getResponse() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletResponse response =
            (HttpServletResponse)ctx.getExternalContext().getResponse();
        return response;

    }

    public static BindingContainer getBindingContainer() {
        return (BindingContainer)JSFUtils.resolveExpression("#{bindings}");
    }

    public static boolean hasRecords(String iteratorName) {
        Row[] rows = getAllRows(iteratorName);
        if ((rows == null) || (rows.length == 0)) {
            return false;
        }
        return true;
    }


    public static void setIteratorPosition(String iteratorName,
                                           String whereClause) throws Exception {
        ViewObject viewObject = getViewObject(iteratorName);
        if (viewObject.getWhereClause() != null) {
            viewObject.setWhereClauseParams(null);
            viewObject.executeQuery();

        }
        viewObject.setWhereClause(whereClause);
        //        viewObject.defineNamedWhereClauseParam(param, null, null);
        //        viewObject.setNamedWhereClauseParam(param, value);
        viewObject.executeQuery();
    }


    public static ViewObject getViewObject(String iteratorName) {
        ViewObject viewObject = null;
        BindingContainer bindings = JSFUtils.getBindingContainer();
        if (bindings != null) {
            DCIteratorBinding iter =
                (DCIteratorBinding)bindings.get(iteratorName);
            viewObject = iter.getViewObject();
        }
        return viewObject;
    }

    public static Row[] getAllRows(String iteratorName) {
        ViewObject vObject = getViewObject(iteratorName);
        vObject.executeQuery();
        Row[] rows = vObject.getAllRowsInRange();
        return rows;
    }

    public static ViewObject executeViewObject(String iteratorName) {
        ViewObject vObject = getViewObject(iteratorName);
        vObject.executeQuery();
        System.out.println("....Total rows..." + vObject.getRowCount());
        return vObject;
    }


    public static Row getCurrentRow(String iteratorName) {
        BindingContainer bindings = getBindingContainer();
        Row currentRow = null;
        if (bindings != null) {

            DCIteratorBinding iter =
                (DCIteratorBinding)bindings.get(iteratorName);
            ViewObject vObject = iter.getViewObject();
            currentRow = vObject.getCurrentRow();
        }
        return currentRow;
    }

    public static void executeIterator(String iteratorName) {
        BindingContainer bindings = getBindingContainer();
        if (bindings != null) {
            DCIteratorBinding iter =
                (DCIteratorBinding)bindings.get(iteratorName);
            ViewObject vObject = iter.getViewObject();
            vObject.executeQuery();
        }
    }

    public static Object getCurrentRowAttribute(String iteratorName,
                                                String attributeName) {
        Row row = getCurrentRow(iteratorName);
        return row.getAttribute(attributeName);
    }

    public static Object getFromRequestParameterMap(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getExternalContext().getRequestParameterMap().get(key);

    }

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public static void postMessageToFacesContext(FacesMessage.Severity severity,
                                                 String summary,
                                                 String detail) {
        postMessage(null, severity, summary, detail);
    }

    public static void postMessage(String componentId,
                                   FacesMessage.Severity severity,
                                   String summary, String detail) {
        getFacesContext().addMessage(componentId,
                                     new FacesMessage(severity, summary,
                                                      detail));
    }

}
