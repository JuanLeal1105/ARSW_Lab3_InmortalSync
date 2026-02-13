package edu.eci.arsw.highlandersim;

import edu.eci.arsw.immortals.Immortal;
import edu.eci.arsw.immortals.ImmortalManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public final class ControlFrame extends JFrame {
  private ImmortalManager manager;
  private final JTextArea output = new JTextArea(14, 40);
  private final JButton startBtn = new JButton("Start");
  private final JButton pauseAndCheckBtn = new JButton("Pause & Check");
  private final JButton resumeBtn = new JButton("Resume");
  private final JButton stopBtn = new JButton("Stop");

  private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 5000, 1));
  private final JSpinner healthSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
  private final JSpinner damageSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
  private final JComboBox<String> fightMode = new JComboBox<>(new String[]{"ordered", "naive"});

  public ControlFrame(int count, String fight) {
    setTitle("Highlander Simulator — ARSW");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel("Count:"));
    countSpinner.setValue(count);
    top.add(countSpinner);
    top.add(new JLabel("Health:"));
    top.add(healthSpinner);
    top.add(new JLabel("Damage:"));
    top.add(damageSpinner);
    top.add(new JLabel("Fight:"));
    fightMode.setSelectedItem(fight);
    top.add(fightMode);
    add(top, BorderLayout.NORTH);

    output.setEditable(false);
    output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    add(new JScrollPane(output), BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottom.add(startBtn);
    bottom.add(pauseAndCheckBtn);
    bottom.add(resumeBtn);
    bottom.add(stopBtn);
    add(bottom, BorderLayout.SOUTH);

    startBtn.addActionListener(this::onStart);
    pauseAndCheckBtn.addActionListener(this::onPauseAndCheck);
    resumeBtn.addActionListener(this::onResume);
    stopBtn.addActionListener(this::onStop);

    pack();
    setLocationByPlatform(true);
    setVisible(true);
  }

  private void onStart(ActionEvent e) {
    safeStop();
    int n = (Integer) countSpinner.getValue();
    int health = (Integer) healthSpinner.getValue();
    int damage = (Integer) damageSpinner.getValue();
    String fight = (String) fightMode.getSelectedItem();
    System.setProperty("fight", fight);
    manager = new ImmortalManager(n, fight, health, damage);
    manager.start();
    output.setText("Started: %d immortals, %d health, %d damage\n".formatted(n, health, damage));
  }

  private void onPauseAndCheck(ActionEvent e) {
    if (manager == null) return;
    manager.pause();

    List<Immortal> snapshot = manager.populationSnapshot();
    int vivos = manager.aliveCount();
    int muertos = manager.deadCount();
    long saludTotal = manager.totalHealth();
    long numBatallas = manager.scoreBoard().totalFights();

    StringBuilder sb = new StringBuilder();
    sb.append("--- SNAPSHOT ---\n");
    for (Immortal im : snapshot) {
      sb.append(String.format("%-14s : %5d HP%n", im.name(), im.getHealth()));
    }
    sb.append("--------------------------------\n");
    sb.append(String.format("Total Health  : %d%n", saludTotal));
    sb.append(String.format("Battles     : %d%n", numBatallas));
    sb.append(String.format("Alive        : %d%n", vivos));
    sb.append(String.format("Death     : %d%n", muertos));
    sb.append("--------------------------------\n");
    output.setText(sb.toString());

    manager.pruneDead();
  }

  private void onResume(ActionEvent e) {
    if (manager != null) manager.resume();
  }

  private void onStop(ActionEvent e) {
    if (manager == null) return;
    manager.pause();
    int option = javax.swing.JOptionPane.showConfirmDialog(
            this, "¿Do you want to keep fighting?", "Confirm Exit",
            javax.swing.JOptionPane.YES_NO_OPTION);

    if (option == javax.swing.JOptionPane.NO_OPTION) {
      safeStop();
      output.setText("Simulation Ended.");
    } else {
      manager.resume();
    }
  }

  private void safeStop() {
    if (manager != null) {
      manager.stop();
      manager = null;
    }
  }
}
