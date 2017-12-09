package soot;

public class runner {

	public static void main(String[] args) {
		SDG sdg = new SDG("a");
		sdg.toDotGraph().plot("output/dotGraphTest.dot");
	}
}
