package com.github.se7_kn8.xcontrolplus.gridview;

import com.github.se7_kn8.xcontrolplus.gridview.model.GridCell;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

public class GridRenderer<T extends GridCell> extends AnimationTimer {

	private final GridView<T> view;
	private final GraphicsContext gc;

	public GridRenderer(GridView<T> view) {
		this.view = view;
		this.gc = view.getGraphicsContext2D();
	}

	@Override
	public void handle(long now) {
		clearScreen();
		gc.save();
		gc.setTransform(view.getGridTransform());
		view.getBackgroundCallback().accept(now, this);

		if (view.isRenderGrid()) {
			renderGrid();
		}

		renderCells(now);

		if (view.isHighlightSelectedCell()) {
			renderHighlight();
		}

		view.getForegroundCallback().accept(now, this);
		gc.restore();
		view.getOverlayCallback().accept(now, this);
	}

	private void renderHighlight() {
		if (view.getSelectedCell() != null) {
			gc.setLineWidth(view.getGridLineWidth() * 1.5);
			gc.setStroke(view.getHighlightColor());
			gc.strokeRect(getPosX(view.getSelectedCell().getGridX()), getPosY(view.getSelectedCell().getGridY()), view.getGridSize(), view.getGridSize());
		}
	}

	private void renderCells(long now) {
		for (T cell : view.getCells()) {
			gc.save();
			Affine newTransform = gc.getTransform();
			newTransform.appendRotation(cell.getRotation().rotation(), getMidX(cell.getGridX()), getMidY(cell.getGridY()));
			gc.setTransform(newTransform);
			cell.render(now, gc, this);
			gc.restore();
		}
	}

	private void renderGrid() {
		gc.setStroke(view.getGridColor());
		gc.setLineWidth(view.getGridLineWidth());
		// TODO allow floating point numbers
		for (int x = (int) view.getGridStartX(); x <= calcPixelWidth(); x += view.getGridSize()) {
			gc.strokeLine(x, view.getGridStartY(), x, calcPixelHeight());
		}
		// TODO allow floating point numbers
		for (int y = (int) view.getGridStartY(); y <= calcPixelHeight(); y += view.getGridSize()) {
			gc.strokeLine(view.getGridStartX(), y, calcPixelWidth(), y);
		}
	}

	private void clearScreen() {
		gc.setFill(view.getClearColor());
		gc.fillRect(0.0, 0.0, view.getWidth(), view.getHeight());
	}

	private double calcPixelWidth() {
		return view.getGridWidth() * view.getGridSize();
	}

	private double calcPixelHeight() {
		return view.getGridHeight() * view.getGridSize();
	}

	public double getPosX(double gridX) {
		return gridX * view.getGridSize();
	}

	public double getPosY(double gridY) {
		return gridY * view.getGridSize();
	}

	public double getMidX(double gridX) {
		return getPosX(gridX) + view.getGridSize() / 2.0;
	}

	public double getMidY(double gridY) {
		return getPosY(gridY) + view.getGridSize() / 2.0;
	}

	public GridView<T> getView() {
		return view;
	}

	public double getGridSize() {
		return view.getGridSize();
	}

	public GraphicsContext getGc() {
		return gc;
	}

}
