package com.mbp.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ViewObjectImpl;

public class QueryUtils {
    public QueryUtils() {
        super();
    }
    
    public static Row qryOneDataByVO(ViewObject vo, Map attrs){
        Row[] rows = qryDataByVO(vo, attrs);
        if(rows != null && rows.length > 0) {
            return rows[0];
        }
        return null;
    }
    
    public static Row[] qryDataByVO(ViewObject vo, Map attrs){
        if (attrs != null) {
            // via VO to create a standard selection
            ViewCriteria vc = vo.createViewCriteria();
            // via standard selection to create a standard selection row
            ViewCriteriaRow vcr = vc.createViewCriteriaRow();
            
            Set<Map.Entry<Object, Object>> mE = attrs.entrySet();
            Iterator<Map.Entry<Object, Object>> it = mE.iterator();
            
            while(it.hasNext()) {
                Map.Entry<Object, Object> temp = it.next();
                vcr.setAttribute(temp.getKey().toString(), temp.getValue());
            }
            
            vc.addElement(vcr);
            RowIterator rowIt = vo.findByViewCriteria(vc, -1, ViewObjectImpl.QUERY_MODE_SCAN_DATABASE_TABLES);
            
            Row[] result = rowIt.getAllRowsInRange();
            rowIt.reset();
            vo.reset();
            return result;
            
        }else {
            return null;    
        }    
    }
}
