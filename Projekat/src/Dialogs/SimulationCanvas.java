package Dialogs;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.PaintEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import Aero.*;
import timer.SimulationClock;

class SimulationCanvas extends Panel {
    // --- Fields ---
    private ArrayList<Airport> airportsToDraw;
    private List<Flight> allFlights;
    private final FlickerListener flickerListener;
    private final SimulationClock simClock;

    private final int PADDING = 40;
    private final int SQUARE_SIZE = 30;

    // --- CHANGE: Use the fully qualified name for the Swing Timer ---
    private javax.swing.Timer flickerTimer;
    private volatile boolean drawFlickerRed = false;

    private final List<Airplane> activeAirplanes = new ArrayList<>();
    private final Map<String, Queue<Flight>> departureQueues = new HashMap<>();
    private final Map<String, Integer> lastDepartureTimes = new HashMap<>();
    private Timer simulationEngineTimer;
    
    private Image offScreenImage;
    private Graphics offScreenGraphics;
    
    
    public SimulationCanvas(ArrayList<Airport> airports, List<Flight> flights, FlickerListener listener, SimulationClock clock) {
        this.airportsToDraw = airports;
        this.allFlights = flights;
        this.flickerListener = listener;
        this.simClock = clock;
        setBackground(new Color(70, 130, 180));

        // Setup the flicker timer correctly in the constructor
        ActionListener flickerAction = e -> {
            drawFlickerRed = !drawFlickerRed;
            repaint();
        };
        // --- CHANGE: Use the fully qualified name here as well ---
        flickerTimer = new javax.swing.Timer(400, flickerAction);
        flickerTimer.setRepeats(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    private void handleMouseClick(int clickX, int clickY) {
        Airport clickedAirport = findAirportAt(clickX, clickY);

        if (clickedAirport != null) {
            clickedAirport.isFlickering = !clickedAirport.isFlickering;

            for (Airport a : airportsToDraw) {
                if (a != clickedAirport) a.isFlickering = false;
            }

            flickerListener.onFlickerStateChanged(clickedAirport.isFlickering);

            // Use the simple start() and stop() methods of the Swing Timer
            if (clickedAirport.isFlickering) {
                flickerTimer.start();
            } else {
                flickerTimer.stop();
                drawFlickerRed = false; // Reset color state
            }
        }
        repaint();
    }
    
   
    
    public void updateFlights(List<Flight> flights) {
        this.allFlights = flights;
    }
    @Override
    public void update(Graphics g) {
        // By overriding this method and just calling paint, we prevent the default
        // behavior which is to clear the screen, causing the flicker.
        paint(g);
    }
    @Override
    public void paint(Graphics g) {
        final int width = getWidth();
        final int height = getHeight();

        // Create the off-screen buffer if it doesn't exist or if the window was resized
        if (offScreenImage == null || offScreenImage.getWidth(this) != width || offScreenImage.getHeight(this) != height) {
            offScreenImage = createImage(width, height);
            offScreenGraphics = offScreenImage.getGraphics();
        }

        // 1. Clear the off-screen image (not the screen)
        offScreenGraphics.setColor(getBackground());
        offScreenGraphics.fillRect(0, 0, width, height);
        offScreenGraphics.setColor(getForeground());

        // 2. Perform all drawing operations on the off-screen graphics
        render(offScreenGraphics);

        // 3. Draw the completed off-screen image to the screen in one go.
        g.drawImage(offScreenImage, 0, 0, this);
    }
    // --- NEW: The actual drawing logic, moved from paint() ---
    private void render(Graphics g) {
        if (airportsToDraw == null || airportsToDraw.isEmpty()) {
            g.setColor(Color.WHITE);
            g.drawString("No airport data loaded.", getWidth() / 2 - 70, getHeight() / 2);
            return;
        }

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Airport a : airportsToDraw) {
            if (a.getX() < minX) minX = a.getX(); if (a.getX() > maxX) maxX = a.getX();
            if (a.getY() < minY) minY = a.getY(); if (a.getY() > maxY) maxY = a.getY();
        }
        int canvasWidth = getWidth() - 2 * PADDING;
        int canvasHeight = getHeight() - 2 * PADDING;
        double xScale = (maxX - minX == 0) ? 1 : canvasWidth / (maxX - minX);
        double yScale = (maxY - minY == 0) ? 1 : canvasHeight / (maxY - minY);

        ArrayList<Airport> visibleAirports = new ArrayList<>();
        for (Airport a : airportsToDraw) {
            if (a.isShown) {
                visibleAirports.add(a);
            }
        }

        for (Airport a : visibleAirports) {
            int screenX = PADDING + (int) ((a.getX() - minX) * xScale);
            int screenY = PADDING + (int) ((a.getY() - minY) * yScale);
            if (a.isFlickering && drawFlickerRed) g.setColor(Color.RED);
            else g.setColor(Color.GRAY);
            g.fillRect(screenX, screenY, SQUARE_SIZE, SQUARE_SIZE);
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(a.getKod());
            g.drawString(a.getKod(), screenX + (SQUARE_SIZE - textWidth) / 2, screenY + fm.getAscent() + (SQUARE_SIZE - fm.getHeight()) / 2);
        }

        g.setColor(new Color(0, 0, 139));
        for (Airplane plane : activeAirplanes) {
            int screenX = PADDING + (int) ((plane.currX - minX) * xScale);
            int screenY = PADDING + (int) ((plane.currY - minY) * yScale);
            g.fillOval(screenX - 3, screenY - 3, 6, 6);
        }
    }
    public void updateAirports(ArrayList<Airport> airports) {
        this.airportsToDraw = airports;
        repaint();
    }
    private Airport findAirportAt(int x, int y) {
        // This requires recalculating screen positions just for hit detection
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Airport a : airportsToDraw) {
            if (a.getX() < minX) minX = a.getX(); if (a.getX() > maxX) maxX = a.getX();
            if (a.getY() < minY) minY = a.getY(); if (a.getY() > maxY) maxY = a.getY();
        }
        int canvasWidth = getWidth() - 2 * PADDING;
        int canvasHeight = getHeight() - 2 * PADDING;
        double xScale = (maxX - minX == 0) ? 1 : canvasWidth / (maxX - minX);
        double yScale = (maxY - minY == 0) ? 1 : canvasHeight / (maxY - minY);

        for (Airport a : airportsToDraw) {
            int screenX = PADDING + (int) ((a.getX() - minX) * xScale);
            int screenY = PADDING + (int) ((a.getY() - minY) * yScale);
            if (x >= screenX && x <= screenX + SQUARE_SIZE && y >= screenY && y <= screenY + SQUARE_SIZE) {
                return a;
            }
        }
        return null;
    }
    
