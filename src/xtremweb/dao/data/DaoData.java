package xtremweb.dao.data;

import java.util.Collection;
import java.util.Vector;
import javax.jdo.Query;

import xtremweb.core.obj.dc.Data;
import xtremweb.dao.DaoJDOImpl;
import xtremweb.serv.dc.DataStatus;

/**
 * This class interface with data table thorugh jdo
 * 
 * @author jsaray
 * 
 */
public class DaoData extends DaoJDOImpl {

    /**
     * This method is used by the ActiveData API to erase the data locally stored
     * in cache from the scheduler response
     * 
     * @param newdatauid the data that must be persisted 
     * @return a Data Collection of UID that must be erased 
     */
    public Collection getDataToDelete(Vector newdatauid) throws Exception {
	String datauids = "";

	for (int i = 0; i < newdatauid.size(); i++)
	    datauids += "uid != \"" + ((String) newdatauid.elementAt(i))
		    + "\" && ";

	Query query = pm.newQuery(xtremweb.core.obj.dc.Data.class, datauids
		+ "  status != " + DataStatus.TODELETE);
	Collection result = (Collection) query.execute();
	return result;
    }
    
    /**
     * This method get a Data if it has the uid specified by parameter and its status is different to TODELETE
     * TODO WITH DSSSS
     * although functionally it works,this method has not been unitary tested !!!!!!!!!, TEST IT WITH DSSSS
     * @param uid
     * @return
     */
    public Data getByUidNotToDelete(String uid) {
	Query query = pm.newQuery(xtremweb.core.obj.dc.Data.class, "uid == \""+ uid + "\" && status != " + DataStatus.TODELETE);
	query.setUnique(true);
	Data d = (Data) query.execute();
	return d;
    }
    
    /**
     * Gets a data given its md5 signature
     * @param md5 the requested hash
     * @return the data whose hash is equals to md5 parameter
     */
    public Data getDataFromMd5(String md5)
    {
    	Query query = pm.newQuery(xtremweb.core.obj.dc.Data.class, "checksum == \"" + md5+"\"");
    	query.setUnique(true);
    	
    	
    	Data d = (Data)query.execute();
    
    	return d;
    }

}
