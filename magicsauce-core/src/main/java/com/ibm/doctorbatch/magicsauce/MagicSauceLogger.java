/*
 * Copyright 2015 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.doctorbatch.magicsauce;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MagicSauceLogger {
	private final static Logger LOG = Logger.getLogger( MagicSauceLogger.class.getCanonicalName() );
	private final static MagicSauceLogger INSTANCE = new MagicSauceLogger();
	
	private MagicSauceLogger() {
		
	}
	
	public static MagicSauceLogger getInstance() {
		return INSTANCE;
	}
	
	public void trace( String message ) {
		LOG.finest( message );
	}
	
	public void info( String message ) {
		LOG.info( message );
	}
	
	public void warning( String message ) {
		LOG.warning( message );
	}
	
	public void severe( String message ) {
		LOG.severe( message );
	}
	
	public void exception( String message, Throwable t ) {
		LOG.log( Level.SEVERE, message, t );
	}
}
