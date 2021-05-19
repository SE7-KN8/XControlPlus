package com.github.se7_kn8.xcontrolplus.gridview;

import com.github.se7_kn8.xcontrolplus.gridview.model.GridCell;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.Optional;
import java.util.function.BiConsumer;

public class GridView<T extends GridCell> extends Canvas {

	private final BiConsumer<Long, GridRenderer<T>> EMPTY_CALLBACK = (now, gc) -> {
		// NOP
	};

	private final GridRenderer<T> renderer;
	private final ObservableList<T> cells = FXCollections.observableArrayList();

	private double moveOffsetX;
	private double moveOffsetY;
	private double mouseStartPosX;
	private double mouseStartPosY;

	public GridView() {
		this(50.0, 100.0, 100.0);
	}

	public GridView(double gridSize, double gridWidth, double gridHeight) {
		setGridSize(gridSize);
		setGridWidth(gridWidth);
		setGridHeight(gridHeight);

		renderer = new GridRenderer<>(this);

		translationXProperty().addListener((o, oV, newValue) -> getGridTransform().setTx(newValue.doubleValue()));
		translationYProperty().addListener((o, oV, newValue) -> getGridTransform().setTy(newValue.doubleValue()));

		scaleProperty().addListener((o, oldValue, newValue) -> {
			double scaleChangeFactor = oldValue.doubleValue() / newValue.doubleValue();
			Point2D midPoint = transformScreenToGrid(getWidth() / 2.0, getHeight() / 2.0);
			getGridTransform().appendScale(scaleChangeFactor, scaleChangeFactor, midPoint.getX(), midPoint.getY());
			setTranslationX(getGridTransform().getTx());
			setTranslationY(getGridTransform().getTy());
		});

		setOnMousePressed(this::onMousePressed);
		setOnMouseDragged(this::onMouseDragged);
		setOnScroll(this::onScroll);
		setOnMouseMoved(this::onMouseMoved);

		pauseProperty().addListener((o, oV, newValue) -> {
			if (newValue) {
				renderer.stop();
			} else {
				renderer.start();
			}
		});

		renderer.start();
		getStyleClass().setAll("grid-view");
	}

	private void onMousePressed(MouseEvent event) {
		requestFocus();
		moveOffsetX = getTranslationX();
		moveOffsetY = getTranslationY();
		mouseStartPosX = event.getX();
		mouseStartPosY = event.getY();
		if (event.getButton() != getMoveMouseButton()) {
			if (isHighlightSelectedCell()) {
				findCell(getMouseGridX(), getMouseGridY()).ifPresentOrElse(this::setSelectedCell, () -> this.setSelectedCell(null));
			}
			getClickCallback().accept(event, false);
		}
	}

	private void onMouseDragged(MouseEvent event) {
		if (event.getButton() == getMoveMouseButton()) {
			double translationX = event.getX() - mouseStartPosX + moveOffsetX;
			double translationY = event.getY() - mouseStartPosY + moveOffsetY;
			setTranslationX(translationX);
			setTranslationY(translationY);
		} else if (isClickAndDrag() && event.getButton() != getMoveMouseButton()) {
			int oldMousePosX = getMouseGridX();
			int oldMousePosY = getMouseGridY();
			updateMousePos(event.getX(), event.getY());
			if (oldMousePosX != getMouseGridX() || oldMousePosY != getMouseGridY()) {
				getClickCallback().accept(event, true);
			}
		}
	}

	private void onScroll(ScrollEvent event) {
		double zoom = -event.getDeltaY() * event.getMultiplierY() * 0.0001 * getZoomFactor() + 1.0;
		double newValue = getScale() * zoom;

		newValue = Math.min(newValue, getMaxScale());
		newValue = Math.max(newValue, getMinScale());

		setScale(newValue);

		updateMousePos(event.getX(), event.getY());
	}

	private void onMouseMoved(MouseEvent event) {
		updateMousePos(event.getX(), event.getY());
	}


