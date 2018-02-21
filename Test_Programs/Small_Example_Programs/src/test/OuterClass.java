package test;
public class OuterClass {
	public static void main(String[] args) {
		new OuterClass();		
	}
	
	OuterClass() {
		System.out.println("Constructor of outer class");
		new InnerClass();
	}
		
	public class InnerClass {
		InnerClass() {
			System.out.println("Constructor of inner class");
		}
	}
}


