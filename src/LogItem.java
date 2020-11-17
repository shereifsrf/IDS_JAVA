
public class LogItem implements Comparable<LogItem>{
	//The logItem object is used to store information concerning a single log event. Log Items can be sorted on time, which is why
	//it implements the Comparable interface
	int hours;
	int mins;
	int secs;
	String name;
	double size;
	String unit;
	String type;
	
	public LogItem(int hours, int mins, int secs, String name, double size, String unit, String type) {
		this.hours = hours;
		this.mins = mins;
		this.secs = secs;
		this.name = name;
		this.size = size;
		this.unit = unit;
		this.type = type;
	}
	
	public String toString(){
		return "\n"+hours+":"+mins+":"+secs+"  "+ name + "  " +size +  "  " +unit ; 
	}

	@Override
	public int compareTo(LogItem o) {
		int h = o.hours-hours;
		int m = o.mins - mins;
		int s = o.secs - mins;
		
		if(h>0){
			return -1;
		}
		if(h<0){
			return 1;
		}
		if(h==0){
			if(m>0){
				return -1;
			}
			if(m<0){
				return 1;
			}
			if(m==0){
				if(s>0){
					return -1;
				}
				if(s<0){
					return 1;
				}
				if(s==0){
					return 0;
				}
			}
		}
		
		return 0;
		
		}
	


	
	
	
	
	
}
