/*
 *  SQLInputHandler
 *  
 *  cassjohnston@gmail.com
 *  
 */

package gate.cloud.io.sql;

import static gate.cloud.io.IOConstants.*;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.GateException;
import gate.Gate;



import gate.cloud.io.InputHandler;
import gate.cloud.io.DocumentData;
import gate.cloud.batch.DocumentID;
import gate.cloud.io.IOConstants;
import gate.cloud.io.InputHandler;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/* SQLInputHandler
 *
 * This takes a single ID and returns a GATE document for that ID from the database
 * Also see SQLStreamingInputHandler, which will take a WHERE clause rather than  
 * an ID and will then hand back the results as GATE documents one by one
 *
 */

public class SQLInputHandler implements InputHandler {

  private static Logger log = Logger.getLogger(SQLInputHandler.class);

  public static class DBSettings {
    public String driver;
    public String dbms;
    public String host;
    public String port;
    public String name;
    public String user;
    public String password;
    public String table;
    public String idcol;
    public String textcol;
    public String where;
  }

  public DBSettings db;
  protected Connection conn;
  protected PreparedStatement stmt;
  protected String mimeType;
  protected String encoding;
  
  /*-----------  Implementation of the InputHandler Interface -------------------------*/
  public void config(Map<String, String> configData) throws IOException, GateException {
      
    // database settings 
    db = new DBSettings();
    db.driver   = configData.get("driver");  // eg. com.microsoft.sqlserver.jdbc.SQLServerDriver
    db.dbms     = configData.get("dbms");    // eg sqlserver
    db.host     = configData.get("host");
    db.port     = configData.get("port");
    db.name     = configData.get("name");
    db.user     = configData.get("user");
    db.password = configData.get("password");
    db.table    = configData.get("table");
    db.idcol    = configData.get("idcol");
    db.textcol  = configData.get("textcol");

    // mime type
    mimeType = configData.get(PARAM_MIME_TYPE);
    //encoding
    encoding = configData.get(PARAM_ENCODING);               
 
  }

  public void init () throws IOException , GateException {
  
    // make the database connection
    conn = getConnection(db); 
    Gate.init();
  }

  public void close () throws IOException , GateException {
 
    // close the database connection
    try{
      conn.close();
    }
    catch (SQLException e){
      log.error("Problem closing database connection", e);
      System.exit(5);
    }

  }

  public DocumentData getInputDocument(DocumentID id) throws IOException, GateException {

    // fetch the specified document from the database
    String query = "SELECT " + db.textcol + " FROM " + db.table + " WHERE " + db.idcol + "='" + id +"'";   
    log.debug("attempting to prepare query: "+ query);

    String content = "";
    try{
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      int test = 1; 
      while(rs.next()) {
         if (test > 1){
           throw new IOException("Multiple rows retrieved from database. DocumentID must be a unique identifier");
         }
         content = rs.getString(db.textcol);
         test++; 
       }

    } catch(SQLException e){
      log.error("Problem running SQL query", e);
      System.exit(5);
    } 

    FeatureMap params = Factory.newFeatureMap();
    if(mimeType != null && mimeType.length() > 0) {
      params.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, mimeType);
    }
    if(encoding!= null && encoding.length() > 0){
      params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, encoding);
    }
    params.put(Document.DOCUMENT_MARKUP_AWARE_PARAMETER_NAME, Boolean.TRUE);
    params.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, content);

    DocumentData docData = new DocumentData(
            (Document)Factory.createResource("gate.corpora.DocumentImpl",
                params, Factory.newFeatureMap(), id.toString()), id);

    return docData;
    
  }

  /*-----------  Helper Methods -------------------------------------------------------*/

  protected Connection getConnection(DBSettings dbSettings) {

    try {
      Class.forName(dbSettings.driver).newInstance();
 
   }
    catch(Exception e) {
      log.error("Could not load JDBC driver", e);
      System.exit(4);
    }
    StringBuilder dbUrl = new StringBuilder("jdbc:");
    dbUrl.append(dbSettings.dbms);
    dbUrl.append("://");
    dbUrl.append(dbSettings.host);
    if(dbSettings.port != null) {
      dbUrl.append(":");
      dbUrl.append(dbSettings.port);
    }
    dbUrl.append(";DatabaseName=");
    dbUrl.append(dbSettings.name);
    if("sqlserver".equals(dbSettings.dbms)) {
      // jtds-specific parameters
      //dbUrl.append(";useCursors=true");
      // we're not using jtds. Nto sure what ian was doing here - he was instanciating the ms driver not eht jtds one anyway.
    }
    try {
      return DriverManager.getConnection(
              dbUrl.toString(), dbSettings.user,
              dbSettings.password);
    }
    catch(SQLException e) {
      log.error("Could not connect to database at " + dbUrl, e);
      System.exit(5);
    }
    // unreachable, but compiler doesn't know that
    return null;

  }

}
