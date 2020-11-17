
public class Event {
	//The event object is used to store event information from the Event.txt document
	//The event object allows for empty fields, so the input is checked with a boolean that is true if the input is valid, and false if it 
	//does not exist.
	String name;
	String type;
	boolean hasMax;
	double max;
	boolean hasMin;
	double min;
	boolean hasUnit;
	String unit;
	boolean hasWeight;
	int weight;
	
	
	public Event(String name, String type, boolean hasMax, double max,
			boolean hasMin, double min, boolean hasUnit, String unit,
			boolean hasWeight, int weight) {
		super();
		this.name = name;
		this.type = type;
		this.hasMax = hasMax;
		this.max = max;
		this.hasMin = hasMin;
		this.min = min;
		this.hasUnit = hasUnit;
		this.unit = unit;
		this.hasWeight = hasWeight;
		this.weight = weight;
	}




	public String toString(){
		return name;
	}
	

}
