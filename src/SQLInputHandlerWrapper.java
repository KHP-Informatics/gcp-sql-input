/*
 *  SQLInputHandlerWrapper.java
 *
 * Copyright (c) 2000-2012, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  root, 16/6/2015
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package gate.cloud.io.sql;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;

import gate.cloud.io.sql.SQLInputHandler;
import gate.cloud.io.sql.SQLStreamingInputHandler;

/** 
 * This class is the implementation of the resource SQLINPUTHANDLER.
 */
@CreoleResource(name = "SQLInputHandlerWrapper",
                comment = "Wrapper class to load SQLInputHandler and SQLStreamingInputHandler classes into a GATE application for GCP")
public class SQLInputHandlerWrapper  extends AbstractProcessingResource
  implements ProcessingResource {
  // doesn't actually do anything, just imports the SQLInputHandler and SQLStreamingInputHandler classes.

} // class SQLInputHandler
