package Dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class AboutDialog extends Dialog {
    public AboutDialog(Frame owner) {
        super(owner, "About", true);
        setLayout(new BorderLayout(15, 15));

        Panel contentPanel = new Panel(new BorderLayout(10, 10));
        contentPanel.add(new Label("Flight Simulator v1.0", Label.CENTER), BorderLayout.CENTER);
        contentPanel.add(new Label("Created by Andrija Ursl 2023/0200", Label.CENTER), BorderLayout.SOUTH);

        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
        setSize(300, 150);
        setLocationRelativeTo(owner);
    }
}
