package xtremweb.role.cmdline;
/**
 * CommandLineTool.java
 *
 *
 * Created: Fri Apr 14 10:35:22 2006
 *
 * @author <a href="mailto:">Gilles Fedak</a>
 * @version 1.0
 */
import xtremweb.api.bitdew.*;
import xtremweb.api.activedata.*;
import xtremweb.api.transman.*;
import xtremweb.serv.dc.*;
import xtremweb.serv.ds.*;
import xtremweb.core.iface.*;
import xtremweb.core.log.*;
import xtremweb.core.com.com.*;
import xtremweb.core.com.idl.*;
import xtremweb.core.serv.*;
import xtremweb.role.ui.*;
import xtremweb.core.obj.dc.Data;
import xtremweb.core.obj.dr.Protocol;
import xtremweb.core.obj.dc.Locator;
import xtremweb.core.obj.dt.Transfer;
import xtremweb.core.obj.ds.Attribute;
import xtremweb.serv.ds.AttributeType;
import xtremweb.core.log.*;
import xtremweb.core.http.*;
import java.io.File;
import jargs.gnu.CmdLineParser;
import java.util.*;


public class CommandLineTool {

    private BitDew bitdew;
    private ActiveData activeData;
    private TransferManager transferManager = null;
    private enum HelpFormat {SHORT, LONG};

    private String host;
    private String attruid;
    private String fileName;
    private String dirName;
    private int port;
    private boolean server = false;
    private static Logger log = LoggerFactory.getLogger("CommandLineTool");

    public CommandLineTool(String[] args) {

	//force the log4J configuration to log level info without formatting
	if (log instanceof Log4JLogger) {
	    try {
		Log4JLogger.setProperties("conf/log4jcmdlinetool.properties");
	    } catch (LoggerException le) {
		log.debug(le.toString());
	    }
	}
	
	String[] otherArgs = parse(args);
	
	//if there's no other argument display helps
	if (otherArgs.length==0) 
	    usage(HelpFormat.SHORT); 

	//start services
	if (otherArgs[0].equals("serv")) {
	    
	    Vector services =  new Vector();
	    for (String s: otherArgs) {
		if (s.equals("dc") ||
		    s.equals("ds") ||
		    s.equals("dr") ||
		    s.equals("dt"))
		    services.add(s);
	    }
	    ServiceLoader sl = new ServiceLoader("RMI", port, services);
	    UIFactory.createUIFactory();
	    server = true;
	    return;
	} else {
	    try {
		Vector comms = ComWorld.getMultipleComms(host, "rmi", port, "dc", "dr", "dt", "ds");
		activeData = new ActiveData(comms);		
		bitdew = new BitDew(comms);
	    } catch(ModuleLoaderException e) {
		log.warn("Cannot find service " + e);
		log.warn("Make sure that your classpath is correctly set");
		System.exit(0);
	    }	    
	}
	
	//create attr
	if (otherArgs[0].equals("attr")) {
	    if (otherArgs.length==1) 
		usage(HelpFormat.LONG); 
	    Attribute attr = null;
	    try {
		attr = AttributeUtil.parseAttribute(otherArgs);
	    } catch (ActiveDataException ade) {
		log.warn(" Cannot parse attribute definition : " + ade);
	    }
	    try {
		Attribute _attr = activeData.registerAttribute(attr);
		log.info("attribute registred : " + AttributeUtil.toString(_attr));
	    } catch (ActiveDataException ade) {
		log.warn(" Cannot registrer attribute : " + ade);
		System.exit(0);
	    }
	}
	
	//create data
	if (otherArgs[0].equals("data")) {
	    if (otherArgs.length==1) 
		usage(HelpFormat.LONG);
	    File file = new File(otherArgs[1]);
	    if (!file.exists()) {
		log.warn(" File does not exist : " + otherArgs[1]);
		System.exit(0);	
	    }
	    Data data = null;
	    try {
		 data = bitdew.createData(file);
		log.info("Data registred : " + DataUtil.toString(data));
	    } catch (BitDewException ade) {
		log.warn(" Cannot registrer data : " + ade);
		System.exit(0);
	    }
	}

	//put file [dataId]
	if (otherArgs[0].equals("put")) {
	    if (otherArgs.length==1) 
		usage(HelpFormat.LONG);

	    File file = new File(otherArgs[1]);
	    if (!file.exists()) {
		log.warn(" File does not exist : " + otherArgs[1]);
		System.exit(0);	
	    }
	    //no dataId
	    if (otherArgs.length==2) {
		Data data = null;
		try {
		    data = bitdew.createData(file);
		    log.info("Data registred : " + DataUtil.toString(data));
		    bitdew.put(file, data);
		} catch (BitDewException ade) {
		    log.warn(" Cannot registrer data : " + ade);
		    System.exit(0);
		}
		try {
		    transferManager.start();
		    transferManager.waitFor(data);
		    log.info("Transfer complete");
		} catch (TransferManagerException ade) {
		    log.warn(" Transfer data : " + ade);
		    System.exit(0);
		}
	    }
	}

    } // CommandLineTool constructor

