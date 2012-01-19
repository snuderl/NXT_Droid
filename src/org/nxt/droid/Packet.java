package org.nxt.droid;

public class Packet {
	static public String make(String command, String content) {
		return command + "||" + content;
	}
	
	public static String content(Object... data) {
		String a = "";
		for (Object object : data) {
			a+= object.toString()+"{{";
		}
		return a.substring(0, a.length()-2);
	}
	
	final String packet;
	final String command;
	final String[] values;
	public Packet(String packet) {
		this.packet=packet;
		String[] split = packet.split("||");
		command = split[0];;
		values = split[1].split("{{");
	}
	
	public String getCommand() {
		return command;
	}
	
	public String[] getValues() {
		return values;
	}
}
