/*
 *  SQLStreamingInputHandler
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
import gate.cloud.batch.Batch;
import gate.cloud.batch.DocumentID;
import gate.cloud.io.IOConstants;
import gate.cloud.io.InputHandler;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/* SQLStreamingInputHandler
 *
 * Returns elements from a specified text field in a specified database table
 * one by one as Gate documents.
 * Takes an optional WHERE clause to limit the resultset 
 *
 */

public class SQLStreamingInputHandler implements InputHandler {

  private static Logger log = Logger.getLogger(SQLStreamingInputHandler.class);

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
  protected ResultSet rs;

  /**
   * Document IDs that are already complete after a previous run of this
   * batch.
   */
  protected Set<String> completedDocuments;
 

 
  /*-----------  Implementation of the StreamingInputHandler Interface -------------------------*/

  public DocumentData getInputDocument(DocumentID id) throws IOException,
          GateException {
    throw new UnsupportedOperationException(
            "SQLStreamingInputHandler can only operate in streaming mode");
  }


  public void config(Map<String, String> configData) throws IOException, GateException {
    
    // Database settings
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
    db.where    = configData.get("where"); 

    // mime type
    mimeType = configData.get(PARAM_MIME_TYPE);
    //encoding
    encoding = configData.get(PARAM_ENCODING);               
 
  }

  public void startBatch(Batch b) {

    // determine which documents have already been done
    completedDocuments = b.getCompletedDocuments();
    if(completedDocuments != null && completedDocuments.size() > 0) {
      log.info("Restarting failed batch - " + completedDocuments.size()
              + " documents already processed");
    }
   
  }

  public void init () throws IOException , GateException {
  
    // make the database connection
    conn = getConnection(db); 
    Gate.init();

    String query = "SELECT " + db.idcol + ',' + db.textcol + " FROM " + db.table;
    if(db.where != null && !"".equals(db.where)){
     query = query + " WHERE " + db.where;
    }
    log.debug("attempting to prepare query: "+ query);

    // fetch a resultset  
    try{
      PreparedStatement stmt = conn.prepareStatement(query);
      rs = stmt.executeQuery();
    } catch(SQLException e){
      log.error("Problem running SQL query", e);
      System.exit(5);
    }
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

  public DocumentData nextDocument() throws IOException, GateException {

    String content = "";
    String idText = ""; 

    try{
      // get the next entry from the resultset
      if(rs.next()){
        content = rs.getString(db.textcol);
        idText      = rs.getString(db.idcol);
      } else {
        //reached the end of the resultset
        return null;
      }
    }catch (SQLException e){
      log.error("Failed to get next entry from resultset", e);
      System.exit(5);
    }


    DocumentID id = new DocumentID(idText);

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
