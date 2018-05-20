//PDYSHA009 Semaphores Assignment
//see readme for any information

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Simulator
{

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        String fileName = args[0];
        
        final int numberPeople;
        final int numberBranches;
        
        if(fileName.length()==0)
        {
            throw new IllegalArgumentException();
        }
        else
        {
            System.out.println("Input from File: "+fileName+"\n");
            
            //set up file for reading
            File inputFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            
            //get the Number of people and number of branches
            numberPeople = Integer.parseInt(br.readLine());
            numberBranches = Integer.parseInt(br.readLine());
            
            //create an array to store Person objects
            ArrayList<Person> AL_employees = new ArrayList<>();
            
            //read the rest of the people data
            String line = "";
            String processedLine="";
            while((line = br.readLine()) != null)
            {//start of while
               int start = line.indexOf(" ");
               int personNumber = Integer.parseInt(line.substring(0,start));
               
               //remove brackets so can split using ','
               for(int i=start+1; i<line.length(); i++)
               {
                    if(line.charAt(i) != '(' && line.charAt(i) != ')')
                    {
                        processedLine = processedLine+line.charAt(i);
                    }
               } 
               
               //split the data to obtain ints
               String[] data = processedLine.split(", ");
               
               //create Stop objects
               ArrayDeque<Stop> AD_Stops = new ArrayDeque<>();
               for(int i=0; i<data.length-1; i+=2)
               {
                    //System.out.print(data[i]);
                    //System.out.println(data[i+1]);
                    int branch = Integer.parseInt(data[i]);
                    int duration = Integer.parseInt(data[i+1]);
                    
                    AD_Stops.add(new Stop(branch, duration));
               }
               
               //add person with array of stops
               Semaphore Sim_Sem = new Semaphore(0); // semaphores of bound 0 for blocking
               AL_employees.add(new Person(personNumber, AD_Stops, Sim_Sem));
               
               //clear line for reading next person
               processedLine="";
               
            }//end of while for reading Person data from file
            
            //===============================================================
            //-------------loque narosh my warrior---------------------------
            
            //create branch objects
            Branch[] branches = new Branch[numberBranches];
            for(int i = 0; i < numberBranches; ++i){
                branches[i] = new Branch(i);
            }
            // all employees start at HQ they are initially added here
            branches[0].add(AL_employees);

            final long startTime = System.currentTimeMillis();
            Person.startTime = startTime;
            final Taxi mainTaxi = new Taxi(branches, AL_employees, startTime);
            Person.TAXI = mainTaxi;
            
            Taxi.currentlyProcessing = AL_employees.size();
            mainTaxi.start();
            
            for(Person employee : AL_employees)
            {
                // start a thread for each person in the array of Person objects
                employee.start();
            }
            //---------------------------------------------------------------
            //===============================================================
        }
    
    }//main end

}//class end
