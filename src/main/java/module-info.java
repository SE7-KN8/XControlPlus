module xcontrolplus {
	requires xcontrolplus.gridview;
	requires xcontrolplus.protocol;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;

	requires kotlin.stdlib;

	requires com.google.gson;
	requires kotlin.stdlib.jdk8;
	requires kotlin.stdlib.jdk7;

	requires kotlin.logging.jvm;

	requires org.jfxtras.styles.jmetro;

	// Required for javafx to start and logging
	exports com.github.se7_kn8.xcontrolplus to javafx.graphics, java.logging;
	exports com.github.se7_kn8.xcontrolplus.app to javafx.graphics;


	// Required for gson
	opens com.github.se7_kn8.xcontrolplus.app.grid to com.google.gson;
	opens com.github.se7_kn8.xcontrolplus.app.project to com.google.gson;
}
