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

import static com.ibm.doctorbatch.magicsauce.Constants.RECORD_PARSER_CLASSNAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.mvs.RDWRecordWriter;

public class RDWRecordTestSuiteFixture {
	private String fileName;
	private Properties bdsProperties;

	protected Properties getBatchDataStreamProperties() {
		return bdsProperties;
	}
	
	public String getFileName() { 
		return fileName;
	}

	
	@Before
	public void setup() {
		RecordBytesParser<TestRecord> parser = new RecordBytesParser<TestRecord>();

		bdsProperties = new Properties();
		bdsProperties.put( "MVS_RECORDBYTES_CLASSNAME", TestRecord.class.getName());
		parser.initialize(bdsProperties);
				
		try {
			File f = File.createTempFile("test", "test");
			fileName = f.getAbsolutePath();
			
			FileOutputStream fos = new FileOutputStream(f);
			RDWRecordWriter<TestRecord> recordWriter = new RDWRecordWriter<TestRecord>(fos, TestRecord.LRECL, parser);
			
			for ( int i = 0; i < 500000; ++i ) {
				recordWriter.write( new TestRecord(i+1, "test string " + (i+1) ) );
			}
			
			fos.flush();
			fos.close();
		} catch (IOException ioe ) {
			ioe.printStackTrace();
		}
		
		bdsProperties.put("RECORD_LENGTH", Integer.toString(TestRecord.LRECL));
		bdsProperties.put("FILE_NAME", fileName);
		bdsProperties.put(RECORD_PARSER_CLASSNAME, RecordBytesParser.class.getName());
		
	}
	
	@After 
	public void teardown() {
		File f = new File(fileName);
		if ( f.exists() )
			f.delete();
	}
}
