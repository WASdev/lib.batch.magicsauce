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

package com.ibm.doctorbatch.magicsauce.websphere;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.batch.api.BatchJobStepInterface;
import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.websphere.batch.BatchDataStreamMgr;
import com.ibm.websphere.batch.BatchContainerDataStreamException;
import com.ibm.websphere.batch.context.JobStepContextMgr;

/**
 * AbstractBatchJobStep is an Observable job step base class. 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 *
 * @version 1.0
 * 
 */
public abstract class AbstractJobStep extends java.util.Observable implements BatchJobStepInterface {

	private Properties properties;
	private String jobStepId;

	private Context context;

	@Override
	public void createJobStep() {
		this.jobStepId = JobStepContextMgr.getContext().getJobStepID().getJobstepid();
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Properties props) {
		this.properties = props;
	}
	
	protected String getProperty( String name ) {
		return properties.getProperty( name );
	}

	protected String getProperty( String name, String defaultValue ) {		
		return properties.getProperty(name,defaultValue);
	}
	
	protected String getJobStepId() {
		return this.jobStepId;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getBatchDataStream(String dataStreamName ) {
		try {
			return (T) BatchDataStreamMgr.getBatchDataStream(
					dataStreamName, getJobStepId());
		} catch (BatchContainerDataStreamException e) {
			throw new BatchException("Error retrieving batch data stream", e);
		}
	}

    protected Context getContext(){
        try {
              if (context == null){
                    context = new InitialContext();
              }
              return context;
        } catch (NamingException ne) {
              throw new BatchException(ne.getMessage(), ne.getCause());
        }
    }
	
}
