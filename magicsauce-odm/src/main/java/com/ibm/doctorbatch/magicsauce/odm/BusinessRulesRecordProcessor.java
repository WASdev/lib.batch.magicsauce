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

package com.ibm.doctorbatch.magicsauce.odm;

import ilog.rules.res.model.IlrPath;
import ilog.rules.res.session.IlrJ2SESessionFactory;
import ilog.rules.res.session.IlrPOJOSessionFactory;
import ilog.rules.res.session.IlrSessionCreationException;
import ilog.rules.res.session.IlrSessionFactory;
import ilog.rules.res.session.IlrSessionResponse;
import ilog.rules.res.session.IlrStatefulSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.RecordProcessor;

import static com.ibm.doctorbatch.magicsauce.odm.Constants.*;

/**
 * <p>This record process behavior implementation invokes ILOG Business Rules 
 * to process, validate, or transform the input record for use with the 
 * ETLJobStep implementation. This implementation assumes that a rule path
 * has a single input paramter, and single output parameter (which may, for IN_OUT values, 
 * be the same parameter). 
 * 
 * This behavior expects three job-step level properties: 
 * <ul><li>RULEPATH: The ILR rule path to invoke</li>
 *     <li>RULEPATH_INPUT_PARAMETER: The name of the input parameter for the rule path</li>
 *     <li>RULEPATH_OUTPUT_PARAMETER: The name of the output parameter for the rule path</li>
 *     <li>USE_JAVASE_SESSION: Defaults to true, set to false to use the POJO Session Factory instead</li>
 * </ul></p>
 * 
 * 
 * @param <S> The input XOM record type for this step.
 * @param <R> The output XOM record type for this step.
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 */
public class BusinessRulesRecordProcessor<S, R> implements RecordProcessor<S, R> {
	private String rulePath, inputParam, outputParam;
	private IlrSessionFactory sessionFactory;
	private IlrStatefulSession session;
	private Map<String,Object> inputParameterMap;
	
	@Override
	public void initialize(Properties jobStepProperties) {
		this.rulePath = jobStepProperties.getProperty( RULEPATH );
		this.inputParam = jobStepProperties.getProperty(RULEPATH_INPUT_PARAMETER );
		this.outputParam = jobStepProperties.getProperty(RULEPATH_OUTPUT_PARAMETER );
		

		if ( rulePath == null )
			throw new BatchException( RULEPATH + " is requied for RuleBehavior use." );
		
		if ( inputParam == null )
			throw new BatchException( RULEPATH_INPUT_PARAMETER + " is requied for RuleBehavior use." );
		
		if ( outputParam == null )
			throw new BatchException( RULEPATH_OUTPUT_PARAMETER + " is requied for RuleBehavior use." );
		
		boolean javase = Boolean.parseBoolean(jobStepProperties.getProperty(USE_JAVASE_SESSION, "true"));
		sessionFactory = (javase ? new IlrJ2SESessionFactory() : new IlrPOJOSessionFactory());  

		try {
			session = sessionFactory.createStatefulSession( new IlrPath(rulePath), null, null, false);
		} 
		catch ( IlrSessionCreationException isce ) 
		{
			isce.printStackTrace();
			throw new RuntimeException(isce);
		}

		inputParameterMap = new HashMap<String,Object>();

	}

	/**
	 * Override this method to add custom input parameters needed
	 * by your rulesets. This method is called before the request
	 * is executed.
	 * 
	 * @param request
	 */
	protected void initializeInputParameters( Map<String,Object> inputParameterMap ) {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public R process(S record) {
		try {
			inputParameterMap.clear();
			inputParameterMap.put( inputParam, record );
			initializeInputParameters(inputParameterMap);
			
			session.reset();
			IlrSessionResponse response = session.execute(inputParameterMap);
			
			return (R) response.getOutputParameters().get(outputParam);
			
		} catch ( Throwable t ) {
			throw new BatchException(t);
		}
	}

	@Override
	public int getReturnCode() {
		session.close();
		return 0;
	}

	@Override
	public void tearDown() {

		
	}
}
