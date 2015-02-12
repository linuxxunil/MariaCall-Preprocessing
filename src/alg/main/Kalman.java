package alg.main;

public class Kalman {
	private double X_;
	private double P_;
	private double K;
	private double X;
	private double P;
	private double Q = 0.01;
	private double R = 0.01;
	private double Z;
	
	public Kalman(double X, double P,double Q, double R) {
		this.Z = Z;
		this.X = X;
		this.P = P;
		this.Q = Q;
		this.R = R;	
	}

	public void correct(double currentZ)
	{
		Z = currentZ;	
	}
	
	public void predict() 
	{
		X_ = X;
		P_ = P + Q;
	}
	
	public void update()
	{
		K  = P_ / (P_ + R);
		X  = X_ + K * (Z - X_);
		P  = ( 1 - K) * P_;
	}
	
	public double getStateEstimation()
	{
		return X;
	}
	
}
