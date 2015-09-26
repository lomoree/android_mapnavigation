package ca.uwaterloo.lab3_201_16;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

class AccelerometerSensorEventListener implements SensorEventListener {

	float smoothedx;
	float smoothedy;
	float smoothedz;

	float[] smoothedValues;

	public AccelerometerSensorEventListener() {
		this.smoothedx = 0;
		this.smoothedy = 0;
		this.smoothedz = 0;
		this.smoothedValues = new float[3];

	}

	@Override
	public void onAccuracyChanged(Sensor s, int i) {
	}

	// Given a new SensorEvent, updates current and record values
	@Override
	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			smoothedx += (se.values[0] - smoothedx) / 8;
			smoothedy += (se.values[1] - smoothedy) / 8;
			smoothedz += (se.values[2] - smoothedz) / 8;
			smoothedValues[0] = smoothedx;
			smoothedValues[1] = smoothedy;
			smoothedValues[2] = smoothedz;

			MainActivity.graph.addPoint(smoothedValues);

			StepMachine.StateCaller(smoothedValues);
		}
	}
}
