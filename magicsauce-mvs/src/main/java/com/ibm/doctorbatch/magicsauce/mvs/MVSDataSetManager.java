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

import static com.ibm.doctorbatch.magicsauce.Constants.RECORD_PARSER_CLASSNAME;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_ALLOC;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_ALLOC_RESTART;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DD_FREE;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DSNAME;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DSPATH;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DS_MODE;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DS_PARAMS;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.DS_PARAMS_RESTART;
import static com.ibm.doctorbatch.magicsauce.mvs.Constants.LARGE_DATASET_SUPPORT;

import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.MagicSauceLogger;
import com.ibm.etools.marshall.RecordBytes;
import com.ibm.jzos.RcException;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;

/**
 * <p>MVSDataSetManager provides reusable logic for interacting with MVS Data Sets. It's intended for use
 * with either WAS Batch BatchDataStream or JSR-352 Reader/Writer implementations.</p>
 * 
 * <p>Expected properties:
 * <ol>
 *  <li>DSNAME: The qualified name of the dataset to open; may be specified in place of DSOATH. This DSNAME will be slash-quoted - that is, specifying DSNAME is the same as specifying <b>//'<i>DSNAME</i>'</b> for DSPATH. If DSPATH is specified, it will be used instead of DSNAME
 *  <li>DSPATH: The path argument passed to the fopen() command; may be specified in place of DSNAME and supports the "dd:<i>ddname</i>" syntax. If DSNAME is also specified, it will be ignored.</li>
 *  <li>DS_PARAMS: The dataset parameters passed to the 'fopen' method, through ZFile.open(...) call, during normal job initialization</li>
 *  <li>DS_PARAMS_RESTART: the dataset parameters passed to the 'fopen' method, through the ZFile.open(...) call, during job restart initialization</li>
 *  <li>ds_mode: Optional parameter. If DD_ALLOC allocates a new dataset, specifying recfm and lrecl, then you can specify the open mode here, and omit ds_parameters... </li>
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
public class MVSDataSetManager<T extends RecordBytes> implements DataSetManager<T> {
	
	/*
	 * DataSet name to open. This will be either //'DSNAME' or 
	 * just DSPATH if specified. DSPATH overrides DSNAME.
	 */
	private String dsname = "";
	
	/*
	 * TSO Alloc command used on job start
	 */
	private String tsoAlloc = "";
	
	/*
	 * TSO Alloc command used on job restart
	 */
	private String tsoAllocRestart = "";
	
	/*
	 * TSO Free command used when job ends, or exception thrown from this BDS instance.
	 */
	private String tsoFree = "";

	/*
	 * Dataset parameters used by ZFile open on job start
	 */
	private String ds_parameters = "ab,recfm=fb,type=record,lrecl=80";
	
	/*
	 * Dataset parameters used by ZFile open on job restart
	 */
	private String ds_parameters_restart = "rb+,recfm=fb,type=record,lrecl=80";
	
	/*
	 * This ZFile instance for interacting with the dataset
	 */
	private ZFile zFile;
	
	/*
	 * Determines if we're using byte-count offsets or not.
	 */
	private boolean useSeekAndTell = true;
	
	/**
	 * Set to true on a restart scenario
	 */
	private boolean restart = false;



	/*
	 * The record parser instance to use, obtained in initialize.
	 */
	private RecordBytesParser<T> recordParser = null;
	
	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#getRecordParser()
	 */
	@Override
	public RecordBytesParser<T> getRecordParser() {
		return this.recordParser;
	}
	
	public ZFile getZFile() {
		return this.zFile;
	}
	
	public String getDataSetName() {
		return this.dsname;
	}	
	
	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#initialize(java.util.Properties)
	 */
	@Override
	public void initialize(Properties props) {
		this.initialize(props,false);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#initialize(java.util.Properties, boolean)
	 */
	@Override
	public void initialize(Properties props, boolean restart) {
		this.restart = restart;
		

		
		String dspath = props.getProperty( DSPATH );		
		if ( dspath != null ) {
			this.dsname = dspath;
		} else {
			this.dsname = props.getProperty( DSNAME );
			this.dsname = ZFile.getSlashSlashQuotedDSN(this.dsname,true);
		}

		// DS Parameters used by calls to "fopen" through the ZFile.open() method.
		ds_parameters = props.getProperty(
				DS_PARAMS, 
				ds_parameters);
		ds_parameters_restart = props.getProperty(
				DS_PARAMS_RESTART,
				ds_parameters_restart);


		// TSO commands used to manage the DD card for the dataset.
		tsoAlloc = props.getProperty( DD_ALLOC );
		tsoAllocRestart = props.getProperty( DD_ALLOC_RESTART );
		tsoFree = props.getProperty( DD_FREE );

		// DS Parameters used by calls to "fopen" through the ZFile.open() method.
		String dsparm = props.getProperty( DS_PARAMS );
		if ( dsparm == null ) {
			String mode = props.getProperty( DS_MODE );
			if ( mode == null ) {
				MagicSauceLogger.getInstance().warning( "DS_PARAMS and DS_MODE missing. Using default parameters: " + ds_parameters );
			} 
			else {
				ds_parameters = MVSUtility.buildDSParameters(mode, tsoAlloc);
			}
		}

		// ds_parameters_restart are used on restart scenarios instead of ds_parameters
		ds_parameters_restart = props.getProperty(
				DS_PARAMS_RESTART,
				ds_parameters_restart);


		String largeFileSupportStr = props.getProperty(LARGE_DATASET_SUPPORT);
		if (largeFileSupportStr != null && largeFileSupportStr.equalsIgnoreCase("true")) {
			useSeekAndTell = false;
		}

		int isNoSeek = ds_parameters.indexOf("noseek");
		if (isNoSeek != -1) {
			useSeekAndTell = false;
		}

		String recordParserClassName = props.getProperty(RECORD_PARSER_CLASSNAME);
		recordParser = ClassUtil.getInstanceForClass(recordParserClassName);
		recordParser.initialize(props);
	}

	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#flush()
	 */
	@Override
	public void flush() {
		try {
			if ( zFile != null )
				zFile.flush();
		} catch (ZFileException zfEx) {
			MVSUtility.logZFileExceptionDetails(zfEx, dsname);
			throw new BatchException("Unexpected error while trying to flush", zfEx.getCause());
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#getCurrentPosition()
	 */
	@Override
	public long getCurrentPosition() {
		try {
			flush();
			if (useSeekAndTell)
				return zFile.tell();
			else {
				return zFile.getRecordCount();
			}
		} catch (ZFileException zfe) {
			MVSUtility.logZFileExceptionDetails(zfe, dsname);
			throw new BatchException(
					"Failed to retrieve the current position of the zFile.",
					zfe);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#setPosition(long)
	 */
	@Override
	public void setPosition(long position) {
		try {
			if (useSeekAndTell) {
				zFile.seek(position, 0);
			} else {
				byte[] buf = new byte[zFile.getLrecl()];
				for (int i = 0; i < position; i++) {
					zFile.read(buf);
				}
				flush();
			}
		} catch (ZFileException zfe) {
			throw new BatchException("Failed to set position of zFile", zfe);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#open()
	 */
	@Override
	public void open() {
		boolean successAlloc = false;
		BatchException rethrow = null;
		
		try {
			String allocate = (restart) ? tsoAllocRestart : tsoAlloc;
			String dsparams = (restart) ? ds_parameters_restart : ds_parameters;
			
			if ( allocate != null && !allocate.trim().isEmpty() ) {
				MagicSauceLogger.getInstance().info( "Executing BPXWDYN command: " + allocate );

				ZFile.bpxwdyn(allocate);
				successAlloc = true;

				if (dsparams.trim().startsWith("w")) {
					MagicSauceLogger.getInstance().warning("Opening in w mode may cause ZFile contructor to reallocate the DS.");
				}
			}
			
			MagicSauceLogger.getInstance().info( "Opening dataset " + dsname + " with dsparams: " + dsparams );
			zFile = new ZFile( dsname, dsparams );
		} catch (ZFileException zfe) {
			MVSUtility.logZFileExceptionDetails(zfe, dsname);			
			rethrow = new BatchException("Unexpected error while opening DS", zfe.getCause());
		} catch (RcException rce) {
			MagicSauceLogger.getInstance().severe("RcException: " + rce.getMessage() + " RC#: " + rce.getRc() );			
			rethrow = new BatchException("Unexpected error while opening DS", rce.getCause());
		} finally {
			if (successAlloc) {
				freeDDCard();
				
				if ( rethrow != null ) 
					throw rethrow;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.doctorbatch.framework.mvs.DataSetManager#close()
	 */
	@Override
	public void close() {
		try {
			zFile.close();
		} catch (ZFileException zfEx) {
			MVSUtility.logZFileExceptionDetails(zfEx, dsname);
			throw new BatchException("Unexpected error while trying to close", zfEx.getCause());
		} finally {
			freeDDCard();
		}

	}

	public void freeDDCard() {
		if (!restart && tsoFree != null && tsoFree.trim().length() > 0) {
			try {
				ZFile.bpxwdyn( tsoFree );
			} catch ( RcException rce ) {
				MagicSauceLogger.getInstance().exception("Could not execute the \"DD_FREE\" command from xJCL: \""+tsoFree+"\": " + rce.getMessage() + " with RC: " + rce.getRc(), rce );
			}
		}
	}




}
