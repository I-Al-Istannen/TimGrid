package me.ialistannen.timgrid.view;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import me.ialistannen.timgrid.model.ImageSupplier;
import me.ialistannen.timgrid.util.Util;
import me.ialistannen.timgrid.util.dialog.NumberInputDialog;

/**
 * A AnchorPane displaying a grid
 */
public class GridAnchorPane extends AnchorPane {
    private GridCanvas gridCanvas;
    private int gridHeight, gridWidth;

    {
        getStyleClass().add("grid");
    }

    /**
     * Creates an AnchorPane layout.
     *
     * @param gridHeight The height of the grid
     * @param gridWidth The width of the grid
     */
    public GridAnchorPane(int gridHeight, int gridWidth) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;

        gridCanvas = new GridCanvas(this);

        getChildren().add(gridCanvas);

        setOnDragOver(event -> {
            Object gestureSource = event.getGestureSource();
            if (!(gestureSource instanceof Node)) {
                return;
            }
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

            event.consume();
        });

        setOnDragDropped(event -> {
            Object gestureSource = event.getGestureSource();
            if (!(gestureSource instanceof Node)) {
                return;
            }
            Node node = (Node) event.getGestureSource();

            // handle adding an image specially!
            if (node instanceof ImageSupplier) {
                Image image = ((ImageSupplier) node).getImage();
                node = new ImageView(image);
                getChildren().add(node);
            }

            double offsetX = 0;
            double offsetY = 0;
            if (event.getDragboard().hasString()) {
                String string = event.getDragboard().getString();
                if (string.contains("!-->!")) {
                    String[] split = string.split("!-->!");
                    offsetX = -Double.parseDouble(split[0]);
                    offsetY = -Double.parseDouble(split[1]);
                }
            }

            double y = event.getY() + offsetY;
            double x = event.getX() + offsetX;

            Point2D nearestGridKnot = getNearestGridKnot(x, y);
            node.setTranslateX(nearestGridKnot.getX());
            node.setTranslateY(nearestGridKnot.getY());
        });

