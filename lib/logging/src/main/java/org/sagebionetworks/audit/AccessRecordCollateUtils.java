package org.sagebionetworks.audit;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.audit.AccessRecord;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Utilities for collating and splitting AccessRecord CSV files.
 * @author John
 *
 */
public class AccessRecordCollateUtils {
	/**
	 * Given a list of CSV files, each sorted on timeStamp, create an output
	 * collated file. The resulting file will contain all row from all of the
	 * input files and will be sorted by timeStamp.
	 * 
	 * Note: If the input file are not already sorted the resulting output file
	 * will be meaningless.
	 * 
	 * This method collates the files by comparing one row at a time from each
	 * file. Only one row from each file is ever loaded into memory. Therefore,
	 * file size does not impact on the memory requirements of this algorithm.
	 * 
	 * @param one
	 *            - This file must already be sorted on timestamp.
	 * @param two
	 * @param out
	 */
	public static void collateSortedCSVFiles(List<Reader> input, Writer out) {
		// Setup the output CSV
		CSVWriter outCSV = new CSVWriter(out);
		// write the default header as the first row.
		outCSV.writeNext(AccessRecordCSVUtils.DEFAULT_HEADERS);
		// Get an iterator from each file.
		List<Iterator<AccessRecord>> iterators = new ArrayList<Iterator<AccessRecord>>();
		for(Reader reader: input){
			iterators.add(AccessRecordCSVUtils.readFromCSV(reader));
		}
		// This will contain the current time stamps
		AccessRecord[] current = new AccessRecord[iterators.size()];
		// Prime the pump
		for(int i=0; i<current.length; i++){
			Iterator<AccessRecord> it = iterators.get(i);
			if(it.hasNext()){
				current[i] = it.next();
			}else{
				current[i] = null;
			}
		}
		// Keep finding the minimum until there is no more data in any of the input streams.
		while(!isEmpty(current)){
			// Find the minimum value
			int minimumIndex = 0;
			for(int i=0; i<current.length; i++){
				if(i == minimumIndex) continue;
				if(current[i] == null) continue;
				if(current[i].getTimestamp().compareTo(current[minimumIndex].getTimestamp()) < 0 ){
					minimumIndex = i;
				}
			}
			// write the the minimum to out
			AccessRecordCSVUtils.appendToCSV(current[minimumIndex], outCSV, AccessRecordCSVUtils.DEFAULT_HEADERS);
			// Get the next 
			Iterator<AccessRecord> minIt = iterators.get(minimumIndex);
			if(minIt.hasNext()){
				current[minimumIndex] = minIt.next();
			}else{
				current[minimumIndex] = null;
			}
		}
	}
	
	/**
	 * Simple helper to determine if an array is empty.
	 * @param array
	 * @return
	 */
	public static boolean isEmpty(AccessRecord[] array){
		for(int i=0; i<array.length; i++){
			if(array[i] != null) return false;
		}
		return true;
	}
	

	/**
	 * This Comparator compares AccessRecord based on the time stamp.
	 * 
	 * @author jmhill
	 * 
	 */
	public static class AccessRecordComparator implements
			Comparator<AccessRecord> {
		@Override
		public int compare(AccessRecord one, AccessRecord two) {
			if (one == null)
				throw new IllegalArgumentException("One cannot be null");
			if (one.getTimestamp() == null)
				throw new IllegalArgumentException(
						"One.timestamp cannot be null");
			if (two == null)
				throw new IllegalArgumentException("Two cannot be null");
			if (two.getTimestamp() == null)
				throw new IllegalArgumentException(
						"Two.timestamp cannot be null");
			return one.getTimestamp().compareTo(two.getTimestamp());
		}
	}

	/**
	 * Sort the list of AccessRecord based on timestamp
	 * @param toSort
	 */
	public static void sortByTimestamp(List<AccessRecord> toSort){
		Collections.sort(toSort, new AccessRecordComparator());
	}

}
