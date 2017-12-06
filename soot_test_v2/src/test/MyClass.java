package test;

public class MyClass {
	public static void main(String[] args) {
		foo();
	}

	public static int foo() {
		int a = 1 + 2;
		int b = a + 3;
		if (a > b)
			return bar();
		else
			return 2;
	}

	public static int bar() {
		int x = 1;
		int y = 0;
		if (x < 2) {
			y = 2 * x;
		}
		return y;
	}
}