    //Simulation engine
 // --- NEW: Start and Stop the Simulation Engine ---
    public void startSimulation() {
        // Reset state
        activeAirplanes.clear();
        lastDepartureTimes.clear();
        buildDepartureQueues();

        if (simulationEngineTimer != null) {
            simulationEngineTimer.cancel();
        }
        simulationEngineTimer = new Timer(true);
        simulationEngineTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Run the simulation tick on a background thread
                updateSimulation();
            }
        }, 0, 200); // Run every 200ms
    }

    public void stopSimulation() {
        pauseSimulation(); // Stop the timer first
        activeAirplanes.clear(); // Then clear the screen
        EventQueue.invokeLater(this::repaint); // Repaint to show the cleared screen
    }

    private void updateSimulation() {
        boolean hasStateChanged = false;
        int currentSimTime = simClock.getTotalSimMinutes();

        // 1. Check for new departures
        for (String airportCode : departureQueues.keySet()) {
            Queue<Flight> queue = departureQueues.get(airportCode);

            while (!queue.isEmpty() && currentSimTime >= timeToMinutes(queue.peek().getVremePoletanja())) {
                int lastDeparture = lastDepartureTimes.getOrDefault(airportCode, -100);
                if (currentSimTime < lastDeparture + 10) {
                    break;
                }

                Flight departingFlight = queue.poll();
                lastDepartureTimes.put(airportCode, currentSimTime);

                Airport from = findAirport(departingFlight.getOdAerodroma());
                Airport to = findAirport(departingFlight.getDoAerodroma());

                if (from != null && to != null) {
                    Airplane plane = new Airplane(departingFlight, from.getX(), from.getY(), to.getX(), to.getY(), currentSimTime);
                    activeAirplanes.add(plane);
                    hasStateChanged = true; // A new plane was launched, so we must repaint.
                }
            }
        }

        // 2. Update positions only if there are active airplanes
        if (!activeAirplanes.isEmpty()) {
            activeAirplanes.removeIf(plane -> !plane.updatePosition(currentSimTime));
            hasStateChanged = true; // The planes have moved, so we must repaint.
        }

        // 3. Only trigger a repaint if the state has actually changed.
        if (hasStateChanged) {
            EventQueue.invokeLater(this::repaint);
        }
    }
    // --- NEW HELPER METHODS ---
    private void buildDepartureQueues() {
        departureQueues.clear();
        if (allFlights == null) return; // Defensive check
        for (Flight f : allFlights) {
            departureQueues.computeIfAbsent(f.getOdAerodroma(), k -> new LinkedList<>()).add(f);
        }
    }

    private Airport findAirport(String identifier) {
        if (identifier == null || airportsToDraw == null) return null;
        for (Airport a : airportsToDraw) {
            // --- THIS IS THE FIX ---
            // Check if the identifier matches the code (e.g., "CTR") OR the name (e.g., "True Center")
            if (identifier.equals(a.getKod()) || identifier.equals(a.getNaziv())) {
                return a;
            }
        }
        return null; // Return null if no airport is found
    }
    private int timeToMinutes(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return -1; // Invalid time format
        }
    }
    public void pauseSimulation() {
        if (simulationEngineTimer != null) {
            simulationEngineTimer.cancel();
            simulationEngineTimer = null;
        }
        // We DO NOT clear the activeAirplanes list here, so they stay on screen.
    }
    
    
}
