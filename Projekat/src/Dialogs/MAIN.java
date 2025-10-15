package Dialogs;


import java.awt.AWTEvent;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import Aero.*;
import timer.SessionTImer;
import timer.SimulationClock;

public class MAIN extends Frame implements AWTEventListener,FlickerListener{
	   // --- Layout and Navigation Components ---
    private CardLayout cardLayout;
    private Panel cardPanel; // The panel that holds the "cards" (simulation, airports, flights)
    private Panel navigationPanel; // The panel for the buttons at the bottom
    
    private MenuItem openItem;
    private MenuItem saveAsItem;
    private String currentView = "SIM"; // Default view
	
	//Add airport	
	private Panel airportsPanel=new Panel();
	private Panel airportsListPanel= new Panel();
	private TextField nameField= new TextField();
	private TextField codeField= new TextField();
	private TextField xField= new TextField();
	private TextField yField= new TextField();
	private Button addAirport=new Button("Add-Airport");
	
	private ArrayList<Airport> airports=new ArrayList<>();
	
	//Add flight
	private Panel flightsPanel=new Panel();
	private Panel flightsListPanel= new Panel();
	private TextField fromAirport= new TextField();
	private TextField toAirport= new TextField();
	private TextField lenght= new TextField();
	private TextField timeOfDeparture= new TextField();
	private Button addFlight=new Button("Add-Flight");
	
	private ArrayList<Flight> flights=new ArrayList<>();
	
    private Panel simulationPanel;

    private SessionTImer sessionTimer;
    
