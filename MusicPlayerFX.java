import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;
public class MusicPlayerFX extends Application {
        private static final String BG_MAIN = "#09090B";
    private static final String BG_PANEL = "#18181B";
    private static final String BG_CARD = "#18181B";
    private static final String BG_INPUT = "#27272A";
    private static final String BG_ITEM = "transparent";
    private static final String BG_ITEM_HOVER = "#27272A";
    private static final String BG_ITEM_SELECTED = "#3F3F46";
    private static final String BG_BOTTOM = "#09090B";
    private static final String TEXT_PRIMARY = "#FAFAFA";
    private static final String TEXT_SECONDARY = "#A1A1AA";
    private static final String TEXT_MUTED = "#71717A";
    private static final String ACCENT_COLOR = "#8B5CF6";
    private static final String BORDER_COLOR = "#27272A";
    private static final String BORDER_LIGHT = "#3F3F46";
    private static final String ACCENT_HIGHLIGHT = "#A78BFA";
    private static final String PROGRESS_BG = "#27272A";
    private static final String PROGRESS_FG = "#8B5CF6";
        private Label songNameLabel;
    private Label nowPlayingSmallLabel;
    private StackPane albumArtLarge;
    private StackPane albumArtSmall;
    private Label albumArtEmoji;
    private Label albumArtSmallEmoji;
    private ImageView albumImageView;
    private ImageView albumSmallImageView;
    private Button playPauseButton;
    private Slider progressSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private HBox spectrumBox;
    private Rectangle[] spectrumBars;
    private VBox fileListContainer;
    private Label folderPathLabel;
    private Label statusLabel;
    private MediaPlayer mediaPlayer;
    private File currentFile;
    private boolean isPlaying = false;
    private List<File> musicFiles = new ArrayList<>();
    private int currentIndex = -1;
    private boolean shuffleOn = false;
        private Clip audioClip;
    private Player jlayerPlayer;
    private boolean usingFallback = false;
    private Thread progressThread;
    private boolean repeatOn = false;
        private static final String SLIDER_CSS = ".slider { -fx-padding: 0; }" +
            ".slider .track { -fx-background-color: #27272A; -fx-background-radius: 4; -fx-pref-height: 6; }" +
            ".slider .thumb { -fx-background-color: #8B5CF6; -fx-pref-width: 14; -fx-pref-height: 14; -fx-background-radius: 7; -fx-effect: dropshadow(three-pass-box, rgba(139, 92, 246, 0.5), 8, 0, 0, 0); }" +
            ".slider:hover .thumb { -fx-background-color: #A78BFA; -fx-pref-width: 16; -fx-pref-height: 16; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(167, 139, 250, 0.8), 10, 0, 0, 0); }"
            +
            ".slider .colored-track { -fx-background-color: linear-gradient(to right, #6D28D9, #8B5CF6); -fx-background-radius: 4; }" +
            ".slider:hover .colored-track { -fx-background-color: linear-gradient(to right, #7C3AED, #A78BFA); -fx-background-radius: 4; }";
    private static final String SCROLLPANE_CSS = ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }"
            +
            ".scroll-pane .viewport { -fx-background-color: transparent; }" +
            ".scroll-bar { -fx-background-color: transparent; -fx-pref-width: 6; }" +
            ".scroll-bar .thumb { -fx-background-color: #FFFFFF30; -fx-background-radius: 3; }" +
            ".scroll-bar .track { -fx-background-color: transparent; }" +
            ".scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-pref-height: 0; -fx-pref-width: 0; }";
    private javafx.scene.control.TextArea debugTextArea;
    private javafx.stage.Stage debugStage;
    private void setupDebugConsole() {
        debugTextArea = new javafx.scene.control.TextArea();
        debugTextArea.setEditable(false);
        debugTextArea.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #00FF00; -fx-font-family: monospace;");
        java.io.OutputStream out = new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                Platform.runLater(() -> {
                    debugTextArea.appendText(String.valueOf((char) b));
                });
            }
            @Override
            public void write(byte[] b, int off, int len) throws java.io.IOException {
                String str = new String(b, off, len);
                Platform.runLater(() -> {
                    debugTextArea.appendText(str);
                });
            }
        };
        System.setOut(new java.io.PrintStream(out, true));
        System.setErr(new java.io.PrintStream(out, true));
        debugStage = new javafx.stage.Stage();
        debugStage.setTitle("Terminal Debug");
        javafx.scene.Scene debugScene = new javafx.scene.Scene(new javafx.scene.layout.StackPane(debugTextArea), 600, 400);
        debugStage.setScene(debugScene);
        System.out.println("--- Terminal Debug Captured ---");
    }
    @Override
    public void start(Stage primaryStage) {
        setupDebugConsole();
        primaryStage.setTitle("Music Player");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_MAIN + ";");
                HBox mainPanel = new HBox(0);
        mainPanel.setPadding(new Insets(12, 12, 0, 12));
        HBox.setHgrow(mainPanel, Priority.ALWAYS);
                VBox leftPanel = buildLeftPanel(primaryStage);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
                VBox rightPanel = buildRightPanel();
        rightPanel.setPrefWidth(300);
        rightPanel.setMinWidth(260);
        mainPanel.getChildren().addAll(leftPanel, rightPanel);
        root.setCenter(mainPanel);
        root.setBottom(buildBottomBar(primaryStage));
        Scene scene = new Scene(root, 950, 600);
        scene.getStylesheets().clear();
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                if (!(scene.getFocusOwner() instanceof javafx.scene.control.TextField)) {
                    playPauseButton.fire();
                    e.consume();
                }
            }
        });
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
        javafx.animation.Timeline fallbackSpectrumTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(80), e -> {
                if (isPlaying && usingFallback) {
                    for (Rectangle bar : spectrumBars) {
                        double h = 4 + Math.random() * 30;
                        bar.setHeight(h);
                    }
                } else if (!isPlaying || (!usingFallback && mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED)) {
                    for (Rectangle bar : spectrumBars) {
                        if (bar.getHeight() > 4) bar.setHeight(4);
                    }
                }
            })
        );
        fallbackSpectrumTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        fallbackSpectrumTimeline.play();
                loadDownloadsFolder();
    }
                private VBox buildLeftPanel(Stage stage) {
        VBox panel = new VBox(0);
        panel.setStyle(
                "-fx-background-color: " + BG_PANEL + ";" +
                        "-fx-background-radius: 12 0 0 0;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 12 0 0 0;" +
                        "-fx-border-width: 1;");
        panel.setPadding(new Insets(0));
                HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setSpacing(16);
        Label folderLabel = new Label("Folder Music");
        folderLabel.setTextFill(Color.web(TEXT_PRIMARY));
        folderLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Button inputFileBtn = new Button("Input File Music");
        inputFileBtn.setStyle(
                "-fx-background-color: " + BG_INPUT + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + BORDER_LIGHT + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 8 24 8 24;" +
                        "-fx-cursor: hand;");
        inputFileBtn.setCursor(Cursor.HAND);
        inputFileBtn.setOnMouseEntered(e -> inputFileBtn.setStyle(
                "-fx-background-color: " + BG_ITEM_HOVER + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + ACCENT_HIGHLIGHT + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 8 24 8 24;" +
                        "-fx-cursor: hand;"));
        inputFileBtn.setOnMouseExited(e -> inputFileBtn.setStyle(
                "-fx-background-color: " + BG_INPUT + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + BORDER_LIGHT + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 8 24 8 24;" +
                        "-fx-cursor: hand;"));
        inputFileBtn.setOnAction(e -> openFiles(stage));
        Button debugBtn = new Button("🛠 Debug");
        debugBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;");
        debugBtn.setOnMouseEntered(e -> debugBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"));
        debugBtn.setOnMouseExited(e -> debugBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"));
        debugBtn.setOnAction(e -> {
            if (debugStage != null) {
                if (debugStage.isShowing()) {
                    debugStage.hide();
                } else {
                    debugStage.show();
                    debugStage.toFront();
                }
            }
        });
        header.getChildren().addAll(folderLabel, headerSpacer, debugBtn, inputFileBtn);
                folderPathLabel = new Label("");
        folderPathLabel.setTextFill(Color.web(TEXT_MUTED));
        folderPathLabel.setFont(Font.font("System", 11));
        folderPathLabel.setPadding(new Insets(0, 20, 8, 20));
                fileListContainer = new VBox(6);
        fileListContainer.setPadding(new Insets(8, 12, 12, 12));
                Label placeholder = new Label("Belum ada file musik ditambahkan.");
        placeholder.setTextFill(Color.web(TEXT_MUTED));
        placeholder.setFont(Font.font("System", 13));
        placeholder.setPadding(new Insets(20));
        fileListContainer.getChildren().add(placeholder);
        ScrollPane scrollPane = new ScrollPane(fileListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        addInlineCSS(scrollPane, SCROLLPANE_CSS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
                HBox ytSection = new HBox(10);
        ytSection.setPadding(new Insets(0, 20, 10, 20));
        ytSection.setAlignment(Pos.CENTER_LEFT);
        TextField ytInput = new TextField();
        ytInput.setPromptText("Paste YouTube URL here...");
        ytInput.setStyle(
                "-fx-background-color: " + BG_INPUT + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-border-color: " + BORDER_LIGHT + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 12 6 12;");
        HBox.setHgrow(ytInput, Priority.ALWAYS);
        Button ytDownloadBtn = new Button("Download");
        ytDownloadBtn.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;");
        ytDownloadBtn.setOnAction(e -> {
            String url = ytInput.getText().trim();
            if (!url.isEmpty()) {
                ytInput.clear();
                downloadFromYoutube(url);
            }
        });
        ytSection.getChildren().addAll(ytInput, ytDownloadBtn);
        panel.getChildren().addAll(header, ytSection, folderPathLabel, scrollPane);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }
                private VBox buildRightPanel() {
        VBox panel = new VBox(0);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-background-radius: 0 12 0 0;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 0 12 0 0;" +
                        "-fx-border-width: 1 1 1 0;");
        panel.setPadding(new Insets(30, 20, 20, 20));
                albumArtLarge = new StackPane();
        albumArtLarge.setPrefSize(220, 220);
        albumArtLarge.setMinSize(220, 220);
        albumArtLarge.setMaxSize(220, 220);
        albumArtLarge.setStyle(
                "-fx-background-color: " + TEXT_PRIMARY + ";" +
                        "-fx-background-radius: 8;");
        albumArtLarge.setEffect(new DropShadow(25, Color.rgb(0, 0, 0, 0.6)));
        albumArtEmoji = new Label("🎵");
        albumArtEmoji.setFont(Font.font("System", 72));
        albumImageView = new ImageView();
        albumImageView.setFitWidth(220);
        albumImageView.setFitHeight(220);
        albumImageView.setPreserveRatio(false);
        albumImageView.setSmooth(true);
                Rectangle clip = new Rectangle(220, 220);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        albumImageView.setClip(clip);
        albumImageView.setVisible(false);
        albumArtLarge.getChildren().addAll(albumArtEmoji, albumImageView);
                songNameLabel = new Label("NAMA LAGU");
        songNameLabel.setTextFill(Color.web(TEXT_PRIMARY));
        songNameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        songNameLabel.setWrapText(true);
        songNameLabel.setAlignment(Pos.CENTER);
        songNameLabel.setMaxWidth(250);
        songNameLabel.setPadding(new Insets(24, 0, 0, 0));
                statusLabel = new Label("");
        statusLabel.setTextFill(Color.web(TEXT_MUTED));
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setPadding(new Insets(6, 0, 0, 0));
                spectrumBox = new HBox(4);
        spectrumBox.setAlignment(Pos.BOTTOM_CENTER);
        spectrumBox.setPrefHeight(40);
        spectrumBox.setMinHeight(40);
        spectrumBox.setMaxHeight(40);
        spectrumBox.setPadding(new Insets(10, 0, 0, 0));
        spectrumBars = new Rectangle[20];
        for (int i = 0; i < spectrumBars.length; i++) {
            spectrumBars[i] = new Rectangle(6, 4);
            spectrumBars[i].setFill(Color.web(ACCENT_COLOR));
            spectrumBars[i].setArcWidth(4);
            spectrumBars[i].setArcHeight(4);
            spectrumBox.getChildren().add(spectrumBars[i]);
        }
        panel.getChildren().addAll(albumArtLarge, songNameLabel, statusLabel, spectrumBox);
        return panel;
    }
                private HBox buildBottomBar(Stage stage) {
        HBox bottomBar = new HBox(0);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle(
                "-fx-background-color: " + BG_BOTTOM + ";" +
                        "-fx-border-color: " + BORDER_COLOR + " transparent transparent transparent;" +
                        "-fx-border-width: 1 0 0 0;");
        bottomBar.setPadding(new Insets(10, 16, 10, 16));
        bottomBar.setPrefHeight(70);
        bottomBar.setMaxHeight(70);
                albumArtSmall = new StackPane();
        albumArtSmall.setPrefSize(48, 48);
        albumArtSmall.setMinSize(48, 48);
        albumArtSmall.setMaxSize(48, 48);
        albumArtSmall.setStyle(
                "-fx-background-color: " + BG_ITEM + ";" +
                        "-fx-background-radius: 6;");
        albumArtSmallEmoji = new Label("🎵");
        albumArtSmallEmoji.setFont(Font.font("System", 18));
        albumSmallImageView = new ImageView();
        albumSmallImageView.setFitWidth(48);
        albumSmallImageView.setFitHeight(48);
        albumSmallImageView.setPreserveRatio(false);
        albumSmallImageView.setSmooth(true);
        Rectangle smallClip = new Rectangle(48, 48);
        smallClip.setArcWidth(12);
        smallClip.setArcHeight(12);
        albumSmallImageView.setClip(smallClip);
        albumSmallImageView.setVisible(false);
        albumArtSmall.getChildren().addAll(albumArtSmallEmoji, albumSmallImageView);
                nowPlayingSmallLabel = new Label("");
        nowPlayingSmallLabel.setTextFill(Color.web(TEXT_SECONDARY));
        nowPlayingSmallLabel.setFont(Font.font("System", 12));
        nowPlayingSmallLabel.setPadding(new Insets(0, 0, 0, 12));
        nowPlayingSmallLabel.setMaxWidth(150);
                Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        Button shuffleButton = createControlButton("🔀", false);
        Button prevButton = createControlButton("⏮", false);
        playPauseButton = createPlayButton();
        Button nextButton = createControlButton("⏭", false);
        Button repeatButton = createControlButton("🔁", false);
        shuffleButton.setOnAction(e -> {
            shuffleOn = !shuffleOn;
            if (shuffleOn && repeatOn) {
                repeatOn = false;
                repeatButton.setUserData(TEXT_MUTED);
                repeatButton.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + ";" +
                                "-fx-font-size: 16px; -fx-padding: 4;");
            }
            String color = shuffleOn ? ACCENT_COLOR : TEXT_MUTED;
            shuffleButton.setUserData(color);
            shuffleButton.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + color + ";" +
                            "-fx-font-size: 16px; -fx-padding: 4;");
        });
        repeatButton.setOnAction(e -> {
            repeatOn = !repeatOn;
            if (repeatOn && shuffleOn) {
                shuffleOn = false;
                shuffleButton.setUserData(TEXT_MUTED);
                shuffleButton.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + ";" +
                                "-fx-font-size: 16px; -fx-padding: 4;");
            }
            String color = repeatOn ? ACCENT_COLOR : TEXT_MUTED;
            repeatButton.setUserData(color);
            repeatButton.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + color + ";" +
                            "-fx-font-size: 16px; -fx-padding: 4;");
        });
        prevButton.setOnAction(e -> playPrevious());
        playPauseButton.setOnAction(e -> {
            if (mediaPlayer == null && !musicFiles.isEmpty()) {
                currentIndex = 0;
                loadAndPlay(musicFiles.get(0));
            } else if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });
        nextButton.setOnAction(e -> playNext());
        Button deleteButton = createControlButton("🗑", false);
        deleteButton.setOnAction(e -> deleteCurrentSong());
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #EF4444;" +                         "-fx-font-size: 16px; -fx-padding: 4;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 16px; -fx-padding: 4;"));
        controls.getChildren().addAll(shuffleButton, prevButton, playPauseButton, nextButton, repeatButton, deleteButton);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
                VBox controlsWithProgress = new VBox(6);
        controlsWithProgress.setAlignment(Pos.CENTER);
        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(400);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        addInlineCSS(progressSlider, SLIDER_CSS);
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setTextFill(Color.web(TEXT_MUTED));
        currentTimeLabel.setFont(Font.font("System", 11));
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setTextFill(Color.web(TEXT_MUTED));
        totalTimeLabel.setFont(Font.font("System", 11));
        HBox sliderBox = new HBox(10);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> currentTimeLabel.setText(formatTime(newVal.doubleValue())));
        });
        controlsWithProgress.getChildren().addAll(controls, sliderBox);
        bottomBar.getChildren().addAll(albumArtSmall, nowPlayingSmallLabel, leftSpacer, controlsWithProgress,
                rightSpacer);
        return bottomBar;
    }
                private Button createControlButton(String text, boolean active) {
        Button button = new Button(text);
        String color = active ? ACCENT_COLOR : TEXT_MUTED;
        button.setUserData(color);
        button.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + color + ";" +
                        "-fx-font-size: 16px; -fx-padding: 4;");
        button.setCursor(Cursor.HAND);
        button.setOnMouseEntered(e -> {
            if (!button.isDisabled())
                button.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: " + TEXT_PRIMARY + ";" +
                                "-fx-font-size: 16px; -fx-padding: 4;");
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisabled()) {
                Object c = button.getUserData();
                String outColor = c != null ? c.toString() : TEXT_MUTED;
                button.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: " + outColor + ";" +
                                "-fx-font-size: 16px; -fx-padding: 4;");
            }
        });
        return button;
    }
    private Button createPlayButton() {
        Button btn = new Button("▶");
        btn.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 20px; -fx-padding: 8 18; -fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(139, 92, 246, 0.4), 10, 0, 0, 4);");
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + ACCENT_HIGHLIGHT + "; -fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 20px; -fx-padding: 8 18; -fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(167, 139, 250, 0.6), 12, 0, 0, 4);"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 20px; -fx-padding: 8 18; -fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(139, 92, 246, 0.4), 10, 0, 0, 4);"));
        return btn;
    }
    private void loadDownloadsFolder() {
        File downloadsDir = new File("downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            File[] files = downloadsDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                        lower.endsWith(".aac") || lower.endsWith(".m4a") || lower.endsWith(".aiff");
            });
            if (files != null && files.length > 0) {
                musicFiles.clear();
                for (File file : files) {
                    musicFiles.add(file);
                }
                folderPathLabel.setText("Folder: " + downloadsDir.getAbsolutePath());
                updateFileList();
                                currentIndex = 0;
                loadAndPlay(musicFiles.get(currentIndex));
                statusLabel.setText("Memuat lagu dari folder downloads...");
            }
        }
    }
                private void openFiles(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Musik");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac", "*.m4a", "*.aiff"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File f : selectedFiles) {
                if (!musicFiles.contains(f)) {
                    musicFiles.add(f);
                }
            }
            updateFileList();
            if (currentFile == null) {
                folderPathLabel.setText("📂 " + selectedFiles.get(0).getParent());
            }
        }
    }
    private void updateFileList() {
        fileListContainer.getChildren().clear();
        for (int i = 0; i < musicFiles.size(); i++) {
            File f = musicFiles.get(i);
            HBox item = createFileItem(f, i);
            fileListContainer.getChildren().add(item);
        }
    }
    private HBox createFileItem(File file, int index) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setCursor(Cursor.HAND);
        boolean isCurrentlyPlaying = (currentFile != null && currentFile.equals(file));
        item.setStyle(
                "-fx-background-color: " + (isCurrentlyPlaying ? BG_ITEM_SELECTED : BG_ITEM) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: " + (isCurrentlyPlaying ? BORDER_LIGHT : "transparent") + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;");
                Label icon = new Label(isCurrentlyPlaying ? "▶" : "🎵");
        icon.setFont(Font.font("System", 14));
        icon.setTextFill(Color.web(isCurrentlyPlaying ? ACCENT_HIGHLIGHT : TEXT_MUTED));
        icon.setMinWidth(20);
                String name = file.getName().replaceFirst("[.][^.]+$", "");
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(isCurrentlyPlaying ? TEXT_PRIMARY : TEXT_SECONDARY));
        nameLabel.setFont(Font.font("System", isCurrentlyPlaying ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
                String ext = file.getName().contains(".")
                ? file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase()
                : "";
        Label extLabel = new Label(ext);
        extLabel.setTextFill(Color.web(TEXT_MUTED));
        extLabel.setFont(Font.font("System", 10));
        extLabel.setStyle(
                "-fx-background-color: " + BG_MAIN + ";" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 2 6 2 6;");
        item.getChildren().addAll(icon, nameLabel, extLabel);
                item.setOnMouseEntered(e -> {
            if (!(currentFile != null && currentFile.equals(file))) {
                item.setStyle(
                        "-fx-background-color: " + BG_ITEM_HOVER + ";" +
                                "-fx-background-radius: 8;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-radius: 8;" +
                                "-fx-border-width: 1;");
            }
        });
        item.setOnMouseExited(e -> {
            boolean isCurrent = (currentFile != null && currentFile.equals(file));
            item.setStyle(
                    "-fx-background-color: " + (isCurrent ? BG_ITEM_SELECTED : BG_ITEM) + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: " + (isCurrent ? BORDER_LIGHT : "transparent") + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 1;");
        });
                item.setOnMouseClicked(e -> {
            currentIndex = index;
            loadAndPlay(file);
        });
        return item;
    }
                private void stopFallbackAudio() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
        if (jlayerPlayer != null) {
            jlayerPlayer.close();
            jlayerPlayer = null;
        }
        if (progressThread != null) {
            progressThread.interrupt();
            progressThread = null;
        }
        usingFallback = false;
    }
    private void loadAndPlay(File file) {
        currentFile = file;
                if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        stopFallbackAudio();
        String name = file.getName().replaceFirst("[.][^.]+$", "");
        songNameLabel.setText(name);
        nowPlayingSmallLabel.setText(name);
        statusLabel.setText("Loading...");
        updateFileList();
        System.out.println("Loading file: " + file.getAbsolutePath());
                try {
            String uriString = file.toURI().toASCIIString();
            System.out.println("Trying JavaFX MediaPlayer with URI: " + uriString);
            Media media = new Media(uriString);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(1.0);
            usingFallback = false;
                        mediaPlayer.setOnError(() -> {
                System.err.println("JavaFX MediaPlayer failed, trying fallback...");
                Platform.runLater(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.dispose();
                        mediaPlayer = null;
                    }
                    loadAndPlayFallback(file);
                });
            });
            media.setOnError(() -> {
                System.err.println("JavaFX Media failed, trying fallback...");
                Platform.runLater(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.dispose();
                        mediaPlayer = null;
                    }
                    loadAndPlayFallback(file);
                });
            });
            mediaPlayer.setOnReady(() -> {
                double totalSec = mediaPlayer.getMedia().getDuration().toSeconds();
                progressSlider.setMax(totalSec);
                progressSlider.setValue(0);
                Platform.runLater(() -> totalTimeLabel.setText(formatTime(totalSec)));
                mediaPlayer.setAudioSpectrumNumBands(20);
                mediaPlayer.setAudioSpectrumInterval(0.05);                 mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                    for (int i = 0; i < spectrumBars.length; i++) {
                        if (i < magnitudes.length) {
                            double mag = magnitudes[i] + 60;                             if (mag < 0) mag = 0;
                            double height = mag * 0.6;                             if (height < 4) height = 4;
                            spectrumBars[i].setHeight(height);
                        }
                    }
                });
                mediaPlayer.play();
                isPlaying = true;
                playPauseButton.setText("⏸");
                statusLabel.setText("▶ Now Playing");
                System.out.println("Playing via JavaFX MediaPlayer");
                ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
                st.setToX(1.03);
                st.setToY(1.03);
                st.play();
                loadAlbumArt(media);
            });
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    Platform.runLater(() -> progressSlider.setValue(newTime.toSeconds()));
                }
            });
            progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging && mediaPlayer != null && !usingFallback) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
            });
            progressSlider.setOnMousePressed(e -> {
                if (mediaPlayer != null && !usingFallback) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                } else if (usingFallback && audioClip != null) {
                    long microseconds = (long) (progressSlider.getValue() * 1_000_000);
                    audioClip.setMicrosecondPosition(microseconds);
                }
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                if (repeatOn) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    playNext();
                }
            });
        } catch (Exception ex) {
            System.err.println("JavaFX MediaPlayer exception: " + ex.getMessage());
            loadAndPlayFallback(file);
        }
    }
    private void loadAndPlayFallback(File file) {
        System.out.println("Using fallback for: " + file.getName());
        stopFallbackAudio();
        usingFallback = true;
                if (file.getName().toLowerCase().endsWith(".mp3")) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                jlayerPlayer = new Player(bis);
                                                double totalSec = file.length() / (128.0 * 1024 / 8);
                progressSlider.setMax(totalSec);
                progressSlider.setValue(0);
                Platform.runLater(() -> totalTimeLabel.setText(formatTime(totalSec)));
                isPlaying = true;
                playPauseButton.setText("⏸");
                statusLabel.setText("▶ Now Playing (JLayer)");
                ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
                st.setToX(1.03);
                st.setToY(1.03);
                st.play();
                final long startTime = System.currentTimeMillis();
                progressThread = new Thread(() -> {
                    try {
                                                Thread updater = new Thread(() -> {
                            while (jlayerPlayer != null && !Thread.interrupted()) {
                                try {
                                    int posMillis = jlayerPlayer.getPosition();
                                    double elapsed = posMillis / 1000.0;
                                    Platform.runLater(() -> {
                                        if (!progressSlider.isValueChanging() && elapsed <= progressSlider.getMax()) {
                                            progressSlider.setValue(elapsed);
                                        }
                                    });
                                    Thread.sleep(200);
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        });
                        updater.setDaemon(true);
                        updater.start();
                                                jlayerPlayer.play();
                                                if (jlayerPlayer != null && jlayerPlayer.isComplete()) {
                            double finalElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                            Platform.runLater(() -> {
                                if (finalElapsed < 1.0) {
                                                                        statusLabel.setText("Error: File MP3 rusak / bukan MP3 asli!");
                                    System.err.println("JLayer aborted: File is not a valid MP3.");
                                } else if (repeatOn) {
                                    loadAndPlayFallback(file);
                                } else {
                                    playNext();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                progressThread.setDaemon(true);
                progressThread.start();
                return;
            } catch (Exception e) {
                System.err.println("JLayer MP3 fallback failed: " + e.getMessage());
            }
        }
                AudioInputStream audioIn = null;
        AudioInputStream decodedStream = null;
        try {
            audioIn = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = audioIn.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            if (AudioSystem.isConversionSupported(decodedFormat, baseFormat)) {
                decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioIn);
            } else {
                decodedStream = audioIn;
            }
            audioClip = AudioSystem.getClip();
            audioClip.open(decodedStream);
            double totalSec = audioClip.getMicrosecondLength() / 1_000_000.0;
            progressSlider.setMax(totalSec);
            progressSlider.setValue(0);
            Platform.runLater(() -> totalTimeLabel.setText(formatTime(totalSec)));
            audioClip.start();
            isPlaying = true;
            playPauseButton.setText("⏸");
            statusLabel.setText("▶ Now Playing");
            ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
            progressThread = new Thread(() -> {
                while (audioClip != null && audioClip.isRunning() && !Thread.interrupted()) {
                    double currentSec = audioClip.getMicrosecondPosition() / 1_000_000.0;
                    Platform.runLater(() -> {
                        if (!progressSlider.isValueChanging()) {
                            progressSlider.setValue(currentSec);
                        }
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (audioClip != null && !audioClip.isRunning()) {
                    Platform.runLater(() -> {
                        if (repeatOn) {
                            audioClip.setMicrosecondPosition(0);
                            audioClip.start();
                        } else {
                            playNext();
                        }
                    });
                }
            });
            progressThread.setDaemon(true);
            progressThread.start();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            System.err.println("Fallback error: " + e.getMessage());
            closeStreamSilently(audioIn);
            closeStreamSilently(decodedStream);
        }
    }
    private void downloadFromYoutube(String url) {
        statusLabel.setText("Downloading YT: Menyiapkan...");
        System.out.println("Starting download for: " + url);
        Thread dlThread = new Thread(() -> {
            try {
                File downloadsDir = new File("downloads");
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }
                                                long timeBefore = System.currentTimeMillis();
                ProcessBuilder pb = new ProcessBuilder(
                        "bin\\yt-dlp.exe",
                        "-x",
                        "--audio-format", "mp3",
                        "-o", "downloads\\%(title)s.%(ext)s",
                        url);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[yt-dlp] " + line);
                    final String logLine = line;
                    if (logLine.contains("[download]") && logLine.contains("%")) {
                        Platform.runLater(() -> statusLabel.setText(
                                "Downloading: " + logLine.substring(logLine.indexOf("[download]") + 10).trim()));
                    } else if (logLine.contains("Extracting audio")) {
                        Platform.runLater(() -> statusLabel.setText("Converting to MP3 (FFmpeg)..."));
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Download complete!");
                    Platform.runLater(() -> statusLabel.setText("Download Selesai!"));
                                        File[] files = downloadsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
                    if (files != null && files.length > 0) {
                        File newestFile = files[0];
                        for (File f : files) {
                            if (f.lastModified() > newestFile.lastModified()) {
                                newestFile = f;
                            }
                        }
                                                if (newestFile.lastModified() > timeBefore - 10000) {
                            final File toPlay = newestFile;
                            Platform.runLater(() -> {
                                if (!musicFiles.contains(toPlay)) {
                                    musicFiles.add(toPlay);
                                }
                                loadAndPlay(toPlay);
                            });
                        }
                    }
                } else {
                    System.err.println("yt-dlp failed with exit code: " + exitCode);
                    Platform.runLater(() -> statusLabel.setText("Error: Download Gagal!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        });
        dlThread.setDaemon(true);
        dlThread.start();
    }
    private void closeStreamSilently(AudioInputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                            }
        }
    }
    private void loadAlbumArt(Media media) {
                Image image = null;
        if (media.getMetadata() != null) {
            media.getMetadata().addListener((javafx.collections.MapChangeListener<String, Object>) change -> {
                if (change.wasAdded() && "image".equals(change.getKey())) {
                    Platform.runLater(() -> {
                        Image img = (Image) change.getValueAdded();
                        if (img != null) {
                            albumImageView.setImage(img);
                            albumImageView.setVisible(true);
                            albumArtEmoji.setVisible(false);
                            albumSmallImageView.setImage(img);
                            albumSmallImageView.setVisible(true);
                            albumArtSmallEmoji.setVisible(false);
                        }
                    });
                }
            });
        }
                if (currentFile != null) {
            File parentDir = currentFile.getParentFile();
            String[] artNames = { "cover.jpg", "cover.png", "folder.jpg", "folder.png", "album.jpg", "album.png" };
            for (String artName : artNames) {
                File artFile = new File(parentDir, artName);
                if (artFile.exists()) {
                    try {
                        Image coverImage = new Image(artFile.toURI().toString(), 220, 220, false, true);
                        albumImageView.setImage(coverImage);
                        albumImageView.setVisible(true);
                        albumArtEmoji.setVisible(false);
                        Image coverSmall = new Image(artFile.toURI().toString(), 48, 48, false, true);
                        albumSmallImageView.setImage(coverSmall);
                        albumSmallImageView.setVisible(true);
                        albumArtSmallEmoji.setVisible(false);
                        return;
                    } catch (Exception e) {
                                            }
                }
            }
        }
                albumImageView.setVisible(false);
        albumArtEmoji.setVisible(true);
        albumSmallImageView.setVisible(false);
        albumArtSmallEmoji.setVisible(true);
    }
                private void playNext() {
        if (musicFiles.isEmpty())
            return;
        if (shuffleOn) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < musicFiles.size(); i++) {
                if (i != currentIndex)
                    indices.add(i);
            }
            if (!indices.isEmpty()) {
                Collections.shuffle(indices);
                currentIndex = indices.get(0);
            }
        } else {
            currentIndex = (currentIndex + 1) % musicFiles.size();
        }
        loadAndPlay(musicFiles.get(currentIndex));
    }
    private void playPrevious() {
        if (musicFiles.isEmpty())
            return;
        if (shuffleOn) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < musicFiles.size(); i++) {
                if (i != currentIndex)
                    indices.add(i);
            }
            if (!indices.isEmpty()) {
                Collections.shuffle(indices);
                currentIndex = indices.get(0);
            }
        } else {
            currentIndex = (currentIndex - 1 + musicFiles.size()) % musicFiles.size();
        }
        loadAndPlay(musicFiles.get(currentIndex));
    }
                private void playAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            playPauseButton.setText("⏸");
            statusLabel.setText("▶ Now Playing");
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        }
    }
    private void pauseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseButton.setText("▶");
            statusLabel.setText("⏸ Paused");
            ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            for (Rectangle bar : spectrumBars) {
                bar.setHeight(4);
            }
        }
    }
    private void deleteCurrentSong() {
        if (currentFile == null) return;
        File fileToDelete = currentFile;
        int indexToDelete = currentIndex;
                if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (audioClip != null) {
            audioClip.stop();
            audioClip = null;
        }
        if (jlayerPlayer != null) {
            jlayerPlayer.close();
            jlayerPlayer = null;
            usingFallback = false;
        }
        isPlaying = false;
                try {
            boolean deleted = false;
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.MOVE_TO_TRASH)) {
                deleted = java.awt.Desktop.getDesktop().moveToTrash(fileToDelete);
            }
            if (!deleted && fileToDelete.exists()) {
                                deleted = fileToDelete.delete();
            }
            if (deleted || !fileToDelete.exists()) {
                Platform.runLater(() -> {
                    musicFiles.remove(fileToDelete);
                    statusLabel.setText("🗑 File dipindah ke Trash");
                    if (musicFiles.isEmpty()) {
                        songNameLabel.setText("NAMA LAGU");
                        nowPlayingSmallLabel.setText("Belum ada lagu");
                        progressSlider.setValue(0);
                        currentTimeLabel.setText("0:00");
                        totalTimeLabel.setText("0:00");
                        albumImageView.setVisible(false);
                        albumArtEmoji.setVisible(true);
                        playPauseButton.setText("▶");
                        currentFile = null;
                    } else {
                        if (indexToDelete >= musicFiles.size()) {
                            currentIndex = 0;
                        } else {
                            currentIndex = indexToDelete; 
                        }
                        loadAndPlay(musicFiles.get(currentIndex));
                    }
                    updateFileList();
                });
            } else {
                Platform.runLater(() -> statusLabel.setText("❌ Gagal menghapus (mungkin terkunci)"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("❌ Error menghapus file"));
        }
    }
    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            playPauseButton.setText("▶");
            progressSlider.setValue(0);
            statusLabel.setText("⏹ Stopped");
            ScaleTransition st = new ScaleTransition(Duration.millis(200), albumArtLarge);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
    }
                private void updateUIState(String status) {
        if (currentFile != null) {
            String name = currentFile.getName().replaceFirst("[.][^.]+$", "");
            songNameLabel.setText(name);
            nowPlayingSmallLabel.setText(name);
            statusLabel.setText(status);
        } else {
            songNameLabel.setText("NAMA LAGU");
            nowPlayingSmallLabel.setText("");
            statusLabel.setText("");
        }
    }
                private String formatTime(double seconds) {
        int mins = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }
                private void addInlineCSS(javafx.scene.Parent node, String css) {
        try {
            java.nio.file.Path tmpCss = java.nio.file.Files.createTempFile("musicfx_", ".css");
            java.nio.file.Files.writeString(tmpCss, css);
            node.getStylesheets().add(tmpCss.toUri().toString());
            tmpCss.toFile().deleteOnExit();
        } catch (Exception e) {
                    }
    }
    private void addInlineCSS(ScrollPane pane, String css) {
        try {
            java.nio.file.Path tmpCss = java.nio.file.Files.createTempFile("musicfx_", ".css");
            java.nio.file.Files.writeString(tmpCss, css);
            pane.getStylesheets().add(tmpCss.toUri().toString());
            tmpCss.toFile().deleteOnExit();
        } catch (Exception e) {
                    }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
