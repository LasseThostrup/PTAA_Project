package sdg;

import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class SDGCreator {

	public static final String DEFAULT_OPTIONS = "-p cg enabled:true -whole-program -allow-phantom-refs -no-bodies-for-excluded -x java.";

	public SDG createSDG(String pathToBin, String options) {
		SDGTransformer sdgTransformer = new SDGTransformer();
		String[] args = (options + " -process-dir " + pathToBin).split(" ");
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", sdgTransformer));
		Scene.v().setSootClassPath(pathToBin);
		soot.Main.main(args);
		return sdgTransformer.getSDG();
	}

}
