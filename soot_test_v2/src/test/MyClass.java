package test;

import java.util.ArrayList;

import soot.JastAddJ.List;

public class MyClass {
	public static void main(String[] args) {
//		ArrayList<Integer> asd = new ArrayList<Integer>();
//		asd.add(1);
//		asd.add(2);
		int a = 2;
		foo(a);
	}

	public static int foo(int c) {
		int a = 1 + bar();
		int b = a + c;
		if (a > b)
			return 4;
		else
			return 2;
	}

	public static int bar() {
		int x = 1;
		int y = 0;
		if (x < 2) {
			y = 2 * x;
		}
		calc(x);
		return y;
	}
	
	public static void calc(int i) {
		int a = 1+i;
	}
}
