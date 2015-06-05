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

package com.ibm.doctorbatch.magicsauce.websphere;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import com.ibm.batch.api.BatchConstants;
import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.SkipRecordException;

import static com.ibm.doctorbatch.magicsauce.websphere.Constants.*;

/**
 * <p>SkipRecordJobStep is a BatchJobStepInterface that allows Observer objects to register for
 * notification of skipped records during the batch process loop. Implementations of this job
 * step class must implement "doUnitOfWork()", rather than overriding processJobStep. The 
 * contract for doUnitOfWork is the same - perform your batch job step work as you would 
 * directly in processJobStep, and return the same BatchConstansts values.</p>
 *
 * <p>To skip a record, you should throw a new SkippedRecordException out of your doUnitOfWork
 * implementation (therefore, it can be thrown from your input- or output- BDS, or during
 * the processing of the record).</p>
 * 
 * <p>On the job step in xJCL, you register observers by specifying the property/properties: 
 * 	<ul><li>SKIP_RECORD_OBSERVER; or</li>
 *      <li>SKIP_RECORD_OBSERVER.1, SKIP_RECORD_OBSERVER.2, ..., SKIP_RECORD_OBSERVER.N</li>
 *  </ul></p>
 *  
 * <p>Use SKIP_RECORD_OBSERVER to register one observer. If you need multiple observers, use the 
 * SKIP_RECORD_OBSERVER.1, SKIP_RECORD_OBSERVER.2, ..., SKIP_RECORD_OBSERVER.N properties to register N observers, 
 * where the numbering starts at one, and counts sequentially.</p>
 *  
 * <p>The value of the SKIP_RECORD_OBSERVER* properties is the fully qualified class name of a class that implements 
 * java.util.Observer. The observer's "update" method will receive the record that wrapped in the SkippedRecordException
 * as its argument, as well as a reference to this SkipRecordJobStep.</p>
 * 
 * <p>Use the MAX_SKIP_RECORDS property to set a limit to how many skip record exceptions
 * are tolerable during the job step. If not set, it defaults to -1, indicating no limit. 
 * If the limit is set, and reached, the SkipRecordException will be wrapped in a BatchException, 
 * and rethrown out of processJobStep to terminate the job and place it in a restartable state.</p>
 * 
 * <p>Here is an example xJCL snippet showing this job step (note: the bds sections
 * are templated out, and classes referenced under com.customer.* are user-provided)</p>
 * <pre>{@code
 *   <job-step name="jobStep1">
 *    <classname>com.customer.batch.CustomResilientJobStep</classname>
 *    <checkpoint-algorithm-ref name="recordbased"/>
 *    <bds> ... </bds>
 *    <props>
 *      <prop name="MAX_SKIP_RECORDS" value="100"/>
 *      <prop name="SKIP_RECORD_OBSERVER.1" value="com.customer.batch.SkipRecordDataLogger"/>
 *      <prop name="SKIP_RECORD_OBSERVER.2" value="com.customer.batch.SkipRecordEMailAlert"/>
 *    </props>
 *    <results-ref name="maxRC"/>
 *   </job-step>
 * }</pre>
 *  
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @version 1.0
 */
public abstract class AbstractSkipRecordJobStep extends AbstractJobStep {

	/*
	 * NOTE: This class has an UNDOCUMENTED "tolerable exception" feature, which
	 * allows you to list "tolerable exceptions" in the job step properties using 
	 * SKIP_EXCEPTION.1, SKIP_EXCEPTION.2, ..., SKIP_EXCEPTION.N properties, where
	 * the value is a fully qualified exception class name.
	 * 
	 * When these exceptions occurs, a warning will be logged and processing will 
	 * continue, unless MAX_EXCEPTIONS has been reached; in which case the exception
	 * will be wrapped in a BatchException object and rethrown to terminate processing.
	 * 
	 * This capability may be refactored out of this class at a later time, and 
	 * placed elsewhere in the hierarchy. Until then, it will remain undocumented.
	 */
	
	private Integer maxTolerableExceptions;
	private Integer maxSkippedRecords;

	private int currentTolerableExceptions;
	private int currentSkippedRecords;

	private final static Logger LOG = Logger.getLogger( AbstractSkipRecordJobStep.class.getName() );

	/**
	 * The list of tolerable exception class types.  
	 */
	private List<Class<?>> exceptionClasses = new ArrayList<Class<?>>();
	
