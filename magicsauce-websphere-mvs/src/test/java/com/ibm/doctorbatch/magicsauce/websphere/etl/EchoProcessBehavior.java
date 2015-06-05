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

package com.ibm.doctorbatch.magicsauce.websphere.etl;

import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.RecordProcessor;
import com.ibm.doctorbatch.magicsauce.SkipRecordException;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.TestRecord;

public class EchoProcessBehavior implements RecordProcessor<TestRecord, TestRecord> {
	private int throwSkipRecordOn = -1;
	private int processCounter = 0;
	
	@Override
	public void initialize(Properties jobStepProperties) {
		
	}
	
	/**
	 * -1 for never.
	 * -2 for always.
	 * +n for once on that count.
	 * 
	 * @param count
	 */
	public void throwSkipRecordOn( int count ) {
		this.throwSkipRecordOn = count;
	}

	@Override
	public TestRecord process(TestRecord record) {
		++processCounter;
		
		if ( throwSkipRecordOn == processCounter || throwSkipRecordOn == -2 ) {
			throw new SkipRecordException( record );
		} 
		
		return record;
	}

	@Override
	public int getReturnCode() {
		return 0;
	}

	@Override
	public void tearDown() {
		
	}


}
