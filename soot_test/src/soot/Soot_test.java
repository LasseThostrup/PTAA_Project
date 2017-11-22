package soot;

public class Soot_test {

	public static void main(String[] args) {
		System.out.println(Scene.v().getSootClassPath());
		SootClass c = Scene.v().loadClassAndSupport("test.MyClass");
		c.setApplicationClass();
	}
}
