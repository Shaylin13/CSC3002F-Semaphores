import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class Person extends Thread{
    
    private final int pid;
    public static Taxi TAXI;
    private final ArrayDeque<Stop> stops;
    private final Semaphore person_sem;
    public static long startTime;
    private int currentBID;
    
    public Person(int number, ArrayDeque<Stop> AD_Stops, Semaphore sema){
        pid = number;
        stops = AD_Stops;
        person_sem = sema;
        currentBID = 0;
    }
    
    public Semaphore getSemaphore(){
        return person_sem;
    }
    
    public int getPID(){
        return pid;
    }
    
    public boolean stopHere(int bid){
        return stops.peekFirst().getBranchID() == bid;
    }
    
    public int nextStopID(){
        return stops.peek().getBranchID();
    }
    
    public void acquire_block() throws InterruptedException{
        person_sem.acquire();
    }
    
    public void EmployeeStay() throws InterruptedException{
        
        Stop tempStop = stops.pop();
        currentBID = tempStop.getBranchID();
        
        //make the employee stay at the branch for the duration * 33ms to simulate real time scaled down
        sleep(33 * tempStop.getDuration());
    }
    
    @Override
    public void run(){
        try {
            acquire_block(); // initially acquire_block at HQ
            while(true){
                synchronized (TAXI){
                    TAXI.request(currentBID, this.nextStopID(), pid);
                }
                acquire_block();
                
                //make to employee wait at the specific branch for the asscociated duration
                EmployeeStay();
                
                //check if all stops have been made
                if(stops.isEmpty() == true)
                    break;
                
                synchronized (TAXI){
                    TAXI.hail(currentBID, this);
                    TraceHail(currentBID, pid, System.currentTimeMillis());
                }
                
                acquire_block();
            }

            synchronized (Person.class){
                Taxi.currentlyProcessing = Taxi.currentlyProcessing - 1;
            }

        } 
        catch (InterruptedException e) {
            System.out.println(e);
        }
        
    }
    //=======================================================
    //to print out who has hailed and from where
    public void TraceHail(int bid, int pid, long currentTime){

        long timeDifference = currentTime - startTime;
        int minutes = 0;
        int hour = 9;//starting stime is 9 hence the value
        
        while( timeDifference >= 33)
        {
            timeDifference = timeDifference - 33;
            minutes = minutes + 1;
            
            if(minutes >= 60){ 
                hour = hour + 1;
                minutes = 0;
            }
        }
        //add in the extra minute if there is one
        if (timeDifference > 16)
        {
         minutes = minutes+1;
        }
        
        String timeS = "";
        String min = minutes+"";
        //check for double digits for string formatting
        if (minutes <= 9)
        {
            min = "0"+min;
        }
        
        if (hour <= 9)
        {
            timeS += "0";
        }
        
        timeS += hour +":" + min;
        String s= timeS + " branch " + bid + ": person " + pid + " hail";
        
        System.out.println(s);
    }
    
    //=======================================================
    
}
