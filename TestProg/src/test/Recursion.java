package test;

import java.util.ArrayList;

public class Recursion {
	int x = 2;
	public static void main(String[] args) {
		if ("abc".contains("a"))
			new Recursion().bar();
		int i = Integer.compare(0, 1);
		ArrayList<Integer> al = new ArrayList<Integer>();
	}
	
	
	public int bar() {
		if (x > 0) {
			x = x-1;
			bar();
		}
		return x;
	}
}