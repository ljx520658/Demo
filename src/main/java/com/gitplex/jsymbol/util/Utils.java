package com.gitplex.jsymbol.util;

public class Utils {
	
	public static int[] getOrdinals(Enum<?> array[]) {
		int[] intArray = new int[array.length];
		for (int i=0; i<array.length; i++)
			intArray[i] = array[i].ordinal();
		return intArray;
	}
	
}
