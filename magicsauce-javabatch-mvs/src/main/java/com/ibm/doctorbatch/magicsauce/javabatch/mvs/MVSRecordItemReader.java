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

import java.io.Serializable;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.mvs.MVSDataSetManager;
import com.ibm.doctorbatch.magicsauce.mvs.MVSRecordReader;
import com.ibm.etools.marshall.RecordBytes;

/**
 * <p>This ItemReader reads records from an MVS data set. This ItemReader records one record
 * per invocation of read(). To customize that behavior, extend this class and override the fetchRecord(...)
 * method. Requires the following ItemReader properites:
 * 
 * <ol>
 *  <li>DSNAME: The qualified name of the dataset to open; may be specified in place of DSOATH. This DSNAME will be slash-quoted - that is, specifying DSNAME is the same as specifying <b>//'<i>DSNAME</i>'</b> for DSPATH. If DSPATH is specified, it will be used instead of DSNAME
 *  <li>DSPATH: The path argument passed to the fopen() command; may be specified in place of DSNAME and supports the "dd:<i>ddname</i>" syntax. If DSNAME is also specified, it will be ignored.</li>
 *  <li>ds_parameters: The dataset parameters passed to the 'fopen' method, through ZFile.open(...) call, during normal job initialization</li>
 *  <li>ds_parameters_restart: the dataset parameters passed to the 'fopen' method, through the ZFile.open(...) call, during job restart initialization</li>
 *  <li>DD_ALLOC: The TSO 'alloc' command used to allocate the dynamic DD card referencing DSNAME within the batch job's address space.</li>
 *  <li>DD_ALLOC_RESTART: The TSO 'alloc' command used to allocate the dynamic DD card referencing DSNAME within the batch job's address space, when restarting this job after a failure.</li>
 *  <li>DD_FREE: The TSO 'free' command used to free the dynamic DD card referencing DSNAME within the batch job's address space.</li>
 *  <li>RECORD_PARSER_CLASSNAME: The fully qualified name of the class implementing com.ibm.websphere.batch.framework.mvs.RecordParser, used to translate records to java bean instances, and vice-versa.</li>
 * </ol></p>
 *
 * <p>Example inclusion in JSL:</p>
 * <pre>{@code
 *    <chunk>
 *      <reader ref="myReaderReference">
 *        <properties>
 *           <property name="DSNAME" value="DB2AFW.SAMPLES.INPUT.FILE01"/>
 *           <property name="ds_parameters" value="rb,,recfm=fb,type=record,lrecl=80"/>
 *           <property name="DD_ALLOC" value="alloc fi(batchdd1) da(DB2AFW.SAMPLES.INPUT.FILE01) new"/>
 *           <property name="DD_FREE" value="free fi(batchdd1)"/>
 *           <property name=RECORD_PARSER_CLASSNAME value="com.mycompany.mybatchapp.CustomerRecordParser"/>
 *        </properties>
 *      </reader>
 *      <processor>...</processor>
 *      <writer>...</writer>
 *    </chunk>
 * }</pre>
 * 
 * <p>Note that the DD_ALLOC and DD_ALLOC_RESTART commands can be used to differentiate between scenarios when 
 * this job should allocate a new dataset ("alloc fi(batchdd1) da(DB2AFW.SAMPLES.INPUT.FILE01) new space((100,50),CYL)"), 
 * and scenarios where a pre-existing dataset should be used instead ("alloc fi(batchdd1) da(DB2AFW.SAMPLES.INPUT.FILE01) shr").</p>
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @param <T>
 */
public class MVSRecordItemReader<T extends RecordBytes> extends AbstractMVSBatchArtifact<T> implements ItemReader {
	private MVSRecordReader<T> magicSauceReader;
	
		
	@Override
	public void open(Serializable state)  {
		magicSauceReader = new MVSRecordReader<T>();
		magicSauceReader.setDataSetManager((MVSDataSetManager<T>) initializeMVSDataSetManager());
		
		magicSauceReader.open( state );
	}
	
	@Override
	public Object readItem() throws Exception {
		return magicSauceReader.read();
	}

	@Override
	public void close() throws Exception {
		magicSauceReader.close();
	}
}
