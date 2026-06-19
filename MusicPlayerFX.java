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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

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
import java.util.Map;
import java.util.LinkedHashMap;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;

public class MusicPlayerFX extends Application {
    // Full Dark Mode Immersive Theme
    private static final String BG_MAIN = "#0A0A0A"; // Charcoal-black background
    private static final String BG_PANEL = "#161616"; // Subtle dark grey panels
    private static final String BG_CARD = "#2A2A2A"; // Dark grey placeholder/input
    private static final String BG_INPUT = "#222222"; // Dark buttons
    private static final String BG_ITEM = "#1E1E1E";
    private static final String BG_ITEM_HOVER = "#2A2A2A";
    private static final String BG_ITEM_SELECTED = "#333333"; // Subtle dark grey highlight
    private static final String BG_BOTTOM = "#161616";
    private static final String TEXT_PRIMARY = "#FFFFFF";
    private static final String TEXT_SECONDARY = "#E0E0E0";
    private static final String TEXT_MUTED = "#999999";
    private static final String TEXT_INVERTED = "#FFFFFF";
    private static final String ACCENT_COLOR = "#FFFFFF";
    private static final String BORDER_COLOR = "#222222";
    private static final String BORDER_LIGHT = "#333333";
    private static final String ACCENT_HIGHLIGHT = "#888888";
    private static final String PROGRESS_BG = "#333333";
    private static final String PROGRESS_FG = "#00E5FF";
    private static final String CYAN_ACCENT = "#00E5FF";
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
    private List<File> allMusicFiles = new ArrayList<>();
    private Map<String, List<File>> playlists = new LinkedHashMap<>();
    private String activePlaylist = null;

