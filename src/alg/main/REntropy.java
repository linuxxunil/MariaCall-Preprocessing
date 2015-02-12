package alg.main;
import java.lang.Math;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import alg.database.DatabaseDriver;
import alg.database.SqliteDriver;
 
public class REntropy {
 
  @SuppressWarnings("boxing")
  public static double getShannonEntropy(String s) {
    int n = 0;
    Map<Character, Integer> occ = new HashMap<>();
 
    for (int c_ = 0; c_ < s.length(); ++c_) {
      char cx = s.charAt(c_);
      if (occ.containsKey(cx)) {
        occ.put(cx, occ.get(cx) + 1);
      } else {
        occ.put(cx, 1);
      }
      ++n;
    }
 
    double e = 0.0;
    for (Map.Entry<Character, Integer> entry : occ.entrySet()) {
      char cx = entry.getKey();
      double p = (double) entry.getValue() / n;
      e += p * log2(p);
    }
    return -e;
  }
 
  private static double log2(double a) {
    return Math.log(a) / Math.log(2);
  }

  private static String[] getDataSet(String path) {
	  DatabaseDriver db = new SqliteDriver(String.format(path));
	  	db.onConnect();
	  	Hashtable<Integer, String> hash;
		String[] table= {"a","b","c","d","e","f","g","h","i","j",
						 "k","l","m","n","o","p","q","r","s","t",
						 "u","v","w","x","y","z"};
	  	String sql = "SELECT * FROM Ibeacon WHERE id=";
	  	ResultSet rs;
	  	int rssi;
	  	String[] pattern = new String[50]; 
	  	int count,k=0;
	  	double sum=0,total=0,sum2=0;
	  	for ( int i=1; i<=5; i++ ) {
	  		rs = db.select(sql+i);
	  		try {
	  			hash = new Hashtable<Integer,String>();
	  			pattern[k] = new String("");
	  			count= 0;
	  			sum = total = 0.0;
	  			while ( rs.next() ) {
	  				rssi = rs.getInt("rssi");
	  				sum += rssi;
	  				if ( hash.get(rssi) == null ) {
	  					hash.put(rssi, table[count]);
	  					pattern[k] += table[count];
	  					count++;
	  				} else {
	  					pattern[k] += hash.get(rssi);
	  				}	
	  				total++;
	  			}
	  			k++;
	  			sum2 +=(sum/total);
	  			System.out.println("Avg["+k+"]="+(sum/total));
	  		} catch ( Exception e ) {
	  			e.printStackTrace();
	  		}		
	  	}
	  	System.out.println("TotalAvg="+(sum2/k));
	  	return pattern;
  }
  
/*
  public static void main(String[] args) {
	  String[] data = getDataSet("location.entropy00.sqlite");
	  double sum=0;
	  int k =0;
	    for (String ss : data) {
	      double entropy = REntropy.getShannonEntropy(ss);
	      sum += entropy;
	      System.out.printf("entropy of %40s: %.12f%n", "\"" + ss + "\"", entropy);
	      k++;
	    }
	    System.out.println(sum/k);
	    return;
	  } 
*/
 //  Example 
  public static void main(String[] args) {
    String[] sstr = {
      "1223334444",
      "13333", 
      "122333", 
      "1224444777",
      "aaBBcccDDDD",
      "1234567890abcdefghijklmnopqrstuvwxyz",
      "Rosetta Code",
    };
 
    for (String ss : sstr) {
      double entropy = REntropy.getShannonEntropy(ss);
      System.out.printf("Shannon entropy of %40s: %.12f%n", "\"" + ss + "\"", entropy);
    }
    return;
  } 
}