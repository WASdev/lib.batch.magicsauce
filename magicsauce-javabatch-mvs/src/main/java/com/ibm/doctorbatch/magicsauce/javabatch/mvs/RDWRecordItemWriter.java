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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemWriter;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.RDWOutputRecordStream;

public class RDWRecordItemWriter<T extends RecordBytes> implements ItemWriter {
	private int currentRecordInd = 0;
	private RecordBytesParser<T> recordParser;
	private RDWOutputRecordStream rdwOutputStream;
	private RandomAccessFile outputFile;
	
	@Inject @BatchProperty(name="FILE_NAME") private String fileName;
	@Inject @BatchProperty(name="RECORD_LENGTH") private String recordLength;
	@Inject @BatchProperty(name=RECORD_PARSER_CLASSNAME) private String recordParserClassName;
	@Inject @BatchProperty(name="MVS_RECORDBYTES_CLASSNAME") private String recordBytesClassName;
	private FileOutputStream fileOut;

	
	@Override
	public Serializable checkpointInfo() throws Exception {
		return (Integer) currentRecordInd;
	}

	@Override
	public void close() throws Exception {
		try {
			if ( this.rdwOutputStream != null ) 
			{
				this.rdwOutputStream.close();
				this.rdwOutputStream = null;
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
			outputFile = new RandomAccessFile( fileName, "rw" );
		    fileOut = new FileOutputStream(outputFile.getFD());
			rdwOutputStream = new RDWOutputRecordStream(fileOut);
			this.currentRecordInd = 0;
			
			if ( position != null ) {
				Long recordCount = (Long) position;
				setPosition(recordCount);
			}
		} catch(IOException ioEx) {
			throw new BatchException("Unexpected IO error while opening the stream", ioEx.getCause());
		}
	}

	private void write(T record) {
		writeRecord( this.recordParser, record, this.rdwOutputStream );
    	currentRecordInd++;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeItems(List<Object> items) throws Exception {
		for ( Object t : items )
			write((T)t);
	}
	
	protected void writeRecord( RecordBytesParser<T> recordProcessor, T record, RDWOutputRecordStream bos ) {
    	try {
        	byte[] recordBytes = this.recordParser.parseObjectToRecord(record);
    		bos.write( recordBytes );
		} catch (IOException e) {
			throw new BatchException( e );
		}		
	}
	
	protected void setPosition(long position) {
		int currentPos = 0;
		int reclen = -1;
		
		// Calculate correct offset
		for ( int i = 0; i < position; ++i ) {
			try {
				reclen = outputFile.readShort();

				currentPos += reclen;
				outputFile.seek( currentPos );
			} catch (IOException e) {
				throw new BatchException(e);
			}			
		}

		// Truncate at that offset
		try {
			outputFile.getChannel().truncate( currentPos );
		} catch (IOException e) {
			e.printStackTrace();
			throw new BatchException(e);
		}

	}
}
