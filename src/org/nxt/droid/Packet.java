package org.nxt.droid;
import java.util.ArrayList;


/*
 * Da prebereš is stream uporabiš inputstream.readUTF();
 * To ti vrne string. Ta string vstaviš v paketek Packet p = new Packet(string)
 * Pol pa maš metodo p.getCommand al pa p.command ki ti vrne strign kira komanda je
 * Druga metoda p.values pa vrne array vseh parametrov.
 * Pošiljat pa za enkrat tak nerabiš še nič:)
 */

public class Packet {

	static public String make(String command, String content, Boolean response) {
		return command + "||" + content + "||" + response;
	}
	
	static public String make(String command, String content) {
		return make(command, content, false);
	}

	public static String content(Object... data) {
		String a = "";
		for (Object object : data) {
			a += object.toString() + "::";
		}
		return a.substring(0, a.length() - 2);
	}

		final String packet;
		final String command;
		final Boolean response;
		final String[] values;

		public String[] split(String s, String exp) {
			ArrayList<String> list = new ArrayList<String>();
			if(s.indexOf(exp)==-1){
				return new String[]{s};
			}
			while (s.indexOf(exp) >= 0) {
				int index = s.indexOf(exp);
				if (index == 0) {
					s = s.substring(2);
				} else {
					list.add(s.substring(0, index));
					s = s.substring(index+2);
				}
			}
			list.add(s);
			String[] polje = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				polje[i] = list.get(i);
			}
			return polje;
		}

		public Packet(String packet) {
			this.packet = packet;
			String[] polje = split(packet, "||");
			command = polje[0];
			values = split(polje[1], "::");
			response = Boolean.parseBoolean(polje[2]);
		}

		public String getCommand() {
			return command;
		}

		public String[] getValues() {
			return values;
		}
	}
