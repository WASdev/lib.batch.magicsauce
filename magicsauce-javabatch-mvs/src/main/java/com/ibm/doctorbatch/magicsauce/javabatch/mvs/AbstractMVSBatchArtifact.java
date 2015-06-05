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
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_ALLOC;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_ALLOC_RESTART;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_FREE;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DSNAME;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DSPATH;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DS_PARAMS;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DS_PARAMS_RESTART;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.LARGE_DATASET_SUPPORT;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.MVS_RECORDBYTES_CLASSNAME;

import java.io.Serializable;
import java.util.Properties;

import javax.batch.api.BatchProperty;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.mvs.DataSetManager;
import com.ibm.doctorbatch.magicsauce.mvs.MVSDataSetManager;
import com.ibm.etools.marshall.RecordBytes;

public class AbstractMVSBatchArtifact<T extends RecordBytes> {

	@Inject @BatchProperty(name=DSNAME) private String datasetName;
	@Inject @BatchProperty(name=DSPATH) private String datasetPath;
	@Inject @BatchProperty(name=DD_ALLOC) private String datasetAlloc;
	@Inject @BatchProperty(name=DD_ALLOC_RESTART) private String datasetRestartAlloc;
	@Inject @BatchProperty(name=DD_FREE) private String datasetFree;
	@Inject @BatchProperty(name=RECORD_PARSER_CLASSNAME) private String parserClassName;
	@Inject @BatchProperty(name=MVS_RECORDBYTES_CLASSNAME) private String recordbytesClassName;

	@Inject @BatchProperty(name=DS_PARAMS) private String datasetParameters;
	@Inject @BatchProperty(name=DS_PARAMS_RESTART) private String datasetRestartParameters;
	@Inject @BatchProperty(name=LARGE_DATASET_SUPPORT) private String largeDatasetSupport;

	@SuppressWarnings("unused")
	@Inject private StepContext stepContext;
	
	private DataSetManager<T> mvsDataSetManager;
	
	public DataSetManager<T> initializeMVSDataSetManager() {
		// Need properties...		
		Properties props = new Properties();
		if ( datasetName != null ) props.put(DSNAME, datasetName);
		if ( datasetPath != null ) props.put(DSPATH, datasetPath);
		if ( datasetAlloc != null ) props.put(DD_ALLOC, datasetAlloc);
		if ( datasetRestartAlloc != null ) props.put(DD_ALLOC_RESTART, datasetRestartAlloc);
		if ( datasetFree != null ) props.put(DD_FREE, datasetFree);
		if ( parserClassName != null ) props.put(RECORD_PARSER_CLASSNAME, parserClassName);
		if ( recordbytesClassName != null ) props.put(MVS_RECORDBYTES_CLASSNAME, recordbytesClassName );
		
		if ( datasetParameters != null ) props.put(DS_PARAMS, datasetParameters);
		if ( datasetRestartParameters != null ) props.put(DS_PARAMS_RESTART, datasetRestartParameters);
		if ( largeDatasetSupport != null ) props.put(LARGE_DATASET_SUPPORT, largeDatasetSupport);

		return initializeMVSDataSetManager(props);
	}
	
	public DataSetManager<T> initializeMVSDataSetManager( Properties props ) {
		DataSetManager<T> mvsDataSetManager  = new MVSDataSetManager<T>();
		mvsDataSetManager.initialize(props);

		return mvsDataSetManager;
	}
	
	
	public Serializable checkpointInfo() throws Exception {
		return String.valueOf(mvsDataSetManager.getCurrentPosition());
	}



	protected MVSDataSetManager<T> getMVSDataSetManager() {
		return (MVSDataSetManager<T>) this.mvsDataSetManager;
	}

	public void open(Properties props, Serializable arg0) {
		mvsDataSetManager = initializeMVSDataSetManager(props);
		mvsDataSetManager.open();
		
		if ( arg0 != null ) {
			Long position = (Long) arg0;
			mvsDataSetManager.setPosition( position.longValue() );
		}
	}
}
