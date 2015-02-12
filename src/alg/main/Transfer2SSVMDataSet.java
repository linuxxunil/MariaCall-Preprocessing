package alg.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import alg.database.DatabaseDriver;
import alg.database.DatabaseTable;
import alg.database.SqliteDriver;

public class Transfer2SSVMDataSet extends Common{
	private DatabaseDriver srcdb = null;
	private DatabaseDriver dstdb = null;

	public Transfer2SSVMDataSet() {
	}

	private void insertTesting(DatabaseDriver db, int id, String mac,
			String rssi, String distance, int mode1, int mode2) {
		insertTraining(db, id, mac, rssi, distance, mode1, mode2);
	}

	private void insertTraining(DatabaseDriver db, int id, String mac, String rssi, String distance,
			int mode1, int mode2) {
		String s = "INSERT INTO " + DatabaseTable.Stair.name + "(\""
				+ DatabaseTable.Stair.colDeviceID + "\"," + "	\""
				+ DatabaseTable.Stair.colDeviceMAC + "\"," + " \""
				+ DatabaseTable.Stair.colRSSI + "\"," + " \""
				+ DatabaseTable.Stair.colDistance + "\"," + " \""
				+ DatabaseTable.Stair.colWinAvg + "\"," + " \""
				+ DatabaseTable.Stair.colKalman + "\")" + " VALUES " + "(\""
				+ id + "\"," + "	\"" + mac + "\"," + " \"" + rssi + "\","
				+ " \"" + 0 + "\"," + " \"" + mode1 + "\"," + " \"" + mode2
				+ "\")";
		System.out.println(s);
		db.inset(s);
	}

	private void insertRecalling(DatabaseDriver db, int id,int predict, String mac, String rssi,
			int mode1, int mode2) {
		String s = "INSERT INTO " + DatabaseTable.SvmTesting.name + "(\""
				+ DatabaseTable.SvmTesting.colDeviceID + "\"," + "	\""
				+ DatabaseTable.SvmTesting.colPredict + "\"," + " \""
				+ DatabaseTable.SvmTesting.colDeviceMAC + "\"," + " \""
				+ DatabaseTable.SvmTesting.colRSSI + "\"," + " \""
				+ DatabaseTable.SvmTesting.colWinAvg + "\"," + " \""
				+ DatabaseTable.SvmTesting.colKalman + "\")" + " VALUES " + "(\""
				+ id + "\",\"" +predict+ "\",\"" + mac + "\"," + " \"" + rssi + "\",\""
				+ mode1 + "\"," + " \"" + mode2
				+ "\")";
		System.out.println(s);
		db.inset(s);
	}
	
	private void deleteTrainingGarbageRows(int id, String mac, int winAvg,
			int kalman, int limit) {
		String sql = String
				.format("DELETE FROM %s WHERE rowid in "
						+ "(SELECT rowid FROM Stair WHERE id=\'%d\' AND device=\'%s\' "
						+ "AND winAvg=\'%d\' AND kalman=\'%d\' LIMIT %d)",
						DatabaseTable.Stair.name,
						id,mac, winAvg, kalman, limit);
		srcdb.delete(sql);
	}

