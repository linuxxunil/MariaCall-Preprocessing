package alg.database;

public class DatabaseTable {
	static public class Ibeacon {
		// table name define
		public static String name = "Ibeacon";
		
		public static final String colDeviceID 	= "id";
		public static final String colDeviceMAC = "device";
		public static final String colRSSI 		= "rssi";
		public static final String colDistance 	= "distance";
		public static final String colWinAvg	= "winAvg";
		public static final String colKalman 	= "kalman";

		// sql syntax : create
		public static String create() {
			return "CREATE TABLE IF NOT EXISTS " + name + "("
					+ colDeviceID	+ " nvarchar(3)	NOT NULL,"
					+ colDeviceMAC	+ " nvarchar(6)	NOT NULL,"
					+ colRSSI		+ " nvarchar(6)	NOT NULL,"
					+ colDistance 	+ " nvarchar(64) NULL   ,"
					+ colWinAvg 	+ " nvarchar(1) NULL    ,"
					+ colKalman 	+ " nvarchar(1) NULL    )";
		}
	}
	
	static public class Stair {
		// table name define
		public static String name = "Stair";
		
		public static final String colDeviceID 	= "id";
		public static final String colDeviceMAC = "device";
		public static final String colRSSI 		= "rssi";
		public static final String colDistance 	= "distance";
		public static final String colWinAvg	= "winAvg";
		public static final String colKalman 	= "kalman";

		// sql syntax : create
		public static String create() {
			return "CREATE TABLE IF NOT EXISTS " + name + "("
					+ colDeviceID	+ " nvarchar(3)	NOT NULL,"
					+ colDeviceMAC	+ " nvarchar(6)	NOT NULL,"
					+ colRSSI		+ " nvarchar(6)	NOT NULL,"
					+ colDistance 	+ " nvarchar(64) NULL   ,"
					+ colWinAvg 	+ " nvarchar(1) NULL    ,"
					+ colKalman 	+ " nvarchar(1) NULL    )";
		}
	}
	
	static public class AnnTesting {
		// table name define
		public static String name = "AnnTesting";
		
		public static final String colDeviceID 	= "id";
		public static final String colPredict	= "predict";
		public static final String colDeviceMAC = "device";
		public static final String colRSSI 		= "rssi";
		public static final String colWinAvg	= "winAvg";
		public static final String colKalman 	= "kalman";

		// sql syntax : create
		public static String create() {
			return "CREATE TABLE IF NOT EXISTS " + name + "("
					+ colDeviceID	+ " nvarchar(3)	NOT NULL,"
					+ colPredict	+ " nvarchar(3)	NOT NULL,"
					+ colDeviceMAC	+ " nvarchar(64)	NOT NULL,"
					+ colRSSI		+ " nvarchar(32)	NOT NULL,"
					+ colWinAvg 	+ " nvarchar(1) NULL    ,"
					+ colKalman 	+ " nvarchar(1) NULL    )";
		}
	}
	
	static public class SvmTesting {
		// table name define
		public static String name = "SvmTesting";
		
		public static final String colDeviceID 	= "id";
		public static final String colPredict	= "predict";
		public static final String colDeviceMAC = "device";
		public static final String colRSSI 		= "rssi";
		public static final String colWinAvg	= "winAvg";
		public static final String colKalman 	= "kalman";

		// sql syntax : create
		public static String create() {
			return "CREATE TABLE IF NOT EXISTS " + name + "("
					+ colDeviceID	+ " nvarchar(3)	NOT NULL,"
					+ colPredict	+ " nvarchar(3)	NOT NULL,"
					+ colDeviceMAC	+ " nvarchar(64)	NOT NULL,"
					+ colRSSI		+ " nvarchar(32)	NOT NULL,"
					+ colWinAvg 	+ " nvarchar(1) NULL    ,"
					+ colKalman 	+ " nvarchar(1) NULL    )";
		}
	}
}
