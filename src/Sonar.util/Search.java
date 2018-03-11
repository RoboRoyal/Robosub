package Sonar.util;

import java.util.Arrays;

public class Search {
	public static int[] left, right, top;
	static int boxes;
	static int size;
	static int detection;
	
	public static int tmp(double[] data, int freq){
		for(int index = 0; index < data.length-size; index++){
			double[] test = copySub(data,index,size);
			if(fft(test, boxes)[freq] > detection){
				if(index != 0)
					return index;
				return -1;
			}
		}
		return -1;
	}
	private static int[] fft(double[] test, int boxes2) {
		// TODO return fft of test with boxes2 number of partitions
		return null;
	}
	private static double[] copySub(double[] data, int index, int size2) {
		double[] tmp = new double[size2];
		int t = 0;
		for(int x = index; x<index+size;x++){
			tmp[t] = data[x];
		}
		return tmp;
	}
	public static void work(){
		
		
	}
	public static int[] getFreqs(){
		int[] freqs = new int[4];
		int[][] mods = SonarInterface.update(2000);
		Complex[] mid = new Complex[mods[1].length];
		mid = FFT.fft(mid);
		int place = 0;
		int x = 0;
		double max = 0;
		for(Complex i:mid){
			if(i.abs()>max){
				freqs[x] = place;
				x++;
				x=x%4;
				max = i.abs();
			}
			place++;
		} 
		return freqs;
	}
	public static int findBucket(){
		//update hydrophones
		int bucket = doWork(left, right, true, 256,-1);
		if(bucket < 0){
			System.out.println("No bucket found");
			return 0;
		}else{
			//System.out.println("Found bucket: "+bucket);
			return bucket;
		}
	}
	public static int findDir(int bucket){
		return doWork(left, right, false, 256, bucket);
	}
	private static int doWork(final int[] left_data,final int[] right_data,boolean do_search, int simple, int bucket){
		double[] right_fft = null;
		int max_position = 1;
		int max_value = -1;
		int left_time = -1, right_time = -1;
		boolean found_left = false, found_right = false;
		final int max_scan_size = 32000;
		for(int index = 1; index<max_scan_size-simple-2;index++){
			int[] left_tmp = Arrays.copyOfRange(left_data, index, index+simple);
			int[] right_tmp = Arrays.copyOfRange(right_data, index, index+simple);
			//double[] right_fft = fft(right_tmp);
			double[] left_fft = fft(left_tmp);
			if(!do_search){//TODO
				right_fft = fft(right_tmp);
			}else{
				for(int index2 = 0;index2<simple-1;index2++){
					if(left_fft[index2] > max_value){
						max_value = (int) left_fft[index2];
						max_position = index2;
					}
				}
			}
			if(!do_search && left_fft[bucket] > 20480*16 && !found_left){
				//System.out.println("ME : "+left_fft[bucket]+", index: "+index);
				left_time = index;
				found_left = true;
			}
			if(!do_search && right_fft[bucket] > 20480*16 && !found_right){
				//System.out.println("ME2 : "+right_fft[bucket]+", index: "+index);
				right_time = index;
				found_right = true;
			}
			
		}
		if(do_search){
			//if max_pos == -1, didnt find
			//System.out.println(max_value);
			return max_position;//found bucket
		}else{
			double diff = (1498*((double)left_time-(double)right_time))/100000;//100000
			//System.out.println("difference in timing: "+diff);
			double tmp = (int) ((180/Math.PI)*Math.atan(Math.pow(1-Math.pow(diff, 2), .5)/diff));
			//System.out.println(tmp);
			int dir = (int)tmp;
			return dir;
		}
	}
	private static double[] fft(int[] real) {
		double[] doubles = new double[real.length];
		for(int i=0; i<real.length; i++) {
		    doubles[i] = real[i];
		}
		double[] cmplx = new double[real.length];
		return FFT.transformRadix2(doubles, cmplx);
	}
	
	
	
	/*From one hydrophone
	 * Run through the data, 
	 * perform FFT on small segments
	 * search FFT for spikes
	 * -Found pinger freq
	 */
	
	/*Start with one hydrophone
	 * Run through the data, 
	 * perform FFT on small segments
	 * search FFT at pinger freqs for spike
	 * record time
	 * Look at next hydrophone
	 * Run through the data, 
	 * perform FFT on small segments
	 * search FFT at pinger freqs for spike
	 * record time
	 * Compare times
	 * Use math (diq function) to figure out direction baised on TOA
	 * -Found direction
	 */
	

}
