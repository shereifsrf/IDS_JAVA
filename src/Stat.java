
public class Stat {
	//The stat object is used to store statistical information generated in the program and read in through the Stat.txt file
	
	String name;
	double mean;
	double std;
	
	
	public Stat(String name, double mean, double std) {
		this.name = name;
		this.mean = mean;
		this.std = std;
	}
	
	public String toString(){
		return name + "   " +mean+ "   "+std;
	}
	
	

}
