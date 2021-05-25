module xcontrolplus {
	requires xcontrolplus.gridview;
	requires xcontrolplus.protocol;

	requires javafx.controls;
	requires javafx.graphics;

	requires kotlin.stdlib;

	requires com.google.gson;
	requires kotlin.stdlib.jdk8;
	requires kotlin.stdlib.jdk7;

	// Required for javafx to start
	exports com.github.se7_kn8.xcontrolplus.app to javafx.graphics;

	// Required for gson
	opens com.github.se7_kn8.xcontrolplus.app.grid to com.google.gson;
	opens com.github.se7_kn8.xcontrolplus.app.project to com.google.gson;
}
