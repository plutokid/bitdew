package xtremweb.serv.dt.jsaga;

import java.util.Properties;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import fr.in2p3.jsaga.adaptor.security.VOMSContext;
import xtremweb.core.conf.ConfigurationException;
import xtremweb.core.conf.ConfigurationProperties;
import xtremweb.core.obj.dc.Data;
import xtremweb.core.obj.dc.Locator;
import xtremweb.core.obj.dr.Protocol;
import xtremweb.core.obj.dt.Transfer;
import xtremweb.serv.dt.BlockingOOBTransferImpl;
import xtremweb.serv.dt.OOBException;

/**
 * This class allows Jsaga transfers between a local host and a production grid
 * 
 * @author josefrancisco
 */
public class JsagaTransfer extends BlockingOOBTransferImpl {

    public JsagaTransfer(Data d, Transfer t, Locator rl, Locator ll, Protocol rp, Protocol lp) {
	super(d, t, rl, ll, rp, lp);
    }
    
    public JsagaTransfer()
    {
	
    }

    /**
     * JSaga Session
     */
    private Session session;

    /**
     * JSaga properties
     */
    private Properties mainprops;

    /**
     * Jsaga security context
     */
    private Context ctx;

    /**
     * Connect to the grid via a pre-defined proxy
     * @throws  
     */
    public void connect() throws OOBException {
	try {
	    mainprops = ConfigurationProperties.getProperties();
	
	ctx = ContextFactory.createContext();

	// Define the voms server
	ctx.setAttribute(Context.SERVER, mainprops.getProperty("xtremweb.serv.dr.gsiftp.vomsserver"));

	// Define your VO
	ctx.setAttribute(Context.USERVO, mainprops.getProperty("xtremweb.serv.dr.gsiftp.uservo"));

	// Define your proxy life time
	ctx.setAttribute(Context.LIFETIME, mainprops.getProperty("xtremweb.serv.dr.gsiftp.lifetime"));

	// The path of the proxy generated by JSAGA
	ctx.setAttribute(Context.USERPROXY, mainprops.getProperty("xtremweb.serv.dr.gsiftp.userproxy"));

	// The path of your user certificate
	ctx.setAttribute(Context.USERCERT, mainprops.getProperty("xtremweb.serv.dr.gsiftp.usercert"));

	// The path of your user key
	ctx.setAttribute(Context.USERKEY, mainprops.getProperty("xtremweb.serv.dr.gsiftp.userkey"));

	// The password of your user key
	ctx.setAttribute(Context.USERPASS, mainprops.getProperty("xtremweb.serv.dr.gsiftp.userpass"));

	// A directory containing the CA certificates
	// (http://dist.eugridpma.info/distribution/igtf/current/accredited/tgz/)
	ctx.setAttribute(Context.CERTREPOSITORY, mainprops.getProperty("xtremweb.serv.dr.gsiftp.certrepository"));

	// Needed ??? Might be leaved empty
	ctx.setAttribute(VOMSContext.VOMSDIR, mainprops.getProperty("xtremweb.serv.dr.gsiftp.vomsdir"));

	ctx.setAttribute(Context.TYPE, mainprops.getProperty("xtremweb.serv.dr.gsiftp.type"));

	session = SessionFactory.createSession(false);
	session.addContext(ctx);
	} catch (ConfigurationException e) {
	    e.printStackTrace();
	    throw new OOBException("ConfigurationException");
	} catch (IncorrectStateException e) {
	    e.printStackTrace();
	    throw new OOBException("IncorrectStateException");
	} catch (TimeoutException e) {
	    e.printStackTrace();
	    throw new OOBException("TimeoutException");
	} catch (NoSuccessException e) {
	    e.printStackTrace();
	    throw new OOBException("NoSuccessException");
	} catch (NotImplementedException e) {
	    e.printStackTrace();
	    throw new OOBException("NotImplementedException");
	} catch (AuthenticationFailedException e) {
	    e.printStackTrace();
	    throw new OOBException("AuthenticationFailedException");
	} catch (AuthorizationFailedException e) {
	    e.printStackTrace();
	    throw new OOBException("AuthorizationFailedException");
	} catch (PermissionDeniedException e) {
	    e.printStackTrace();
	    throw new OOBException("PermissionDeniedException");
	} catch (BadParameterException e) {
	    e.printStackTrace();
	    throw new OOBException("BadParemeterException");
	} catch (DoesNotExistException e) {
	    e.printStackTrace();
	    throw new OOBException("DoesNotExistException");
	}

    }

