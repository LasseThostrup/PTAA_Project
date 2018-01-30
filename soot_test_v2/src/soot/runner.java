package soot;

import java.util.LinkedList;
import java.util.List;

public class runner {

	public static void main(String[] args) {
		/*SDG sdg = new SDG("a");
		sdg.toDotGraph().plot("output/dotGraphTest.dot");*/

		List<String> process_dirs = new LinkedList<String>();
		process_dirs.add("/home/gregor/Eclipse_Workspaces/PTAA_Project/Library");

		SDG sdg = new SDG(process_dirs, "test.MyClass", "foo");
		sdg.toDotGraph().plot("output/dotGraphTest_Library.dot");
	}
}
