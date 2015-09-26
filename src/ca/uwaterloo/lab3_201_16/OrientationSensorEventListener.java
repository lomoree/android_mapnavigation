package ca.uwaterloo.lab3_201_16;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class OrientationSensorEventListener implements SensorEventListener {

	static float[] gravity;
	static float[] geomagnetic;
	float R[] = new float[9];
	float I[] = new float[9];
	float orientation[] = new float[3];
	static double angle;
	static double xdir;
	static double ydir;
	
	public OrientationSensorEventListener()
	{  
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent se) {
	
		if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			gravity = se.values;
		}
		if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			geomagnetic = se.values;
		}
		if(gravity != null && geomagnetic != null)
		{
			boolean enable = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if(enable){
				SensorManager.getOrientation(R, orientation);
				
				// Cartesian Plane Conversion: 90 - (angle + 360) mod 360
				angle = Math.PI/2 - (orientation[0]+2*Math.PI)%(2*Math.PI);
				
				// Low pass filter on vector components
				xdir = (Math.cos(angle) + xdir) / 8;
				ydir = (Math.sin(angle) + ydir) / 8;
				
				// Reconstruct angle
				double angle = Math.atan(ydir/xdir);
				
				// Quadrant 3 detection
				if (ydir < 0 && xdir < 0) {
					angle += Math.PI;
				}
				// Quadrant 2 detection
				if (ydir > 0 && xdir < 0) {
					angle = Math.PI - angle;
				}

				
				MainActivity.heading.setText("Heading: " + String.valueOf(angle));
				
			}
		}
	}

}
