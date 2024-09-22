package dev.ivank;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
            if (btnNumber > 0) {
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

        renderValueButtons(btnValues, valuesPanel);

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
        sort.setMaximumSize(new Dimension(75, 25));
        sort.setPreferredSize(new Dimension(75, 25));

        optionsPanel.add(sort);

        optionsPanel.add(Box.createVerticalStrut(5));

        JButton reset = createButton("Reset", e -> {
            jFrame.dispose();
            btnValues.clear();
            displayIntroScreen();
        }, Color.GREEN);

        reset.setAlignmentX(Component.RIGHT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(75, 25));
        reset.setPreferredSize(new Dimension(75, 25));

        optionsPanel.add(reset);

        optionsPanel.add(Box.createVerticalGlue());

        mainPanel.add(valuesPanel);
        mainPanel.add(Box.createHorizontalGlue());
        mainPanel.add(optionsPanel);

        jFrame.add(mainPanel);
        jFrame.setVisible(true);
    }

    private static void renderValueButtons(List<Integer> values, JPanel valuesPanel) {
        valuesPanel.removeAll();

        final int BUTTONS_PER_ROW = 10;

        for (int i = 0; i < values.size(); i += BUTTONS_PER_ROW) {
            final JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(BUTTONS_PER_ROW, 1));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);

            final int toIndex = Math.min(i + BUTTONS_PER_ROW, values.size());

            values.subList(i, toIndex).stream().map(val -> {
                final JButton button = new JButton(val.toString());
                button.setBackground(Color.BLUE);
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> {
                    if (val <= 30) {
                        List<Integer> newNumbers = getValues(btnNumber);

                        btnValues.addAll(newNumbers);

                        renderValueButtons(btnValues, valuesPanel);

                        valuesPanel.revalidate();
                        valuesPanel.repaint();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Please select a value smaller or equal to 30.",
                                "Invalid Selection",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
                return button;
            }).forEach(panel::add);

            valuesPanel.add(panel);
        }
    }

    private static List<Integer> getValues(int btnNumber) {
        final Random random = new Random();
        final List<Integer> numbers = new ArrayList<>();
        boolean hasValueLessThanOrEqualTo30 = false;

        for (int i = 0; i < btnNumber; i ++) {
            final int value = random.nextInt(1000) + 1;
            if (value <= 30) {
                hasValueLessThanOrEqualTo30 = true;
            }
            numbers.add(value);
        }

        if (!hasValueLessThanOrEqualTo30) {
            numbers.set(random.nextInt(btnNumber), random.nextInt(30) + 1);
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
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(500, 300);
        // jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        return jFrame;
    }

    private static class SortWorker extends SwingWorker<Void, List<Integer>> {
        private final List<Integer> values;
        private final JPanel valuesPanel;
        private final boolean isIncreasing;

        public SortWorker(List<Integer> values, JPanel valuesPanel, boolean isIncreasing) {
            this.values = values;
            this.valuesPanel = valuesPanel;
            this.isIncreasing = isIncreasing;
        }

        @Override
        protected Void doInBackground() {
            quickSort(values, 0, values.size() - 1, isIncreasing);
            return null;
        }

        private void quickSort(List<Integer> list, int low, int high, boolean isIncreasing) {
            if (low < high) {
                int pi = partition(list, low, high, isIncreasing);
                publish(new ArrayList<>(list));
                try {
                    // A little delay for visualization purposes
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                quickSort(list, low, pi - 1, isIncreasing);
                publish(new ArrayList<>(list));
                try {
                    Thread.sleep(150);
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
            renderValueButtons(updatedValues, valuesPanel);
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