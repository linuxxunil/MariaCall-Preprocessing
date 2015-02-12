package alg.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.util.TransferFunctionType;

/**
 * This sample shows how to create, train, save and load simple Multi Layer
 * Perceptron
 */
public class NeuroTest extends Common{
	private static String nnDir = "PC-FANN/";
	public static void learning(String nnFile, String trainingFile,int nInput, int nHidden,int nOutput,double maxError) {
		DataSet trainingSet = 
				DataSet.createFromFile("PC-FANN/"+trainingFile, nInput, nOutput, ",", false);
		
		// create multi layer perceptron
		MultiLayerPerceptron ml = new MultiLayerPerceptron(
				TransferFunctionType.SIGMOID, nInput, nHidden, nOutput);
		
		final BackPropagation bp = new BackPropagation();
		bp.setMaxError(maxError);
		bp.setBatchMode(true);
		// learn the training set
		bp.addListener(new LearningEventListener(){

			@Override
			public void handleLearningEvent(LearningEvent event) {
				// TODO Auto-generated method stub
				System.out.println(bp.getTotalNetworkError());
				
			}
			
		});
		ml.learn(trainingSet,bp);

		ml.save(nnDir+nnFile);
	}

	
	public static void testing(String nnFile, String testingFile,int nInput,int nOutput) {
		DataSet testingSet = 
				DataSet.createFromFile("PC-FANN/"+testingFile, nInput, nOutput, ",", false);

		
		NeuralNetwork nn = NeuralNetwork.createFromFile(nnDir+nnFile);
	
		testNeuralNetwork(nn, testingSet);

	}
	
	public static void testNeuralNetwork(NeuralNetwork nnet, DataSet testSet) {
		int total = 1;double max = 0; int p =0;int correct=0;
		MeanSquaredError mse = new MeanSquaredError(testSet.getRows().size());
		double[] desiredOutput;
		double[] output;
		double[] error = new double[nnet.getOutputsCount()];
		mse.reset();
		for (DataSetRow dataRow : testSet.getRows()) {

			nnet.setInput(dataRow.getInput());
			nnet.calculate();

			desiredOutput = dataRow.getDesiredOutput();
			output = nnet.getOutput();
			
			
			System.out.print("Input: " + Arrays.toString(dataRow.getInput()));
			System.out.print(" Output: " + Arrays.toString(output));
			for(int i=0; i<error.length; i++) {
				error[i] = (float)output[i] - (float)desiredOutput[i];	
			}
			//System.out.println("Error: "+Arrays.toString(error));
			mse.addOutputError(error);
			
			// compute hit ratio
			for ( int i=0; i<desiredOutput.length; i++) {
				if ( desiredOutput[i] == 1 ) {
					p = 0;
					max = output[0];
					for (int j=1; j<output.length; j++) {
						if ( max < output[j]) {
							max = output[j];
							p = j;
						}
					}
					if ( i == p )correct++;
					
					break;
				}
			}
			total++;
		}
		System.out.println("MSE: "+mse.getTotalError());
		
		
		double accuracy = (double)correct/total*100;
		System.out.println("Accuracy = "+accuracy+"% ("+correct+"/"+total+")");
	}

	private static void writeLine(String file,String str) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			bw.write(str);
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
	
	private static double[] getWeight(String nnName) {
		FileReader fr = null;
		BufferedReader br = null;
		double[] weight = null;
		LinkedList<Double> list = new LinkedList();
		try {
			fr = new FileReader(nnName+".wei");
			br = new BufferedReader(fr);
			
			String tmp="";
			while((tmp=br.readLine())!=null){
				list.add(Double.valueOf(tmp));
			}
			
			weight = new double[list.size()];
			
			for(int i=0; i<list.size(); i++)
				weight[i] = list.get(i);
			
		} catch (FileNotFoundException e) {
		} catch (IOException e1) {
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException e) {
				// nothing
			}
		}
		return  weight;
	}

	public static void writeWeightToTxt(String nnName) {
		
		NeuralNetwork nn = NeuralNetwork
				.createFromFile(nnDir+nnName+".nnet");
		String path = "FANN/"+nnName+".wei";
		File f = new File(path);
		if ( f.exists() ) f.delete(); 
		Double[] w = nn.getWeights();
		for(int i=0; i<w.length; i++) {
			writeLine(path,String.valueOf(w[i]));
			System.out.println(w[i]);
		}
	}

	public static void writeTrainingTimetToTxt(String time) {
		String path = "traning.time";
		writeLine(path,time);
	}

	public static void main(String[] args) {

				
		int beaconQuantity = macSet.length;
		int areaQuantity = 6;
		int HiddenQuantity = 20;
		
		int seq;
		for(int i=0; i<4; i++) {
			seq = i;
			//long StartTime = System.currentTimeMillis(); // 取出目前時間
			
			if ( i == 0 ) { 
				learning("NN0"+seq+".nnet", "NNtraining0"+seq+".txt",
						beaconQuantity,HiddenQuantity,areaQuantity,0.02);
			} else {
				learning("NN0"+seq+".nnet", "NNtraining0"+seq+".txt",
									beaconQuantity,HiddenQuantity,areaQuantity, 0.001);
			}
			
			//long ProcessTime = System.currentTimeMillis() - StartTime; // 計算處理時間
			writeWeightToTxt(String.format("NN%02d", i));
			//writeTrainingTimetToTxt(String.valueOf(ProcessTime));
		
		//	testing("NN0"+seq+".nnet", "NNtesting0"+ seq+".txt",beaconQuantity,areaQuantity);
		}
	}

}