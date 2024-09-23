package dev.ivank;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Ivan Krylosov
 */
public class QuickSortSwingApp {
    private static SortWorker sortWorker;
    private static int btnNumber;
    private static List<Integer> btnValues;

    private static final Random RANDOM = new Random();

    private static final int INIT_SCREEN_WIDTH = 500;
    private static final int INIT_SCREEN_HEIGHT = 300;
    private static final int MAX_BUTTON_SIZE = 75;
    private static final int BUTTON_HEIGHT = 25;
    /**
     * This should be the number of buttons per column
     */
    private static final int BUTTONS_PER_COL = 10;
    private static final int MAX_RANDOM_VALUE = 1000;
    private static final int MAX_INPUT_NUMBER = 1000;
    private static final int MIN_RANDOM_VALUE = 1;
    private static final int SLEEP_DELAY = 150;
    private static final int VALUE_THRESHOLD = 30;

    public static void main(String[] args) {
        displayIntroScreen();
    }

    private static void displayIntroScreen() {
        final JFrame jFrame = createJFrame();

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalGlue());

        final JLabel textLabel = new JLabel("How many numbers to display?");
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(textLabel);

        final JTextField textField = createNumericInputField();
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(textField);

        final JButton jButton = createButton("Enter", e -> {
            btnNumber = Integer.parseInt(textField.getText());
            if (btnNumber > 0 && btnNumber <= MAX_INPUT_NUMBER) {
                jFrame.dispose();
                displaySortScreen(btnNumber);
            }
        }, Color.BLUE);

        jButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(jButton);

        mainPanel.add(Box.createVerticalGlue());

