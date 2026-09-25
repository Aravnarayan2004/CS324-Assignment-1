/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import common.*;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientGUI extends JFrame {

    private JTextField coordinatorField;
    private JTextField portField;

    private JComboBox<String> jobTypeBox;

    private JTextField startField;
    private JTextField endField;

    private JTextArea numbersArea;

    private JTextArea outputArea;

    private JButton submitButton;
    private JButton clearButton;

    private final ExecutorService clientPool =
            Executors.newFixedThreadPool(5);

    public ClientGUI() {

        setTitle(
                "CS324 Distributed System - Client"
        );

        setSize(650, 600);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        createGUI();
    }

    private void createGUI() {

        JPanel main =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        main.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        // ---------------------------------------------
        // Connection panel
        // ---------------------------------------------

        JPanel connectionPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                5,
                                5
                        )
                );

        connectionPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Coordinator Connection"
                )
        );

        connectionPanel.add(
                new JLabel(
                        "Coordinator Worker ID:"
                )
        );

        coordinatorField =
                new JTextField("4");

        connectionPanel.add(
                coordinatorField
        );

        connectionPanel.add(
                new JLabel(
                        "Coordinator Port:"
                )
        );

        portField =
                new JTextField("2004");

        connectionPanel.add(
                portField
        );

        main.add(
                connectionPanel,
                BorderLayout.NORTH
        );

        // ---------------------------------------------
        // Job input panel
        // ---------------------------------------------

        JPanel jobPanel =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                5,
                                5
                        )
                );

        jobPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Job"
                )
        );

        jobPanel.add(
                new JLabel(
                        "Job Type:"
                )
        );

        jobTypeBox =
                new JComboBox<>(
                        new String[]{
                                "MAX",
                                "PRIMESUM",
                                "PRIMECOUNT"
                        }
                );

        jobPanel.add(
                jobTypeBox
        );

        jobPanel.add(
                new JLabel(
                        "Start:"
                )
        );

        startField =
                new JTextField("1");

        jobPanel.add(
                startField
        );

        jobPanel.add(
                new JLabel(
                        "End:"
                )
        );

        endField =
                new JTextField("1000");

        jobPanel.add(
                endField
        );

        jobPanel.add(
                new JLabel(
                        "Numbers:"
                )
        );

        numbersArea =
                new JTextArea(
                        "10, 20, 5, 100, 50"
                );

        numbersArea.setRows(3);

        jobPanel.add(
                new JScrollPane(
                        numbersArea
                )
        );

        main.add(
                jobPanel,
                BorderLayout.CENTER
        );

        // ---------------------------------------------
        // Buttons
        // ---------------------------------------------

        JPanel buttonPanel =
                new JPanel();

        submitButton =
                new JButton(
                        "Submit Job"
                );

        clearButton =
                new JButton(
                        "Clear"
                );

        buttonPanel.add(
                submitButton
        );

        buttonPanel.add(
                clearButton
        );

        // ---------------------------------------------
        // Output
        // ---------------------------------------------

        outputArea =
                new JTextArea();

        outputArea.setEditable(false);

        outputArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        13
                )
        );

        JPanel outputPanel =
                new JPanel(
                        new BorderLayout()
                );

        outputPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Results"
                )
        );

        outputPanel.add(
                new JScrollPane(
                        outputArea
                ),
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new BorderLayout()
                );

        bottom.add(
                buttonPanel,
                BorderLayout.NORTH
        );

        bottom.add(
                outputPanel,
                BorderLayout.CENTER
        );

        main.add(
                bottom,
                BorderLayout.SOUTH
        );

        // ---------------------------------------------
        // Button events
        // ---------------------------------------------

        submitButton.addActionListener(
                e -> submitJob()
        );

        clearButton.addActionListener(
                e -> {

                    numbersArea.setText("");

                    startField.setText("1");

                    endField.setText("1000");

                    outputArea.setText("");
                }
        );

        setContentPane(main);
    }

    // ---------------------------------------------
    // Submit job
    // ---------------------------------------------

    private void submitJob() {

        submitButton.setEnabled(false);

        clientPool.submit(() -> {

            try {

                int coordinatorId =
                        Integer.parseInt(
                                coordinatorField.getText()
                        );

                int port =
                        Integer.parseInt(
                                portField.getText()
                        );

                String type =
                        (String)
                                jobTypeBox.getSelectedItem();

                Job job;

                // -------------------------------------
                // MAX
                // -------------------------------------

                if (type.equals("MAX")) {

                    int[] numbers =
                            parseNumbers(
                                    numbersArea.getText()
                            );

                    job =
                            new Job(
                                    JobType.MAX,
                                    numbers
                            );

                }

                // -------------------------------------
                // PRIMESUM
                // -------------------------------------

                else if (
                        type.equals("PRIMESUM")) {

                    int start =
                            Integer.parseInt(
                                    startField.getText()
                            );

                    int end =
                            Integer.parseInt(
                                    endField.getText()
                            );

                    job =
                            new Job(
                                    JobType.PRIMESUM,
                                    start,
                                    end
                            );

                }

                // -------------------------------------
                // PRIMECOUNT
                // -------------------------------------

                else {

                    int[] numbers =
                            parseNumbers(
                                    numbersArea.getText()
                            );

                    job =
                            new Job(
                                    JobType.PRIMECOUNT,
                                    numbers
                            );
                }

                // -------------------------------------
                // Connect to coordinator
                // -------------------------------------

                System.setProperty(
                        "java.rmi.server.hostname",
                        "127.0.0.1"
                );

                /*
                 * IMPORTANT:
                 *
                 * The Worker ID is NOT the hostname.
                 *
                 * Always connect using localhost /
                 * 127.0.0.1.
                 */

                Registry registry =
                        LocateRegistry.getRegistry(
                                "127.0.0.1",
                                port
                        );

                CoordinatorRemote coordinator =
                        (CoordinatorRemote)
                                registry.lookup(
                                        "WorkerService"
                                );

                appendOutput(
                        "Submitting " +
                        job.jobId +
                        " to Worker " +
                        coordinatorId
                );

                JobResult result =
                        coordinator.submitJob(job);

                appendOutput(
                        job.jobId +
                        " -> RESULT = " +
                        result.result
                );

            } catch (Exception e) {

                appendOutput(
                        "ERROR: " +
                        e.getMessage()
                );
            }

            SwingUtilities.invokeLater(
                    () ->
                            submitButton.setEnabled(
                                    true
                            )
            );
        });
    }

    // ---------------------------------------------
    // Parse numbers
    // ---------------------------------------------

    private int[] parseNumbers(
            String text) {

        if (text == null ||
                text.trim().isEmpty()) {

            return new int[0];
        }

        String[] parts =
                text.trim().split(
                        "[,\\s]+"
                );

        int[] numbers =
                new int[parts.length];

        for (int i = 0;
                i < parts.length;
                i++) {

            numbers[i] =
                    Integer.parseInt(
                            parts[i]
                    );
        }

        return numbers;
    }

    // ---------------------------------------------
    // Display output
    // ---------------------------------------------

    private void appendOutput(
            String text) {

        SwingUtilities.invokeLater(
                () -> {

                    outputArea.append(
                            text +
                            "\n"
                    );

                    outputArea.setCaretPosition(
                            outputArea.getDocument()
                                    .getLength()
                    );
                }
        );
    }

    // ---------------------------------------------
    // Main
    // ---------------------------------------------

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    ClientGUI gui =
                            new ClientGUI();

                    gui.setVisible(true);
                }
        );
    }
}
