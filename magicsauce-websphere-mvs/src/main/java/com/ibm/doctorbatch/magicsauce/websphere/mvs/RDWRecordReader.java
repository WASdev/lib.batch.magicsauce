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
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.FILE_NAME;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.RECORD_LENGTH;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.AbstractBatchDataStream;
import com.ibm.doctorbatch.magicsauce.websphere.etl.Readable;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.RDWInputRecordStream;
import com.ibm.websphere.batch.BatchDataStream;

/**
 * <p>This data stream reads records from MVS datasets which have been FTP'd to Unix, Linux, or Windows
 * workstations using "RDW" mode. This allows developers to fully test their batch solutions on 
 * their development workstations prior to deploying the application to z/OS.</p> 
 * 
 * <p>To FTP a dataset to your workstation for consumption by this data stream implementation, you must
 * issue the FTP command "quote site rdw" and also transfer the data set in binary mode. For example:
 * <pre>ftp server1.mycompany.com
 * Username: user1
 * Password: *****
 * ftp> bin
 * ftp> quote site rdw
 * ftp> get 'USER1.TESTDAT.SAMPLE(INFILE)' myInputData.dat
 * </pre>
 * 
 * <p>The 'quote site rdw' command indicates to the server that it should preserve the record-deliminating-
 * word on the records. This allows this input BDS to process both fixed-block and variable-block record 
 * types.</p>
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch) *
 * @param <T> The record type read by this reader
 */
public class RDWRecordReader<T extends RecordBytes> extends AbstractBatchDataStream implements Readable<T>, BatchDataStream {

	private RecordBytesParser<T> recordParser;
	 
	private RDWInputRecordStream rdwInputStream;
	private String fileName;
	private int recordLength;
	private long currentRecordInd;
	
	public RDWRecordReader() {
		
	}
	
	/*
	 * Constructs an RDWRecordReader based on the given input stream, record length, and
	 * parser. This constructor is intended only for use with the frameworks unit test 
	 * suites, and is not for public use. When using this constructor, do not invoke
	 * initialize(props).
	 * 
	 * @param is
	 * @param recordLength
	 * @param parser
	 */
	RDWRecordReader( InputStream is, int recordLength, RecordBytesParser<T> parser ) {
		this.rdwInputStream = new RDWInputRecordStream(is);
		this.recordLength = recordLength;
		this.recordParser = parser;
		currentRecordInd = 0;
	}
	
	
	@Override
	protected void initialize(Properties props) {
		fileName = props.getProperty(FILE_NAME);
		currentRecordInd = 0; 
		
		if(fileName == null) {
			throw new BatchException(this.getName() + " " + this.getJobStepId() + " File Name not specified");
		}
		
		recordLength = Integer.parseInt(props.getProperty(RECORD_LENGTH, "-1"));
		
		if(recordLength == -1) {
			throw new BatchException(this.getName() + " " + this.getJobStepId() + " Record Length not specified");
		}

		String recordParserClassName = props.getProperty( RECORD_PARSER_CLASSNAME );
		recordParser = ClassUtil.getInstanceForClass(recordParserClassName);
		recordParser.initialize(props);
	}
	
	@Override
	public void open() {
		try {
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(fileName));
			this.rdwInputStream = new RDWInputRecordStream( bufferedInStream );
			this.currentRecordInd = 0;
		} catch(IOException ioEx) {
			throw new BatchException("Unexpected IO error while opening the stream", ioEx.getCause());
		}
	}

	@Override
	public void close() {
		try {
			this.rdwInputStream.close();
		} catch (IOException ioe) {
			throw new BatchException("Unexpected error while closing stream", ioe.getCause());
		}

	}

	@Override
	public void internalizeCheckpointInformation(String token) {
		try {
			if (token == null)
				this.positionAtInitialCheckpoint();
			else
				this.setPosition(Long.valueOf(token).longValue());

		} catch (BatchException nhbe) {
			throw new BatchException("Unexpected error in internalize", nhbe.getCause());
		}
	}

	@Override
	public String externalizeCheckpointInformation() {
		
		return Long.toString(currentRecordInd);
	}

	@Override
	public void positionAtInitialCheckpoint() {
		// Nothing to do here as open keeps the file at initial checkpoint
	}

	@Override
	public void positionAtCurrentCheckpoint() {
		// Nothing to do here as setPosition call from internalizeCheckpointInformation takes care of this
	}

	@Override
	public void intermediateCheckpoint() {
		// Nothing to do here
	}
	
	public long getCurrentPosition() {
		return currentRecordInd;
	}
	
	protected T fetchRecord( RDWInputRecordStream input, RecordBytesParser<T> recordParser ) {
    	byte[] recordBytes = null;
    	
    	try {
    		recordBytes = new byte[ this.recordLength ];
    		
    		int nread = input.read( recordBytes );
			if ( nread == -1 )
				return null;
		} catch (IOException e) {
			throw new BatchException("Unexpected error while reading record", e);
		} 

		T record = recordParser.parseRecordToObject(recordBytes);
    	return record;
	}
	
	
	@Override
	public T read() {	
		T currentRecord = null;
		
		try {
			currentRecord = fetchRecord(rdwInputStream, recordParser);
    	} finally {
	    	currentRecordInd++;
    	}
    	
    	return currentRecord;
	}

	protected void setPosition(long position) {
		try {
			currentRecordInd = position;
			for ( int i = 0; i < position; ++i )
				rdwInputStream.read();
		} catch (IOException ioe) {
			throw new BatchException("Unexpected error while setting position", ioe.getCause());
		}
	}
	
}