    private String[] parse(String[] args) {
	
	//if there's no argument display helps
	if (args.length==0) 
	    usage(HelpFormat.SHORT); 

	CmdLineParser parser = new CmdLineParser();

	CmdLineParser.Option helpOption = parser.addBooleanOption('h',"help");
	CmdLineParser.Option dirOption = parser.addStringOption('d',"dir");
	CmdLineParser.Option hostOption = parser.addStringOption("host");
	CmdLineParser.Option fileOption = parser.addStringOption("file");
	CmdLineParser.Option attrOption = parser.addStringOption("attr");

	try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            log.debug(e.getMessage());
            usage(HelpFormat.SHORT);
        }

	host = (String) parser.getOptionValue(hostOption,"localhost");
	attruid = (String) parser.getOptionValue(attrOption);
	fileName = (String) parser.getOptionValue(fileOption);
	dirName = (String) parser.getOptionValue(dirOption, ".");	
	boolean help = ((Boolean)parser.getOptionValue(helpOption, Boolean.FALSE)).booleanValue();

 	//if the help option is set display long help
	if (help) 
	    usage(HelpFormat.LONG); 

	return parser.getRemainingArgs();
    }

    public void usage(HelpFormat format) {
	Usage usage = new Usage();
	switch (format) {
	case LONG:
	    usage.title();
	    usage.ln();
	    usage.section("BitDew command line client");
	    usage.ln();
	    usage.usage("java -jar bitdew-stand-alone.jar [Options] Commands [Command Options]");
	    usage.ln();
	    usage.section("Options:");
	    usage.option("-h", "--help","display this helps" );
	    usage.option("-d", "--dir","working directory" );
	    usage.option("--host","service hostname" );
	    usage.option("--port","service port" );
	    usage.ln();
	    usage.section("Services:");
	    usage.option("serv [dc|dr|dt|ds]","start the list of services separated by a space");
	    usage.ln();
	    usage.section("Attributes:");
	    usage.option("attr attr_definition", "create attribute where attr_definition has the syntax att_Name = {field1=value1, field2=value2}.");
	    usage.option("", "Field can have the following values :");
	    usage.option("    replicat=int","number of data replicat in the system. The special value -1    means that the data will be replicated to each node");
	    usage.option("    affinity=dataId", "affinity to data Identifier. Schedule the data on node where   dataId is present.");
	    usage.option("    lftabs=int", "absolute life time. The value is the life duration in minutes.");
	    usage.option("    lftabs=dataId", "relative lifetime. The data will be obsolete when dataId is    deleted.");
	    usage.option("    oob=protocol", "out-of-band file transfer protocol. Protocol can be one of the following [dummy|ftp|bittorrent]");
	    usage.option("    ft=[true|false]", "fault tolerance. If true data will be rescheduled if one host  holding the data is considered as dead.");
	    //	    usage.option("setattr","set attribute to data");
	    usage.ln();
	    usage.section("Data:");
	    usage.option("data file_name","create a new data from the file file_name");
	    usage.ln();
	    usage.section("File:");
	    usage.option("put file_name [dataId]","copy a file in the data space. If dataId is not specified, a new data will be created from the file.");
	    usage.option("get dataId [file_name]","get the file from dataId.");
	    usage.ln();
	    //	    usage.option("--file filename","file name to be created" );
	    //	    usage.option("--attr attruid","attribute uid associated to the data" );

	    break;
	case SHORT:
	    usage.usage("try java -jar bitdew-stand-alone.jar [-h, --help] for more information");
	    break;
	}
	System.exit(2);
    }

    public static void main(String[] args){
	CommandLineTool cmd = new CommandLineTool(args);
    }
} // CommandLineTool
