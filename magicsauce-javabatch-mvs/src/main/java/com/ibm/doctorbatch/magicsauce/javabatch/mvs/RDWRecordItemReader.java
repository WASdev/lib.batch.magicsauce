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

package com.ibm.doctorbatch.magicsauce.javabatch.mvs;

import static com.ibm.doctorbatch.magicsauce.Constants.RECORD_PARSER_CLASSNAME;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.RDWInputRecordStream;

public class RDWRecordItemReader<T extends RecordBytes> implements ItemReader {
	private int currentRecordInd = 0;
	private RecordBytesParser<T> recordParser;
	private RDWInputRecordStream rdwInputStream;
	
	@Inject @BatchProperty(name="FILE_NAME") private String fileName;
	@Inject @BatchProperty(name="RECORD_LENGTH") private String recordLength;
	@Inject @BatchProperty(name=RECORD_PARSER_CLASSNAME) private String recordParserClassName;
	@Inject @BatchProperty(name="MVS_RECORDBYTES_CLASSNAME") private String recordBytesClassName;

	
	@Override
	public Serializable checkpointInfo() throws Exception {
		return (Integer) currentRecordInd;
	}

	@Override
	public void close() throws Exception {
		try {
			if ( this.rdwInputStream != null ) {
				this.rdwInputStream.close();
				this.rdwInputStream = null;
			}
		} catch (IOException ioe) {
			throw new BatchException("Unexpected error while closing stream", ioe.getCause());
		}
	}

	@Override
	public void open(Serializable position) throws Exception {
		currentRecordInd = 0; 
		
		if( fileName == null ) 
			throw new BatchException("Error in RDWRecordReader: FILE_NAME not specified");
		
		if ( recordLength == null ) 
			throw new BatchException("Error in RDWRecordReader: RECORD_LENGTH not specified");

		if ( recordParserClassName == null )
			throw new BatchException( "Error in RDWRecordReader: RECORD_PARSER_CLASSNAME not specified." );
		
		if ( recordBytesClassName == null ) 
			throw new BatchException( "Error in RDWRecordReader: MVS_RECORDBYTES_CLASSNAME not specified." );
		
		Properties props = new Properties();
		props.put( "FILE_NAME", fileName );
		props.put( "RECORD_LENGTH", recordLength );
		props.put( RECORD_PARSER_CLASSNAME, recordParserClassName );
		props.put( "MVS_RECORDBYTES_CLASSNAME", recordBytesClassName );
		
		recordParser = ClassUtil.getInstanceForClass(recordParserClassName);
		recordParser.initialize(props);
		
		try {
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(fileName));
			this.rdwInputStream = new RDWInputRecordStream( bufferedInStream );
			this.currentRecordInd = 0;
			
			if ( position != null ) {
				Integer recordCount = (Integer) position;
				for ( int i = 0; i < recordCount; ++i )
					readItem();
			}
		} catch(IOException ioEx) {
			throw new BatchException("Unexpected IO error while opening the stream", ioEx.getCause());
		}
	}

	@Override
	public Object readItem() throws Exception {
		T currentRecord = null;
		
		try {
			currentRecord = fetchRecord(rdwInputStream, recordParser);
    	} finally {
	    	currentRecordInd++;
    	}
    	
    	return currentRecord;
	}


	protected T fetchRecord( RDWInputRecordStream input, RecordBytesParser<T> recordParser ) {
    	byte[] recordBytes = null;
    	
    	try {
    		recordBytes = new byte[ Integer.parseInt(this.recordLength) ];
    		
    		int nread = input.read( recordBytes );
			if ( nread == -1 )
				return null;
		} catch (IOException e) {
			throw new BatchException("Unexpected error while reading record", e);
		} 

		T record = recordParser.parseRecordToObject(recordBytes);
    	return record;
	}
}
