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
import gate.cloud.io.sql.SQLStreamingInputHandler;
import gate.cloud.io.DocumentData;
import gate.cloud.batch.DocumentID;
import gate.cloud.io.DocumentData;


public class TestSQLStreamingInputHandler {
  protected static SQLStreamingInputHandler sq;

  @BeforeClass
  public static void init() throws IOException , GateException{
    sq = new SQLStreamingInputHandler();

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


  @Test 
  public void testConfig() {
    assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver",sq.db.driver);
  }
  
  @Test
  public void testConnect() throws IOException , GateException, SQLException {
    sq.init();
    sq.close(); 
  }
   
  @Test
  public void testDocumentRetrieval() throws IOException, GateException, SQLException {
     sq.init();
     DocumentData doc =sq.nextDocument();
     System.out.println(doc.document.toString());
     assertEquals(doc.getClass().getName(),"gate.cloud.io.DocumentData");
     doc = sq.nextDocument();
     assertEquals(doc, null);
     sq.close();
   }

}
