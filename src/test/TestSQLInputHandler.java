import org.junit.* ;
import static org.junit.Assert.* ;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.sql.SQLException;

import gate.util.GateException;
import gate.cloud.io.sql.SQLInputHandler;
import gate.cloud.io.DocumentData;
import gate.cloud.batch.DocumentID;



public class TestSQLInputHandler {
  protected static SQLInputHandler sq;

  @BeforeClass
  public static void init() throws IOException , GateException{
    sq = new SQLInputHandler();

    Properties prop = new Properties();
    InputStream input = null;

    String filename = "db.properties";
    input = TestSQLInputHandler.class.getClassLoader().getResourceAsStream(filename);
    if(input==null){
      throw new IOException("File not found");
    }
    prop.load(input);
    @SuppressWarnings("unchecked")
    Map<String, String> configData = new HashMap<String, String>((Map) prop);

    sq.config(configData);
  }

  @org.junit.Test 
  public void testConfig() {
    assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver",sq.db.driver);

  }
  
  @org.junit.Test
  public void testConnect() throws IOException , GateException, SQLException {
    sq.init();
    sq.close(); 
  }
   

  @org.junit.Test
  public void testDocumentRetrieval() throws IOException, GateException, SQLException {

    Properties prop = new Properties();
    InputStream input = null;

    String filename = "db.properties";
    input = TestSQLInputHandler.class.getClassLoader().getResourceAsStream(filename);
    if(input==null){
      throw new IOException("File not found");
    }
    prop.load(input);
    String testid = prop.getProperty("testid");

    System.out.println("WTF: "+testid);

    sq.init();
    DocumentID id = new DocumentID("10000040650599");
    sq.getInputDocument(id);
    sq.close();
  }


}
