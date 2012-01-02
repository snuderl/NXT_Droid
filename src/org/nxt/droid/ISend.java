/**
 * 
 */
package org.nxt.droid;

/**
 * @author Blaž Šnuderl
 *
 */
public interface ISend {
	public void send(int command, boolean wantReturn, float... params);

}
