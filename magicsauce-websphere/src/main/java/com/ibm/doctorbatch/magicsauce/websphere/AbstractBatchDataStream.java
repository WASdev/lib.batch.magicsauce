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

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.websphere.batch.BatchDataStream;
import com.ibm.websphere.batch.devframework.configuration.BDSFrameworkConstants;

import java.util.logging.Logger;

/**
 * AbstractBatchDataStream is the common base class for batch data stream implementations in
 * this framework. Job step implementations should refer to Reader<T> and 
 * Writer<T> interface instances instead of this class directly. 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 *
 * @version 1.0
 */
public abstract class AbstractBatchDataStream implements BatchDataStream {
	private Properties props;
	private String name;
	private String jobStepId;
	private boolean restart;
	private Context context;

	protected static final String jobStepKey = "JobStepId";
	private Logger logger;

	protected Logger getBatchDataStreamLogger() {
		return logger;
	}
	
	@Override
	public void initialize(String name, String jobstepId) {        
		this.name = name;
		this.jobStepId = jobstepId;
		this.props.put(jobStepKey, jobstepId);
		restart = Boolean.parseBoolean(props.getProperty(
				BDSFrameworkConstants.IS_JOB_RESTART, "false"));
	
		logger = Logger.getLogger(name);
		
		initialize(props);
	}

	protected abstract void initialize(Properties props);

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Properties getProperties() {
		return this.props;
	}
	
	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * @return the jobstepId
	 */
	public String getJobStepId() {
		return jobStepId;
	}

	/**
	 * @return the restart
	 */
	public boolean isRestart() {
		return restart;
	}

	/**
	 * Convenience method to return a property value.
	 * Throws an exception if the property was not set, or was
	 * set to an empty value.
	 * 
	 * @param key
	 *   Property key.
	 * @return
	 *   Property value.
	 *  
	 * @throws BatchException
	 *   No value was specified for the given key.
	 */
    protected String getRequiredProperty(String key) {
        String value = getProperties().getProperty(key);
        if (value == null) {
            throw new BatchException("Missing required property '" + key + "'");
        }
        value = value.trim();
        if (value.length() == 0) {
            throw new BatchException("Required property '" + key + "' has empty value");
        }
        return value;
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
