package Dialogs;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WarningDialog extends Dialog {
	 public WarningDialog(Frame owner, Runnable onContinue, Runnable onLogout) {
	        super(owner, "Session Timeout Warning", true); // `true` makes it a modal dialog
	        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
	        add(new Label("Your session is about to expire. Continue?"));

	        Button yesButton = new Button("Yes, Continue");
	        Button noButton = new Button("No, Logout");

	        // When "Yes" is clicked, close the dialog and run the 'onContinue' action.
	        yesButton.addActionListener(e -> {
	            dispose();
	            onContinue.run();
	        });

	        // When "No" is clicked, close the dialog and run the 'onLogout' action.
	        noButton.addActionListener(e -> {
	            dispose();
	            onLogout.run();
	        });

	        add(yesButton);
	        add(noButton);

	        // Also treat closing the dialog window as a "Logout" action.
	        addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent we) {
	                dispose();
	                onLogout.run();
	            }
	        });
	        
	        setSize(350, 120);
	        setLocationRelativeTo(owner);
	    }
}
