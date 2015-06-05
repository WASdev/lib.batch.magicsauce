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
import java.util.List;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.Writer;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;

public class MVSRecordWriter<T extends RecordBytes> implements Writer<T> {
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
	public void write(T record) {
		byte[] bytes = parser.parseObjectToRecord(record);
		try {
			file.write( bytes );
		} catch (ZFileException zfe) {
			MVSUtility.logZFileExceptionDetails(zfe, mvsDataSetManager.getDataSetName() );
			throw new BatchException(zfe);
		}	
	}


	@Override
	public void write(List<? extends T> records) {
		for ( T t : records )
			write(t);
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
