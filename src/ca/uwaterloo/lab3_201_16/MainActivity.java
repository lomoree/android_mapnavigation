package ca.uwaterloo.lab3_201_16;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.uwaterloo.sensortoy.LineGraphView;

public class MainActivity extends ActionBarActivity {

	static TextView steps;
	static TextView stepsNorth;
	static TextView stepsEast;
	static TextView state;
	static TextView heading;
	static TextView distance;
	static LineGraphView graph;
	static MapView mv;
	static boolean enable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		enable = true;
		// Instantiate the Linear Layout that will display the outputs
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		layout.setOrientation(LinearLayout.VERTICAL);

		// Instantiate the graph to display our accelerometer values over time
		graph = new LineGraphView(this, 100, Arrays.asList("x", "y", "z"));
		layout.addView(graph);
		graph.setVisibility(View.VISIBLE);

		// Instantiate and load map
		MapView mv = new MapView(getApplicationContext(), 950, 750, 55, 60);
		layout.addView(mv);
		mv.setVisibility(View.VISIBLE);
		registerForContextMenu(mv);

		NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null),
				"Lab-room-peninsula.svg");
		mv.setMap(map);

		// Steps and State TextViews for FSM
		TextView overview = new TextView(this);
		layout.addView(overview);
		overview.setText("--Overview--");
		overview.setTypeface(null, Typeface.BOLD);
		
		heading = new TextView(this);
		layout.addView(heading);
		heading.setText("Heading: 0");
		
		steps = new TextView(this);
		layout.addView(steps);
		steps.setText("Steps: 0");
		
		state = new TextView(this);
		layout.addView(state);
		state.setText("State: 0");
		
		TextView components = new TextView(this);
		layout.addView(components);
		components.setText("--Step Components--");
		components.setTypeface(null, Typeface.BOLD);

		distance = new TextView(this);
		layout.addView(distance);
		distance.setText("Distance: 0");
		
		stepsNorth = new TextView(this);
		layout.addView(stepsNorth);
		stepsNorth.setText("Steps North: 0");
		
		stepsEast = new TextView(this);
		layout.addView(stepsEast);
		stepsEast.setText("Steps East: 0");


		// Instantiate the sensorManager, and subsequent Sensors that will be
		// used
		SensorManager sensorManager = (SensorManager) this
				.getSystemService(SENSOR_SERVICE);

		// StepMachine: Linear Accelerometer Sensor (l)
		Sensor linearaccelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		SensorEventListener l = new AccelerometerSensorEventListener();
		sensorManager.registerListener(l, linearaccelerometerSensor,
				SensorManager.SENSOR_DELAY_FASTEST);

		// OrientationDetection: Accelerometer & Magnetic Sensors (a and m)
		Sensor accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		SensorEventListener a = new OrientationSensorEventListener();
		sensorManager.registerListener(a, accelerometerSensor,
				SensorManager.SENSOR_DELAY_FASTEST);

		Sensor magneticSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		SensorEventListener m = new OrientationSensorEventListener();
		sensorManager.registerListener(m, magneticSensor,
				SensorManager.SENSOR_DELAY_FASTEST);

		// Pause/Resume Button - unregisters/registers listeners as appropriate
		Button b1 = new Button(this);
		layout.addView(b1);
		b1.setText("Pause");
		b1.setOnClickListener(new pauseOnClickListener(l,
				linearaccelerometerSensor, a, accelerometerSensor, m,
				magneticSensor, sensorManager, b1) {
		});

		// Reset Button - resets state, and steps data
		Button b2 = new Button(this);
		layout.addView(b2);
		b2.setText("Reset");
		b2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				steps.setText("Steps: 0");
				state.setText("State: 0");
				distance.setText("Distance: 0");
				stepsNorth.setText("Steps North: 0");
				stepsEast.setText("Steps East: 0");

				StepMachine.currentState = 00;
				StepMachine.stateConfirmation = 00;
				StepMachine.steps = 0;
				StepMachine.distance = 0;
				StepMachine.direction = 0;

			}
		});

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Custom OnClickListener that can receive our sensors as parameters
	public class pauseOnClickListener implements OnClickListener {

		AccelerometerSensorEventListener a;
		Sensor as;
		OrientationSensorEventListener l;
		Sensor ls;
		OrientationSensorEventListener m;
		Sensor ms;
		SensorManager sensorManager;
		Button b1;

		public pauseOnClickListener(SensorEventListener a, Sensor as,
				SensorEventListener l, Sensor ls, SensorEventListener m,
				Sensor ms, SensorManager sm, Button b1) {
			this.a = (AccelerometerSensorEventListener) a;
			this.as = as;
			this.l = (OrientationSensorEventListener) l;
			this.ls = ls;
			this.m = (OrientationSensorEventListener) m;
			this.ms = ms;
			this.sensorManager = sm;
			this.b1 = b1;
		}

		// Complements the enable boolean, and registers/unregisters listener
		@Override
		public void onClick(View v) {
			enable = !enable;
			if (!enable) {
				sensorManager.unregisterListener(a, as);
				sensorManager.unregisterListener(l, ls);
				sensorManager.unregisterListener(m, ms);

				b1.setText("Resume");
			} else {
				sensorManager.registerListener(a, as,
						SensorManager.SENSOR_DELAY_FASTEST);
				sensorManager.registerListener(l, ls,
						SensorManager.SENSOR_DELAY_FASTEST);
				sensorManager.registerListener(m, ms,
						SensorManager.SENSOR_DELAY_FASTEST);
				b1.setText("Pause");
			}
		}

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mv.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item)
				|| mv.onContextItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