    private SimulationCanvas simulationCanvas;
	
    
    private SimulationClock simClock;
    private Label simClockLabel;
    private Button startSimButton, pauseSimButton, resetSimButton;
    
    
	MAIN(){			
			setResizable(true);
			setSize(1400,700);
			setTitle("Flights simulator");
			setLayout(new BorderLayout());
			
			
	        setupAirportsPanel();
	        setupFlightPanel();
	        setupNavigationPanel();
	        setupMenuBar();
	        
	        setupSimulationPanel();

	        simClock = new SimulationClock(simClockLabel);
	        simClock.start();

	        simulationCanvas = new SimulationCanvas(this.airports, this.flights, this, this.simClock);
	        simulationPanel.add(simulationCanvas, BorderLayout.CENTER);
	     
	        
	        sessionTimer=new SessionTImer(this);
	        sessionTimer.start();
	        
	      
	        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

			//Init some data
			airports.add(new Airport("Nikola Tesla","SRB",44.81, 20.29));
			airports.add(new Airport("Kita Kitic","KIT",45.81, 18.29));
			
			  // --- 1. Create the Card Panel that will hold the different views ---
	        cardLayout = new CardLayout();
	        cardPanel = new Panel(cardLayout);

	        // --- 2. Setup each main panel (but don't add them to the frame yet) ---




	        // --- 3. Add each main panel to the Card Panel with a unique name ---
	        cardPanel.add(simulationPanel, "SIM");
	        cardPanel.add(airportsPanel, "AIRPORTS");
	        cardPanel.add(flightsPanel, "FLIGHTS");

	        // --- 4. Add the Card Panel to the center of the main window ---
	        add(cardPanel, BorderLayout.CENTER);

	        // --- 5. Create the navigation buttons at the bottom ---
	        add(navigationPanel, BorderLayout.SOUTH);
			
			//populateWindow();
			//showHelpDialog();
			//pack();
			
			//addComponentListener(new ComponentAdapter() {
			//	public void componentResized(ComponentEvent e) {
			//		scene.packScene();
			//		scene.repaint();
			//		pack();
			//	}
		//	});
			
	        addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                sessionTimer.stop();
	                simClock.interrupt(); // Cleanly stop the clock thread
	                dispose();
	            }
	        });
			
			setVisible(true);
	
	}
    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        // --- CHANGE: Create and add Open and Save As items ---
        openItem = new MenuItem("Open...");
        openItem.addActionListener(e -> openFile());
        openItem.setEnabled(false); // Disabled by default

        saveAsItem = new MenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveFile());
        saveAsItem.setEnabled(false); // Disabled by default

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator(); // Adds a line for visual separation
        fileMenu.add(exitItem);

        // --- About Menu (unchanged) ---
        Menu aboutMenu = new Menu("About");
        MenuItem aboutItem = new MenuItem("About App");
        aboutItem.addActionListener(e -> new AboutDialog(this).setVisible(true));
        aboutMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        setMenuBar(menuBar);
    }
    
    
    private void openFile() {
        FileDialog fd = new FileDialog(this, "Open " + currentView, FileDialog.LOAD);
        fd.setFile("*.csv");
        fd.setVisible(true);

        if (fd.getFile() == null) return; // User cancelled

        String filename = fd.getDirectory() + fd.getFile();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            if (currentView.equals("AIRPORTS")) {
                airports.clear();
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
	                    	String name = parts[0];
	                    	for(Airport a:airports) {
	                    		if(a.getNaziv()==name) {
	                  	        	 ErrorDialog error=new ErrorDialog(this, "You have entered a duplicate entry "+name, "code07");
	                	        	 error.setVisible(true);
	                	        	 return;
	                    		}
	                    	}
	            	        String code = parts[1];
	            	        for(Airport a:airports) {
	                    		if(a.getKod()==code) {
	                  	        	 ErrorDialog error=new ErrorDialog(this, "You have entered a duplicate entry code "+code, "code08");
	                	        	 error.setVisible(true);
	                	        	 return;
	                    		}
	                    	}
	            	        double x = Double.parseDouble(parts[2]);
	            	        double y = Double.parseDouble(parts[3]);
	            	        if(name.isEmpty()||code.isEmpty()) {
	            	        	 ErrorDialog error=new ErrorDialog(this, "Text fields MUST NOT be empty", "code01");
	            	        	 error.setVisible(true);
	            	        	 return;
	            	        }
	            	        if(!code.matches("^[A-Z]{3}$")) {
	            	        	 ErrorDialog error=new ErrorDialog(this, "Code MUST be 3 UPPERCASE LETTERS long", "code02");
	            	        	 error.setVisible(true);
	            	        	 return;
	            	        }
	            	        if(x<-90 ||x>90 || y<-90 || y>90) {
	            	        	 ErrorDialog error=new ErrorDialog(this, "Coordinates must be between -90 and 90 degrees", "code05");
	            	        	 error.setVisible(true);
	            	        	 return;
	            	        }
	                        airports.add(new Airport(parts[0], parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                    	
                    }
                    else {
                    ErrorDialog error=new ErrorDialog(this, "You must have only 4 rows", "code06");
       	        	 error.setVisible(true);
       	        	 return;
                    }
                }
                refreshAirportList();
                simulationCanvas.repaint();
            } else if (currentView.equals("FLIGHTS")) {
                flights.clear();
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                    	String from=parts[0];
                    	String to=parts[1];
                    	String timeOfD=parts[2];
                    	String leng=parts[3];
                    	// 1. Check for any empty fields first.
                	    if (from.isEmpty() || to.isEmpty() || timeOfD.isEmpty() || leng.isEmpty()) {
                	        new ErrorDialog(this, "All fields are required.", "INPUT-006").setVisible(true);
                	        return;
                	    }

                	    // 2. Validate the date format.
                	    if (!timeOfD.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                	        new ErrorDialog(this, "Time of departure must be in HH:mm format (e.g., 08:30).", "TIME-001").setVisible(true);
                	        return;
                	    }
                	    
                	    // 3. Validate the length (format and value).
                	    int lengthValue; // Declare it here so we can use it later.
                	    try {
                	        lengthValue = Integer.parseInt(parts[3]);
                	        if (lengthValue <= 0) {
                	            new ErrorDialog(this, "Length must be a positive number.", "INPUT-007").setVisible(true);
                	            return;
                	        }
                	    } catch (NumberFormatException e) {
                	        new ErrorDialog(this, "Invalid flight length. Please enter a whole number.", "INPUT-004").setVisible(true);
                	        return;
                	    }
                        flights.add(new Flight(parts[0], parts[1], parts[2], Integer.parseInt(parts[3])));
                    }
                    else {
                        ErrorDialog error=new ErrorDialog(this, "You must have only 4 rows", "code06");
          	        	 error.setVisible(true);
          	        	 return;
                    }
                }
                refreshFlightList();
            }
        } catch (IOException e) {
            new ErrorDialog(this, "Could not read the file.", "IO-001").setVisible(true);
        } catch (NumberFormatException e) {
            new ErrorDialog(this, "File contains invalid number format.", "PARSE-001").setVisible(true);
        }
    }

    /**
     * Opens a file dialog and saves the current view's data to a CSV file.
     */
    private void saveFile() {
        FileDialog fd = new FileDialog(this, "Save " + currentView, FileDialog.SAVE);
        fd.setFile("data.csv");
        fd.setVisible(true);

        if (fd.getFile() == null) return; // User cancelled

        String filename = fd.getDirectory() + fd.getFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            if (currentView.equals("AIRPORTS")) {
                for (Airport a : airports) {
                    writer.write(String.format("%s,%s,%.2f,%.2f\n", a.getNaziv(), a.getKod(), a.getX(), a.getY()));
                }
            } else if (currentView.equals("FLIGHTS")) {
                for (Flight f : flights) {
                    writer.write(String.format("%s,%s,%s,%s\n", f.getOdAerodroma(), f.getDoAerodroma(), f.getVremePoletanja(), f.getTrajanje()));
                }
            }
        } catch (IOException e) {
            new ErrorDialog(this, "Could not save the file.", "IO-002").setVisible(true);
        }
    }
	private void setupNavigationPanel() {
        navigationPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        Button simButton = new Button("Simulation");
        Button airportsButton = new Button("Airports");
        Button flightsButton = new Button("Flights");

        // --- CHANGE: Update listeners to track view and update menu state ---
        simButton.addActionListener(e -> {
            currentView = "SIM";
            updateMenuState();
            cardLayout.show(cardPanel, "SIM");
        });
        airportsButton.addActionListener(e -> {
            currentView = "AIRPORTS";
            updateMenuState();
            cardLayout.show(cardPanel, "AIRPORTS");
        });
        flightsButton.addActionListener(e -> {
            currentView = "FLIGHTS";
            updateMenuState();
            cardLayout.show(cardPanel, "FLIGHTS");
        });

        navigationPanel.add(simButton);
        navigationPanel.add(airportsButton);
        navigationPanel.add(flightsButton);
    }
	    
    private void updateMenuState() {
        boolean isDataView = currentView.equals("AIRPORTS") || currentView.equals("FLIGHTS");
        openItem.setEnabled(isDataView);
        saveAsItem.setEnabled(isDataView);
    }

    private void setupSimulationPanel() {
        simulationPanel = new Panel(new BorderLayout());

        // Canvas remains in the center

        // --- ADD A NEW CONTROL PANEL AT THE BOTTOM ---
        Panel controlPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(100, 100, 100));

        startSimButton = new Button("Start");
        pauseSimButton = new Button("Pause");
        resetSimButton = new Button("Reset");
        simClockLabel = new Label("00:00");
        simClockLabel.setForeground(Color.WHITE);
        simClockLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        controlPanel.add(new Label("Sim Time:"));
        controlPanel.add(simClockLabel);
        controlPanel.add(startSimButton);
        controlPanel.add(pauseSimButton);
        controlPanel.add(resetSimButton);

        simulationPanel.add(controlPanel, BorderLayout.SOUTH);

        // --- ADD ACTION LISTENERS ---
        // --- ADD ACTION LISTENERS ---
        startSimButton.addActionListener(e -> {
            System.out.println("Simulation started. Pausing session timer.");
            sessionTimer.stop();
            simClock.go();
            simulationCanvas.startSimulation();
        });

        // --- THIS IS THE CHANGE ---
        pauseSimButton.addActionListener(e -> {
            System.out.println("Simulation paused. Resuming session timer.");
            simClock.pause();
            sessionTimer.start();
            simulationCanvas.pauseSimulation(); // Call the new 'pause' method
        });

        resetSimButton.addActionListener(e -> {
            System.out.println("Simulation reset. Resuming session timer.");
            simClock.reset();
            sessionTimer.start();
            simulationCanvas.stopSimulation(); // 'stop' is now the 'reset' method
        });
    }
	
	private void setupAirportsPanel() {
		
		 	airportsPanel.setLayout(new BorderLayout());;
	        airportsPanel.setBackground(new Color(104, 106, 108)); // Light steel blue
	        airportsPanel.add(new Label("Airports",Label.CENTER),BorderLayout.NORTH);
	           

	        // Airport list display
	        airportsListPanel = new Panel();
	        
	        //Make list scrollable
	        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	        scrollPane.add(airportsListPanel);
	        
	        //Speed of scrolling
	        Adjustable verticalScrollBar = scrollPane.getVAdjustable();
	        verticalScrollBar.setUnitIncrement(30);


	        airportsPanel.add(scrollPane, BorderLayout.CENTER);
	        refreshAirportList();

	        // "Add Airport" form at the bottom
	        Panel addAirportPanel = new Panel(new GridLayout(3, 1,0,0));
	        
	        Panel row1 = new Panel(new FlowLayout(FlowLayout.CENTER));
	        row1.add(new Label("Name:"));
	        nameField = new TextField(30);
	        row1.add(nameField);

	        Panel row2 = new Panel(new FlowLayout(FlowLayout.CENTER)); // Centered the whole row
	        row2.add(new Label("Code:"));
	        codeField = new TextField(4);
	        row2.add(codeField);

	        // Group X and Y fields in their own sub-panel
	        Panel xyPanel = new Panel(new FlowLayout());
	        xyPanel.add(new Label("X:"));
	        xField = new TextField(4);
	        xyPanel.add(xField);
	        xyPanel.add(new Label("Y:"));
	        yField = new TextField(4);
	        xyPanel.add(yField);
	        row2.add(xyPanel); // Add the group to the row
	        // --- End of changes for this row ---

	        Panel row3 = new Panel(new FlowLayout(FlowLayout.CENTER));
	        addAirport = new Button("Add Airport");
	        row3.add(addAirport);

	        addAirportPanel.add(row1);
	        addAirportPanel.add(row2);
	        addAirportPanel.add(row3);
	        
	        airportsPanel.add(addAirportPanel, BorderLayout.SOUTH);

	        // --- Action Listener for the button ---
	        addAirport.addActionListener(e -> addAirport());		
	}

	private void refreshAirportList() {
        airportsListPanel.removeAll();
        airportsListPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 10, 2, 10); // Padding
        //gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.anchor = GridBagConstraints.CENTER; // Center components in their cells


        // --- Add Headers ---
        gbc.gridy = 0; // Row 0
        gbc.gridx = 0; // Column 0
        airportsListPanel.add(new Label("Name"), gbc);
        gbc.gridx = 1; // Column 1
        airportsListPanel.add(new Label("Code"), gbc);
        gbc.gridx = 2; // Column 2
        airportsListPanel.add(new Label("X"), gbc);
        gbc.gridx = 3; // Column 3
        airportsListPanel.add(new Label("Y"), gbc);
        gbc.gridx = 4; // Column 4
        airportsListPanel.add(new Label("Shown"), gbc);

        // --- Add Airport Data ---
        int row = 1;
        for (Airport airport : airports) { // Use a final variable for the lambda
            final Airport currentAirport = airport;
            gbc.gridy = row;

            gbc.gridx = 0; airportsListPanel.add(new Label(currentAirport.getNaziv()), gbc);
            gbc.gridx = 1; airportsListPanel.add(new Label(currentAirport.getKod()), gbc);
            gbc.gridx = 2; airportsListPanel.add(new Label(String.valueOf(currentAirport.getX())), gbc);
            gbc.gridx = 3; airportsListPanel.add(new Label(String.valueOf(currentAirport.getY())), gbc);

            // --- CHANGE: Add a Checkbox instead of a Label ---
            Checkbox showCheckbox = new Checkbox();
            showCheckbox.setState(currentAirport.isShown); // Set its initial state from the Airport object

            // Add a listener to update the airport and repaint the canvas
            showCheckbox.addItemListener(e -> {
                currentAirport.isShown = showCheckbox.getState();
                simulationCanvas.updateAirports(this.airports); // Tell the canvas to update
            });

            gbc.gridx = 4; airportsListPanel.add(showCheckbox, gbc);
            row++;
        }
        
        // Add a "filler" panel to push everything to the top
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weighty = 1.0; // This makes the filler panel take all extra vertical space
        airportsListPanel.add(new Panel(), gbc);


        airportsListPanel.revalidate();
        airportsListPanel.repaint();
    }
	
	private void addAirport() {
	    try {
	        String name = nameField.getText();
	        String code = codeField.getText();
	        double x = Double.parseDouble(xField.getText());
	        double y = Double.parseDouble(yField.getText());
	        if(name.isEmpty()||code.isEmpty()) {
	        	 ErrorDialog error=new ErrorDialog(this, "Text fields MUST NOT be empty", "code01");
	        	 error.setVisible(true);
	        	 return;
	        }
	        if(!code.matches("^[A-Z]{3}$")) {
	        	 ErrorDialog error=new ErrorDialog(this, "Code MUST be 3 UPPERCASE LETTERS long", "code02");
	        	 error.setVisible(true);
	        	 return;
	        }
	        if(x<-90 ||x>90 || y<-90 || y>90) {
	        	 ErrorDialog error=new ErrorDialog(this, "Coordinates must be between -90 and 90 degrees", "code05");
	        	 error.setVisible(true);
	        	 return;
	        }
	        if (!name.isEmpty() && !code.isEmpty()) {
	            airports.add(new Airport(name, code, x, y));
	            refreshAirportList(); // Refresh the list to show the new airport
	            simulationCanvas.repaint();; // <-- ADD THIS LINE

	            // Clear input fields
	            nameField.setText("");
	            codeField.setText("");
	            xField.setText("");
	            yField.setText("");
	        }
	    } catch (NumberFormatException e) {
	    	ErrorDialog error=new ErrorDialog(this,"Invalid coordinates. Please enter numbers.", "code03");
	    	error.setVisible(true);
	    }
	}
	
	private void setupFlightPanel() {
	    flightsPanel.setLayout(new BorderLayout());
	    flightsPanel.setBackground(new Color(255, 141, 141));
	    flightsPanel.add(new Label("Flights", Label.CENTER), BorderLayout.NORTH);

	    flightsListPanel = new Panel();
        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.add(flightsListPanel);
        
        Adjustable verticalScrollBar = scrollPane.getVAdjustable();
        verticalScrollBar.setUnitIncrement(30);
	    
        flightsPanel.add(scrollPane, BorderLayout.CENTER);
	    refreshFlightList();

	    // The GridBagLayout panel for the form
	    Panel addFlightPanel = new Panel(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(2, 5, 2, 5);

	    // ... (all the gbc configuration and adding components to addFlightPanel) ...
	    // --- Row 0: From Airport ---
	    gbc.gridy = 0;
	    gbc.gridx = 0;
	    gbc.anchor = GridBagConstraints.EAST;
	    addFlightPanel.add(new Label("From airport:"), gbc);
	    gbc.gridx = 1;
	    gbc.anchor = GridBagConstraints.WEST;
	    fromAirport = new TextField(10);
	    addFlightPanel.add(fromAirport, gbc);
	    // --- Row 0: To Airport ---
	    gbc.gridx = 2;
	    gbc.anchor = GridBagConstraints.EAST;
	    addFlightPanel.add(new Label("To airport:"), gbc);
	    gbc.gridx = 3;
	    gbc.anchor = GridBagConstraints.WEST;
	    toAirport = new TextField(10);
	    addFlightPanel.add(toAirport, gbc);
	    // --- Row 1: Time of Departure ---
	    gbc.gridy = 1;
	    gbc.gridx = 0;
	    gbc.anchor = GridBagConstraints.EAST;
	    addFlightPanel.add(new Label("Time of departure:"), gbc);
	    gbc.gridx = 1;
	    gbc.anchor = GridBagConstraints.WEST;
	    timeOfDeparture = new TextField(10);
	    addFlightPanel.add(timeOfDeparture, gbc);
	    // --- Row 1: Length ---
	    gbc.gridx = 2;
	    gbc.anchor = GridBagConstraints.EAST;
	    addFlightPanel.add(new Label("Length:"), gbc);
	    gbc.gridx = 3;
	    gbc.anchor = GridBagConstraints.WEST;
	    lenght = new TextField(10);
	    addFlightPanel.add(lenght, gbc);
	    // --- Row 2: Add Flight Button ---
	    gbc.gridy = 2;
	    gbc.gridx = 0;
	    gbc.gridwidth = 4;
	    gbc.anchor = GridBagConstraints.CENTER;
	    addFlight = new Button("Add Flight");
	    addFlightPanel.add(addFlight, gbc);

	    // --- CHANGE: Wrap the form in a panel that provides vertical padding ---
	    Panel southWrapper = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // 0 hgap, 10 vgap
	    southWrapper.add(addFlightPanel);
	    flightsPanel.add(southWrapper, BorderLayout.SOUTH);
	    // --- END CHANGE ---

	    addFlight.addActionListener(e -> addFlight());
	}
	private void refreshFlightList() {
	    flightsListPanel.removeAll();
	    flightsListPanel.setLayout(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(2, 10, 2, 10);
	    gbc.anchor = GridBagConstraints.CENTER;

	    // --- Add Headers ---
	    gbc.gridy = 0;
	    gbc.gridx = 0; flightsListPanel.add(new Label("From"), gbc);
	    gbc.gridx = 1; flightsListPanel.add(new Label("To"), gbc);
	    gbc.gridx = 2; flightsListPanel.add(new Label("Departure Time"), gbc);
	    gbc.gridx = 3; flightsListPanel.add(new Label("Duration (min)"), gbc);

	    // --- Add Flight Data ---
	    int row = 1;
	    for (Flight f : flights) {
	        gbc.gridy = row;
	        
	        // **FIX**: Use the public fields from the Aero.Flight class
	        gbc.gridx = 0; flightsListPanel.add(new Label(f.getOdAerodroma()), gbc);
	        gbc.gridx = 1; flightsListPanel.add(new Label(f.getDoAerodroma()), gbc);
	        gbc.gridx = 2; flightsListPanel.add(new Label(f.getVremePoletanja()), gbc);
	        gbc.gridx = 3; flightsListPanel.add(new Label(String.valueOf(f.getTrajanje())), gbc);
	        
	        row++;
	    }

	    // Add a "filler" panel to push everything to the top
	    gbc.gridy = row;
	    gbc.weighty = 1.0;
	    flightsListPanel.add(new Panel(), gbc);

	    flightsListPanel.revalidate();
	    flightsListPanel.repaint();
	}

	private void addFlight() {
	    String from = fromAirport.getText().trim();
	    String to = toAirport.getText().trim();
	    String timeOfD = timeOfDeparture.getText().trim();
	    String lengStr = lenght.getText().trim(); // Use a different name to avoid confusion
	    
	    // --- VALIDATION SECTION ---

	    // 1. Check for any empty fields first.
	    if (from.isEmpty() || to.isEmpty() || timeOfD.isEmpty() || lengStr.isEmpty()) {
	        new ErrorDialog(this, "All fields are required.", "INPUT-006").setVisible(true);
	        return;
	    }

	    // 2. Validate the date format.
	    if (!timeOfD.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
	        new ErrorDialog(this, "Time of departure must be in HH:mm format (e.g., 08:30).", "TIME-001").setVisible(true);
	        return;
	    }

	    // 3. Validate the length (format and value).
	 // 3. Validate the length.
	    int lengthValue; // Declare it here so we can use it later.
	    try {
	        lengthValue = Integer.parseInt(lengStr);
	        if (lengthValue <= 0) {
	            new ErrorDialog(this, "Length must be a positive number.", "INPUT-007").setVisible(true);
	            return;
	        }
	    } catch (NumberFormatException e) {
	        new ErrorDialog(this, "Invalid flight length. Please enter a whole number.", "INPUT-004").setVisible(true);
	        return;
	    }

	    // --- END VALIDATION ---

	    // If we get here, all validations have passed.
	    flights.add(new Flight(from, to, timeOfD, Integer.parseInt(lengStr)));
	    refreshFlightList();
	    simulationCanvas.updateFlights(this.flights);

	    // Clear input fields
	    fromAirport.setText("");
	    toAirport.setText("");
	    timeOfDeparture.setText("");
	    lenght.setText("");
	}
	
	private boolean isAirportData(String csvLine) {
	        if (csvLine == null) return false;
	        String[] parts = csvLine.split(",");
	        if (parts.length != 4) return false;

	        try {
	            // Try to parse the 3rd and 4th parts as numbers (coordinates).
	            Double.parseDouble(parts[2]);
	            Double.parseDouble(parts[3]);
	            return true; // If successful, it's very likely an airport file.
	        } catch (NumberFormatException e) {
	            return false; // If parsing fails, it's not an airport file.
	        }
	    }

	    /**
	     * Checks if a CSV line has the structure of a Flight (String,String,DateString,IntString).
	     * @param csvLine The line to check.
	     * @return true if the line likely represents a Flight, false otherwise.
	     */
	private boolean isFlightData(String csvLine) {
	        if (csvLine == null) return false;
	        String[] parts = csvLine.split(",");
	        if (parts.length != 4) return false;

	        try {
	            // Check if the 3rd part is a valid date and the 4th is a number.
	            LocalDate.parse(parts[2], DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	            Integer.parseInt(parts[3]);
	            return true; // If successful, it's very likely a flight file.
	        } catch (DateTimeParseException | NumberFormatException e) {
	            return false; // If parsing fails, it's not a flight file.
	        }
	    }

	public static void main(String[] args) {
		new MAIN();
	}
	@Override
	public void eventDispatched(AWTEvent event) {
        // We only care about events that indicate user activity, like a mouse click
        if (event.getID() == MouseEvent.MOUSE_CLICKED) {
            sessionTimer.reset();
        }		
	}
	 @Override
	    public void onFlickerStateChanged(boolean isFlickering) {
	        if (isFlickering) {
	            System.out.println("Flickering started. Pausing session timer.");
	            sessionTimer.stop(); // "stop" effectively pauses it
	        } else {
	            System.out.println("Flickering stopped. Resuming session timer.");
	            sessionTimer.start(); // "start" resumes it by creating a new timer
	        }
	    }
}
