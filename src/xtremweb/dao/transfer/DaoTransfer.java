package xtremweb.dao.transfer;

import java.util.Collection;

import javax.jdo.Query;


import xtremweb.api.transman.TransferStatus;
import xtremweb.core.obj.dt.Transfer;
import xtremweb.dao.DaoJDOImpl;

/**
 * This class interface with transfer table thorugh jdo
 * @author jsaray
 *
 */
public class DaoTransfer extends DaoJDOImpl {

    /**
     * Get all transfers with a specified status
     * @param st the transfer status
     * @param countflag ??
     * @param tocount ??
     * @return a collection of the transfers having a common status
     */
    public Collection getTransfersDifferentStatus(int st)//,boolean countflag,String ...tocount)
    {
        Query q = pm.newQuery(Transfer.class, "status != " + st  );
        Collection col = (Collection)q.execute();
        Collection cold = pm.detachCopyAll(col);
        return cold;
    }

    public Object getTransfersByStatus(int st,boolean countflag,String countfield)
    {
        Query q = pm.newQuery(Transfer.class, "status == " + st  );
        if(countflag){
            q.setResult("count(" + countfield +")");
            return q.execute();
        }
        Collection col = (Collection)q.execute();
        Collection cold = pm.detachCopyAll(col);
        return cold;
    }

    /**
     * get all transfers having a specified data uid
     * @param datauid the given datauid
     * @return all the transfers associated to the data having datauid
     */
    public Collection getTransfersByDataUid(String datauid)
    {
        Query query = pm.newQuery(xtremweb.core.obj.dt.Transfer.class,
                      "datauid == \"" + datauid +"\"" );
        Collection results = (Collection)query.execute();
        Collection resultsd = pm.detachCopyAll(results);
        return resultsd;
    }

}
