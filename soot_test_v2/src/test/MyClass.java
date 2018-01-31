package test;

import java.util.ArrayList;

import soot.JastAddJ.List;

public class MyClass {
	public static void main(String[] args) {
		// ArrayList<Integer> asd = new ArrayList<Integer>();
		// asd.add(1);
		// asd.add(2);
//		int a = 2;
//		foo(a);
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
		int ret1 = calc(x);
		int ret2 = calc(y);
		return calc(ret1) + calc(ret2);

	}

	public static int calc(int i) {
		return 1 + i;
	}
}
