module xcontrolplus {
	requires javafx.controls;
	requires javafx.graphics;
	requires kotlin.stdlib;
	requires com.google.gson;
	requires xcontrolplus.gridview;

	// Required for javafx to start
	exports com.github.se7_kn8.xcontrolplus.app to javafx.graphics;

	// Required for gson
	opens com.github.se7_kn8.xcontrolplus.app.grid to com.google.gson;
}