	/**
	 * createJobStep registers the observers defined in the SKIP_RECORD* job step properties
	 * from the xJCL with this batch job step instance. 
	 * 
	 * When overriding createJobStep, you must call super.createJobStep() to ensure the
	 * observers are registered with the job step instance.
	 */
	@Override
	public void createJobStep() {
		super.createJobStep();
		
		currentSkippedRecords = 0;
		currentTolerableExceptions = 0;
		maxSkippedRecords = Integer.valueOf(getProperty(MAX_SKIP_RECORDS, "-1"));
		maxTolerableExceptions = Integer.valueOf(getProperty(MAX_EXCEPTIONS, "-1"));

		String observerClassName = getProperty(SKIP_RECORD_OBSERVER, "");
		if ( !observerClassName.isEmpty() )
		{
			Observer observer = ClassUtil.getInstanceForClass(observerClassName);
			this.addObserver( observer );
			LOG.info( getJobStepId() + ": Registered skip observer instance of " + observerClassName );
			
		} else {
			// Either there's no SKIP_RECORD_OBSERVER defined, or there are several, with 
			// suffixes .1, .2, .3, ..., .N, for N observers.
			
			int i = 1;
			Observer observer = null;
			while ( true ) {
				observerClassName = getProperty(SKIP_RECORD_OBSERVER + "."+ i++, "");
				if ( !observerClassName.isEmpty() )
				{
					observer = ClassUtil.getInstanceForClass(observerClassName);
					this.addObserver( observer );
					LOG.info( getJobStepId() + ": Registered skip observer instance of " + observerClassName );
				} 
				else {
					break;
				}
			}
		}
		
		registerSkipMetricsObserver();
		registerListedExceptions();
	}

	private void registerSkipMetricsObserver() {
		this.addObserver( new Observer() {
			/**
			 * UDPATE SKIP RECORD METRICS HERE!!!
			 */
			public void update(Observable observable, Object data) {
				

			}
		});
	}

	// TODO need to explore if this is necessary
	private void registerListedExceptions() {
		String exceptionClassName = getProperty(SKIP_EXCEPTION, "");
		if ( !exceptionClassName.isEmpty() )
		{
			try {
				Object o = Class.forName( exceptionClassName );
				this.exceptionClasses.add( o.getClass() );
				
				LOG.info( getJobStepId() +": Will notify registered skip observers on occurance of exception " + exceptionClassName );
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			// Either there's no SKIP_RECORD_OBSERVER defined, or there are several, with 
			// suffixes .1, .2, .3, ..., .N, for N observers.
			
			int i = 1;
			while ( true ) {
				exceptionClassName = getProperty(SKIP_EXCEPTION+"."+i, "");
				if ( !exceptionClassName.isEmpty() )
				{
					try {
						Object o = Class.forName( exceptionClassName );
						this.exceptionClasses.add( o.getClass() );
						LOG.info( getJobStepId()+": Will notify registered skip observers on occurance of exception " + exceptionClassName );
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				} 
				else {
					break;
				}
			}
		}
	}

	/**
	 * Performs a unit of work for the batch step process. This method is called from 
	 * processJobStep. 
	 * 
	 * @return The job step return code: BatchConstants.STEP_COMPLETE, BatchConstants.STEP_CONTINUE, etc. The return value is propagated through processJobStep and returned to the container.
	 */
	protected abstract int doUnitOfWork();

	/**
	 * processJobStep invokes doUnitOfWork. If a BatchSkippedRecordException is thrown out of
	 * doUnitOfWork, the registered observers are notified of the skipped record. The return 
	 * value from doUnitOfWork is propagated through processJobStep and returned to the 
	 * container.
	 * 
	 * If the MAX_SKIP_RECORDS job step property is defined, its value determines the maximum
	 * number of tolerable skipped records. Once that limit has been reached, an occurrence
	 * of NHBatchSkippedRecordException will be rethrown out of this method to the container, after 
	 * the registered observers are notified, causing the job to stop processing and end in 
	 * a RESTARTABLE state.
	 */
	@Override
	public int processJobStep() {
		int result = BatchConstants.STEP_CONTINUE;
		
		try {
			result = doUnitOfWork();		
		} 
		catch ( SkipRecordException sre ) {
			setChanged();
			notifyObservers( sre );
			
			if ( maxSkippedRecords != -1 && ++currentSkippedRecords >= maxSkippedRecords ) {
				LOG.warning( getJobStepId() + ": terminating job because maxSkippedRecords was reached." );
				throw new BatchException(sre.getMessage(), sre);
			}			
		}
		catch ( Throwable t ){
			boolean isRegisteredExceptionType = false;
			// Can I cast this "t" object to any of the registered exception types?
			for ( Class<?> registeredExceptionType : this.exceptionClasses ) {
				try {
					registeredExceptionType.cast(t);					
					isRegisteredExceptionType = true;
					break;
				} catch ( ClassCastException cce ) {
					// nope.
				}
			}
			
			if ( isRegisteredExceptionType ) {
				LOG.warning( "Registered tolerable exception of type '"+t.getClass().getName()+"' occurred, with message: " + t.getCause() );
				if ( maxTolerableExceptions != -1 ) {
					if ( ++currentTolerableExceptions >= maxTolerableExceptions ) {
						LOG.warning( getJobStepId() + ": terminating job because maxTolerableExceptions ("+maxTolerableExceptions+") was reached." );
						throw new BatchException(t.getMessage(), t);
					} else {
						LOG.warning( currentTolerableExceptions + " tolerable exceptions have occurred out of " + maxTolerableExceptions + " maximum allowed." );
					}	
				} 
			}
		}

		return result;
	}
}
