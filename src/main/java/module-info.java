module cc.wanforme.ofx {
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;
	
	requires spring.boot;
	requires spring.beans;
	requires transitive spring.context;
	requires spring.core;

	requires spring.boot.autoconfigure; // test
	
	exports cc.wanforme.ofx;
}
