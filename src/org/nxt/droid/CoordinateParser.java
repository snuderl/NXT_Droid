/**
 * 
 */
package org.nxt.droid;

/**
 * @author Blaž Šnuderl
 *
 */
public class CoordinateParser {
	
	public void send(ISend send, float... params){
		
		float forward = 0;
		float steer = 0;
		if(params[0]<Math.abs(params[0])){
			forward = 80-Math.abs(params[0]);
			if(params[0]<0){
				forward = forward*-1;
			}
		}
		
		send.send(1, false, forward);
	}

}
