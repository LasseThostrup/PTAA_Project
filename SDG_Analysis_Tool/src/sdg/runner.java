package sdg;

import java.nio.file.Paths;

public class runner {

	public static void main(String[] args) {
		SDGCreator sdgCreator = new SDGCreator();
		SDG sdg = sdgCreator.createSDG("C:\\JavaProjects\\PTAA_Project\\Test_Programs\\Small_Example_Programs\\bin".replaceAll("\\\\", "/"),
		//SDG sdg = sdgCreator.createSDG("C:\\JavaProjects\\PTAA_Project\\Test_Programs\\jabref-master\\bin".replaceAll("\\\\", "/"),
		//SDG sdg = sdgCreator.createSDG("C:\\JavaProjects\\PTAA_Project\\Test_Programs\\JApps\\bin".replaceAll("\\\\", "/"),
				SDGCreator.DEFAULT_OPTIONS);
		sdg.toDotGraph().plot("output/dotGraphTest.dot");
//
//		SDGCreator sdgCreator = new SDGCreator();
//		SDG sdg = sdgCreator.createSDG("/home/gregor/Eclipse_Workspaces/PTAA_Project/Library/bin",
//				SDGCreator.DEFAULT_OPTIONS);
//		sdg.toDotGraph().plot("output/dotGraphTest_Library.dot");

	}
}
