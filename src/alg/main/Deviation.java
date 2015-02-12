package alg.main;

import java.sql.ResultSet;
import java.util.LinkedList;

import alg.database.DatabaseDriver;
import alg.database.SqliteDriver;

/***********************************************************
 * Introduction to Computers and Programming (Fall 2009) * Professor Evan Korth
 * * * File Name: Deviation.java * PIN: K0002F09084 * Description: Calculating
 * Standard Deviation * * Focus: * a. Calculating Standard Deviation *
 ***********************************************************/

// Beginning of class Deviation
public class Deviation {
	/* Method for computing deviation of double values */
	// Beginning of double findDeviation(double[])
	public static double findDeviation(double[] nums) {
		double mean = findMean(nums);
		double squareSum = 0;

		for (int i = 0; i < nums.length; i++) {
			squareSum += Math.pow(nums[i] - mean, 2);
		}

		return Math.sqrt((squareSum) / (nums.length - 1));
	} // End of double findDeviation(double[])

	/* Method for computing deviation of int values */
	// Beginning of double findDeviation(int[])
	public static double findDeviation(int[] nums) {
		double mean = findMean(nums);
		double squareSum = 0;

		for (int i = 0; i < nums.length; i++) {
			squareSum += Math.pow(nums[i] - mean, 2);
		}

		return Math.sqrt((squareSum) / (nums.length - 1));
	} // End of double findDeviation(int[])

	/** Method for computing mean of an array of double values */
	// Beginning of double findMean(double[])
	public static double findMean(double[] nums) {
		double sum = 0;

		for (int i = 0; i < nums.length; i++) {
			sum += nums[i];
		}

		return sum / nums.length;
	} // End of double getMean(double[])

	/** Method for computing mean of an array of int values */
	// Beginning of double findMean(int[])
	public static double findMean(int[] nums) {
		double sum = 0;

		for (int i = 0; i < nums.length; i++) {
			sum += nums[i];
		}

		return sum / nums.length;
	} // End of double getMean(int[])

	/* Method for printing array */
	// Beginning of void printArray(double[])
	public static void printArray(double[] nums) {
		for (int i = 0; i < nums.length; i++) {
			System.out.print(nums[i] + " ");
		}

		System.out.println();
	}

	public static double[][] getDataSet(String path) {
		DatabaseDriver db = new SqliteDriver(String.format(path));
		db.onConnect();
		double[][] array = new double[5][50];
		String sql = "SELECT * FROM Ibeacon WHERE id=";
		ResultSet rs;
		
		for (int i = 11,j=0; i <= 15; i++,j++) {
			rs = db.select(sql + i);
			try {
				int k = 0;
				while ( rs.next() && k<50 ) {
					array[j][k] = rs.getDouble("rssi");
					k++;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return array;
	}
	// Beginning of method main
	public static void main(String[] args) {

		// Declare and create an array for 10 numbers
		
		double[][] array = getDataSet("location.updn.sqlite");
		double total = 0;
		for ( int i=0; i<5 ;i++ ) {
			total += findMean(array[i]);
			System.out.println(findMean(array[i]));
		}
		System.out.println(total/5 +"\n===================");
		
		total = 0;
		for ( int i=0; i<5 ;i++ ) {
			total += findDeviation(array[i]);
			System.out.println( findDeviation(array[i]));
		}
		System.out.println(total/5 +"\n===================");
		// Display mean and deviation
		

	} // End of main


}
