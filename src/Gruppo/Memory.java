package Gruppo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class Memory {

    // Configurazione griglia
    static int colonne = 6, righe = 3;
    static int maxSize = 200;

    // Stato giocatori
    static int numeroGiocatori = 1;
    static int giocatoreCorrente = 0;
    static int[] punteggi;

    static final Color[] COLORI_GIOCATORI = {
        new Color(52, 152, 219),   // P1 – blu
        new Color(231, 76, 60),    // P2 – rosso
        new Color(46, 204, 113),   // P3 – verde
        new Color(243, 156, 18)    // P4 – arancione
    };
    static final Color SFONDO_DEFAULT = Color.WHITE;

    // Componenti UI principali
    static JFrame frame;
    static JPanel mainPanel;
    static CardLayout cardLayout;

    // Pannelli score (angoli)
    static JLabel[] labelPunteggi;
    static JPanel scorePanel;

    // Carte
    static JButton[] bottoni;
    static ImageIcon[] valori;
    static ImageIcon retro;

    static JButton prima  = null;
    static JButton seconda = null;
    static boolean blocco = false;

    // Coppie trovate (per rilevare fine partita)
    static int coppieRimaste;

    // ═════════════════════════════ MAIN ═══════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Memory Game");
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            cardLayout = new CardLayout();
            mainPanel  = new JPanel(cardLayout);

            mainPanel.add(creaSchermataSelezione(), "selezione");
            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    
    // ════════════════════════════════ SCHERMATA SELEZIONE GIOCATORI ════════════════════════════════════════
    static JPanel creaSchermataSelezione() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 25, 50));

        // Pannello centrale con contenuto
        JPanel contenuto = new JPanel();
        contenuto.setLayout(new BoxLayout(contenuto, BoxLayout.Y_AXIS));
        contenuto.setOpaque(false);

        // Titolo
        JLabel titolo = new JLabel("MEMORY GAME");
        titolo.setFont(new Font("Arial Black", Font.BOLD, 52));
        titolo.setForeground(Color.WHITE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenuto.add(titolo);

        JLabel sottotitolo = new JLabel("Seleziona il numero di giocatori");
        sottotitolo.setFont(new Font("Arial", Font.PLAIN, 24));
        sottotitolo.setForeground(new Color(170, 200, 230));
        sottotitolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenuto.add(sottotitolo);
        contenuto.add(Box.createVerticalStrut(50));

        // 4 rettangoli di selezione
        JPanel rettangoli = new JPanel(new GridLayout(1, 4, 20, 0));
        rettangoli.setOpaque(false);
        rettangoli.setMaximumSize(new Dimension(900, 220));

        String[] etichette = {"1 Giocatore", "2 Giocatori", "3 Giocatori", "4 Giocatori"};
        String[] emoji     = {"🎮", "👥", "👨‍👩‍👦", "🎉"};

        for (int i = 0; i < 4; i++) {
            final int numG = i + 1;
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(COLORI_GIOCATORI[i]);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(20, 20, 20, 20)
            ));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel emojiLabel = new JLabel(emoji[i]);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel testo = new JLabel(etichette[i]);
            testo.setFont(new Font("Arial Black", Font.BOLD, 18));
            testo.setForeground(Color.WHITE);
            testo.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createVerticalGlue());
            card.add(emojiLabel);
            card.add(Box.createVerticalStrut(12));
            card.add(testo);
            card.add(Box.createVerticalGlue());

            // Listener
            Color base   = COLORI_GIOCATORI[i];
            Color bright = base.brighter();
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { card.setBackground(bright); }
                public void mouseExited (MouseEvent e) { card.setBackground(base);   }
                public void mouseClicked(MouseEvent e) {
                    numeroGiocatori = numG;
                    avviaGioco();
                }
            });

            rettangoli.add(card);
        }

        contenuto.add(rettangoli);

        // Pannello wrapper per centrare il contenuto
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(contenuto);

        panel.add(centerWrapper, BorderLayout.CENTER);

        // Bottone Esci in basso a destra
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 20, 20));

        JButton bottonEsci = new JButton("Esci");
        bottonEsci.setFont(new Font("Arial", Font.BOLD, 16));
        bottonEsci.setBackground(new Color(231, 76, 60));
        bottonEsci.setForeground(Color.WHITE);
        bottonEsci.setFocusPainted(false);
        bottonEsci.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bottonEsci.setPreferredSize(new Dimension(120, 40));
        bottonEsci.addActionListener(e -> System.exit(0));

        bottomPanel.add(bottonEsci, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════

    // ════════════════════════════════ AVVIO GIOCO ════════════════════════════════════════
    static void avviaGioco() {
        punteggi          = new int[numeroGiocatori];
        giocatoreCorrente = 0;
        coppieRimaste     = (righe * colonne) / 2;

        JPanel gamePanel = creaSchermataDiGioco();
        mainPanel.add(gamePanel, "gioco");
        cardLayout.show(mainPanel, "gioco");
        aggiornaColoreSfondo();
    }

    // ════════════════════════════════════════════════════════════════════════

    // ═════════════════════════════════ SCHERMATA DI GIOCO ═══════════════════════════════════════
    static JPanel creaSchermataDiGioco() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SFONDO_DEFAULT);

        // Top panel con Score e bottone Indietro
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Score panel (nord-sinistra/centro)
        scorePanel = new JPanel(new GridLayout(1, numeroGiocatori));
        scorePanel.setOpaque(false);

        labelPunteggi = new JLabel[numeroGiocatori];
        String[] nomi = {"P1", "P2", "P3", "P4"};

        for (int i = 0; i < numeroGiocatori; i++) {
            JLabel lbl = new JLabel(nomi[i] + ": 0", SwingConstants.CENTER);
            lbl.setFont(new Font("Arial Black", Font.BOLD, 22));
            lbl.setForeground(COLORI_GIOCATORI[i]);
            lbl.setBorder(new EmptyBorder(10, 20, 10, 20));
            labelPunteggi[i] = lbl;
            scorePanel.add(lbl);
        }

        // Bottone Indietro (nord-destra)
        JButton bottonIndietro = new JButton("← Indietro");
        bottonIndietro.setFont(new Font("Arial", Font.BOLD, 14));
        bottonIndietro.setBackground(new Color(155, 89, 182));
        bottonIndietro.setForeground(Color.WHITE);
        bottonIndietro.setFocusPainted(false);
        bottonIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bottonIndietro.setPreferredSize(new Dimension(130, 40));
        bottonIndietro.setBorder(new EmptyBorder(5, 15, 5, 15));
        bottonIndietro.addActionListener(e -> tornaAllaScelta());

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRight.setOpaque(false);
        topRight.setBorder(new EmptyBorder(10, 10, 10, 20));
        topRight.add(bottonIndietro);

        // Mostra score panel solo se ci sono più giocatori
        if (numeroGiocatori > 1) {
            topPanel.add(scorePanel, BorderLayout.CENTER);
        }
        topPanel.add(topRight, BorderLayout.EAST);

        root.add(topPanel, BorderLayout.NORTH);

        // Griglia carte (centro)
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(righe, colonne, 10, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Carica retro
        retro = caricaImmagine("retro.png", maxSize, maxSize);

        // Crea coppie
        List<ImageIcon> simboli = new ArrayList<>();
        for (int i = 1; i <= (righe * colonne) / 2; i++) {
            ImageIcon img = caricaImmagine(i + ".png", maxSize, maxSize);
            simboli.add(img);
            simboli.add(img);
        }
        Collections.shuffle(simboli);
        valori  = simboli.toArray(new ImageIcon[0]);
        bottoni = new JButton[righe * colonne];

        for (int i = 0; i < bottoni.length; i++) {
            JButton b = new JButton();
            b.setIcon(retro);
            b.setPreferredSize(new Dimension(retro.getIconWidth(), retro.getIconHeight()));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBackground(Color.WHITE);

            int idx = i;
            b.addActionListener(e -> clickCarta(b, idx));
            bottoni[i] = b;
            grid.add(b);
        }

        gridPanel.add(grid);
        root.add(gridPanel, BorderLayout.CENTER);

        // Indicatore turno (sud)
        if (numeroGiocatori > 1) {
            JLabel turnoLabel = new JLabel("", SwingConstants.CENTER);
            turnoLabel.setName("turnoLabel");
            turnoLabel.setFont(new Font("Arial", Font.BOLD, 18));
            turnoLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
            root.add(turnoLabel, BorderLayout.SOUTH);
            aggiornaTurnoLabel(root);
        }

        return root;
    }

    // ════════════════════════════════════════════════════════════════════════

    // ═════════════════════════════════ LOGICA GIOCO ═══════════════════════════════════════
    static void clickCarta(JButton b, int index) {
        if (blocco || b.getIcon() != retro) return;
        b.setIcon(valori[index]);

        if (prima == null) {
            prima = b;
        } else {
            seconda = b;
            blocco  = true;
            Timer timer = new Timer(800, e -> controllaCoppia());
            timer.setRepeats(false);
            timer.start();
        }
    }

    static void controllaCoppia() {
        boolean match = prima.getIcon().equals(seconda.getIcon());

        if (match) {
            prima.setEnabled(false);
            seconda.setEnabled(false);
            punteggi[giocatoreCorrente]++;
            coppieRimaste--;
            aggiornaPunteggi();

            if (coppieRimaste == 0) {
                mostraVincitore();
                prima  = null;
                seconda = null;
                blocco = false;
                return;
            }
            // Se coppia trovata, lo stesso giocatore gioca ancora
        } else {
            prima.setIcon(retro);
            seconda.setIcon(retro);
            // Passa al prossimo giocatore
            if (numeroGiocatori > 1) {
                giocatoreCorrente = (giocatoreCorrente + 1) % numeroGiocatori;
                aggiornaColoreSfondo();
                aggiornaTurnoLabel(null);
            }
        }

        prima  = null;
        seconda = null;
        blocco = false;
    }

    // ════════════════════════════════════════════════════════════════════════

    // ═════════════════════════════════ AGGIORNAMENTI UI ═══════════════════════════════════════
    static Color schiarisciColore(Color c, float percentuale) {
        int r = (int) (c.getRed()   + (255 - c.getRed())   * percentuale);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * percentuale);
        int b = (int) (c.getBlue()  + (255 - c.getBlue())  * percentuale);
        return new Color(r, g, b);
    }
    
    static void aggiornaColoreSfondo() {
        if (numeroGiocatori <= 1) return;

        Color base = COLORI_GIOCATORI[giocatoreCorrente];
        Color sfondo = schiarisciColore(base, 0.65f); // percentuale di schiarimento

        Component comp = mainPanel.getComponent(mainPanel.getComponentCount() - 1);
        if (comp instanceof JPanel) {
            comp.setBackground(sfondo);
            aggiornaSfondoRicorsivo((JPanel) comp, sfondo);
        }
        mainPanel.repaint();
    }

    static void aggiornaSfondoRicorsivo(JPanel p, Color c) {
        p.setBackground(c);
        for (Component child : p.getComponents()) {
            if (child instanceof JPanel) {
                aggiornaSfondoRicorsivo((JPanel) child, c);
            }
        }
    }

    static void aggiornaTurnoLabel(JPanel root) {
        // Cerca il turnoLabel nel pannello gioco
        Component gameComp = mainPanel.getComponent(mainPanel.getComponentCount() - 1);
        if (!(gameComp instanceof JPanel)) return;
        JPanel gamePanel = (JPanel) gameComp;

        for (Component c : gamePanel.getComponents()) {
            if (c instanceof JLabel && "turnoLabel".equals(c.getName())) {
                String[] nomi = {"P1", "P2", "P3", "P4"};
                ((JLabel) c).setText("Turno di: " + nomi[giocatoreCorrente]);
                ((JLabel) c).setForeground(COLORI_GIOCATORI[giocatoreCorrente]);
                break;
            }
        }
    }

    static void aggiornaPunteggi() {
        if (numeroGiocatori <= 1 || labelPunteggi == null) return;
        String[] nomi = {"P1", "P2", "P3", "P4"};
        for (int i = 0; i < numeroGiocatori; i++) {
            labelPunteggi[i].setText(nomi[i] + ": " + punteggi[i]);
        }
    }

    // ════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════ FINE PARTITA ═════════════════════════════════════════
    static void mostraVincitore() {
        String[] nomi = {"P1", "P2", "P3", "P4"};
        StringBuilder msg = new StringBuilder();

        if (numeroGiocatori == 1) {
            msg.append("Hai trovato tutte le coppie! 🎉");
        } else {
            int maxPts = Arrays.stream(punteggi).max().getAsInt();
            List<String> vincitori = new ArrayList<>();
            for (int i = 0; i < numeroGiocatori; i++) {
                if (punteggi[i] == maxPts) vincitori.add(nomi[i]);
                msg.append(nomi[i]).append(": ").append(punteggi[i]).append(" coppie\n");
            }
            msg.append("\n");
            if (vincitori.size() == 1) {
                msg.append("🏆 Vince ").append(vincitori.get(0)).append("!");
            } else {
                msg.append("🤝 Pareggio tra: ").append(String.join(", ", vincitori));
            }
        }

        int risposta = JOptionPane.showConfirmDialog(frame,
            msg.toString() + "\n\nVuoi giocare ancora?",
            "Fine partita!", JOptionPane.YES_NO_OPTION);

        if (risposta == JOptionPane.YES_OPTION) {
            // Rigioca con lo stesso numero di giocatori
            mainPanel.remove(mainPanel.getComponent(mainPanel.getComponentCount() - 1));
            avviaGioco();
        } else {
            // Torna alla selezione (non esce più direttamente)
            tornaAllaScelta();
        }
    }

    // ════════════════════════════════════════════════════════════════════════

    // ═════════════════════════════════ UTILITY ═══════════════════════════════════════
    static void tornaAllaScelta() {
        // Rimuove il pannello di gioco e torna alla selezione
        mainPanel.remove(mainPanel.getComponent(mainPanel.getComponentCount() - 1));
        cardLayout.show(mainPanel, "selezione");
        
        // Reset variabili di gioco
        prima = null;
        seconda = null;
        blocco = false;
    }

    static ImageIcon caricaImmagine(String path, int maxW, int maxH) {
        ImageIcon icon = new ImageIcon(Memory.class.getResource(path));
        Image scaled = icon.getImage().getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}