package me.ialistannen.timgrid.view;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * A Canvas with a base grid
 */
class GridCanvas extends Canvas {

    private StyleableObjectProperty<Paint> gridColor = new StyleableObjectProperty<Paint>(Color.BLACK) {
        @Override
        public Object getBean() {
            return GridCanvas.this;
        }

        @Override
        public String getName() {
            return "grid-color";
        }

        @Override
        public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
            return GRID_COLOR;
        }
    };

    private static final CssMetaData<? extends Styleable, Paint> GRID_COLOR = new CssMetaData<Styleable, Paint>(
            "grid-color",
            new StyleConverter<>()
    ) {
        @Override
        public boolean isSettable(Styleable styleable) {
            return true;
        }

        @Override
        public StyleableProperty<Paint> getStyleableProperty(Styleable styleable) {
            return ((GridCanvas) styleable).gridColor;
        }
    };

    {
        getStyleClass().add("grid-canvas");
    }

    private GridAnchorPane gridAnchorPane;

    /**
     * Creates a new instance of Canvas with the given size.
     *
     * @param gridAnchorPane The parent {@link GridAnchorPane}
     */
    GridCanvas(GridAnchorPane gridAnchorPane) {
        this.gridAnchorPane = gridAnchorPane;

        // just assume both will change...
        widthProperty().addListener((observable, oldValue, newValue) -> redraw());

        widthProperty().bind(gridAnchorPane.prefWidthProperty());
        heightProperty().bind(gridAnchorPane.prefHeightProperty());

        // listen to the first layout change of the grid anchor pane. I found no other way to determine whether the 
        // parent already has all bounds calculated and this node can be drawn 
        ChangeListener<Number> widthChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                redraw();

                // detach. We have our pref width listener for that
                gridAnchorPane.widthProperty().removeListener(this);
            }
        };
        gridAnchorPane.widthProperty().addListener(widthChangeListener);
    }

    /**
     * Redraws this canvas
     */
    void redraw() {
        drawGrid();
    }

    private void drawGrid() {
        GraphicsContext graphics = getGraphicsContext2D();
        graphics.clearRect(0, 0, getWidth(), getHeight());
        graphics.setStroke(gridColor.get());

        double maxDrawHeight = getMaxDrawHeight();
        double maxDrawWidth = getMaxDrawWidth();

        for (int x = 0; x <= getWidth(); x += gridAnchorPane.getGridWidth()) {
            graphics.strokeLine(x, 0, x, maxDrawHeight);
        }

        for (int y = 0; y <= getHeight(); y += gridAnchorPane.getGridHeight()) {
            graphics.strokeLine(0, y, maxDrawWidth, y);
        }
    }

    private double getMaxDrawWidth() {
        for (int i = 0; i <= getWidth(); i += gridAnchorPane.getGridWidth()) {
            if (i + gridAnchorPane.getGridWidth() > getWidth()) {
                return i;
            }
        }
        return getWidth();
    }

    private double getMaxDrawHeight() {
        for (int i = 0; i <= getHeight(); i += gridAnchorPane.getGridHeight()) {
            if (i + gridAnchorPane.getGridHeight() > getHeight()) {
                return i;
            }
        }
        return getHeight();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> classCssMetaData = new ArrayList<>(getClassCssMetaData());
        classCssMetaData.add(GRID_COLOR);
        return classCssMetaData;
    }
}