	private void deleteTestingGarbageRows(int id,int winAvg,
			int kalman, int keepLastCount) {
		int rowCount;
		try {
			String sql = String.format("SELECT COUNT(*) FROM %s WHERE id=\'%d\' "
					+ "AND winAvg=\'%d\' AND kalman=\'%d\'",
					DatabaseTable.SvmTesting.name,
					id, winAvg, kalman);

			ResultSet rs = srcdb.select(sql);
			if (rs == null) {
				rowCount = 0;
			} else {
				rowCount = rs.getInt(1);
			}

			sql = String.format("DELETE FROM %s WHERE rowid in "
					+ "(SELECT rowid FROM %s WHERE id=\'%d\' "
					+ "AND winAvg=\'%d\' AND kalman=\'%d\' LIMIT %d)", 
					DatabaseTable.SvmTesting.name,
					DatabaseTable.SvmTesting.name,
					id,winAvg, kalman, rowCount - keepLastCount);
			srcdb.delete(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void processTraining(String src, int maxID, int garbageRowsCount,
			int trainingCount, int testingCount, String[] macSet) {
		int[] mode1 = { 0, 0, 1, 1 };
		int[] mode2 = { 0, 1, 0, 1 };

		try {
			Files.copy(new File(src + ".sqlite.orig").toPath(), new File("PC-SSVM/SVM"+src
					+ ".sqlite").toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		srcdb = new SqliteDriver("PC-SSVM/SVM" + src + ".sqlite");
		srcdb.onConnect();
		// 產生Filter後的Database
		ResultSet[][] rs = new ResultSet[maxID][macSet.length];
		for (int k = 0; k < 4; k++) { // handle 4 mode
										// (None/WinAvg/Kalman/Hybrid)
			// Delete Garbage Rows
			for (int id = 0; id < maxID; id++) { //
				for (int m = 0; m < macSet.length; m++) {
					deleteTrainingGarbageRows(id, macSet[m], mode1[k],
							mode2[k], garbageRowsCount);
				}
			}
		}

		// Process Traning Databas
		for (int k = 0; k < 4; k++) { // handle 4 mode
										// (None/WinAvg/Kalman/Hybrid

			dstdb = new SqliteDriver(String.format("PC-SSVM/SVMtraining%02d.sqlite", k));
			dstdb.onConnect();
			dstdb.createTable(DatabaseTable.Stair.create());

			for (int id = 0; id < maxID; id++) { //
				for (int m = 0; m < macSet.length; m++) {
					String sql = String.format("SELECT * FROM Stair WHERE "
							+ "id=\'%d\' AND device=\'%s\' AND "
							+ "winAvg=\'%d\' AND Kalman=\'%d\'", id, macSet[m],
							mode1[k], mode2[k]);
					rs[id][m] = srcdb.select(sql);
				}
			}

			try {

				for (int i = 0; i < testingCount; i++) {
					for (int id = 0; id < maxID; id++) {
						for (int m = 0; m < macSet.length; m++) {
							if (rs[id][m].next()) {
								insertTraining(dstdb,id, macSet[m],
										rs[id][m].getString("rssi"), "0",
										mode1[k], mode2[k]);
							}

						}
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			dstdb.close();
		}

		// Process Testing Database
		for (int k = 0; k < 4; k++) { // handle 4 mode
										// (None/WinAvg/Kalman/Hybrid

			dstdb = new SqliteDriver(String.format("PC-SSVM/SVMtesting%02d.sqlite", k));
			dstdb.onConnect();
			dstdb.createTable(DatabaseTable.Stair.create());

			for (int id = 0; id < maxID; id++) { //
				for (int m = 0; m < macSet.length; m++) {
					String sql = String.format("SELECT * FROM Stair WHERE "
							+ "id=\'%d\' AND device=\'%s\' AND "
							+ "winAvg=\'%d\' AND Kalman=\'%d\'", id, macSet[m],
							mode1[k], mode2[k]);
					rs[id][m] = srcdb.select(sql);
				}
			}

			try {

				for (int i = 0; i < testingCount; i++) {
					for (int id = 0; id < maxID; id++) {
						for (int m = 0; m < macSet.length; m++) {
							if (rs[id][m].next()) {
								insertTraining(dstdb,id, macSet[m],
										rs[id][m].getString("rssi"), "0",
										mode1[k], mode2[k]);
							}

						}
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			dstdb.close();
		}
		srcdb.close();
		System.out.println("Finish");

	}

	private Kalman initKalman() {
		return new Kalman(kX, kP, kQ, kR);
	}

	private double exeKalman(Kalman kal, double rssi) {
		kal.correct(rssi);
		kal.predict();
		kal.update();
		return kal.getStateEstimation();
	}

	int maxWinAvg = 10;

	private double[] initWinAvg() {
		double[] winAvg = new double[10];
		for (int i = 0; i < winAvg.length; i++)
			winAvg[i] = -59;
		return winAvg;
	}

	private double exeWinAvg(double[] winAvg, double rssi, int exeCount) {
		double sum = 0;

		winAvg[exeCount % maxWinAvg] = rssi;

		for (int i = 0; i < maxWinAvg; i++) {
			sum += winAvg[i];
		}
		return (sum / (double) maxWinAvg);
	}

	public void processTraining2(String src, int maxID, int garbageRowsCount,
			int trainingCount, int testingCount, String[] macSet) {
		DatabaseDriver dstTrainingDb = null;
		DatabaseDriver dstTestingDb = null;
		int[] mode1 = { 0, 0, 1, 1 };
		int[] mode2 = { 0, 1, 0, 1 };

		try {
			Files.copy(new File(src + ".sqlite.orig").toPath(), new File("PC-SSVM/SVM"+src
					+ ".sqlite").toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		srcdb = new SqliteDriver("PC-SSVM/SVM" + src + ".sqlite");
		srcdb.onConnect();
		// 產生Filter後的Database
		ResultSet rs;
		double[][][][] rssi = new double[maxID][macSet.length][4][garbageRowsCount+trainingCount+testingCount]; // BeaconNmber/Mode/DataSet
		
		int i;
		for (int id = 0; id < maxID; id++) { // Area
			for (int m = 0; m < macSet.length; m++) { // Beacon
				String sql = String.format("SELECT * FROM Stair WHERE "
						+ "id=\'%d\' AND device=\'%s\' AND "
						+ "winAvg=\'%d\' AND Kalman=\'%d\'", id, macSet[m],
						0, 0);
				System.out.println(sql);
				rs = srcdb.select(sql);
				i = 0;
				try {
					
					Kalman kalman = initKalman();
					double[] winAvg = initWinAvg();
					double[] hyber = initWinAvg();
					
					while ( rs.next() ) { // dataset
						rssi[id][m][0][i] = rs.getInt("rssi");
						rssi[id][m][1][i] = exeKalman(kalman,rssi[id][m][0][i]);
						rssi[id][m][2][i] = exeWinAvg(winAvg,rssi[id][m][0][i],i);
						rssi[id][m][3][i] = exeWinAvg(winAvg,rssi[id][m][1][i],i);
						i++;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
					
				}
			}
		}
		
		for ( int mode=0; mode<4; mode++) {//Mode
			dstTrainingDb = new SqliteDriver(String.format("PC-SSVM/SVMtraining%02d.sqlite", mode));
			dstTrainingDb.onConnect();
			dstTrainingDb.createTable(DatabaseTable.Stair.create());

			dstTestingDb = new SqliteDriver(String.format("PC-SSVM/SVMtesting%02d.sqlite", mode));
			dstTestingDb.onConnect();
			dstTestingDb.createTable(DatabaseTable.Stair.create());
			
			
			
			for (int id=0; id<maxID; id++) {// Area
				for ( i=garbageRowsCount;i<garbageRowsCount+trainingCount+testingCount; i++) {
					for (int m = 0; m < macSet.length; m++) { // Beacon
						if ( i <garbageRowsCount+trainingCount) {
							insertTraining(dstTrainingDb, id, macSet[m], 
								String.valueOf(rssi[id][m][mode][i]),
								"0",mode1[mode], mode2[mode]);
						} else {
							insertTesting(dstTestingDb, id, macSet[m], 
								String.valueOf(rssi[id][m][mode][i]),
								"0",mode1[mode], mode2[mode]);
					}
				}
			}
		}
			
			dstTrainingDb.close();
			dstTestingDb.close();
		}
		

		srcdb.close();
		System.out.println("Finish");

	}

	public void calRecalling(String src,int maxID) {
		int total=0,correct=0;double accuracy=0;
		double total2=0;
		srcdb = new SqliteDriver("PC-SSVM/"+src+".sqlite");
		srcdb.onConnect();
		
		
		for (int id = 0; id < maxID; id++) { //
				String sql = String.format("SELECT * FROM %s WHERE id=%d", 
						DatabaseTable.SvmTesting.name,
						id);
				correct=total=0;
				try {
					ResultSet rs = srcdb.select(sql);
					while ( rs.next() ) {
						if ( 1 == rs.getInt("predict") ) {
							correct++;
						}
						total++;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				total2 = total2 +((double)correct/total*100);
				System.out.println("Accuracy["+id+"]="+
						((double)correct/total*100)+"% ("+correct+"/"+total+")");
				writeToTxt("PC-SSVM/"+src + ".txt", ((double)correct/total*100)+"%");
				
		}
		System.out.println("TotalAccuracy="+(total2/maxID));
		writeToTxt("PC-SSVM/"+src + ".txt", ((double)(total2/maxID))+"%");
		srcdb.close();
	}
	
	public void calRecallingErrorBlack(String src,int maxID) {
		Hashtable hash = new Hashtable();
		srcdb = new SqliteDriver("PC-SSVM/"+src+".sqlite");
		srcdb.onConnect();
		
		for (int id = 0; id < maxID; id++) { //
				String sql = String.format("SELECT * FROM %s WHERE id=%d AND predict!=%d", 
						DatabaseTable.SvmTesting.name,
						id,id);
				
				try {
					ResultSet rs = srcdb.select(sql);
					while ( rs.next() ) 
						hash.put(rs.getString("predict"), ""); 
				
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String line = "";
				Set set = hash.keySet();
				boolean isTrue;
				for (int i=0; i<maxID; i++){
					
					isTrue = set.contains(String.valueOf(i));
					if ( isTrue && line.equals(""))
						line += i ;
					else if ( isTrue )
						line += ";" + i;
					
				}
				
				System.out.println(line);
				hash.clear();
				//writeToTxt("PC-SSVM/"+src + ".txt", ((double)correct/total*100)+"%");
				
		}
		
		srcdb.close();
	}
	
	public void processRecalling(String src, int maxID, int keepLastCount) {
		int[] mode1 = { 0, 0, 1, 1 };	// winAvg
		int[] mode2 = { 0, 1, 0, 1 };	// kalman

		try {
			Files.copy(new File(src + ".sqlite.svm.recalling.orig").toPath(), new File("PC-SSVM/SVM"+src
					+ ".sqlite.recalling").toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		srcdb = new SqliteDriver("PC-SSVM/SVM"+src + ".sqlite.recalling");
		srcdb.onConnect();
		// 產生Filter後的Database
		ResultSet[] rs = new ResultSet[maxID];
		for (int k = 0; k < 4; k++) { // handle 4 mode
										// (None/WinAvg/Kalman/Hybrid)
			// Delete Garbage Rows
			for (int id = 0; id < maxID; id++) { //
				deleteTestingGarbageRows(id, mode1[k], mode2[k],keepLastCount);
			}
		}

		// Process Result Databas
		for (int k = 0; k < 4; k++) { // handle 4 mode
										// (None/WinAvg/Kalman/Hybrid
			dstdb = new SqliteDriver(String.format("PC-SSVM/SVMrecalling%02d.sqlite", k));
			dstdb.onConnect();
			dstdb.createTable(DatabaseTable.SvmTesting.create());
			for (int id = 0; id < maxID; id++) { //
				String sql = String.format("SELECT * FROM %s WHERE "
						+ "id=\'%d\' AND winAvg=\'%d\' AND Kalman=\'%d\' "
						+ "ORDER BY rowid DESC LIMIT %d", 
						DatabaseTable.SvmTesting.name,
						id, mode1[k],mode2[k],keepLastCount);
				rs[id] = srcdb.select(sql);
			}
			
			try {
				for ( int id=0; id<maxID; id++) {
					while ( rs[id].next() ) {

						insertRecalling(
dstdb,
id,
								rs[id].getInt(DatabaseTable.SvmTesting.colPredict),
								rs[id].getString(DatabaseTable.SvmTesting.colDeviceMAC),
								rs[id].getString(DatabaseTable.SvmTesting.colRSSI),
								rs[id].getInt(DatabaseTable.SvmTesting.colWinAvg),
								rs[id].getInt(DatabaseTable.SvmTesting.colKalman));
							
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		dstdb.close();
	}

	private void writeToTxt(String file, String line) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			bw.write(line);
			bw.newLine();
		} catch (FileNotFoundException e) {
		} catch (IOException e1) {
		} finally {
			try {
				bw.close();
				fw.close();
			} catch (IOException e) {
				// nothing
			}
		}
	}

	public double normalize(double y) {

		double dMax = 0.8f;
		double dMin = -0.8f;
		double yMax = 100.0f;
		double yMin = 40.0f;
		y = -y;
		if (y > yMax)
			y = yMax;
		else if (y < yMin)
			y = yMin;
		return (double) ((((y - yMin) * (dMax - dMin) / (yMax - yMin))) + dMin);
	}
	public void test(String rssiSet,int target) {
		String[] rssi = rssiSet.split(";");
		String line = String.valueOf(target);
		for (int i=1;i<=rssi.length; i++)
			line += " " + i + ":" + normalize(Double.valueOf(rssi[i-1])); 

		writeToTxt("PC-SSVM/test.txt", line);
		
	}
	public void transferToTxt(String src, int nOutput, String[] macSet) {
		int i = 0;
		int j = 0;
		double rssi;
		int areaID = 0;
		String line = "";
		String input = "";
		ResultSet rs = null;

		srcdb = new SqliteDriver("PC-SSVM/"+src + ".sqlite");
		srcdb.onConnect();

		rs = srcdb.select("SELECT * FROM "+DatabaseTable.Stair.name);
		int id;
		try {
			while (rs.next()) {
				rssi = rs.getDouble("rssi");
				id = rs.getInt("id");
				
				input += " " + ++j + ":" + normalize(rssi);
				if (++i % macSet.length == 0) {
					line = "+1" + input;
					writeToTxt("PC-SSVM/"+ src + "-" + id +".txt", line);
					input = "";
					j=0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		srcdb.close();
	}

	public static void main(String[] args) {
		
		Transfer2SSVMDataSet ps = new Transfer2SSVMDataSet();
		
		int areaNumber=2;
		/*
		ps.processTraining2("location-stair", areaNumber, 20, 30, 20, stairMacSet);
		for (int i = 0; i < 4; i++) {
			ps.transferToTxt(String.format("SVMtraining%02d", i), areaNumber, stairMacSet);
			ps.transferToTxt(String.format("SVMtesting%02d", i),7, stairMacSet);
		}*/
		
		
		//ps.processRecalling("location-stair", 2, 50);
		
		for(int i=0;i<3; i++) {
			ps.calRecalling(String.format("SVMrecalling%02d",i),2);
			System.out.println("================================");
		}
		
		//ps.calRecallingErrorBlack("SVMrecalling00",11);
		//ps.test("-79.74178278497199;-85.59072376520528;-78.33058403658464;-77.08809590357971", 8);

	}
}
