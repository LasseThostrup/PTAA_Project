package test;


public class bar {
	public static final int MAX = 100;
	
	void foo () {
		int x = source ();
		if (x < MAX ) {
			int y = 2 * x;
			sink (y);
		}
	}
	int source () {
		return 1;
	}
	
	void sink ( int x) {
		int a = x;
	}

}
