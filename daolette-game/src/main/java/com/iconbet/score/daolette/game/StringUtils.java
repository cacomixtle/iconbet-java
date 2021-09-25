package com.iconbet.score.daolette.game;

public final class StringUtils {

	StringUtils(){}

	//TODO: review if we need to use string buffer instead of stringbuilder as we define this method as static
	public static String[] split(String str, char separator) {

		char[] main = str.toCharArray();
		int numOfSepartors = 0;
		for(int i = 0; i < main.length; i++) {
			if ( main[i] == separator) {
				numOfSepartors++;
			}
		}

		String[] list = new String[numOfSepartors+1];
		if(numOfSepartors == 0) {
			return new String[] {str};
		}

		StringBuilder sb = new StringBuilder();
		int numOfArrays = 0;
		for(int i = 0; i < main.length; i++) {
			if ( main[i] == separator) {
				list[numOfArrays] = sb.toString();
				numOfArrays++;
				sb = new StringBuilder();
			}else {
				sb.append(main[i]);
			}
		}

		if( sb.length() != 0) {
			list[numOfArrays] = sb.toString();
		}
		return list;
	}

	
}
