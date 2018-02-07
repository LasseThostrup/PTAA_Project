package soot;

public class runner {

	public static void main(String[] args) {
//		SDGCreator sdgCreator = new SDGCreator();
//		SDG sdg = sdgCreator.createSDG("/home/gregor/Eclipse_Workspaces/PTAA_Project/TestProg/bin",
//				SDGCreator.DEFAULT_OPTIONS);
//		sdg.toDotGraph().plot("output/dotGraphTest.dot");

		SDGCreator sdgCreator = new SDGCreator();
		SDG sdg = sdgCreator.createSDG("/home/gregor/Eclipse_Workspaces/PTAA_Project/Library/bin",
				SDGCreator.DEFAULT_OPTIONS);
		sdg.toDotGraph().plot("output/dotGraphTest_Library.dot");

	}
}
