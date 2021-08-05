/* This file is part of the ili2ora project.
 * For more information, please see <http://www.interlis.ch>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ch.ehi.ili2ora;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.DbUrlConverter;
import ch.ehi.ili2db.gui.AbstractDbPanelDescriptor;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2ora.sqlgen.GeneratorOracleSpatial;


/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 07.02.2005 $
 */
public class OraMain extends ch.ehi.ili2db.AbstractMain {
    private static final String DB_PORT="1521";
    private static final String DB_HOST="localhost";

	private String dbservice="";
	
	@Override
	public void initConfig(Config config) {
		super.initConfig(config);
		config.setGeometryConverter(ch.ehi.ili2ora.converter.OracleSpatialColumnConverter.class.getName());
		config.setDdlGenerator(ch.ehi.ili2ora.sqlgen.GeneratorOracleSpatial.class.getName());
		config.setJdbcDriver("oracle.jdbc.driver.OracleDriver");
		config.setIdGenerator(ch.ehi.ili2ora.OraSequenceBasedIdGen.class.getName());
		config.setIli2dbCustomStrategy(ch.ehi.ili2ora.OracleCustomStrategy.class.getName());
		config.setValue(Config.MODELS_TAB_MODELNAME_COLSIZE, "400");
	}
	protected DbUrlConverter getDbUrlConverter() {
		return new DbUrlConverter(){
			public String makeUrl(Config config) {
				/*
				 *  jdbc:oracle:thin:@myhost:1521:orcl
				    */
                String sid = config.getDbdatabase();
                // no option selected
                if(isNullOrEmpty(sid) && isNullOrEmpty(dbservice)) {
                    EhiLogger.logError("SID or Service must be specified");
                    return null;
                    }
                // two options selected: service and database
                if(!isNullOrEmpty(sid) && !isNullOrEmpty(dbservice)) {
                    EhiLogger.logError("SID or Service must be specified but not both");
                    return null;
                }

                String strDbHost = !isNullOrEmpty(config.getDbhost())? config.getDbhost() : DB_HOST;
                String strPort = !isNullOrEmpty(config.getDbport())? config.getDbport() : DB_PORT;
                String strDbdatabase = !isNullOrEmpty(sid)? ":" + sid : "";
                String strService = !isNullOrEmpty(dbservice)? "/" + dbservice : "";
                String subProtocol = "jdbc:oracle:thin:@";
                
                if(!strService.isEmpty()) {
                    subProtocol += "//";
                }
                
                return subProtocol + strDbHost + ":" + strPort + strDbdatabase + strService;
			}
		};
	}

	public AbstractDbPanelDescriptor getDbPanelDescriptor() {
		return new OraDbPanelDescriptor();
	}

	public static void main(String[] args){
		new OraMain().domain(args);
	}

	public String getAPP_NAME() {
		return "ili2ora";
	}

	public String getDB_PRODUCT_NAME() {
		return "oracle";
	}

	public String getJAR_NAME() {
		return "ili2ora.jar";
	}

	protected void printConnectOptions() {
		System.err.println("--dbhost  host         The host name of the server. Defaults to "+DB_HOST+".");
		System.err.println("--dbport  port         The port number the server is listening on. Defaults to "+DB_PORT+".");
		System.err.println("--dbdatabase sid       The database name.");
		System.err.println("--dbservice servicename The Oracle service name.");
		System.err.println("--dbusr  username      User name to access database.");
		System.err.println("--dbpwd  password      Password of user used to access database.");
		System.err.println("--geomwkb              Geometry as WKB (to be used if no Oracle Spatial).");
		System.err.println("--geomwkt              Geometry as WKT (to be used if no Oracle Spatial).");
	}
    protected int doArgs(String[] args,int argi,Config config)
	{
		String arg=args[argi];
		if(arg.equals("--dbhost")){
			argi++;
			config.setDbhost(args[argi]);
			argi++;
		}else if(arg.equals("--dbport")){
			argi++;
			config.setDbport(args[argi]);
			argi++;
		}else if(arg.equals("--dbdatabase")){
			argi++;
			config.setDbdatabase(args[argi]);
			argi++;
		}else if(arg.equals("--dbusr")){
			argi++;
			config.setDbusr(args[argi]);
			argi++;
		}else if(arg.equals("--dbpwd")){
			argi++;
			config.setDbpwd(args[argi]);
			argi++;
		}else if(arg.startsWith("--geomwkb")){
			argi++;
			if (parseBooleanArgument(arg)){
				config.setGeometryConverter(ch.ehi.ili2ora.converter.OracleWKBColumnConverter.class.getName());
				config.setDdlGenerator(ch.ehi.sqlgen.generator_impl.jdbc.GeneratorOracleWKB.class.getName());
			}
		}else if(arg.startsWith("--geomwkt")){
			argi++;
			if (parseBooleanArgument(arg)) {
				config.setGeometryConverter(ch.ehi.ili2ora.converter.OracleWKTColumnConverter.class.getName());
				config.setDdlGenerator(ch.ehi.sqlgen.generator_impl.jdbc.GeneratorOracleWKT.class.getName());
			}
		}else if(arg.equals("--dbservice")) {
			argi++;
			dbservice = args[argi];
			argi++;
		}else if(arg.equals("--dbschema")){
            argi++;
            config.setDbschema(args[argi]);
            argi++;
        }else if(arg.equals("--generalTablespace")) {
            argi++;
            config.setValue(GeneratorOracleSpatial.GENERAL_TABLESPACE, args[argi]);
            argi++;
        }else if(arg.equals("--indexTablespace")) {
            argi++;
            config.setValue(GeneratorOracleSpatial.INDEX_TABLESPACE, args[argi]);
            argi++;
        }else if(arg.equals("--lobTablespace")) {
            argi++;
            config.setValue(GeneratorOracleSpatial.LOB_TABLESPACE, args[argi]);
            argi++;
        }
		
		return argi;
	}
    @Override
    protected void printSpecificOptions() {
        System.err.println("--dbschema  schema     The name of the schema in the database. Defaults to not set.");
        System.err.println("--generalTablespace tablespace  Tablespace to be used in general: tables, indexes, and large objects (lobs).");
        System.err.println("--indexTablespace tablespace    Tablespace for indexes: unique constraints and primary keys. Overrides general tablespace for index.");
        System.err.println("--lobTablespace tablespace      Tablespace for large objects: blob, clob, nclob, and bfile datatypes. Overrides general tablespace for lobs.");
    }
    
    private boolean isNullOrEmpty(String str) {
        return str==null||str.isEmpty();
    }
}
