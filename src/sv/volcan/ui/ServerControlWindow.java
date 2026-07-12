package sv.volcan.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import sv.volcan.core.VolcanLogger;

/**
 * Lightweight Generic Server Window.
 * Provides a minimal GUI with an "X" button so users can gracefully shut down the headless server.
 */
public class ServerControlWindow {

    public static void open() {
        // Ejecutar en el hilo de la interfaz gráfica (AWT Event Dispatch Thread)
        // para no bloquear el hilo principal (Kernel Hot-Path)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Ignore L&F errors
            }

            JFrame frame = new JFrame("VolcanEngine");
            frame.setSize(350, 150);
            frame.setResizable(true);
            frame.setLocationRelativeTo(null); // Centrar en pantalla
            
            // Interceptar el cierre de ventana para invocar un cierre limpio
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    VolcanLogger.info("UI", "Boton 'X' presionado. Iniciando Graceful Shutdown...");
                    // System.exit(0) dispara el ShutdownHook que programamos en EngineKernel
                    System.exit(0);
                }
            });

            // Diseño minimalista
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 30));

            // Intentar cargar la imagen del usuario
            java.net.URL imageUrl = ServerControlWindow.class.getResource("volcanengine_logo.png");
            if (imageUrl != null) {
                final Image bgImage = new ImageIcon(imageUrl).getImage();
                JPanel imagePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        // Escalar imagen dinámicamente al tamaño del panel
                        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    }
                };
                imagePanel.setBackground(new Color(30, 30, 30));
                panel.add(imagePanel, BorderLayout.CENTER);
                frame.setSize(800, 450); // Tamaño inicial más grande
            } else {
                JLabel titleLabel = new JLabel("VolcanEngine Activo", SwingConstants.CENTER);
                titleLabel.setForeground(new Color(220, 220, 220));
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                panel.add(titleLabel, BorderLayout.CENTER);
            }

            JLabel subtitleLabel = new JLabel("Cierra esta ventana (X) para apagar el motor.", SwingConstants.CENTER);
            subtitleLabel.setForeground(new Color(150, 150, 150));
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            panel.add(subtitleLabel, BorderLayout.SOUTH);
            
            // Margen interior
            panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
