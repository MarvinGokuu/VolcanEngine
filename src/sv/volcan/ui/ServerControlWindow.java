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

    public static void open(sv.volcan.kernel.EngineKernel kernel) {
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

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setBackground(new Color(30, 30, 30));

            JLabel subtitleLabel = new JLabel("Cierra esta ventana (X) para apagar el motor.", SwingConstants.CENTER);
            subtitleLabel.setForeground(new Color(150, 150, 150));
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            southPanel.add(subtitleLabel, BorderLayout.NORTH);

            // ICONO PERSONALIZADO (Similar al de la imagen proporcionada)
            Icon nodeIcon = new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(180, 180, 180));
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Cuadrado superior
                    g2.drawRoundRect(x + 4, y + 4, 6, 6, 2, 2);
                    // Cuadrado inferior
                    g2.drawRoundRect(x + 12, y + 12, 6, 6, 2, 2);
                    // Linea conectora
                    g2.drawLine(x + 7, y + 10, x + 7, y + 15);
                    g2.drawLine(x + 7, y + 15, x + 12, y + 15);
                    g2.dispose();
                }
                @Override public int getIconWidth() { return 24; }
                @Override public int getIconHeight() { return 24; }
            };

            // BOTON UNBURNER CON EFECTO HOVER
            JButton unburnerBtn = new JButton(nodeIcon);
            unburnerBtn.setBackground(new Color(30, 30, 30));
            unburnerBtn.setForeground(new Color(255, 85, 85)); // Rojo legible
            unburnerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            unburnerBtn.setFocusPainted(false);
            unburnerBtn.setBorderPainted(false);
            unburnerBtn.setContentAreaFilled(false);
            unburnerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Estado para saber en qué modo estamos
            final boolean[] isUnburner = {false};

            unburnerBtn.addActionListener(e -> {
                isUnburner[0] = !isUnburner[0];
                if (isUnburner[0]) {
                    kernel.getTimeKeeper().activateUnburnerMode();
                    unburnerBtn.setForeground(new Color(85, 255, 85)); // Verde neón legible
                    unburnerBtn.setText(" Por Defecto");
                    kernel.dumpTelemetryToLog();
                } else {
                    kernel.getTimeKeeper().restoreDefaultMode();
                    unburnerBtn.setForeground(new Color(255, 85, 85));
                    unburnerBtn.setText(" Unburner");
                    kernel.dumpTelemetryToLog();
                }
            });

            unburnerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    unburnerBtn.setText(isUnburner[0] ? " Por Defecto" : " Unburner");
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    unburnerBtn.setText(""); // Ocultar letras, dejar solo icono
                }
            });

            // Contenedor para centrar el botón sin estirarlo
            JPanel btnWrapper = new JPanel();
            btnWrapper.setBackground(new Color(30, 30, 30));
            btnWrapper.add(unburnerBtn);
            
            southPanel.add(btnWrapper, BorderLayout.SOUTH);

            panel.add(southPanel, BorderLayout.SOUTH);
            
            // Margen interior
            panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
