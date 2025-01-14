package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data_access.PokemonApiCallInterface;
import data_access.PokemonListFromSpritesInterface;
import entity.Pokemon;
import use_case.InitializePokemonObjectsInterface;
import use_case.InitializeRunGameInterface;

/**
 * Interface for the view where you select Pokemons
 */
public class GameView extends JPanel {
    private JFrame frame;
    private PokemonApiCallInterface apiDataAccess;
    private PokemonListFromSpritesInterface spritesDataAccess;

    // Constructor with dependency injection
    public GameView(PokemonApiCallInterface apiDataAccess, PokemonListFromSpritesInterface spritesDataAccess) {
        this.apiDataAccess = apiDataAccess;
        this.spritesDataAccess = spritesDataAccess;
    }

    public void startGame(JFrame frame) {
        this.frame = frame;

        // Remove existing components
        frame.getContentPane().removeAll();
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();

        // Custom panel with background image
        JPanel backgroundPanel = new MenuView.BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());

        Pokemon[] allPokemonsObjects = InitializePokemonObjectsInterface.GetPokemonObjects(apiDataAccess, spritesDataAccess);

        displayPokemonSelection(allPokemonsObjects);
    }

    private void displayPokemonSelection(Pokemon[] allPokemons) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left Panel with a scroll bar for all Pokemon
        JPanel leftPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 3 columns, gaps of 10 pixels
        JScrollPane leftScrollPane = new JScrollPane(leftPanel);
        leftScrollPane.setPreferredSize(new Dimension(frame.getWidth() / 3, frame.getHeight()));

        // Left Label
        JLabel leftLabel = createLabel("Please select 6 Pokemon!");
        JPanel leftLabelPanel = new JPanel(new BorderLayout());
        leftLabelPanel.add(leftLabel, BorderLayout.NORTH);
        leftLabelPanel.add(leftScrollPane, BorderLayout.CENTER);
        mainPanel.add(leftLabelPanel, BorderLayout.WEST);

        // Right Panel for selected Pokemon
        JPanel rightPanel = new JPanel(new GridLayout(0, 3, 5, 10)); // 3 columns, gaps of 10 pixels
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setPreferredSize(new Dimension(frame.getWidth() / 2, frame.getHeight()));

        // Right Label
        JLabel rightLabel = createLabel("Chosen Pokemon");
        JPanel rightLabelPanel = new JPanel(new BorderLayout());
        rightLabelPanel.add(rightLabel, BorderLayout.NORTH);
        rightLabelPanel.add(rightScrollPane, BorderLayout.CENTER);
        mainPanel.add(rightLabelPanel, BorderLayout.EAST);

        // Track selected Pokemon to avoid duplicates
        List<Pokemon> selectedPokemonList = new ArrayList<>();

        // Add all Pokemon to the left panel
        for (Pokemon pokemon : allPokemons) {
            JPanel pokemonPanel = createPokemonPanel(pokemon, rightPanel, selectedPokemonList);
            leftPanel.add(pokemonPanel);
        }

        // Button to proceed if exactly 6 Pokemon are selected
        JButton proceedButton = new JButton("Proceed");
        proceedButton.addActionListener(e -> {
            if (selectedPokemonList.size() == 6) {
                InitializeRunGameInterface.InitializeRunGame(allPokemons, selectedPokemonList, frame);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select exactly 6 Pokemon.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mainPanel.add(proceedButton, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    private JPanel createPokemonPanel(Pokemon pokemon, JPanel rightPanel, List<Pokemon> selectedPokemonList) {
        JPanel pokemonPanel = new JPanel();
        pokemonPanel.setLayout(new BoxLayout(pokemonPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(pokemon.getName());
        JLabel spriteLabel = new JLabel(new ImageIcon(pokemon.getFrontSprite()));
        JLabel statsLabel = new JLabel("<html><u>Stats</u></html>");
        JLabel healthLabel = new JLabel("Health: " + pokemon.getHealth());
        JLabel attackLabel = new JLabel("Attack: " + pokemon.getAttack());
        JLabel defenseLabel = new JLabel("Defense: " + pokemon.getDefense());

        JButton selectButton = new JButton("Select Pokemon");
        selectButton.addActionListener(e -> {
            if (selectedPokemonList.size() < 6 && !selectedPokemonList.contains(pokemon)) {
                // Add to the right panel if not already selected and less than 6 selected
                rightPanel.add(createSelectedPokemonPanel(pokemon, rightPanel, selectedPokemonList));
                rightPanel.revalidate();
                rightPanel.repaint();
            } else if (selectedPokemonList.contains(pokemon)) {
                // Alert the user that the Pokemon is already selected
                JOptionPane.showMessageDialog(frame, "You've already selected this Pokemon.", "Duplicate Selection", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Alert the user that they can't select more than 6 Pokemon
                JOptionPane.showMessageDialog(frame, "You can't select more than 6 Pokemon.", "Selection Limit", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pokemonPanel.add(nameLabel);
        pokemonPanel.add(spriteLabel);
        pokemonPanel.add(statsLabel);
        pokemonPanel.add(healthLabel);
        pokemonPanel.add(attackLabel);
        pokemonPanel.add(defenseLabel);
        pokemonPanel.add(selectButton);

        return pokemonPanel;
    }

    private JPanel createSelectedPokemonPanel(Pokemon pokemon, JPanel rightPanel, List<Pokemon> selectedPokemonList) {
        JPanel selectedPokemonPanel = new JPanel();
        selectedPokemonPanel.setLayout(new BoxLayout(selectedPokemonPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(pokemon.getName());
        JLabel spriteLabel = new JLabel(new ImageIcon(pokemon.getFrontSprite()));
        JLabel statsLabel = new JLabel("<html><u>Stats</u></html>");
        JLabel healthLabel = new JLabel("Health: " + pokemon.getHealth());
        JLabel attackLabel = new JLabel("Attack: " + pokemon.getAttack());
        JLabel defenseLabel = new JLabel("Defense: " + pokemon.getDefense());

        JButton deselectButton = new JButton("Deselect Pokemon");
        deselectButton.addActionListener(e -> {
            // Remove from the selected list and the right panel
            selectedPokemonList.remove(pokemon);
            rightPanel.remove(selectedPokemonPanel);
            rightPanel.revalidate();
            rightPanel.repaint();
        });

        selectedPokemonPanel.add(nameLabel);
        selectedPokemonPanel.add(spriteLabel);
        selectedPokemonPanel.add(statsLabel);
        selectedPokemonPanel.add(healthLabel);
        selectedPokemonPanel.add(attackLabel);
        selectedPokemonPanel.add(defenseLabel);
        selectedPokemonPanel.add(deselectButton);

        // Add to the selected list
        selectedPokemonList.add(pokemon);

        return selectedPokemonPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }

    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = ImageIO.read(new File("UIAssets/pokemonBackground.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
