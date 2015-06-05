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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemWriter;
import javax.inject.Inject;

import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.mvs.MVSDataSetManager;
import com.ibm.doctorbatch.magicsauce.mvs.MVSRecordWriter;
import com.ibm.etools.marshall.RecordBytes;

/**

 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @param <T>
 */
public class MVSRecordItemWriter<T extends RecordBytes> extends AbstractMVSBatchArtifact<T> implements ItemWriter {
	private MVSRecordWriter<T> magicSauceWriter;
		
	private List<? extends T> itemList = new ArrayList<T>();
		
	@Override
	public void open(Serializable state)  {
		magicSauceWriter = new MVSRecordWriter<T>();
		magicSauceWriter.setDataSetManager((MVSDataSetManager<T>) initializeMVSDataSetManager());
		
		magicSauceWriter.open( state );
	}
	
	@Override
	public void close() throws Exception {
		magicSauceWriter.close();
	}

	@Override
	public void writeItems(List<Object> records) throws Exception {
		itemList.clear();
		Collections.copy(records, itemList);
		
		magicSauceWriter.write( itemList );
	}
}
