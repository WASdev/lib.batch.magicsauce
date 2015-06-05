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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordReader;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordWriter;


public class RDWRecordReaderTestSuite {
	
	
	private RDWRecordReader<TestRecord> recordReader;

	@Before
	public void setup() {
		RecordBytesParser<TestRecord> parser = new RecordBytesParser<TestRecord>();
		Properties p = new Properties();
		p.put( "MVS_RECORDBYTES_CLASSNAME", TestRecord.class.getName());
		parser.initialize(p);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RDWRecordWriter<TestRecord> testWriter = new RDWRecordWriter<TestRecord>(outputStream, TestRecord.LRECL, parser);
		
		testWriter.write( new TestRecord(1, "test string 1" ) );
		testWriter.write( new TestRecord(2, "test string 2" ) );
		testWriter.write( new TestRecord(3, "test string 3" ) );
		testWriter.write( new TestRecord(4, "test string 4" ) );
		testWriter.write( new TestRecord(5, "test string 5" ) );
		
		recordReader = new RDWRecordReader<TestRecord>( new ByteArrayInputStream( outputStream.toByteArray() ), TestRecord.LRECL, parser );
	}
	
	@Test
	public void testRead() {
		for ( int i = 0; i < 5; ++i ) {
			TestRecord tr = recordReader.read();
			assertEquals("Test Record " +i+ "has unexpected id", i+1, tr.getId());
			assertEquals("Test Record " +i+ "has unexpected value", "test string " + (i+1), tr.getValue().trim());
		}
		
		assertNull("Expected null returned at EOF.", recordReader.read());
	}

	@Test
	public void testPositioning() {
		recordReader.setPosition(3);
		TestRecord tr = recordReader.read();
		
		assertEquals("Expected record 4", 4, tr.getId());
		assertEquals("Expected record 4", "test string 4", tr.getValue().trim());
	}
}
