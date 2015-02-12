package alg.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.*;


public class SVMTest extends Common{
	private static String svmDir = "FSVM/";
	private static String pcSvmDir = "PC-FSVM/";
	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}
	
	private svm_problem read_problem(svm_parameter svmParm, String dataPath) throws IOException
	{
		BufferedReader fp = new BufferedReader(new FileReader(dataPath));
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		svm_problem prob = null;
		int max_index = 0;

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(svmParm.gamma == 0 && max_index > 0)
			svmParm.gamma = 1.0/max_index;

		if(svmParm.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					prob = null;
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					prob = null;
				}
			}

		fp.close();
		return prob;
	}

	

	public void learning(String modelPath,String dataPath,svm_parameter svmParm) {
		try {
			String dst = svmDir+modelPath;
			svm_problem svmProb = read_problem(svmParm,pcSvmDir+dataPath);
			svm_model model = svm.svm_train(svmProb, svmParm);
			
			File f = new File(dst);
			if ( f.exists() ) f.delete();
			svm.svm_save_model(svmDir+modelPath, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double testing(String modelPath,String dataPath) {
		int correct = 0, total = 0;
		double accuracy = -1;
		try {
			svm_parameter svmParm = initParmeter(80,80);
			svm_problem svmProb = read_problem(svmParm,pcSvmDir+dataPath);
			svm_model model = svm.svm_load_model(svmDir+modelPath);
			
			for(int i=0;i<svmProb.l;i++){
				double v;
				svm_node[] x = svmProb.x[i];
				
				v = svm.svm_predict(model, x);
			
				System.out.println("Input["+x[0].value + ","
										   +x[1].value + ","
										   +x[2].value + ","
										   +x[3].value + "];"
										   +"Output["  + (int)v + "]");
										   
						
				
				total++;
				if(v == svmProb.y[i]) correct++;
				
			}
			
			accuracy = (double)correct/total*100;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return accuracy;
	}

	public svm_parameter initParmeter(int gamma,int cost) {
		svm_parameter svmParm = new svm_parameter();

		svmParm.svm_type = svm_parameter.C_SVC;
		svmParm.kernel_type = svm_parameter.RBF;
		svmParm.degree = 3;
		svmParm.gamma = gamma; 
		svmParm.coef0 = 0;
		svmParm.nu = 0.5;
		svmParm.cache_size = 100;
		svmParm.C = cost;
		svmParm.eps = 1e-3;
		svmParm.p = 0.1;
		svmParm.shrinking = 1;
		svmParm.probability = 0;
		svmParm.nr_weight = 0;
		svmParm.weight_label = new int[0];
		svmParm.weight = new double[0];
		return svmParm;
	}

	public static void main(String[] args) {
		SVMTest svmTest = new SVMTest();
		double[] accuracy = {-1,-1,-1,-1};
		
		int seq;
		for(int i=0; i<4; i++) {
			seq = i;
			//long StartTime = System.currentTimeMillis(); // 取出目前時間
			svmTest.learning("SVM0"+seq+".model","SVMtraining0"+seq+".txt"
											,svmTest.initParmeter(1,1));
			//long ProcessTime = System.currentTimeMillis() - StartTime; // 計算處理時間
			//System.out.println("Process Time: " + ProcessTime);
			accuracy[i] = svmTest.testing("SVM0"+seq+".model","SVMtesting0"+seq+".txt");
			
		}
		
		//accuracy[0] = svmTest.testing("SVM01.model","test.txt");
		//System.out.println(accuracy[0]);
		
		for (int i=0; i<accuracy.length; i++)
			System.out.println(accuracy[i]);
			
	}
}
