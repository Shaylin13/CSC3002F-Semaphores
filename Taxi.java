import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Taxi extends Thread
{
    public boolean outbound;
    private final PriorityQueue<Integer> stops;
    private final Branch[] branches;
    private int currentBID;
    private final long startTime;
    
    public static volatile int currentlyProcessing = 0;
    
    private final HashMap<Integer, HashSet<Integer>> branch_Hailer_Map = new HashMap<>(); // branch : PIDs
    private final ArrayList<Person> passengersList = new ArrayList<>();
    
    
    public Taxi(Branch[] b_arr, ArrayList<Person> people, long st)
    {
        stops = new PriorityQueue<>();
        //Taxi must travel to the HQ from the depot to fetch Employees in the morning
        stops.add(0);
        branches = b_arr;
        //setting direction to initially be outbound as it cant go inbound from HQ
        outbound = true;
        
        HashSet<Integer> person_IDs = new HashSet<>();
        for (Person p : people) // for HQ
        {
            person_IDs.add(p.getPID());
        }
        branch_Hailer_Map.put(0, person_IDs);
        
       //set start time so that time can be Traced in scale
        startTime = st;
    }
    
    public boolean disembark(Person p)
    { return passengersList.remove(p);}
    
    public boolean isOutbound()
    {
        return outbound;
    }
    
    public void switchDirection()
    {
        if(outbound == true)
        {outbound = false;}
        else
        {outbound = true;}
    }
    
    synchronized public void hail(int bid, Person p) throws InterruptedException
    {
        //add stop to list of stops to make
        if(stops.contains(bid) == false)
        {
            stops.add(bid);
        }
        addHailer(bid, p.getPID());

    }
    
    private void addHailer(int bid, int pid)
    {
        HashSet<Integer> hailerIDs = branch_Hailer_Map.get(bid);
        if(hailerIDs == null)
        {
        
            hailerIDs = new HashSet<>();
            hailerIDs.add(pid);
            branch_Hailer_Map.put(bid, hailerIDs);
        }
        else
        {    
            hailerIDs.add(pid);
            branch_Hailer_Map.replace(bid, hailerIDs);
        }
    }
    
    synchronized public void request(int at, int to, int pid)
    {

        TraceRequest(at, to, pid, System.currentTimeMillis());
        if(stops.contains(to) == false)
            stops.add(to);
    }
    
    @Override
    public void run()
    {
        try {

            
            while(Taxi.currentlyProcessing > 0)
            {
                boolean stopping = false;
                
                Branch currentBranch = branches[currentBID];
                
                if(stops.contains(currentBID) == true) 
                    {stopping = true;}
                    
                if(stopping == false)
                {
                    moveBranch();
                    continue;
                }
                
                //This is assuming that from branch to branch is 2 min ie b1 to b3 is 2 min
                //so b1 to b3 does not got through b2 but rather b1 directly to b3
                //+===================             
                sleep(33*2); // 2*1 min branch to branch travel time
                //+=================== 
                
                //Trace that Taxi is arriving at a destination
                TraceTaxi(true, currentBID, System.currentTimeMillis());

                // wait 1 minute for people to embark/disembark as per requirements
                sleep(33); 
                
                synchronized (this){
                    ArrayList<Person> disembarkers = new ArrayList<>();
                    if(passengersList.isEmpty() == false)
                    {
                        //check if passenger is supposed to get off at this stop
                        for(Iterator<Person> pi = passengersList.iterator(); pi.hasNext(); )
                        { 
                            Person p = pi.next();
                            if(p.stopHere(currentBID))
                            {
                                p.getSemaphore().release();
                                disembarkers.add(p);
                            }
                        }
                        //People who disembarked are added to the branch
                        currentBranch.getEmployees().addAll(disembarkers);
                        //remove people who disembarked from Taxi passenger list
                        passengersList.removeAll(disembarkers);
                    }
                
                
                    //check if current branch has anyone who hailed the Taxi
                    if(branch_Hailer_Map.containsKey(currentBID) == true)
                    { 
                        ArrayList<Person> embarkers = new ArrayList<>();
                        
                        for (Iterator<Person> pi = currentBranch.getEmployees().iterator(); pi.hasNext(); )
                        {
                            Person emp = pi.next();
                            //check if employees at current branch are hailers (have hailed the Taxi)
                            //if they have hailed then they embark the Taxi
                            if(branch_Hailer_Map.get(currentBID).contains(emp.getPID()))
                            {
                                emp.getSemaphore().release();
                                embarkers.add(emp);
                            }
                        }
                        //people who are embarking leave the list of employees at the current branch
                        currentBranch.getEmployees().removeAll(embarkers);
                        //all people who are embarking are added to the taxi passengers list
                        passengersList.addAll(embarkers);
                        //remove branch from hailers as it has been stopped at now
                        branch_Hailer_Map.remove(currentBID);
                    }
                    //remove branch from stops list
                    stops.remove(currentBID);
                }
                
                boolean logged = false;
                //while idle conditions are met
                while(branch_Hailer_Map.isEmpty() && Taxi.currentlyProcessing > 0 && passengersList.isEmpty())
                {
                    if(logged == false){ 
                        //print out that taxi is idle
                        TraceIdle(currentBID, System.currentTimeMillis());
                        logged = true;
                    }
                    sleep(1); // Taxi idle
                }
                
                //print out taxi departures and arrivals
                TraceTaxi(false, currentBID, System.currentTimeMillis());
                //travel to the next destination branch in current direction
                moveBranch();
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    private void moveBranch() throws InterruptedException
    {
        //processing which direction to move branches
        if(outbound == true)
        {
            currentBID = currentBID+1;
        }
        else
        {
            currentBID = currentBID-1;
        }
        
        //if reached begin or end switch direction
        if((currentBID == 0) || (currentBID == (branches.length-1)))
        {
            switchDirection(); 
        }
       
    }
    
    //=====================================================================Trace print Section
    //loque narosh be wary my warriors (dont mind this warcraft 3 reference)
    public String formattedTime(long currentTime){
        String temp = "";
        long timeDifference = currentTime - startTime;
        int minutes = 0;
        int hour = 9;
        while( timeDifference >= 33)
        {
            timeDifference = timeDifference - 33;
            minutes = minutes + 1;
            
            if(minutes >= 60)
            { 
                hour += 1;
                minutes = 0;
            }
        }
        if (timeDifference > 16)
            minutes = minutes + 1;
        
        String minS = minutes+"";
        if (minutes <= 9)
            minS = "0"+minS;
            
        if (hour <= 9)
            temp = temp + "0";
        
        temp = temp + hour +":" + minS;
        return temp;
    }
    
    public void TraceRequest(int at,int to, int pid, long time){
        String s =  formattedTime(time);
        s = s + " branch " + at + ": person " + pid + " request " + to;
        System.out.println(s);
    }
    
    public void TraceTaxi(boolean arriving, int bid, long time){
        String s = formattedTime(time);
        s = s + " branch " + bid + ": taxi ";
        if(arriving == false)
            s = s + "depart";
            
        else
            s = s + "arrive";
        
        System.out.println(s);
    }
    
    public void TraceIdle(int bid, long time){
        String s = formattedTime(time);
        s = s + " branch " + bid + ": taxi idle";
        System.out.println(s);
    }
    //=====================================================================end of Trace section
}
