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

package com.ibm.doctorbatch.magicsauce.javabatch.etl;

import javax.batch.api.BatchProperty;
import javax.batch.api.listener.StepListener;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.MagicSauceLogger;
import com.ibm.doctorbatch.magicsauce.RecordProcessor;

/**
 * Instantiates a RecordProcessor instance for this job step, and injects it 
 * into the DBMSProcessor instance.
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 */
public class DBMSProcessorRegistrationListener implements StepListener {

	@Inject private StepContext stepContext;
	@Inject @BatchProperty(name="RECORD_PROCESSOR") private String processorClassName;
	
	@Override
	public void afterStep() throws Exception {
		DBMSProcessor.getDMBSProcessor().removeProcessorInstance();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void beforeStep() throws Exception {
		RecordProcessor recordProcessor = null;

		MagicSauceLogger.getInstance().trace( "Instantiating record processor: " + processorClassName );
		recordProcessor = ClassUtil.getInstanceForClass( processorClassName );

		MagicSauceLogger.getInstance().trace( "Initializing record processor: " + processorClassName );
		recordProcessor.initialize(stepContext.getProperties());
		
		DBMSProcessor.getDMBSProcessor().setRecordProcessor(recordProcessor);
	}

}
