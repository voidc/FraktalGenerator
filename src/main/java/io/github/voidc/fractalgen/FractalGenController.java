package io.github.voidc.fractalgen;

import io.github.voidc.fractalgen.renderer.LSystemRenderer;
import io.github.voidc.fractalgen.renderer.RecursiveLSytsemRenderer;
import io.github.voidc.fractalgen.renderer.RenderedLSystem;
import javafx.beans.InvalidationListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FractalGenController implements Initializable {
    private GraphicsContext ctx;

    private LSystem lsystem;
    private LSystemRenderer renderer;
    private RenderedLSystem renderedLSystem;
    private double zoom = 1;
    private double minZoom = 1;
    private Point2D translation = new Point2D(0, 0);
    private char newRuleSymbol;

    private static final String NEW_RULE = "<Neue Regel>";

    //CENTER
    @FXML
    private Canvas canvas;
    @FXML
    private Pane canvasPane;

    //LEFT
    @FXML
    private ChoiceBox<LSystem> presetsBox;
    @FXML
    private TextField axiomInput;
    @FXML
    private ListView<String> rules;
    @FXML
    private Label ruleInputLabel;
    @FXML
    private TextField ruleInput;
    @FXML
    private Button ruleInputButton;
    @FXML
    private TextField angleInput;
    @FXML
    private Button renderButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button exportButton;

    //BOTTOM
    @FXML
    private Slider stepSlider;
    @FXML
    private Label stepText;
    @FXML
    private Slider detailSlider;
    @FXML
    private Label detailText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renderer = new RecursiveLSytsemRenderer();
        renderer.getCache().addListener((InvalidationListener) observable -> onCacheChanged());
        ctx = canvas.getGraphicsContext2D();
        loadPresets();

        //LEFT
        presetsBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> loadLSystem(newValue)); //(2)

        axiomInput.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean valid = validateRule(newValue, false);
            axiomInput.setStyle("-fx-text-fill: " + (valid ? "inherit;" : "#FF0000;"));
            renderButton.setDisable(!valid);
            if (valid) {
                lsystem.setAxiom(newValue);
            }
        });

        rules.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            if (newValue.equals(NEW_RULE)) {
                ruleInputLabel.setText(newRuleSymbol + "→");
                ruleInput.setText(newRuleSymbol + "");
                ruleInputButton.setText("Hinzufügen");
            } else {
                ruleInputLabel.setText(newValue.charAt(0) + "→");
                ruleInput.setText(newValue.substring(2));
                ruleInputButton.setText("Ändern");
            }
        });

        ruleInput.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean valid = validateRule(newValue, true);
            ruleInput.setStyle("-fx-text-fill: " + (valid ? "inherit;" : "#FF0000;"));
            ruleInputButton.setDisable(!valid);
            renderButton.setDisable(!valid);
        });

        ruleInputButton.setOnAction(event -> {
            String oldRule = rules.getSelectionModel().getSelectedItem();
            String newRule = ruleInput.getText();
            updateRule(oldRule, newRule);
        });

        angleInput.textProperty().addListener((observable, oldValue, newValue) -> {
            double angle;
            try {
                angle = Double.parseDouble(newValue.replaceAll(",", "."));
            } catch (NumberFormatException e) {
                angle = -1;
            }
            boolean valid = angle >= 0 && angle <= 360;
            angleInput.setStyle("-fx-text-fill: " + (valid ? "inherit;" : "#FF0000;"));
            renderButton.setDisable(!valid);
            if (valid) {
                lsystem.setAngle(Math.toRadians(angle));
            }
        });

        renderButton.setOnAction(event -> renderAsyncAndDraw());

        resetButton.setOnAction(event -> {
            int index = presetsBox.getSelectionModel().getSelectedIndex();
            presetsBox.getItems().set(index, LSystem.presets[index].clone());
            presetsBox.getSelectionModel().select(index);
        });

        exportButton.setOnAction(event -> saveAsImage());

        presetsBox.getSelectionModel().selectFirst(); //load l-system and render (1)

        //BOTTOM
        detailSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != oldValue.intValue())
                onDetailChanged(newValue.intValue());
        });

        stepSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            stepText.setText(newValue.intValue() + "%");
            draw();
        });

        //CENTER
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().addListener(width -> resize());
        canvas.heightProperty().addListener(height -> resize());

        canvas.setOnScroll(event -> {
            zoom *= event.getDeltaY() > 0 ? 1.1 : 0.9;
            zoom = Math.max(minZoom, zoom);
            draw();
        });

        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.MIDDLE)
                resize();
        });
    }

    private void loadLSystem(LSystem ls) {
        lsystem = ls;
        axiomInput.setText(lsystem.getAxiom());

        rules.getItems().clear();
        lsystem.getRules().forEach((symbol, rule) -> {
            rules.getItems().add(symbol + "→" + rule);
        });
        rules.getItems().add(NEW_RULE);
        newRuleSymbol = getNewRuleSymbol();
        rules.getSelectionModel().selectLast();

        angleInput.setText(String.valueOf(Math.round(Math.toDegrees(lsystem.getAngle()))));
        renderAsyncAndDraw(); //(3)
    }

    private void renderAsyncAndDraw() {
        System.out.println("Render!");
        renderer.renderToCache(lsystem); //(4)
    }

    private void onCacheChanged() {
        int size = renderer.getCache().size();
        detailSlider.setMax(size - 1);
        if (size > 0 && size % 2 == 0) {
            detailSlider.setValue(size / 2); //(5)
        }
    }

    private void onDetailChanged(int newValue) {
        detailText.setText(String.valueOf(newValue));
        drawFromCache(newValue); //(6)
    }

    private void drawFromCache(int depth) {
        renderedLSystem = renderer.getCache().get(depth);
        fitBounds();
        draw(); //(7)
    }

    private void draw() {
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ctx.save();
        ctx.translate(translation.getX(), translation.getY());
        ctx.scale(zoom, zoom);
        ctx.setStroke(Color.BLACK);
        ctx.setLineWidth(1 / zoom);
        renderedLSystem.draw(ctx, stepSlider.getValue() / stepSlider.getMax()); //(8)
        ctx.restore();
    }

    private void fitBounds() {
        Rectangle2D bBox = renderedLSystem.boundingBox;
        double zoomX = canvas.getWidth() / bBox.getWidth();
        double zoomY = canvas.getHeight() / bBox.getHeight();
        minZoom = Math.min(zoomX, zoomY);
        zoom = minZoom;

        Point2D start = new Point2D(-bBox.getMinX() * zoom, -bBox.getMinY() * zoom);
        if (zoomX < zoomY) {
            translation = new Point2D(0, (canvas.getHeight() - bBox.getHeight() * zoom) / 2.0).add(start);
        } else {
            translation = new Point2D((canvas.getWidth() - bBox.getWidth() * zoom) / 2.0, 0).add(start);
        }
    }

    private void saveAsImage() {
        SnapshotParameters param = new SnapshotParameters();
        Image snapshot = canvas.snapshot(param, null);

        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle("Als Bild Speichern");
        saveDialog.setInitialDirectory(new File(System.getProperty("user.home")));
        saveDialog.setInitialFileName(lsystem.getName());
        saveDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));

        File saveFile = saveDialog.showSaveDialog(canvas.getScene().getWindow());
        if (saveFile != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPresets() {
        for (LSystem ls : LSystem.presets) {
            presetsBox.getItems().add(ls.clone());
        }
    }

    private boolean validateRule(String rule, boolean allowNewRuleSymbol) {
        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (LSystem.CONSTANTS.indexOf(c) != -1 || (allowNewRuleSymbol && c == newRuleSymbol))
                continue;
            if (!lsystem.getRules().containsKey(c))
                return false;
        }
        return true;
    }

    private void updateRule(String oldRule, String newRule) {
        if (newRule.isEmpty()) {
            if (!oldRule.equals(NEW_RULE)) { //delete
                rules.getItems().remove(rules.getSelectionModel().getSelectedIndex());
                lsystem.getRules().remove(oldRule.charAt(0));
            }
            return;
        }

        char ruleSymbol;
        if (oldRule.equals(NEW_RULE)) { //add rule
            ruleSymbol = newRuleSymbol;
            rules.getItems().add(rules.getItems().size() - 1, ruleSymbol + "→" + newRule);
            rules.getSelectionModel().select(rules.getItems().size() - 2);
            newRuleSymbol = getNewRuleSymbol();
            if (newRuleSymbol == 0) { //no more unused symbols
                rules.getItems().remove(rules.getItems().size() - 1);
            }
        } else { //edit rule
            ruleSymbol = oldRule.charAt(0);
            rules.getItems().set(rules.getSelectionModel().getSelectedIndex(), ruleSymbol + "→" + newRule);
        }
        lsystem.getRules().put(ruleSymbol, ruleInput.getText());
    }

    private char getNewRuleSymbol() {
        char symbol;
        for (symbol = 'A'; symbol <= 'Z'; symbol++) {
            if (!lsystem.getRules().containsKey(symbol) && symbol != newRuleSymbol)
                return symbol;
        }
        return 0;
    }

    private void resize() {
        if (renderedLSystem != null) {
            fitBounds();
            draw();
        }
    }
}
