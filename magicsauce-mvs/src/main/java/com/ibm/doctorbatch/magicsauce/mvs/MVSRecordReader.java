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

package com.ibm.doctorbatch.magicsauce.mvs;

import java.io.Serializable;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.Reader;
import com.ibm.doctorbatch.magicsauce.mvs.MVSDataSetManager;
import com.ibm.doctorbatch.magicsauce.mvs.MVSUtility;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;

/**
 * <p>This ItemReader reads records from an MVS data set. This ItemReader records one record
 * per invocation of read(). To customize that behavior, extend this class and override the fetchRecord(...)
 * method. Requires the following ItemReader properites:
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @param <T>
 */
public  class MVSRecordReader<T extends RecordBytes> implements Reader<T> {
	private ZFile file;
	private RecordBytesParser<T> parser;
	private MVSDataSetManager<T> mvsDataSetManager;

	public void setDataSetManager( MVSDataSetManager<T> datasetManager ) {
		this.mvsDataSetManager = datasetManager;
	}
	
	@Override
	public void open(Serializable state)  {
		mvsDataSetManager.open();
		mvsDataSetManager.setPosition( ((Long) state).longValue() );
		this.file = mvsDataSetManager.getZFile();
		this.parser = mvsDataSetManager.getRecordParser();
	}
	
	@Override
	public T read() {
		return fetchRecord(file, parser);
	}

	protected T fetchRecord(ZFile file, RecordBytesParser<T> recordParser) {
		byte[] recordBytes = null;

		try {
			recordBytes = new byte[file.getLrecl()];
			if ( -1 == file.read(recordBytes))
				return null;
		} catch (ZFileException zfe) {
			MVSUtility.logZFileExceptionDetails(zfe, mvsDataSetManager.getDataSetName() );
			throw new BatchException("Unexpected error while fetching record", zfe.getCause());
		}

		T currentRecord = recordParser.parseRecordToObject(recordBytes);
		return currentRecord;
	}

	@Override
	public void close() {
		mvsDataSetManager.close();
	}

	@Override
	public Serializable getState() {
		return mvsDataSetManager.getCurrentPosition();
	}
}
