package test;

public class PrjDescription_Prog {

	private static final int MAX = 100;

	void foo() {
		int x = source();
		if (x < MAX) {
			int y = 2 * x;
			sink(y);
		}
	}

	private void sink(int y) {
	}

	private int source() {
		return 0;
	}
}
