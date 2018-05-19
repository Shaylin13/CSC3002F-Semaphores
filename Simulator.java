//PDYSHA009 Semaphores Assignment

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
            System.out.println("File Name: "+fileName);
            
            //set up file for reading
            File inputFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            
            //get the Number of people and number of branches
            numberPeople = Integer.parseInt(br.readLine());
            numberBranches = Integer.parseInt(br.readLine());
            
            //read the rest of the people data
            String line = "";
            String processedLine="";
            while((line = br.readLine()) != null)
            {//start of while
               int start = line.indexOf(" ");
               int personNumber = Integer.parseInt(line.substring(0,start));
               
               //remove brackets so can split using ','
               for(int i=start; i<line.length(); i++)
               {
                    if(line.charAt(i) != '(' && line.charAt(i) != ')')
                    {
                        processedLine = processedLine+line.charAt(i);
                    }
               } 
               
               //split the data to obtain ints
               String[] pairs = processedLine.split(", ");
               
               //testing
               /*System.out.print(personNumber+" :");
               for(int i =0; i<pairs.length-1;i+=2)
               {
                    System.out.print(" "+pairs[i]+"-"+pairs[i+1]);
               }
               System.out.println("");*/
               
               processedLine="";
               
            }//end of while
        }
    
    }

}
