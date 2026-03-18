package control;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class EmergencySimulator {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();   
    private Runnable onEmergencyCallback;

    public EmergencySimulator(Runnable onEmergencyCallback) {
        this.onEmergencyCallback = onEmergencyCallback;
    }

    public void startSimulation() {
        scheduler.scheduleAtFixedRate(() -> {
            if (random.nextInt(100) < 15) { 

            	boolean success = TrainingClassControl.getInstance().activateEmergencyMode();
                
                if (success && onEmergencyCallback != null) {
                    onEmergencyCallback.run();
                }
            }
        }, 5, 10, TimeUnit.MINUTES); 
    }
    
    public void stopSimulation() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}