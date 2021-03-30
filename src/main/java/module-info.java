module com.github.se7_kn8.xcontrolplus {
	requires java.base;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires kotlin.stdlib.jdk8;
	requires kotlinx.serialization.core.jvm;
	requires kotlinx.serialization.json.jvm;

	exports com.github.se7_kn8.xcontrolplus.app to javafx.graphics;
}
