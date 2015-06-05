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

import static com.ibm.doctorbatch.magicsauce.Constants.RECORD_PARSER_CLASSNAME;
import static com.ibm.doctorbatch.magicsauce.Constants.RECORD_PROCESSOR;
import static com.ibm.doctorbatch.magicsauce.websphere.Constants.UNIT_OF_WORK_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.AbstractBatchDataStream;
import com.ibm.doctorbatch.magicsauce.websphere.etl.ETLJobStep;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordReader;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordTestSuiteFixture;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordWriter;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.TestRecord;
import com.ibm.websphere.batch.BatchConstants;
import com.ibm.websphere.batch.BatchContainerDataStreamException;

public class ETLJobStepTestSuite extends RDWRecordTestSuiteFixture {
	private ETLJobStep<TestRecord, TestRecord> testJobStep;
	private ETLTestReader testReader;
	private ETLTestWriter testWriter;
	private int unitOfWorkSize = -1;
	
	/**
	 * Extends RDWRecordReader to expose an initialize(Properties) method
	 * to this test suite.
	 * 
	 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@tobat.ch)
	 *
	 */
	public class ETLTestReader extends RDWRecordReader<TestRecord> {
		public void initialize(Properties p) {
			super.initialize( p );
		}
	}	
	
	/**
	 * Extends RDWRecordWriter to expose an initialize(Properties) method
	 * to this test suite.
	 * 
	 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@tobat.ch)
	 *
	 */
	public class ETLTestWriter extends RDWRecordWriter<TestRecord> {
		public void initialize(Properties p) {
			super.initialize( p );
		}
	}
	
	/**
	 * Overrides ETLJobStep to force construction of batch data streams 
	 * for this unit test suite, since we don't have a batch data stream 
	 * manager in this context.
	 * 
	 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@tobat.ch)
	 */
	public class ETLTestJobStep extends ETLJobStep<TestRecord, TestRecord>
	{
		@Override
		@SuppressWarnings("unchecked")
		protected <T> T getBatchDataStream(String dataStreamName) {
			final Properties bdsProperties = new Properties();
			bdsProperties.put("FILE_NAME", getFileName());
			bdsProperties.put("RECORD_LENGTH", Integer.toString(TestRecord.LRECL));
			bdsProperties.put(RECORD_PARSER_CLASSNAME, RecordBytesParser.class.getName());
			bdsProperties.put( "MVS_RECORDBYTES_CLASSNAME", TestRecord.class.getName());
			
			
			AbstractBatchDataStream datastream;
			if ( dataStreamName.equals( "reader" ) ) {
				testReader = new ETLTestReader();
				testReader.initialize(bdsProperties);
				datastream = testReader;
			}
			else if (dataStreamName.equals( "writer" ) ) {
				testWriter = new ETLTestWriter();
				testWriter.initialize(bdsProperties);
				datastream = testWriter;
			} 
			else {
				fail( "Inplausible dataStreamName from ETLJobStep - expected 'reader' or 'writer' only." );
				return null;
			}

			try {
				datastream.open();
			} catch (BatchContainerDataStreamException e) {
				throw new BatchException(e);
			}
			
			return (T) datastream;
		}
	}
	
	@Before
	public void setUp() throws Exception {
		Properties jobStepProperties = new Properties();
		jobStepProperties.put(RECORD_PROCESSOR, EchoProcessBehavior.class.getName());
		
		unitOfWorkSize = new Random().nextInt(1000)+1000;		
		jobStepProperties.put(UNIT_OF_WORK_SIZE, Integer.toString(unitOfWorkSize));
		
		jobStepProperties.put("MAX_SKIP_RECORDS", "5");
		jobStepProperties.put("SKIP_RECORD_OBSERVER.1", TestSkipRecordObserver.class.getName());
		
		testJobStep = new ETLTestJobStep();
		
		testJobStep.setProperties( jobStepProperties );
		testJobStep.createJobStep();

		TestSkipRecordObserver.reset();
	}

	@After
	public void tearDown() throws Exception {
		testJobStep.destroyJobStep();
	}

	@Test
	public void testETLJobStep() {
		int i = 0;
		
		int rc = -1;
		while ( (rc=testJobStep.processJobStep()) == BatchConstants.STEP_CONTINUE )
		{
			++i;
			assertEquals( unitOfWorkSize*i, testWriter.getCurrentPosition() );
			assertEquals( unitOfWorkSize*i, testReader.getCurrentPosition() );
		}
		
		assertEquals( BatchConstants.STEP_COMPLETE, rc );
	}

	@Test
	public void testETLSkipRecord() {
		EchoProcessBehavior epb = (EchoProcessBehavior) testJobStep.getRecordProcessBehavior();
		epb.throwSkipRecordOn(new Random().nextInt(unitOfWorkSize));
		
		int rc = -1;
		do {
			rc = testJobStep.processJobStep();
		} while (rc == BatchConstants.STEP_CONTINUE );

		assertTrue( TestSkipRecordObserver.wasUpdated() );
	}
	

	@Test(expected=BatchException.class)
	public void testETLMaximumSkipRecord() {
		EchoProcessBehavior epb = (EchoProcessBehavior) testJobStep.getRecordProcessBehavior();
		epb.throwSkipRecordOn(-2);
		
		int rc = -1;
		do {
			rc = testJobStep.processJobStep();
		} while (rc == BatchConstants.STEP_CONTINUE );

		assertTrue( TestSkipRecordObserver.wasUpdated() );
	}
	
}