    private StackPane popupOverlay;
    private StackPane root;
    private List<File> musicFiles = new ArrayList<>();
    private int currentIndex = -1;
    private boolean shuffleOn = false;
    private Clip audioClip;
    private Player jlayerPlayer;
    private boolean usingFallback = false;
    private Thread progressThread;
    private boolean repeatOn = false;
    private Slider volumeSlider;
    private double eqBass = 0;
    private double eqMid = 0;
    private double eqTreble = 0;
    private static final String SLIDER_CSS = ".slider { -fx-padding: 0; }" +
            ".slider .track { -fx-background-color: #E2E1DA; -fx-background-radius: 2; -fx-pref-height: 4; }" +
            ".slider .thumb { -fx-background-color: #1C1C1A; -fx-pref-width: 12; -fx-pref-height: 12; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 1); }"
            +
            ".slider:hover .thumb { -fx-background-color: #000000; -fx-pref-width: 14; -fx-pref-height: 14; -fx-background-radius: 7; }"
            +
            ".slider .colored-track { -fx-background-color: #1C1C1A; -fx-background-radius: 2; }" +
            ".slider:hover .colored-track { -fx-background-color: #000000; -fx-background-radius: 2; }";
    private static final String SCROLLPANE_CSS = ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }"
            +
            ".scroll-pane .viewport { -fx-background-color: transparent; }" +
            ".scroll-bar { -fx-background-color: transparent; -fx-pref-width: 6; }" +
            ".scroll-bar .thumb { -fx-background-color: #8E8E8850; -fx-background-radius: 3; }" +
            ".scroll-bar .track { -fx-background-color: transparent; }" +
            ".scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-pref-height: 0; -fx-pref-width: 0; }";
    private javafx.scene.control.TextArea debugTextArea;
    private javafx.stage.Stage debugStage;

    private void setupDebugConsole() {
        debugTextArea = new javafx.scene.control.TextArea();
        debugTextArea.setEditable(false);
        debugTextArea
                .setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #00FF00; -fx-font-family: monospace;");
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
        javafx.scene.Scene debugScene = new javafx.scene.Scene(new javafx.scene.layout.StackPane(debugTextArea), 600,
                400);
        debugStage.setScene(debugScene);
        System.out.println("--- Terminal Debug Captured ---");
    }

    @Override
    public void start(Stage primaryStage) {
        setupDebugConsole();
        primaryStage.setTitle("Music Player");

        root = new StackPane();
        root.setStyle("-fx-background-color: " + BG_MAIN + ";");

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: transparent;");

        HBox topPanels = new HBox(15);
        HBox.setHgrow(topPanels, Priority.ALWAYS);
        VBox.setVgrow(topPanels, Priority.ALWAYS);

        VBox sidebarPanel = buildSidebar();
        sidebarPanel.setPrefWidth(240);
        sidebarPanel.setMinWidth(220);

        VBox mainViewPanel = buildMainView(primaryStage);
        HBox.setHgrow(mainViewPanel, Priority.ALWAYS);

        topPanels.getChildren().addAll(sidebarPanel, mainViewPanel);

        HBox bottomPanel = buildBottomBar(primaryStage);

        mainContainer.getChildren().addAll(topPanels, bottomPanel);

        popupOverlay = new StackPane();
        popupOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        popupOverlay.setVisible(false);
        popupOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == popupOverlay) {
                popupOverlay.setVisible(false);
            }
        });

        root.getChildren().addAll(mainContainer, popupOverlay);
        Scene scene = new Scene(root, 1100, 650);

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
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        javafx.animation.Timeline fallbackSpectrumTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(80), e -> {
                    if (isPlaying && usingFallback) {
                        int half = spectrumBars.length / 2;
                        for (int i = 0; i < spectrumBars.length; i++) {
                            int distFromCenter = Math.abs(i - half);
                            double maxH = 14.0 * (1.0 - distFromCenter / (double) half);
                            double h = 4 + Math.random() * Math.max(maxH, 2);
                            if (h > 14) h = 14;
                            spectrumBars[i].setHeight(h);
                        }
                    } else if (!isPlaying || (!usingFallback && mediaPlayer != null
                            && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED)) {
                        for (Rectangle bar : spectrumBars) {
                            if (bar.getHeight() > 4)
                                bar.setHeight(4);
                        }
                    }
                }));
        fallbackSpectrumTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        fallbackSpectrumTimeline.play();
        loadPlaylists();
        loadDownloadsFolder();
    }

    private void showToast(String message) {
        if (root == null)
            return;
        Label toast = new Label(message);
        toast.setStyle(
                "-fx-background-color: #333333; -fx-text-fill: #FFFFFF; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 4);");
        toast.setOpacity(0);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 150, 0)); // Pop up above play/pause button

        root.getChildren().add(toast);

        javafx.animation.FadeTransition ftIn = new javafx.animation.FadeTransition(Duration.millis(300), toast);
        ftIn.setToValue(1.0);

        javafx.animation.FadeTransition ftOut = new javafx.animation.FadeTransition(Duration.millis(300), toast);
        ftOut.setToValue(0.0);
        ftOut.setDelay(Duration.seconds(2.5));
        ftOut.setOnFinished(ev -> root.getChildren().remove(toast));

        ftIn.setOnFinished(ev -> ftOut.play());
        ftIn.play();
    }

    private void showCustomPopup(javafx.scene.Node content) {
        popupOverlay.getChildren().clear();
        popupOverlay.getChildren().add(content);
        popupOverlay.setVisible(true);
    }

    private void hidePopup() {
        popupOverlay.setVisible(false);
    }

    private void showNewPlaylistDialog() {
        VBox dialog = new VBox(15);
        dialog.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-background-radius: 12; -fx-border-color: "
                + BORDER_COLOR + "; -fx-border-radius: 12;");
        dialog.setPadding(new Insets(20));
        dialog.setMaxSize(300, 150);

        Label title = new Label("Playlist Baru");
        title.setTextFill(Color.web(TEXT_PRIMARY));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        TextField input = new TextField();
        input.setPromptText("Nama playlist...");
        input.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_INVERTED
                + "; -fx-background-radius: 6; -fx-padding: 8;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Batal");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> hidePopup());

        Button saveBtn = new Button("Simpan");
        saveBtn.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_INVERTED
                + "; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String name = input.getText().trim();
            if (!name.isEmpty() && !playlists.containsKey(name)) {
                playlists.put(name, new ArrayList<>());
                savePlaylist(name);
                updatePlaylistList();
                hidePopup();
                showToast("Berhasil membuat playlist: " + name);
            }
        });

        buttons.getChildren().addAll(cancelBtn, saveBtn);
        dialog.getChildren().addAll(title, input, buttons);

        showCustomPopup(dialog);
    }



    private VBox playlistContainer;

    private void updatePlaylistList() {
        if (playlistContainer == null)
            return;
        playlistContainer.getChildren().clear();
        playlistContainer.getChildren().add(createSidebarPlaylistBtn("Semua Lagu", activePlaylist == null));
        for (String plName : playlists.keySet()) {
            playlistContainer.getChildren().add(createSidebarPlaylistBtn(plName, plName.equals(activePlaylist)));
        }
    }

    private javafx.scene.Node createSidebarPlaylistBtn(String name, boolean isActive) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button(name);
        String bgColor = isActive ? CYAN_ACCENT : "transparent";
        String textColor = isActive ? "#000000" : TEXT_MUTED;
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 8; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-padding: 10 14; -fx-cursor: hand; -fx-font-size: 13px;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setOnAction(e -> {
            if ("Semua Lagu".equals(name)) {
                activePlaylist = null;
                musicFiles = new ArrayList<>(allMusicFiles);
            } else {
                activePlaylist = name;
                musicFiles = new ArrayList<>(playlists.getOrDefault(name, new ArrayList<>()));
            }
            updatePlaylistList();
            updateFileList();
        });
        box.getChildren().add(btn);

        if (!"Semua Lagu".equals(name)) {
            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 6;");
            delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF5555; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 6;"));
            delBtn.setOnMouseExited(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 6;"));
            delBtn.setOnAction(e -> {
                e.consume();
                deletePlaylist(name);
                if (name.equals(activePlaylist)) {
                    activePlaylist = null;
                    musicFiles = new ArrayList<>(allMusicFiles);
                    updateFileList();
                }
                updatePlaylistList();
                showToast("Playlist " + name + " dihapus");
            });
            box.getChildren().add(delBtn);
        }
        return box;
    }



    private VBox buildSidebar() {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-background-radius: 12; -fx-padding: 20;");

        Label libLabel = new Label("Library");
        libLabel.setTextFill(Color.web(TEXT_PRIMARY));
        libLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Search...");
        searchBar.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_INVERTED
                + "; -fx-background-radius: 20; -fx-padding: 8 12;");

        HBox plHeader = new HBox(10);
        plHeader.setAlignment(Pos.CENTER_LEFT);
        Label plLabel = new Label("Playlists");
        plLabel.setTextFill(Color.web(TEXT_MUTED));
        plLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Region plSpacer = new Region();
        HBox.setHgrow(plSpacer, Priority.ALWAYS);
        Button addPlaylistBtn = new Button("➕");
        addPlaylistBtn.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY
                + "; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 4 8;");
        addPlaylistBtn.setOnMouseEntered(e -> addPlaylistBtn.setStyle("-fx-background-color: " + CYAN_ACCENT
                + "; -fx-text-fill: #000000; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 4 8;"));
        addPlaylistBtn.setOnMouseExited(
                e -> addPlaylistBtn.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY
                        + "; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 4 8;"));
        addPlaylistBtn.setOnAction(e -> showNewPlaylistDialog());
        plHeader.getChildren().addAll(plLabel, plSpacer, addPlaylistBtn);

        // Use the field playlistContainer so updatePlaylistList() can refresh it
        this.playlistContainer = new VBox(6);
        ScrollPane plScroll = new ScrollPane(this.playlistContainer);
        plScroll.setFitToWidth(true);
        plScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        plScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        plScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        addInlineCSS(plScroll, SCROLLPANE_CSS);
        VBox.setVgrow(plScroll, Priority.ALWAYS);

        updatePlaylistList();

        panel.getChildren().addAll(libLabel, searchBar, plHeader, plScroll);
        return panel;
    }



    private VBox buildMainView(Stage stage) {
        VBox panel = new VBox(12);
        panel.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-background-radius: 12; -fx-padding: 20;");

        // TOP: Now Playing Centerpiece
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);

        albumArtLarge = new StackPane();
        albumArtLarge.setPrefSize(150, 150);
        albumArtLarge.setMinSize(150, 150);
        albumArtLarge.setMaxSize(150, 150);
        albumArtLarge.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 12;");

        albumArtEmoji = new Label("🎵");
        albumArtEmoji.setTextFill(Color.web(CYAN_ACCENT));
        albumArtEmoji.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

        albumImageView = new ImageView();
        albumImageView.setFitWidth(150);
        albumImageView.setFitHeight(150);
        albumImageView.setPreserveRatio(false);
        albumImageView.setSmooth(true);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(150, 150);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        albumImageView.setClip(clip);
        albumImageView.setVisible(false);
        albumArtLarge.getChildren().addAll(albumArtEmoji, albumImageView);

        VBox songDetails = new VBox(6);
        songDetails.setAlignment(Pos.CENTER_LEFT);
        songNameLabel = new Label("NAMA LAGU");
        songNameLabel.setTextFill(Color.web(TEXT_PRIMARY));
        songNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        songNameLabel.setWrapText(true);
        songNameLabel.setMaxWidth(400);

        Label artistLabel = new Label("Unknown Artist");
        artistLabel.setTextFill(Color.web(TEXT_MUTED));
        artistLabel.setFont(Font.font("Segoe UI", 14));

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.web(TEXT_MUTED));
        statusLabel.setFont(Font.font("Segoe UI", 12));

        // Spectrum visualizer dots — fixed size, bass in center
        spectrumBox = new HBox(3);
        spectrumBox.setAlignment(Pos.BOTTOM_CENTER);
        spectrumBox.setPrefHeight(16);
        spectrumBox.setMinHeight(16);
        spectrumBox.setMaxHeight(16);
        spectrumBox.setPrefWidth(160);
        spectrumBox.setMinWidth(160);
        spectrumBox.setMaxWidth(160);
        spectrumBars = new Rectangle[20];
        for (int i = 0; i < spectrumBars.length; i++) {
            spectrumBars[i] = new javafx.scene.shape.Rectangle(4, 4);
            spectrumBars[i].setFill(Color.web(CYAN_ACCENT));
            spectrumBars[i].setArcWidth(4);
            spectrumBars[i].setArcHeight(4);
            spectrumBox.getChildren().add(spectrumBars[i]);
        }

        songDetails.getChildren().addAll(songNameLabel, artistLabel, statusLabel, spectrumBox);
        topSection.getChildren().addAll(albumArtLarge, songDetails);

        // MIDDLE: YouTube Download & Actions
        HBox actionSection = new HBox(15);
        actionSection.setAlignment(Pos.CENTER_LEFT);

        Button inputFileBtn = new Button("Input File Music");
        inputFileBtn.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_INVERTED
                + "; -fx-border-color: " + CYAN_ACCENT
                + "; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 24; -fx-cursor: hand;");
        inputFileBtn.setOnAction(e -> openFiles(stage));

        TextField ytInput = new TextField();
        ytInput.setPromptText("Paste YouTube URL here...");
        ytInput.setStyle(
                "-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_INVERTED + "; -fx-border-color: "
                        + CYAN_ACCENT + "; -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 8 12;");
        HBox.setHgrow(ytInput, Priority.ALWAYS);

        Button ytDownloadBtn = new Button("Download");
        ytDownloadBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: #FFFFFF; -fx-border-color: " + CYAN_ACCENT
                + "; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        ytDownloadBtn.setOnAction(e -> {
            String url = ytInput.getText().trim();
            if (!url.isEmpty()) {
                ytInput.clear();
                downloadFromYoutube(url);
            }
        });
        actionSection.getChildren().addAll(inputFileBtn, ytInput, ytDownloadBtn);

        // BOTTOM: Up Next List
        Label upNextLabel = new Label("Up Next");
        upNextLabel.setTextFill(Color.web(TEXT_PRIMARY));
        upNextLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        folderPathLabel = new Label("");
        folderPathLabel.setTextFill(Color.web(TEXT_MUTED));
        folderPathLabel.setFont(Font.font("Segoe UI", 11));

        VBox listWrapper = new VBox(0);
        listWrapper.setStyle("-fx-background-color: " + BG_MAIN + "; -fx-background-radius: 12;");
        listWrapper.setPadding(new Insets(10));
        VBox.setVgrow(listWrapper, Priority.ALWAYS);

        fileListContainer = new VBox(4);
        ScrollPane scrollPane = new ScrollPane(fileListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        addInlineCSS(scrollPane, SCROLLPANE_CSS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        listWrapper.getChildren().add(scrollPane);

        panel.getChildren().addAll(topSection, actionSection, upNextLabel, folderPathLabel, listWrapper);
        return panel;
    }



    private void applyMarquee(Label label, double containerWidth) {
        label.setWrapText(false);
        label.textProperty().addListener((obs, oldV, newV) -> {
            Platform.runLater(() -> {
                Object userData = label.getUserData();
                if (userData instanceof javafx.animation.TranslateTransition) {
                    ((javafx.animation.TranslateTransition) userData).stop();
                }
                label.setTranslateX(0);

                double width = label.getLayoutBounds().getWidth();
                if (width > containerWidth) {
                    javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                            Duration.seconds(width / 30.0), label);
                    tt.setFromX(containerWidth);
                    tt.setToX(-width);
                    tt.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    tt.setInterpolator(javafx.animation.Interpolator.LINEAR);
                    tt.play();
                    label.setUserData(tt);
                }
            });
        });
    }

    private void updateSongNameDisplay(String name) {
        songNameLabel.setText(name);
        nowPlayingSmallLabel.setText(name);
    }



    private HBox buildBottomBar(Stage stage) {
        HBox bottomBar = new HBox(20);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle(
                "-fx-background-color: " + BG_BOTTOM + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
        bottomBar.setPadding(new Insets(10, 20, 10, 20));
        bottomBar.setPrefHeight(80);
        bottomBar.setMaxHeight(80);

        albumArtSmall = new StackPane();
        albumArtSmall.setPrefSize(56, 56);
        albumArtSmall.setMinSize(56, 56);
        albumArtSmall.setMaxSize(56, 56);
        albumArtSmall.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 8;");
        albumArtSmallEmoji = new Label("🎵");
        albumArtSmallEmoji.setTextFill(Color.web(TEXT_INVERTED));
        albumSmallImageView = new ImageView();
        albumSmallImageView.setFitWidth(56);
        albumSmallImageView.setFitHeight(56);
        Rectangle smallClip = new Rectangle(56, 56);
        smallClip.setArcWidth(8);
        smallClip.setArcHeight(8);
        albumSmallImageView.setClip(smallClip);
        albumSmallImageView.setVisible(false);
        albumArtSmall.getChildren().addAll(albumArtSmallEmoji, albumSmallImageView);

        Pane smallNamePane = new Pane();
        smallNamePane.setPrefSize(150, 20);
        smallNamePane.setMaxSize(150, 20);
        smallNamePane.setMinSize(150, 20);
        Rectangle clipRectSmall = new Rectangle(150, 20);
        smallNamePane.setClip(clipRectSmall);

        nowPlayingSmallLabel = new Label("");
        nowPlayingSmallLabel.setTextFill(Color.web(TEXT_PRIMARY));
        nowPlayingSmallLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nowPlayingSmallLabel.setWrapText(false);
        smallNamePane.getChildren().add(nowPlayingSmallLabel);

        applyMarquee(nowPlayingSmallLabel, 150);

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        VBox controlsWithProgress = new VBox(4);
        controlsWithProgress.setAlignment(Pos.CENTER);

        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        Button shuffleButton = createControlButton("🔀", false);
        Button prevButton = createControlButton("⏮", false);
        playPauseButton = createPlayButton();
        Button nextButton = createControlButton("⏭", false);
        Button repeatButton = createControlButton("🔁", false);
        Button deleteButton = createControlButton("🗑", false);

        shuffleButton.setOnAction(e -> {
            shuffleOn = !shuffleOn;
            if (shuffleOn && repeatOn) {
                repeatOn = false;
                repeatButton.setUserData(TEXT_MUTED);
                repeatButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED
                        + "; -fx-font-size: 22px; -fx-padding: 4;");
            }
            String color = shuffleOn ? ACCENT_COLOR : TEXT_MUTED;
            shuffleButton.setUserData(color);
            shuffleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color
                    + "; -fx-font-size: 22px; -fx-padding: 4;");
        });
        repeatButton.setOnAction(e -> {
            repeatOn = !repeatOn;
            if (repeatOn && shuffleOn) {
                shuffleOn = false;
                shuffleButton.setUserData(TEXT_MUTED);
                shuffleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED
                        + "; -fx-font-size: 22px; -fx-padding: 4;");
            }
            String color = repeatOn ? ACCENT_COLOR : TEXT_MUTED;
            repeatButton.setUserData(color);
            repeatButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color
                    + "; -fx-font-size: 22px; -fx-padding: 4;");
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
        deleteButton.setOnAction(e -> deleteCurrentSong());
        controls.getChildren().addAll(shuffleButton, prevButton, playPauseButton, nextButton, repeatButton,
                deleteButton);

        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(400);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        addInlineCSS(progressSlider, SLIDER_CSS);
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setTextFill(Color.web(TEXT_MUTED));
        currentTimeLabel.setFont(Font.font("Segoe UI", 11));
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setTextFill(Color.web(TEXT_MUTED));
        totalTimeLabel.setFont(Font.font("Segoe UI", 11));
        HBox sliderBox = new HBox(10);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> currentTimeLabel.setText(formatTime(newVal.doubleValue())));
        });
        controlsWithProgress.getChildren().addAll(controls, sliderBox);
        HBox.setHgrow(controlsWithProgress, Priority.ALWAYS);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox rightCluster = new HBox(12);
        rightCluster.setAlignment(Pos.CENTER_RIGHT);

        // Equalizer button
        Button eqButton = new Button("🎛");
        eqButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + CYAN_ACCENT
                + "; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 4;");
        eqButton.setOnMouseEntered(e -> eqButton.setStyle("-fx-background-color: transparent; -fx-text-fill: "
                + TEXT_PRIMARY + "; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 4;"));
        eqButton.setOnMouseExited(e -> eqButton.setStyle("-fx-background-color: transparent; -fx-text-fill: "
                + CYAN_ACCENT + "; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 4;"));
        eqButton.setOnAction(e -> showEqualizerPopup());

        // Volume icon + slider
        Label volIcon = new Label("🔊");
        volIcon.setFont(Font.font("System", 22));
        volIcon.setTextFill(Color.web(CYAN_ACCENT));
        Slider volSlider = new Slider(0, 100, 80);
        volSlider.setPrefWidth(80);
        addInlineCSS(volSlider,
                ".track { -fx-background-color: #333333; -fx-pref-height: 4px; } .thumb { -fx-background-color: "
                        + CYAN_ACCENT + "; -fx-pref-width: 10px; -fx-pref-height: 10px; }");

        // Volume slider controls actual audio volume
        volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(vol);
            }
            if (audioClip != null) {
                // Clip volume uses FloatControl
                try {
                    javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl)
                            audioClip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (20.0 * Math.log10(Math.max(vol, 0.0001)));
                    if (dB < gainControl.getMinimum()) dB = gainControl.getMinimum();
                    gainControl.setValue(dB);
                } catch (Exception ex) { /* ignore if not supported */ }
            }
            // Update icon based on volume level
            if (newVal.doubleValue() == 0) {
                volIcon.setText("🔇");
            } else if (newVal.doubleValue() < 40) {
                volIcon.setText("🔉");
            } else {
                volIcon.setText("🔊");
            }
        });
        this.volumeSlider = volSlider;

        rightCluster.getChildren().addAll(eqButton, volIcon, volSlider);

        bottomBar.getChildren().addAll(albumArtSmall, smallNamePane, leftSpacer, controlsWithProgress, rightSpacer,
                rightCluster);
        return bottomBar;
    }

    private Button createControlButton(String text, boolean active) {
        Button button = new Button(text);
        String color = active ? ACCENT_COLOR : TEXT_MUTED;
        button.setUserData(color);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color
                + "; -fx-font-size: 22px; -fx-padding: 4;");
        button.setCursor(Cursor.HAND);
        button.setOnMouseEntered(e -> {
            if (!button.isDisabled())
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_PRIMARY
                        + "; -fx-font-size: 22px; -fx-padding: 4;");
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisabled()) {
                Object c = button.getUserData();
                String outColor = c != null ? c.toString() : TEXT_MUTED;
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + outColor
                        + "; -fx-font-size: 22px; -fx-padding: 4;");
            }
        });
        return button;
    }

    private Button createPlayButton() {
        Button btn = new Button("▶");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR
                + "; -fx-font-size: 28px; -fx-padding: 4;");
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED
                + "; -fx-font-size: 28px; -fx-padding: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR
                + "; -fx-font-size: 28px; -fx-padding: 4;"));
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
                allMusicFiles.clear();
                for (File file : files) {
                    allMusicFiles.add(file);
                }
                if (activePlaylist == null) {
                    musicFiles.clear();
                    musicFiles.addAll(allMusicFiles);
                }
                folderPathLabel.setText("Folder: " + downloadsDir.getAbsolutePath());
                updateFileList();
                if (!musicFiles.isEmpty()) {
                    currentIndex = 0;
                    loadAndPlay(musicFiles.get(currentIndex));
                }
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
                if (!allMusicFiles.contains(f)) {
                    allMusicFiles.add(f);
                    if (activePlaylist == null) {
                        musicFiles.add(f);
                    }
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
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 20, 14, 20));
        item.setCursor(Cursor.HAND);
        boolean isCurrentlyPlaying = (currentFile != null && currentFile.equals(file));
        item.setStyle(
                "-fx-background-color: " + (isCurrentlyPlaying ? BG_ITEM_SELECTED : BG_ITEM) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: " + (isCurrentlyPlaying ? CYAN_ACCENT : "transparent") + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;");
        Label icon = new Label(isCurrentlyPlaying ? "▶" : "🎵");
        icon.setFont(Font.font("System", 16));
        icon.setTextFill(Color.web(isCurrentlyPlaying ? CYAN_ACCENT : TEXT_MUTED));
        icon.setMinWidth(24);
        String name = file.getName().replaceFirst("[.][^.]+$", "");
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(isCurrentlyPlaying ? TEXT_PRIMARY : TEXT_SECONDARY));
        nameLabel.setFont(Font.font("Segoe UI", isCurrentlyPlaying ? FontWeight.BOLD : FontWeight.NORMAL, 16));

        Pane namePane = new Pane();
        namePane.setPrefSize(450, 22);
        namePane.setMaxSize(450, 22);
        namePane.setMinSize(270, 22);
        javafx.scene.shape.Rectangle clipRectName = new javafx.scene.shape.Rectangle(450, 22);
        namePane.setClip(clipRectName);
        namePane.getChildren().add(nameLabel);

        applyMarquee(nameLabel, 450);
        String ext = file.getName().contains(".")
                ? file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase()
                : "";
        Label extLabel = new Label(ext);
        extLabel.setTextFill(Color.web(TEXT_MUTED));
        extLabel.setFont(Font.font("Segoe UI", 10));
        extLabel.setStyle(
                "-fx-background-color: " + BG_MAIN + ";" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 2 6 2 6;");

        Button addToPlaylistBtn = new Button("➕");
        addToPlaylistBtn.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        addToPlaylistBtn
                .setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand;");
        addToPlaylistBtn.setOnMouseEntered(ev -> addToPlaylistBtn
                .setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-cursor: hand;"));
        addToPlaylistBtn.setOnMouseExited(ev -> addToPlaylistBtn
                .setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand;"));
        addToPlaylistBtn.setOnAction(ev -> {
            ev.consume();
            showAddToPlaylistDialog(file);
        });

        extLabel.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        item.getChildren().addAll(icon, namePane, addToPlaylistBtn, extLabel);
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

    private void showAddToPlaylistDialog(File file) {
        VBox dialog = new VBox(15);
        dialog.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-background-radius: 12; -fx-border-color: "
                + BORDER_COLOR + "; -fx-border-radius: 12;");
        dialog.setPadding(new Insets(20));
        dialog.setMaxSize(300, 400);

        Label title = new Label("Tambah ke Playlist");
        title.setTextFill(Color.web(TEXT_PRIMARY));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        VBox list = new VBox(8);
        list.setAlignment(Pos.TOP_LEFT);

        Button newPlBtn = new Button("➕ Buat Playlist Baru");
        newPlBtn.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_INVERTED
                + "; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        newPlBtn.setMaxWidth(Double.MAX_VALUE);
        newPlBtn.setOnAction(e -> {
            hidePopup();
            showNewPlaylistDialog();
            // Note: we might need to select the file after creation, but for now we'll just
            // open the new playlist dialog.
            // A better way is to do it properly:
        });
        list.getChildren().add(newPlBtn);

        for (String plName : playlists.keySet()) {
            Button btn = new Button(plName);
            btn.setStyle("-fx-background-color: " + BG_ITEM + "; -fx-text-fill: " + TEXT_PRIMARY
                    + "; -fx-background-radius: 6; -fx-cursor: hand;");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> {
                List<File> pl = playlists.get(plName);
                if (!pl.contains(file)) {
                    pl.add(file);
                    savePlaylist(plName);
                    statusLabel.setText("Lagu ditambahkan ke " + plName);
                    showToast("Berhasil menambahkan lagu ke playlist: " + plName);
                    if (plName.equals(activePlaylist)) {
                        musicFiles.add(file);
                        updateFileList();
                    }
                } else {
                    statusLabel.setText("Lagu sudah ada di playlist " + plName);
                    showToast("Lagu sudah ada di playlist " + plName);
                }
                hidePopup();
            });
            list.getChildren().add(btn);
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Button cancelBtn = new Button("Batal");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> hidePopup());

        HBox bottom = new HBox(cancelBtn);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        dialog.getChildren().addAll(title, sp, bottom);
        showCustomPopup(dialog);
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
            double currentVol = (volumeSlider != null) ? (volumeSlider.getValue() / 100.0) : 0.8;
            mediaPlayer.setVolume(currentVol);
            applyEq();
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
                mediaPlayer.setAudioSpectrumInterval(0.05);
                mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                    int half = spectrumBars.length / 2;
                    // Remap: center bars = bass (low freq), edge bars = treble (high freq)
                    for (int i = 0; i < spectrumBars.length; i++) {
                        int distFromCenter = Math.abs(i - half);
                        // Map: center (dist=0) -> band 0 (bass), edge (dist=half) -> band max (treble)
                        int bandIndex = (int) (distFromCenter * (magnitudes.length - 1) / (double) half);
                        if (bandIndex >= magnitudes.length) bandIndex = magnitudes.length - 1;
                        double mag = magnitudes[bandIndex] + 60;
                        if (mag < 0) mag = 0;
                        double height = mag * 0.4;
                        if (height < 4) height = 4;
                        if (height > 14) height = 14; // Max height cap
                        spectrumBars[i].setHeight(height);
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
            if (volumeSlider != null) {
                try {
                    double vol = volumeSlider.getValue() / 100.0;
                    javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl)
                            audioClip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (20.0 * Math.log10(Math.max(vol, 0.0001)));
                    if (dB < gainControl.getMinimum()) dB = gainControl.getMinimum();
                    gainControl.setValue(dB);
                } catch (Exception ex) { }
            }
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
                                if (!allMusicFiles.contains(toPlay)) {
                                    allMusicFiles.add(toPlay);
                                    if (activePlaylist == null) {
                                        musicFiles.add(toPlay);
                                    }
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
        if (currentFile == null)
            return;
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
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.MOVE_TO_TRASH)) {
                deleted = java.awt.Desktop.getDesktop().moveToTrash(fileToDelete);
            }
            if (!deleted && fileToDelete.exists()) {
                deleted = fileToDelete.delete();
            }
            if (deleted || !fileToDelete.exists()) {
                Platform.runLater(() -> {
                    allMusicFiles.remove(fileToDelete);
                    musicFiles.remove(fileToDelete);
                    for (List<File> pl : playlists.values()) {
                        pl.remove(fileToDelete);
                    }
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

    private void loadPlaylists() {
        File dir = new File("playlists");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName().replace(".txt", "");
                List<File> pl = new ArrayList<>();
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        File song = new File(line);
                        if (song.exists()) {
                            pl.add(song);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Gagal memuat playlist " + name);
                }
                playlists.put(name, pl);
            }
            updatePlaylistList();
        }
    }

    private void savePlaylist(String name) {
        File dir = new File("playlists");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, name + ".txt");
        List<File> pl = playlists.get(name);
        if (pl != null) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                for (File song : pl) {
                    pw.println(song.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Gagal menyimpan playlist " + name);
            }
        }
    }

    private void deletePlaylist(String name) {
        playlists.remove(name);
        File file = new File("playlists", name + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    private void showEqualizerPopup() {
        VBox dialog = new VBox(20);
        dialog.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");
        dialog.setPadding(new Insets(20));
        dialog.setMaxSize(300, 250);

        Label title = new Label("Equalizer");
        title.setTextFill(Color.web(TEXT_PRIMARY));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        VBox eqBox = new VBox(15);
        eqBox.getChildren().add(createEqSlider("Bass", -24, 12, eqBass, val -> { eqBass = val; applyEq(); }));
        eqBox.getChildren().add(createEqSlider("Mid", -24, 12, eqMid, val -> { eqMid = val; applyEq(); }));
        eqBox.getChildren().add(createEqSlider("Treble", -24, 12, eqTreble, val -> { eqTreble = val; applyEq(); }));

        Button closeBtn = new Button("Tutup");
        closeBtn.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_INVERTED + "; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e -> hidePopup());

        dialog.getChildren().addAll(title, eqBox, closeBtn);
        showCustomPopup(dialog);
    }

    private HBox createEqSlider(String name, double min, double max, double current, java.util.function.DoubleConsumer onChange) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(name);
        label.setPrefWidth(50);
        label.setTextFill(Color.web(TEXT_MUTED));
        Slider slider = new Slider(min, max, current);
        HBox.setHgrow(slider, Priority.ALWAYS);
        addInlineCSS(slider, ".track { -fx-background-color: #333333; -fx-pref-height: 4px; } .thumb { -fx-background-color: " + CYAN_ACCENT + "; -fx-pref-width: 12px; -fx-pref-height: 12px; }");
        slider.valueProperty().addListener((obs, oldV, newV) -> onChange.accept(newV.doubleValue()));
        box.getChildren().addAll(label, slider);
        return box;
    }

    private void applyEq() {
        if (mediaPlayer != null && mediaPlayer.getAudioEqualizer() != null) {
            mediaPlayer.getAudioEqualizer().setEnabled(true);
            javafx.collections.ObservableList<javafx.scene.media.EqualizerBand> bands = mediaPlayer.getAudioEqualizer().getBands();
            if (bands.size() >= 10) {
                bands.get(0).setGain(eqBass);
                bands.get(1).setGain(eqBass);
                bands.get(2).setGain(eqBass);
                bands.get(3).setGain(eqMid);
                bands.get(4).setGain(eqMid);
                bands.get(5).setGain(eqMid);
                bands.get(6).setGain(eqMid);
                bands.get(7).setGain(eqTreble);
                bands.get(8).setGain(eqTreble);
                bands.get(9).setGain(eqTreble);
            }
        }
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
