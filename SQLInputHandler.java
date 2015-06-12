/*
 *  SQLInputHandler
 *  
 *  cassjohnston@gmail.com
 *  
 */

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.GateException;
import gate.Gate;

import gate.cloud.io.InputHandler;
import gate.cloud.io.DocumentData;
import gate.cloud.batch.DocumentID;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;


public class SQLInputHandler implements InputHandler {

  private static Logger logger = Logger.getLogger(SQLInputHandler.class);


  /*-----------  Implementation of the InputHandler Interface -------------------------*/

  public void config(Map<String, String> configData) throws IOException, GateException {}

  public void init () throws IOException , GateException {}

  public void close () throws IOException , GateException {}

  public DocumentData getInputDocument(DocumentID id) throws IOException, GateException {
    FeatureMap params = Factory.newFeatureMap();
    DocumentData docData = new DocumentData(
            (Document)Factory.createResource("gate.corpora.DocumentImpl",
                params, Factory.newFeatureMap(), id.toString()), id);
    return docData;
  }

  /*-----------  Helper Methods -------------------------------------------------------*/


}
