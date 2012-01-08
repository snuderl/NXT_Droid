/**
 * 
 */
package org.nxt.droid;

import android.util.Log;

/**
 * @author Blaž Šnuderl
 *
 */
public class CoordinateParser {
	
	public void send(ISend send, int multiplier, float... params){
		
		float forward = 0;
		//Minus je levo, pozitivno desno;
		float steer = 0;
		if(params[2]<45f){
		forward = (45 - (params[2]%45))/10;
		}
		else{
			forward = -((params[2]-45)%45)/10;
		}
		
		boolean positive = false;
		if(params[1]>0){
			positive=true;
		}
		float y = Math.abs(params[1]);
		steer = (y%45)/10;
		if(positive){
			steer *= -1;
		}
		
		
		send.send(1, false, forward*multiplier, steer*multiplier);
		String s = ("Forward: "+forward +", steer: "+steer);
		Log.d("Coordinates", s);
	}

}
