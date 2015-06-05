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

package com.ibm.doctorbatch.magicsauce.websphere.mvs;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordReader;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordWriter;

public class RDWFileTestSuite extends RDWRecordTestSuiteFixture {


	/**
	 * Tests that restart positioning works properly when using RDWRecordWriter. 
	 */
	@Test
	public void testWriterRestartPositioning() {
		Properties bdsProperties = getBatchDataStreamProperties();
		
		RDWRecordWriter<TestRecord> testWriter;
		testWriter = new RDWRecordWriter<TestRecord>();
		testWriter.initialize(bdsProperties);
		testWriter.open();
		testWriter.setPosition(3);
		testWriter.close();
		
		RDWRecordReader<TestRecord> recordReader = 
				new RDWRecordReader<TestRecord>( );
		recordReader.initialize(bdsProperties);
		recordReader.open();
		
		int i = 0; 
		TestRecord tr;
		while ( (tr=recordReader.read()) != null ) {
			assertEquals( i+1, tr.getId() );
			++i;
		}
		
		assertEquals("Expected 3 records...", 3, i);
	}
	
}