    /**
     * 
     */
    public void disconnect() throws OOBException {
	// TODO Auto-generated method stub

    }
    public boolean poolTransfer()
    {
	return !isTransfering();
    }
    /**
     * 
     */
    public void blockingSendSenderSide() throws OOBException {

	String urltarget="";
	try {
	    if(remote_protocol.getname().equals("gsiftp"))
		urltarget = "gsiftp://"+remote_protocol.getserver()+":"+remote_protocol.getport()+remote_protocol.getpath()  + "/" + remote_locator.getref();
	    log.debug("The target is " + urltarget + " the local is " + local_locator.getref());
	    URL source = URLFactory.createURL("file://" + local_locator.getref());
	    URL target = URLFactory.createURL(urltarget);
	    int flags = Flags.OVERWRITE.or(((Flags.NONE).or(Flags.NONE.getValue())));
	    NSEntry entry = NSFactory.createNSEntry(session, source, Flags.NONE.getValue());
	    entry.copy(target, flags);
	    entry.close();
	} catch (BadParameterException e) {	   
	    e.printStackTrace();
	    throw new OOBException("BadParameterException, for more information execute with -v");
	} catch (NoSuccessException e) {	   
	    e.printStackTrace();
	    throw new OOBException("NoSuccessException, for more information execute with -v");
	} catch (NotImplementedException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("NotImplementedException, for more information execute with -v");
	} catch (IncorrectURLException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("IncorrectURLException, for more information execute with -v");
	} catch (AuthenticationFailedException e) {
	   
	    e.printStackTrace();
	    throw new OOBException("AuthenticationFailedException, for more information execute with -v");
	} catch (AuthorizationFailedException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("AuthorizationFailedException, for more information execute with -v");
	} catch (PermissionDeniedException e) {
	   
	    e.printStackTrace();
	    throw new OOBException("PermissionDeniedException, for more information execute with -v");
	} catch (DoesNotExistException e) {	    
	    e.printStackTrace();
	    throw new OOBException("DoesNotExistException, for more information execute with -v");
	} catch (AlreadyExistsException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("AlreadyExistsException, for more information execute with -v");
	} catch (TimeoutException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("TimeoutException, for more information execute with -v");
	} catch (IncorrectStateException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("IncorrectStateException, for more information execute with -v");
	} 
    }

    /**
     * Due to the nature of a jsaga transfer this method is not necessary to
     * implement
     */
    public void blockingSendReceiverSide() throws OOBException {
	log.debug("Called blocking send receiver side ");
    }

    /**
     * Due to the nature of a jsaga transfer this method is not necessary to
     * implement
     */
    public void blockingReceiveSenderSide() throws OOBException {
    }

    /**
     * 
     */
    public void blockingReceiveReceiverSide() throws OOBException {
	String urltarget="";
	try {
	    
	    if(remote_protocol.getname().equals("gsiftp"))
		urltarget = "gsiftp://"+remote_protocol.getserver()+":"+remote_protocol.getport()+ remote_protocol.getpath()  + "/" + remote_locator.getdatauid();
	    log.debug("The target is " + urltarget + " the local is " + local_locator.getref());
	    URL source = URLFactory.createURL(urltarget);
	    URL target = URLFactory.createURL("file://" + local_locator.getref());
	    int flags = Flags.OVERWRITE.or(((Flags.NONE).or(Flags.NONE.getValue())));
	    NSEntry entry = NSFactory.createNSEntry(session, source, Flags.NONE.getValue());
	    entry.copy(target, flags);
	    entry.close();
	}catch (BadParameterException e) {	   
	    e.printStackTrace();
	    throw new OOBException("BadParameterException, for more information execute with -v");
	} catch (NoSuccessException e) {	   
	    e.printStackTrace();
	    throw new OOBException("NoSuccessException, for more information execute with -v");
	} catch (NotImplementedException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("NotImplementedException, for more information execute with -v");
	} catch (IncorrectURLException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("IncorrectURLException, for more information execute with -v");
	} catch (AuthenticationFailedException e) {
	   
	    e.printStackTrace();
	    throw new OOBException("AuthenticationFailedException, for more information execute with -v");
	} catch (AuthorizationFailedException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("AuthorizationFailedException, for more information execute with -v");
	} catch (PermissionDeniedException e) {
	   
	    e.printStackTrace();
	    throw new OOBException("PermissionDeniedException, for more information execute with -v");
	} catch (DoesNotExistException e) {	    
	    e.printStackTrace();
	    throw new OOBException("DoesNotExistException, for more information execute with -v");
	} catch (AlreadyExistsException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("AlreadyExistsException, for more information execute with -v");
	} catch (TimeoutException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("TimeoutException, for more information execute with -v");
	} catch (IncorrectStateException e) {
	    
	    e.printStackTrace();
	    throw new OOBException("IncorrectStateException, for more information execute with -v");
	}
    }

}
