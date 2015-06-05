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

import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.mvs.MVSDataSetManager;
import com.ibm.doctorbatch.magicsauce.mvs.RecordBytesParser;
import com.ibm.doctorbatch.magicsauce.websphere.AbstractBatchDataStream;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.ZFile;

/**
 * <p>A common base class for interacting with MVS files using JZOS' ZFile API. BDS instances
 * that extend from this base class will require the following xJCL properites:
 * 
 * <ol>
 *  <li>DSNAME: The qualified name of the dataset to open; may be specified in place of DSOATH. This DSNAME will be slash-quoted - that is, specifying DSNAME is the same as specifying <b>//'<i>DSNAME</i>'</b> for DSPATH. If DSPATH is specified, it will be used instead of DSNAME
 *  <li>DSPATH: The path argument passed to the fopen() command; may be specified in place of DSNAME and supports the "dd:<i>ddname</i>" syntax. If DSNAME is also specified, it will be ignored.</li>
 *  <li>DS_PARAMS: The dataset parameters passed to the 'fopen' method, through ZFile.open(...) call, during normal job initialization</li>
 *  <li>DS_PARAMS_RESTART: the dataset parameters passed to the 'fopen' method, through the ZFile.open(...) call, during job restart initialization</li>
 *  <li>DD_ALLOC: The TSO 'alloc' command used to allocate the dynamic DD card referencing DSNAME within the batch job's address space.</li>
 *  <li>DD_ALLOC_RESTART: The TSO 'alloc' command used to allocate the dynamic DD card referencing DSNAME within the batch job's address space, when restarting this job after a failure.</li>
 *  <li>DD_FREE: The TSO 'free' command used to free the dynamic DD card referencing DSNAME within the batch job's address space.</li>
 *  <li>RECORD_PARSER_CLASSNAME: The fully qualified name of the class implementing com.ibm.websphere.batch.framework.mvs.RecordParser, used to translate records to java bean instances, and vice-versa.</li>
 * </ol></p>
 * 
 * <p>Note that DD_ALLOC, DD_ALLOC_RESTART and DD_FREE are optional parameters. If they are omitted, then the BPXWDYN call 
 * will not be executed, and the dataset will be opened directly. This may result in unexpected behavior when 
 * interacting with generational datasets, or with datasets that are used within multiple steps of a top-level 
 * JCL when this job is triggered via WSGRID. If you do not specify DD_ALLOC, you must not specify DD_FREE; if you 
 * specify DD_ALLOC, you should also specify DD_FREE.</p>
 * 
 * <p>Known limitation: There is a known issue when when your DD_ALLOC statement attempts to allocate a GDG dataset
 * using a relative generation indicator, such as <code>'HLQ.TESTDATA.OUTPUT(+1)'</code>. While the generation will 
 * be allocated, it may not be cataloged prior to the subsequent open call, if your DSPATH refers to the dataset by name
 * rather than with the "dd:" syntax.</p>
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @param <T> The type of record this datastream interacts with.
 */
public abstract class AbstractMVSDataStream<T extends RecordBytes> extends AbstractBatchDataStream {

	private MVSDataSetManager<T> mvsDataSetManager;
	
	@Override
	protected void initialize(Properties props) {
		mvsDataSetManager = new MVSDataSetManager<T>();
		mvsDataSetManager.initialize(props, isRestart());
	}

	protected MVSDataSetManager<T> getMVSDataSetManager() {
		return mvsDataSetManager;
	}
	
	public void flush() {
		mvsDataSetManager.flush();
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

	public ZFile getZFile() {
		return mvsDataSetManager.getZFile();
	}
	
	public String getDataSetName() {
		return mvsDataSetManager.getDataSetName();
	}	
	
	public RecordBytesParser<T> getRecordParser() {
		return mvsDataSetManager.getRecordParser();
	}

}
