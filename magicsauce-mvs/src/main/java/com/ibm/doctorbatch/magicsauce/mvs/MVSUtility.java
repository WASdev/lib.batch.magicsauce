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

import com.ibm.doctorbatch.magicsauce.MagicSauceLogger;
import com.ibm.jzos.ZFileException;

public class MVSUtility {
	private static ThreadLocal<StringBuffer> buffer = new ThreadLocal<StringBuffer>() {
		@Override
		public StringBuffer initialValue() { return new StringBuffer(); }
	};

	private static ThreadLocal<StringBuilder> builder = new ThreadLocal<StringBuilder>() {
		@Override
		public StringBuilder initialValue() { return new StringBuilder(); }
	};

		
	public static void logZFileExceptionDetails(Throwable throwableEx, String dsname) {
		Throwable nested = throwableEx;
		while (nested != null && nested.getClass() != ZFileException.class) {
			nested = nested.getCause();
		}

		if (nested != null) {
			ZFileException zfe = (ZFileException) nested;
			MagicSauceLogger.getInstance().severe("ZFileException occurred writing record to DSN: "
					+ dsname);
			MagicSauceLogger.getInstance().severe(zfe.getMessage() + "(" + zfe.getErrnoMsg() + ")");

			if (zfe.getErrno() != 0) {
				MagicSauceLogger.getInstance().severe("Non-zero errorno: " + zfe.getErrno());
			}

			if (zfe.getAbendCode() != 0) {
				MagicSauceLogger.getInstance().severe("IO Abend Code: " + zfe.getAbendCode());
				MagicSauceLogger.getInstance().severe("IO Abend RC: " + zfe.getAbendRc());
			}
			
			if(zfe.getFeedbackFtncd() != 0) {
				MagicSauceLogger.getInstance().severe("IO Feedback Ftncd: " + zfe.getFeedbackFtncd());
				MagicSauceLogger.getInstance().severe("IO Feedback: " + zfe.getFeedbackFdbk());
			}
			
			if(zfe.getAllocSvc99Error() != 0) {
				MagicSauceLogger.getInstance().severe("AllocSvc99Error: " + zfe.getAllocSvc99Error());
				MagicSauceLogger.getInstance().severe("AllocSvc99Info:" + zfe.getAllocSvc99Info());
			}
			
			if(zfe.getErrorCode() != 0) {
				MagicSauceLogger.getInstance().severe("ErrorCode: " + zfe.getErrorCode());
			}
		}
	}
	
	/**
	 * Builds the ZOpen dataset parameter string from a TSO Allocate statement
	 * 
	 * @param alloc
	 * @return
	 */
	public static String buildDSParameters( String mode, String alloc ) {
		StringBuffer buff = MVSUtility.buffer.get();
		StringBuilder bldr = MVSUtility.builder.get();
		
		buff.setLength(0);
		bldr.setLength(0);
				
		if ( alloc.trim().startsWith("alloc") ) {
			int index;
			buff = new StringBuffer( alloc );
			bldr = new StringBuilder();

			index = buff.indexOf("recfm(") + 3;
			String recfm = buff.substring(index+3, buff.indexOf(")",index));
			recfm = recfm.replaceAll(",", "");

			index = buff.indexOf("lrecl(") + 3;
			String lrecl = buff.substring(index+3, buff.indexOf(")",index));
			
			bldr.append( mode );
			bldr.append(",type=record,recfm=");
			bldr.append( recfm );
			bldr.append( ",lrecl=" );
			bldr.append( lrecl );
			
			return bldr.toString();
		} else {
			System.out.println( "Command is not an allocate statement..." );
			return null;
		}
	}
}
