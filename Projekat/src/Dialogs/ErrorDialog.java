package Dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ErrorDialog extends Dialog {

    public ErrorDialog(Frame owner, String message, String errorCode) {
        super(owner, "Error", true); // `true` makes it a modal dialog
        setLayout(new BorderLayout(10, 10));

        // Use a panel to add some padding around the content
        Panel contentPanel = new Panel(new BorderLayout(10, 10));
        
        // A panel for the text messages, arranged vertically
        Panel textPanel = new Panel(new GridLayout(2, 1));
        textPanel.add(new Label(message, Label.CENTER));
        textPanel.add(new Label("Error Code: " + errorCode, Label.CENTER));
        contentPanel.add(textPanel, BorderLayout.CENTER);

        // A panel for the OK button
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> dispose()); // `dispose()` closes the dialog
        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel); // Add the padded panel to the dialog

        // Add a listener to handle the user clicking the 'X' button on the window
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        setSize(350, 150);
        // This is important: it centers the dialog over your main application window
        setLocationRelativeTo(owner);
    }
	

}
