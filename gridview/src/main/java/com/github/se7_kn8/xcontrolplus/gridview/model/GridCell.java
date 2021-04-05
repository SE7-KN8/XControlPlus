package com.github.se7_kn8.xcontrolplus.gridview.model;

import com.github.se7_kn8.xcontrolplus.gridview.CellRotation;
import com.github.se7_kn8.xcontrolplus.gridview.GridRenderer;

import javafx.scene.canvas.GraphicsContext;

public abstract class GridCell {
	private int gridX;
	private int gridY;
	private CellRotation rotation;

	public GridCell() {
		this(0, 0, CellRotation.D0);
	}

	public GridCell(int gridX, int gridY, CellRotation rotation) {
		this.gridX = gridX;
		this.gridY = gridY;
		this.rotation = rotation;
	}

	public abstract void render(long now, GraphicsContext gc, GridRenderer<? extends GridCell> renderer);

	public void setGridX(int gridX) {
		this.gridX = gridX;
	}

	public void setGridY(int gridY) {
		this.gridY = gridY;
	}

	public int getGridX() {
		return gridX;
	}

	public int getGridY() {
		return gridY;
	}

	public void setRotation(CellRotation rotation) {
		this.rotation = rotation;
	}

	public CellRotation getRotation() {
		return rotation;
	}
}