	private void updateMousePos(double x, double y) {
		Point2D coords = transformScreenToGrid(x, y).multiply(1.0 / getGridSize());

		x = coords.getX();
		y = coords.getY();

		if (x < 0.0) {
			x -= 1.0;
		}

		if (y < 0.0) {
			y -= 1.0;
		}

		if (!isOutsideGridPlacement()) {
			x = Math.min(getGridWidth() - 1, x);
			x = Math.max(getGridStartX(), x);

			y = Math.min(getGridHeight() - 1, y);
			y = Math.max(getGridStartY(), y);
		}

		mouseGridX.set((int) x);
		mouseGridY.set((int) y);
	}

	public Point2D transformScreenToGrid(double x, double y) {
		try {
			return getGridTransform().inverseTransform(new Point2D(x, y));
		} catch (NonInvertibleTransformException e) {
			e.printStackTrace();
		}
		return Point2D.ZERO;
	}

	public Optional<T> findCell(int x, int y) {
		for (T cell : cells) {
			if (cell.getGridX() == x && cell.getGridY() == y) {
				return Optional.of(cell);
			}
		}

		return Optional.empty();
	}

	public ObservableList<T> getCells() {
		return cells;
	}

	private final DoubleProperty gridHeight = new SimpleDoubleProperty();

	public double getGridHeight() {
		return gridHeight.get();
	}

	public DoubleProperty gridHeightProperty() {
		return gridHeight;
	}

	public void setGridHeight(double gridHeight) {
		this.gridHeight.set(gridHeight);
	}

	private final DoubleProperty gridWidth = new SimpleDoubleProperty();

	public double getGridWidth() {
		return gridWidth.get();
	}

	public DoubleProperty gridWidthProperty() {
		return gridWidth;
	}

	public void setGridWidth(double gridWidth) {
		this.gridWidth.set(gridWidth);
	}

	private final DoubleProperty gridSize = new SimpleDoubleProperty();

	public double getGridSize() {
		return gridSize.get();
	}

	public DoubleProperty gridSizeProperty() {
		return gridSize;
	}

	public void setGridSize(double gridSize) {
		this.gridSize.set(gridSize);
	}

	private final DoubleProperty scale = new SimpleDoubleProperty(1.0);

	public double getScale() {
		return scale.get();
	}

