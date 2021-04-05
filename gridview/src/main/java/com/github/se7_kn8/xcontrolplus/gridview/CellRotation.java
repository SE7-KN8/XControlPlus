package com.github.se7_kn8.xcontrolplus.gridview;

public enum CellRotation {

	D0(0.0),
	D90(90.0),
	D180(180.0),
	D270(270.0),
	;

	CellRotation(double rotation) {
		this.rotation = rotation;
	}

	private final double rotation;
	private CellRotation before;
	private CellRotation next;

	static {
		D0.before = D270;
		D0.next = D90;

		D90.before = D0;
		D90.next = D180;

		D180.before = D90;
		D180.next = D270;

		D270.before = D180;
		D270.next = D0;
	}

	public CellRotation rotateCW() {
		return next;
	}

	public CellRotation rotateCCW() {
		return before;
	}

	public double rotation() {
		return rotation;
	}

}
