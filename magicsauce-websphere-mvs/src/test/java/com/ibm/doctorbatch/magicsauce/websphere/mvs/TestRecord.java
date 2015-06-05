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

import com.ibm.etools.marshall.RecordBytes;
import com.ibm.etools.marshall.util.MarshallIntegerUtils;
import com.ibm.etools.marshall.util.MarshallStringUtils;

/*
 * Sample record based off of:
 *    01 TestRecord
 *       05 ID    PIC S9(4)
 *       05 VALUE PIC X(48)
 */
public class TestRecord implements RecordBytes {
	private byte[] buffer;
	public static int LRECL=52;
	
	public TestRecord() {
		
	}
	
	TestRecord( int id, String value ) {
		buffer = new byte[52];
		
		setId(id);
		setValue(value);
	}
	
	public int getId() {
		return MarshallIntegerUtils.unmarshallFourByteIntegerFromBuffer(
				buffer, 
				0, 
				true, 
				MarshallIntegerUtils.SIGN_CODING_TWOS_COMPLEMENT );
	}
	
	public void setId( int id ) {
		MarshallIntegerUtils.marshallFourByteIntegerIntoBuffer(id, buffer, 0, true, MarshallIntegerUtils.SIGN_CODING_TWOS_COMPLEMENT);
	}
	
	public String getValue() {
		return MarshallStringUtils.unmarshallFixedLengthStringFromBuffer(buffer, 4, "IBM-037", 48);
	}
	
	public void setValue( String value ) {
		MarshallStringUtils.marshallFixedLengthStringIntoBuffer(value, buffer, 4, "IBM-037", 48, MarshallStringUtils.STRING_JUSTIFICATION_LEFT, " ");
	}
	
	@Override
	public byte[] getBytes() {
		return buffer;
	}

	@Override
	public int getSize() {
		return buffer.length;
	}

	@Override
	public void setBytes(byte[] arg0) {
		this.buffer = arg0;
	}
	
}