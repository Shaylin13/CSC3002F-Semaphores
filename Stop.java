//Basically to facilitate a Pair since my version of Java doesnt have Pairs

public class Stop {
    private final int branchID;
    private final int duration;
    
    public Stop(int bid, int dTime){
        duration = dTime;  
        branchID = bid;
    }
    
    public int getBranchID(){
        return branchID;
    }
    
    public int getDuration(){
        return duration;
    }
    
}
