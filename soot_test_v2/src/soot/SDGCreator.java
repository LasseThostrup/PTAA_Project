package soot;

public class SDGCreator {

	public static final String DEFAULT_OPTIONS = "-p cg enabled:true -whole-program -allow-phantom-refs -no-bodies-for-excluded -full-resolver";

	public SDG createSDG(String pathToBin, String options) {
		SDGTransformer sdgTransformer = new SDGTransformer();
		String[] args = (options + " -process-dir " + pathToBin).split(" ");
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", sdgTransformer));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() + ":" + pathToBin);
		soot.Main.main(args);
		return sdgTransformer.getSDG();
	}

}