        jFrame.add(mainPanel);
        jFrame.setVisible(true);
    }

    private static void displaySortScreen(int btnNumber) {
        final JFrame jFrame = createJFrame();

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        final JPanel valuesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnValues = getValues(btnNumber);

        renderValueButtons(valuesPanel, btnValues);

        final JScrollPane scrollPane = new JScrollPane(valuesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setPreferredSize(new Dimension(120, 100));

        optionsPanel.add(Box.createVerticalStrut(5));

        final boolean[] isAscending = {false};

        JButton sort = createButton("Sort", e -> {
            if (sortWorker == null || sortWorker.isDone()) {
                sortWorker = new SortWorker(btnValues, valuesPanel, isAscending[0]);
                isAscending[0] = !isAscending[0];
            }
            sortWorker.execute();
        }, Color.GREEN);

        sort.setAlignmentX(Component.RIGHT_ALIGNMENT);
        sort.setMaximumSize(new Dimension(MAX_BUTTON_SIZE, BUTTON_HEIGHT));
        sort.setPreferredSize(new Dimension(MAX_BUTTON_SIZE, BUTTON_HEIGHT));

        optionsPanel.add(sort);

        optionsPanel.add(Box.createVerticalStrut(5));

        JButton reset = createButton("Reset", e -> {
            jFrame.dispose();
            if (sortWorker != null) {
                sortWorker.cancel(true);
            }
            btnValues.clear();
            displayIntroScreen();
        }, Color.GREEN);

        reset.setAlignmentX(Component.RIGHT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(MAX_BUTTON_SIZE, BUTTON_HEIGHT));
        reset.setPreferredSize(new Dimension(MAX_BUTTON_SIZE, BUTTON_HEIGHT));

        optionsPanel.add(reset);

        optionsPanel.add(Box.createVerticalGlue());

        mainPanel.add(scrollPane);
        mainPanel.add(Box.createHorizontalGlue());
        mainPanel.add(optionsPanel);

        jFrame.add(mainPanel);
        jFrame.pack(); // Screen resizes to the space occupied by generated btns
        jFrame.setVisible(true);
    }
    
    private static void renderValueButtons(JPanel valuesPanel, List<Integer> values) {
        renderValueButtons(valuesPanel, values, values);
    }

    private static void renderValueButtons(JPanel valuesPanel, List<Integer> values, List<Integer> originalValues) {
        valuesPanel.removeAll();

        final int numChunks = (int) Math.ceil((double) values.size() / BUTTONS_PER_COL);

        for (int i = 0; i < numChunks; i ++) {
            final JPanel panel = new JPanel(new GridLayout(BUTTONS_PER_COL, 1));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);

            int startIndex = i * BUTTONS_PER_COL;
            int endIndex = Math.min(startIndex + BUTTONS_PER_COL, values.size());

            for (int j = startIndex; j < endIndex; j ++) {
                Integer value = values.get(j);
                Integer originalValue = originalValues.get(j);

                JButton button = getValueButton(valuesPanel, value, originalValue);
                panel.add(button);
            }
            valuesPanel.add(panel);
        }
        valuesPanel.revalidate();
        valuesPanel.repaint();
    }

    private static JButton getValueButton(JPanel valuesPanel, Integer value, Integer originalValue) {
        JButton button = new JButton(value.toString());
        button.setBackground(value.equals(originalValue) ? Color.BLUE : Color.RED);
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> {
            if (value <= VALUE_THRESHOLD) {
                btnValues = getValues(btnNumber);
                renderValueButtons(valuesPanel, btnValues, btnValues);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Please select a value smaller or equal to 30.",
                        "Invalid Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        return button;
    }

    private static List<Integer> getValues(int btnNumber) {
        final List<Integer> numbers = new ArrayList<>();
        boolean hasValueLessThanOrEqualTo30 = false;

        for (int i = 0; i < btnNumber; i ++) {
            final int value = RANDOM.nextInt(MAX_RANDOM_VALUE) + MIN_RANDOM_VALUE;
            if (value <= VALUE_THRESHOLD) {
                hasValueLessThanOrEqualTo30 = true;
            }
            numbers.add(value);
        }

        if (!hasValueLessThanOrEqualTo30) {
            numbers.set(RANDOM.nextInt(btnNumber), RANDOM.nextInt(VALUE_THRESHOLD) + MIN_RANDOM_VALUE);
        }

        return numbers;
    }

    private static JTextField createNumericInputField() {
        final JTextField textField = new JTextField(10);
        ((PlainDocument) textField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        textField.setMaximumSize(textField.getPreferredSize());
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        return textField;
    }

    private static JButton createButton(String text, ActionListener action, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    private static JFrame createJFrame() {
        final JFrame jFrame = new JFrame("Numbers Quicksort Swing app");
        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jFrame.setSize(INIT_SCREEN_WIDTH, INIT_SCREEN_HEIGHT);
        jFrame.setLocationRelativeTo(null);
        return jFrame;
    }

    private static class SortWorker extends SwingWorker<Void, List<Integer>> {
        private final List<Integer> values;
        private List<Integer> originalValues;
        private final JPanel valuesPanel;
        private final boolean isIncreasing;

        public SortWorker(List<Integer> values, JPanel valuesPanel, boolean isIncreasing) {
            this.values = values;
            this.originalValues = new ArrayList<>(values);
            this.valuesPanel = valuesPanel;
            this.isIncreasing = isIncreasing;
        }

        @Override
        protected Void doInBackground() {
            quickSort(values, 0, values.size() - 1, isIncreasing);
            JOptionPane.showMessageDialog(null,
                    "Sorting numbers has finished.",
                    "QuickSort",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        private void quickSort(List<Integer> list, int low, int high, boolean isIncreasing) {
            if (low < high) {
                int pi = partition(list, low, high, isIncreasing);
                publish(new ArrayList<>(list));
                try {
                    // A little delay for visualization purposes
                    Thread.sleep(SLEEP_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                quickSort(list, low, pi - 1, isIncreasing);
                publish(new ArrayList<>(list));
                try {
                    Thread.sleep(SLEEP_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                quickSort(list, pi + 1, high, isIncreasing);
            }
        }

        private int partition(List<Integer> list, int low, int high, boolean isIncreasing) {
            int pivot = list.get(high);
            int i = (low - 1);

            for (int j = low; j < high; j ++) {
                if (isIncreasing ? list.get(j) <= pivot : list.get(j) >= pivot) {
                    i ++;
                    int temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }

            int temp = list.get(i + 1);
            list.set(i + 1, list.get(high));
            list.set(high, temp);

            return i + 1;
        }

        @Override
        protected void process(List<List<Integer>> chunks) {
            updateButtons(chunks.get(chunks.size() - 1));
        }

        private void updateButtons(List<Integer> updatedValues) {
            renderValueButtons(valuesPanel, updatedValues, originalValues);
            originalValues = updatedValues;
            valuesPanel.revalidate();
            valuesPanel.repaint();
        }
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && isNumeric(string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && isNumeric(text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isNumeric(String str) {
            return str.matches("\\d+");
        }
    }
}