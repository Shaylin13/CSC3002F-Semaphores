import java.util.ArrayList;
//Yet again if I had Pairs this class would not exist

public class Branch{
    private ArrayList<Person> employees = new ArrayList<>();
    private final int bid;
    
    public Branch(int id){
        bid = id;
    }
    
    public void add(Person newPerson){
        employees.add(newPerson);
    }
    
    public void add(ArrayList<Person> newEmployees){
        for (Person p : newEmployees) this.add(p);
    }
    
    public boolean remove(Person leavingEmployee){
        return employees.remove(leavingEmployee);
    }
    
    public ArrayList<Person> getEmployees(){
        return employees;
    }
    
    public int getBID(){
        return bid;
    }
    
}