	public DoubleProperty scaleProperty() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale.set(scale);
	}

	private final DoubleProperty translationX = new SimpleDoubleProperty(0.0);

	public double getTranslationX() {
		return translationX.get();
	}

	public DoubleProperty translationXProperty() {
		return translationX;
	}

	public void setTranslationX(double translationX) {
		this.translationX.set(translationX);
	}

	private final DoubleProperty translationY = new SimpleDoubleProperty(0.0);

	public double getTranslationY() {
		return translationY.get();
	}

	public DoubleProperty translationYProperty() {
		return translationY;
	}

	public void setTranslationY(double translationY) {
		this.translationY.set(translationY);
	}


	private final BooleanProperty renderGrid = new SimpleBooleanProperty(true);

	public boolean isRenderGrid() {
		return renderGrid.get();
	}

	public BooleanProperty renderGridProperty() {
		return renderGrid;
	}

	public void setRenderGrid(boolean renderGrid) {
		this.renderGrid.set(renderGrid);
	}

	private final IntegerProperty mouseGridX = new SimpleIntegerProperty(0);

	public int getMouseGridX() {
		return mouseGridX.get();
	}

	public ReadOnlyIntegerProperty mouseGridXProperty() {
		return mouseGridX;
	}

	private final IntegerProperty mouseGridY = new SimpleIntegerProperty(0);

	public int getMouseGridY() {
		return mouseGridY.get();
	}

	public ReadOnlyIntegerProperty mouseGridYProperty() {
		return mouseGridY;
	}

	public GridRenderer<T> getRenderer() {
		return renderer;
	}

	private final DoubleProperty gridLineWidth = new SimpleDoubleProperty(2.0);

	public void setGridLineWidth(double gridLineWidth) {
		this.gridLineWidth.set(gridLineWidth);
	}

	public double getGridLineWidth() {
		return gridLineWidth.get();
	}

	public DoubleProperty gridLineWidthProperty() {
		return gridLineWidth;
	}

	private final ReadOnlyObjectProperty<Affine> gridTransform = new SimpleObjectProperty<>(new Affine());

	public Affine getGridTransform() {
		return gridTransform.get();
	}

	public ReadOnlyObjectProperty<Affine> gridTransformProperty() {
		return gridTransform;
	}

	private final ObjectProperty<BiConsumer<Long, GridRenderer<T>>> backgroundCallback = new SimpleObjectProperty<>(EMPTY_CALLBACK);

	public void setBackgroundCallback(BiConsumer<Long, GridRenderer<T>> backgroundCallback) {
		this.backgroundCallback.set(backgroundCallback);
	}

	public BiConsumer<Long, GridRenderer<T>> getBackgroundCallback() {
		return backgroundCallback.get();
	}

	public ObjectProperty<BiConsumer<Long, GridRenderer<T>>> backgroundCallbackProperty() {
		return backgroundCallback;
	}

	private final ObjectProperty<BiConsumer<Long, GridRenderer<T>>> foregroundCallback = new SimpleObjectProperty<>(EMPTY_CALLBACK);

	public void setForegroundCallback(BiConsumer<Long, GridRenderer<T>> foregroundCallback) {
		this.foregroundCallback.set(foregroundCallback);
	}

	public BiConsumer<Long, GridRenderer<T>> getForegroundCallback() {
		return foregroundCallback.get();
	}

	public ObjectProperty<BiConsumer<Long, GridRenderer<T>>> foregroundCallbackProperty() {
		return foregroundCallback;
	}

	private final ObjectProperty<BiConsumer<Long, GridRenderer<T>>> overlayCallback = new SimpleObjectProperty<>(EMPTY_CALLBACK);

	public void setOverlayCallback(BiConsumer<Long, GridRenderer<T>> overlayCallback) {
		this.overlayCallback.set(overlayCallback);
	}

	public BiConsumer<Long, GridRenderer<T>> getOverlayCallback() {
		return overlayCallback.get();
	}

	public ObjectProperty<BiConsumer<Long, GridRenderer<T>>> overlayCallbackProperty() {
		return overlayCallback;
	}

	private final ObjectProperty<Color> gridColor = new SimpleObjectProperty<>(Color.GREY);

	public void setGridColor(Color gridColor) {
		this.gridColor.set(gridColor);
	}

	public Color getGridColor() {
		return gridColor.get();
	}

	public ObjectProperty<Color> gridColorProperty() {
		return gridColor;
	}

	private final ObjectProperty<Color> clearColor = new SimpleObjectProperty<>(Color.WHITE);

	public void setClearColor(Color clearColor) {
		this.clearColor.set(clearColor);
	}

	public Color getClearColor() {
		return clearColor.get();
	}

	public ObjectProperty<Color> clearColorProperty() {
		return clearColor;
	}

	private final ObjectProperty<MouseButton> moveMouseButton = new SimpleObjectProperty<>(MouseButton.MIDDLE);

	public void setMoveMouseButton(MouseButton moveMouseButton) {
		this.moveMouseButton.set(moveMouseButton);
	}

	public MouseButton getMoveMouseButton() {
		return moveMouseButton.get();
	}

	public ObjectProperty<MouseButton> moveMouseButtonProperty() {
		return moveMouseButton;
	}

	private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);

	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor.set(zoomFactor);
	}

	public double getZoomFactor() {
		return zoomFactor.get();
	}

	public DoubleProperty zoomFactorProperty() {
		return zoomFactor;
	}

	private final DoubleProperty minScale = new SimpleDoubleProperty(0.5);

	public void setMinScale(double minScale) {
		this.minScale.set(minScale);
	}

	public double getMinScale() {
		return minScale.get();
	}

	public DoubleProperty minScaleProperty() {
		return minScale;
	}

	private final DoubleProperty maxScale = new SimpleDoubleProperty(2.0);

	public void setMaxScale(double maxScale) {
		this.maxScale.set(maxScale);
	}

	public double getMaxScale() {
		return maxScale.get();
	}

	public DoubleProperty maxScaleProperty() {
		return maxScale;
	}

	private final ObjectProperty<BiConsumer<MouseEvent, Boolean>> clickCallback = new SimpleObjectProperty<>((event, drag) -> {
	});

	public void setClickCallback(BiConsumer<MouseEvent, Boolean> clickCallback) {
		this.clickCallback.set(clickCallback);
	}

	public BiConsumer<MouseEvent, Boolean> getClickCallback() {
		return clickCallback.get();
	}

	public ObjectProperty<BiConsumer<MouseEvent, Boolean>> clickCallbackProperty() {
		return clickCallback;
	}

	private final BooleanProperty outsideGridPlacement = new SimpleBooleanProperty(false);

	public void setOutsideGridPlacement(boolean outsideGridPlacement) {
		this.outsideGridPlacement.set(outsideGridPlacement);
	}

	public boolean isOutsideGridPlacement() {
		return outsideGridPlacement.get();
	}

	private BooleanProperty outsideGridPlacementProperty() {
		return outsideGridPlacement;
	}

	private final DoubleProperty gridStartX = new SimpleDoubleProperty(0.0);

	public double getGridStartX() {
		return gridStartX.get();
	}

	public void setGridStartX(double gridStartX) {
		this.gridStartX.set(gridStartX);
	}

	public DoubleProperty gridStartXProperty() {
		return gridStartX;
	}

	private final DoubleProperty gridStartY = new SimpleDoubleProperty(0.0);

	public double getGridStartY() {
		return gridStartY.get();
	}

	public void setGridStartY(double gridStartY) {
		this.gridStartY.set(gridStartY);
	}

	public DoubleProperty gridStartYProperty() {
		return gridStartY;
	}

	private final BooleanProperty clickAndDrag = new SimpleBooleanProperty(false);

	public void setClickAndDrag(boolean clickAndDrag) {
		this.clickAndDrag.set(clickAndDrag);
	}

	public boolean isClickAndDrag() {
		return clickAndDrag.get();
	}

	public BooleanProperty clickAndDragProperty() {
		return clickAndDrag;
	}

	private final BooleanProperty pause = new SimpleBooleanProperty(false);

	public void setPause(boolean pause) {
		this.pause.set(pause);
	}

	public boolean isPause() {
		return pause.get();
	}

	public BooleanProperty pauseProperty() {
		return pause;
	}

	private final BooleanProperty highlightSelectedCell = new SimpleBooleanProperty(false);

	public void setHighlightSelectedCell(boolean highlightSelectedCell) {
		this.highlightSelectedCell.set(highlightSelectedCell);
	}

	public boolean isHighlightSelectedCell() {
		return highlightSelectedCell.get();
	}

	public BooleanProperty highlightSelectedCellProperty() {
		return highlightSelectedCell;
	}

	private final ObjectProperty<T> selectedCell = new SimpleObjectProperty<>();

	public T getSelectedCell() {
		return selectedCell.get();
	}

	public ObjectProperty<T> selectedCellProperty() {
		return selectedCell;
	}

	public void setSelectedCell(T value) {
		selectedCell.set(value);
	}

	private final ObjectProperty<Color> highlightColor = new SimpleObjectProperty<>(Color.BLUE);

	public void setHighlightColor(Color color) {
		this.highlightColor.set(color);
	}

	public Color getHighlightColor() {
		return highlightColor.get();
	}

	public ObjectProperty<Color> highlightColorProperty() {
		return highlightColor;
	}
}
