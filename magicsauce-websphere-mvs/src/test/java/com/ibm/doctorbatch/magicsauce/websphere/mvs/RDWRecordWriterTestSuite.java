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

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordWriter;


public class RDWRecordWriterTestSuite {
	private ByteArrayOutputStream outputStream;
	private RecordBytesParser<TestRecord> parser;
	private RDWRecordWriter<TestRecord> recordWriter;

	@Before
	public void setup() {
		parser = new RecordBytesParser<TestRecord>();
		Properties p = new Properties();
		p.put( "MVS_RECORDBYTES_CLASSNAME", TestRecord.class.getName());
		parser.initialize(p);
		
		outputStream = new ByteArrayOutputStream();
		recordWriter = new RDWRecordWriter<TestRecord>(outputStream, TestRecord.LRECL, parser);
		
		recordWriter.write( new TestRecord(1, "test string 1" ) );
		recordWriter.write( new TestRecord(2, "test string 2" ) );
		recordWriter.write( new TestRecord(3, "test string 3" ) );
		recordWriter.write( new TestRecord(4, "test string 4" ) );
		recordWriter.write( new TestRecord(5, "test string 5" ) );
	}


	@Test
	public void testPositioning() {
		assertEquals(5, recordWriter.getCurrentPosition());
	}
	


}
