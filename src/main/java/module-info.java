module xcontrolplus {
	requires javafx.controls;
	requires javafx.graphics;
	requires kotlin.stdlib;
	requires com.google.gson;

	// Required for javafx to start
	exports com.github.se7_kn8.xcontrolplus.app to javafx.graphics;

	// Allow json serialization
	opens com.github.se7_kn8.xcontrolplus.app.grid to com.google.gson;
}
