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

import java.util.Properties;

/**
 * RecordProcessBehavior implementations encapsulate reusable business logic 
 * for use in this framework's JobStepInterface implementation classes, such 
 * as ETLBatchJobStep. 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 *
 * @param <InputRecordType> The type of input record object to process
 * @param <OutputRecordType> The type of the returned, processed record object.
 */
public interface RecordProcessor<InputRecordType,OutputRecordType> {
	/**
	 * Initialize this record process behavior instance with the job step
	 * properties. This should be called during createJobStep() so that the
	 * record process behavior is fully initialized prior to invocation of 
	 * processJobStep, or doUnitOfWork.
	 * 
	 * @param jobStepProperties The job step properties
	 */
	public void initialize( Properties jobStepProperties );
	
	/**
	 * Provides the record processing, or transform, behavior.
	 * 
	 * @param record The input record to process
	 * @return The processed or transformed record object of type P
	 */
	public OutputRecordType process(InputRecordType record );
	
	/**
	 * The record process behavior instance can compute a return code across
	 * multiple invocations of the process(...) method. This return code is 
	 * meant to be used by the job step implementation's destroyJobStep method, 
	 * but could be called at any point during execution for current status 
	 * information.
	 * 
	 * @return The return code for the job step.
	 */
	public int getReturnCode();

	/**
	 * Invoked at the end of the job.
	 */
	public void tearDown();
}
