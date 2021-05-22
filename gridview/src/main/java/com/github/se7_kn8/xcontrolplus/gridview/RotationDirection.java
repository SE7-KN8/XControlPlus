package com.github.se7_kn8.xcontrolplus.gridview;

public enum RotationDirection {
	CLOCKWISE,
	COUNTER_CLOCKWISE,
	HALF,
	;

	public RotationDirection invert() {
		return switch (this) {
			case CLOCKWISE -> COUNTER_CLOCKWISE;
			case COUNTER_CLOCKWISE -> CLOCKWISE;
			case HALF -> HALF;
		};
	}

}
