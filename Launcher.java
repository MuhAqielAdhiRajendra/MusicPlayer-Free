import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class Launcher {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        JFrame frame = new JFrame("MusicPlayerFX Setup");
        frame.setUndecorated(true);
        frame.setSize(420, 160);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(24, 24, 27));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(24, 24, 27));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        JLabel titleLabel = new JLabel("MusicPlayerFX");
        titleLabel.setForeground(new Color(250, 250, 250));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel statusLabel = new JLabel("Memulai pemeriksaan sistem...");
        statusLabel.setForeground(new Color(161, 161, 170));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(139, 92, 246));
        progressBar.setBackground(new Color(39, 39, 42));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(300, 6));
        progressBar.setMaximumSize(new Dimension(300, 6));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(statusLabel);
        panel.add(Box.createVerticalGlue());
        frame.add(panel);
        frame.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(600);
                SwingUtilities.invokeLater(() -> statusLabel.setText("Mencari library JavaFX..."));
                String currentDir = System.getProperty("user.dir");
                File existingFx26 = new File(currentDir, "javafx-26_windows-x64_bin-sdk/javafx-sdk-26.0.1/lib");
                File downloadedFx21 = new File(currentDir, "javafx-sdk-21.0.2/lib");
                String fxPath = null;
                if (existingFx26.exists()) {
                    fxPath = existingFx26.getAbsolutePath();
                } else if (downloadedFx21.exists()) {
                    fxPath = downloadedFx21.getAbsolutePath();
                } else {
                    String envFx = System.getenv("PATH_TO_FX");
                    if (envFx != null && new File(envFx).exists()) {
                        fxPath = envFx;
                    } else {
                        String fxHome = System.getenv("JAVAFX_HOME");
                        if (fxHome != null && new File(fxHome, "lib").exists()) {
                            fxPath = new File(fxHome, "lib").getAbsolutePath();
                        }
                    }
                }
                                if (fxPath == null) {
                    int result = JOptionPane.showConfirmDialog(frame, 
                        "Library JavaFX tidak ditemukan di komputer ini.\n\nApakah Anda ingin mengunduh dan menginstalnya secara otomatis?\n(Dibutuhkan koneksi internet, ukuran file ~40MB)", 
                        "Auto-Installer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        String fxUrl = "https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip";
                        File fxZip = new File(currentDir, "javafx-21.zip");
                        downloadFile(fxUrl, fxZip, statusLabel, progressBar, "JavaFX");
                        unzip(fxZip, new File(currentDir), statusLabel);
                        fxZip.delete();
                        fxPath = new File(currentDir, "javafx-sdk-21.0.2/lib").getAbsolutePath();
                    } else {
                        System.exit(1);
                    }
                }
                                File extLibDir = new File(currentDir, "ext_lib");
                if (!extLibDir.exists()) extLibDir.mkdirs();
                File jlayerJar = new File(extLibDir, "jlayer-1.0.1.jar");
                if (!jlayerJar.exists()) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(true);
                        statusLabel.setText("Mengecek library JLayer...");
                    });
                    String jlayerUrl = "https://repo1.maven.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar";
                    downloadFile(jlayerUrl, jlayerJar, statusLabel, progressBar, "JLayer");
                }
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(true);
                    statusLabel.setText("Mengecek status kompilasi...");
                });
                Thread.sleep(300);
                File sourceFile = new File(currentDir, "MusicPlayerFX.java");
                File classFile = new File(currentDir, "MusicPlayerFX.class");
                boolean needsCompile = true;
                if (classFile.exists() && classFile.lastModified() > sourceFile.lastModified()) {
                    needsCompile = false;
                }
                if (needsCompile) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Mengompilasi ulang MusicPlayerFX... (Bisa memakan waktu)"));
                    String cpArg = currentDir + ";" + currentDir + File.separator + "ext_lib" + File.separator + "*";
                    ProcessBuilder javacPb = new ProcessBuilder("javac", "--module-path", fxPath, "--add-modules", "javafx.controls,javafx.media,javafx.fxml", "-cp", cpArg, "MusicPlayerFX.java");
                    javacPb.directory(new File(currentDir));
                    Process javacP = javacPb.start();
                    int javacExit = javacP.waitFor();
                    if (javacExit != 0) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(javacP.getErrorStream()));
                        StringBuilder errors = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errors.append(line).append("\n");
                        }
                        JOptionPane.showMessageDialog(frame, "Kompilasi Gagal!\nSilakan periksa error di kode Java Anda:\n\n" + errors.toString(), "Error Compile", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
                SwingUtilities.invokeLater(() -> statusLabel.setText("Membuka MusicPlayerFX..."));
                Thread.sleep(400);
                String cpArg = currentDir + ";" + currentDir + File.separator + "ext_lib" + File.separator + "*";
                List<String> command = new ArrayList<>();
                command.add("javaw");
                command.add("-Djava.library.path=" + fxPath);
                command.add("--module-path");
                command.add(fxPath);
                command.add("--add-modules");
                command.add("javafx.controls,javafx.media,javafx.fxml");
                command.add("--enable-native-access=javafx.graphics,javafx.media");
                command.add("-cp");
                command.add(cpArg);
                command.add("MusicPlayerFX");
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File(currentDir));
                pb.start(); 
                System.exit(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Terjadi kesalahan sistem:\n" + e.getMessage(), "Error Fatal", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }).start();
    }
    private static void downloadFile(String urlStr, File target, JLabel statusLabel, JProgressBar progressBar, String name) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
            }
        }
        int fileSize = conn.getContentLength();
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            int downloaded = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;
                if (fileSize > 0) {
                    int progress = (int) ((downloaded * 100L) / fileSize);
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(progress);
                        statusLabel.setText("Mengunduh " + name + "... " + progress + "%");
                    });
                }
            }
        }
        SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(true));
    }
    private static void unzip(File zipFile, File destDir, JLabel statusLabel) throws Exception {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Mengekstrak file... (Harap tunggu)"));
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                                String destDirPath = destDir.getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    continue; 
                }
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