        // Make the nodes draggable!
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                for (Node node : c.getAddedSubList()) {
                    node.setOnDragDetected(getDragDetectedHandler());
                    node.setOnContextMenuRequested(getContextMenuRequestedHandler());
                    node.setOnKeyPressed(getKeyPressMoveHandler());
                    node.setOnMouseClicked(event -> node.requestFocus());
                }
            }
        });
    }

    /**
     * @return The handler creating the context menu for each node
     */
    private EventHandler<ContextMenuEvent> getContextMenuRequestedHandler() {
        return contextMenuEvent -> {
            if (!(contextMenuEvent.getSource() instanceof ImageView)) {
                return;
            }
            ImageView node = (ImageView) contextMenuEvent.getSource();

            MenuItem resizeWidthItem = new MenuItem("Resize Width");
            resizeWidthItem.setOnAction(event -> {
                NumberInputDialog<Integer> enterWidth = new NumberInputDialog<>(
                        new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE)
                );
                enterWidth.showAndWait().ifPresent(integer -> {
                    node.setPreserveRatio(false);
                    node.setFitWidth(integer * gridWidth);
                });
            });

            MenuItem resizeHeightItem = new MenuItem("Resize Height");
            resizeHeightItem.setOnAction(event -> {
                NumberInputDialog<Integer> enterHeight = new NumberInputDialog<>(
                        new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE)
                );
                enterHeight.showAndWait().ifPresent(integer -> {
                    node.setPreserveRatio(false);
                    node.setFitHeight(integer * gridHeight);
                });
            });

            MenuItem resizeSquare = new MenuItem("Resize square");
            resizeSquare.setOnAction(event -> {
                NumberInputDialog<Integer> enterSize = new NumberInputDialog<>(
                        new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE)
                );
                enterSize.showAndWait().ifPresent(integer -> {
                    node.setPreserveRatio(false);
                    node.setFitHeight(integer * gridHeight);
                    node.setFitWidth(integer * gridWidth);
                });
            });

            MenuItem resizeKeepRatioWidth = new MenuItem("Resize keep-ratio width");
            resizeKeepRatioWidth.setOnAction(event -> {
                NumberInputDialog<Integer> enterWidth = new NumberInputDialog<>(
                        new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE)
                );
                enterWidth.showAndWait().ifPresent(integer -> {
                    // reset fit height to scale correctly
                    node.setFitHeight(-1);
                    node.setFitWidth(integer * gridWidth);
                    node.setPreserveRatio(true);
                });
            });

            MenuItem resizeKeepRatioHeight = new MenuItem("Resize keep-ratio height");
            resizeKeepRatioHeight.setOnAction(event -> {
                NumberInputDialog<Integer> enterHeight = new NumberInputDialog<>(
                        new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE)
                );
                enterHeight.showAndWait().ifPresent(integer -> {
                    // reset fit width to scale correctly
                    node.setFitWidth(-1);
                    node.setFitHeight(integer * gridHeight);
                    node.setPreserveRatio(true);
                });
            });

            MenuItem dividerResize = new SeparatorMenuItem();

            MenuItem rotate = new MenuItem("Rotate");
            rotate.setOnAction(event -> {
                double current = node.getRotate();
                NumberInputDialog<Double> rotationDialog = new NumberInputDialog<>(
                        new DoubleSpinnerValueFactory(0, Double.MAX_VALUE, current)
                );
                rotationDialog.showAndWait().ifPresent(node::setRotate);
            });

            ContextMenu contextMenu = new ContextMenu(
                    resizeWidthItem, resizeHeightItem,
                    resizeSquare,
                    resizeKeepRatioWidth, resizeKeepRatioHeight,
                    dividerResize,
                    rotate
            );
            contextMenu.show(node, Side.TOP, contextMenuEvent.getX(), contextMenuEvent.getY());
        };
    }

    /**
     * @return The Handler that accepts drag events and supplies the offset as metadata
     */
    private EventHandler<MouseEvent> getDragDetectedHandler() {
        return event -> {
            Object source = event.getSource();
            if (!(source instanceof Node)) {
                return;
            }
            Node node = (Node) source;

            Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);

            WritableImage snapshot = node.snapshot(null, null);

            double xOffset = event.getX();
            double yOffset = event.getY();

            Point2D offset = new Point2D(xOffset, yOffset);

            dragboard.setDragView(snapshot, offset.getX(), offset.getY());

            ClipboardContent clipboardContent = new ClipboardContent();

            clipboardContent.putString(offset.getX() + "!-->!" + offset.getY());
            dragboard.setContent(clipboardContent);

            event.consume();
        };
    }

    /**
     * @return The key press handler that moves the node on the screen, if the user uses WASD
     */
    private EventHandler<? super KeyEvent> getKeyPressMoveHandler() {
        return event -> {
            Object source = event.getSource();
            if (!(source instanceof Node)) {
                return;
            }

            Node node = (Node) source;
            double modX = 0;
            double modY = 0;
            if (event.getCode() == KeyCode.A) {
                modX = -gridWidth;
            }
            else if (event.getCode() == KeyCode.D) {
                modX = gridWidth;
            }
            else if (event.getCode() == KeyCode.W) {
                modY = -gridHeight;
            }
            else if (event.getCode() == KeyCode.S) {
                modY = gridHeight;
            }

            if (event.isControlDown()) {
                modX *= 2;
                modY *= 2;
            }

            Bounds boundsInParent = node.getBoundsInParent();
            Bounds newBounds = new BoundingBox(
                    modX + boundsInParent.getMinX(), modY + boundsInParent.getMinY(),
                    boundsInParent.getWidth(), boundsInParent.getHeight()
            );

            if (!getLayoutBounds().contains(newBounds)) {
                return;
            }

            node.setTranslateX(node.getTranslateX() + modX);
            node.setTranslateY(node.getTranslateY() + modY);
        };
    }

    /**
     * @param width The new width
     * @param height The new height
     */
    public void setGridSize(int width, int height) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setGridSize(width, height));
            return;
        }
        Map<Point2D, Node> nodesWithGridPosition = getNodesWithGridPosition();
        this.gridHeight = height;
        this.gridWidth = width;

        gridCanvas.redraw();

        replaceNodes(nodesWithGridPosition);
    }

    /**
     * @return The grid height
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * @return The grid width
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Sets the size if this pane
     * <p>
     * This method will block until the action is completed by the FX Application thread
     *
     * @param width The width
     * @param height The height
     *
     * @return True if it was resized, false if nodes are placed outside the new size
     */
    public boolean setSize(int width, int height) {
        if (!Platform.isFxApplicationThread()) {
            return Util.runLaterBlocking(() -> setSize(width, height));
        }
        for (Node node : getChildren()) {
            if (node == gridCanvas) {
                continue;
            }
            Bounds bounds = new BoundingBox(0, 0, width, height);
            if (!bounds.contains(node.getBoundsInParent())) {
                return false;
            }
        }
        setPrefSize(width, height);
        gridCanvas.redraw();
        return true;
    }

    private void replaceNodes(Map<Point2D, Node> nodeMap) {
        for (Entry<Point2D, Node> entry : nodeMap.entrySet()) {
            entry.getValue().setTranslateX(entry.getKey().getX() * gridWidth);
            entry.getValue().setTranslateY(entry.getKey().getY() * gridHeight);
        }
    }

    private Map<Point2D, Node> getNodesWithGridPosition() {
        return getChildren().stream()
                .filter(node -> node != gridCanvas)
                .collect(Collectors.toMap(
                        node -> {
                            double x = roundToMultiple(node.getTranslateX(), gridWidth);
                            double y = roundToMultiple(node.getTranslateY(), gridHeight);
                            return new Point2D(x / gridWidth, y / gridHeight);
                        },
                        Function.identity()
                ));
    }

    private Point2D getNearestGridKnot(double x, double y) {
        double roundedX = roundToMultiple(x, gridWidth);
        double roundedY = roundToMultiple(y, gridHeight);

        return new Point2D(roundedX, roundedY);
    }

    private int roundToMultiple(double number, int multiple) {
        return (int) (multiple * Math.round(number / multiple));
    }
}
