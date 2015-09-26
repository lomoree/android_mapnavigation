package ca.uwaterloo.lab3_201_16;

import java.util.Arrays;

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class StepMachine {

	static int currentState = 00;
	static int stateConfirmation = 00;
	static int steps;
	static float[] lastValues = new float[3];
	static double distance;
	static double direction;

	// Calls the method for the two state FSM. Each state is a component to one
	// step.
	public static void StateCaller(float[] smoothedValues) {

		if (currentState == 00)
			UpStep(smoothedValues);
		if (currentState == 01)
			DownStep(smoothedValues);

	}

	public static void UpStep(float[] smoothedValues) {

		// Looks for increasing Y values, and decreasing X values.
		if (smoothedValues[1] > lastValues[1]
				&& smoothedValues[0] < lastValues[0]) {
			stateConfirmation++;
		}
		// Requires a Y threshold, and further confirmations.
		// Value of 0.75 is lenient to light walkers.
		if (stateConfirmation >= 8 && smoothedValues[1] > 0.75) {
			stateConfirmation++;
			// Confirmed up step; updates steps, and state.
			if (stateConfirmation > 13) {
				currentState = 01;
				stateConfirmation = 00;
				MainActivity.state.setText("State: "
						+ String.valueOf(currentState));
				MainActivity.steps.setText("Steps: " + String.valueOf(++steps));
				calculateDistance();
			}

		}
		lastValues = Arrays.copyOf(smoothedValues, smoothedValues.length);
		return;
	}

	public static void DownStep(float[] smoothedValues) {

		// Expects Y and Z values to cross below 0.
		if (smoothedValues[1] < 0 && smoothedValues[2] < 0) {
			// While Y and Z are still decreasing, we wait.
			// Down step has only just begun.
			if (smoothedValues[1] < lastValues[1]
					&& smoothedValues[2] < lastValues[2]) {
				lastValues = Arrays.copyOf(smoothedValues,
						smoothedValues.length);
				return;
			}
			lastValues = Arrays.copyOf(smoothedValues, smoothedValues.length);
			stateConfirmation++;
		}
		// Confirmed down step; update state.
		if (stateConfirmation >= 6) {
			currentState = 00;
			stateConfirmation = 00;
			MainActivity.state
					.setText("State: " + String.valueOf(currentState));
		}
	}

	public static void calculateDistance() {

		// Current heading of the phone
		double heading = OrientationSensorEventListener.angle;

		// Gets the X and Y component of one step in the current heading
		double xdir = Math.cos(heading);
		double ydir = Math.sin(heading);

		// Gets the X and Y component of the current distance
		double xdistance = distance * Math.cos(direction);
		double ydistance = distance * Math.sin(direction);

		// Sums the components to get the new total distance
		xdistance += xdir;
		ydistance += ydir;

		// Finds net displacement and the angle/direction
		distance = Math.sqrt(Math.pow(xdistance, 2) + Math.pow(ydistance, 2));
		direction = Math.atan(ydistance / xdistance);

		// Quadrant 3 detection
		if (ydistance < 0 && xdistance < 0) {
			direction += Math.PI;
		}
		// Quadrant 2 detection
		if (ydistance > 0 && xdistance < 0) {
			direction = Math.PI - direction;
		}

		// Displays values to user
		MainActivity.distance.setText("Distance: " + String.format("%.1f", distance));
		MainActivity.stepsEast.setText("Steps East: "
				+ String.format("%.1f", distance * Math.cos(direction)));
		MainActivity.stepsNorth.setText("Steps North: "
				+ String.format("%.1f", distance * Math.sin(direction)));
		
		
		/* ORIGINAL CODE THAT OBTAINED DISPLACEMENT IN METERS
		 * // Current heading of the phone double heading =
		 * OrientationSensorEventListener.angle;
		 * 
		 * // Gets the X and Y component of one step in the current heading
		 * double xdir = 0.414*MainActivity.height*Math.cos(heading); double
		 * ydir = 0.414*MainActivity.height*Math.sin(heading);
		 * 
		 * // Gets the X and Y component of the current distance double
		 * xdistance = distance*Math.cos(direction); double ydistance =
		 * distance*Math.sin(direction);
		 * 
		 * // Sums the components to get the new total distance xdistance +=
		 * xdir; ydistance += ydir;
		 * 
		 * // Finds net displacement and the angle/direction distance =
		 * Math.sqrt(Math.pow(xdistance, 2) + Math.pow(ydistance, 2)); direction
		 * = Math.atan(ydistance/xdistance);
		 * 
		 * // Quadrant 3 detection if(ydistance < 0 && xdistance <0){ direction
		 * += Math.PI; } // Quadrant 2 detection if(ydistance > 0 && xdistance
		 * <0){ direction = Math.PI - direction; }
		 * 
		 * // Displays values to user MainActivity.distance.setText("Distance: "
		 * + String.valueOf(distance)+ "m");
		 * MainActivity.xdir.setText("X-Distance: " +
		 * String.valueOf(distance*Math.cos(direction) + "m"));
		 * MainActivity.ydir.setText("Y-Distance: " +
		 * String.valueOf(distance*Math.sin(direction) + "m"));
		 */
	}

}
