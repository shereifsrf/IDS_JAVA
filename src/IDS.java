import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Random;


public class IDS {
	//The Stats and Events Arraylists are used to read in from file and hold event and statistical information
	//Stats is used to hold the base information from the user input
	static ArrayList<Stat> Stats = new ArrayList<Stat>(); // Maybe better off as local since we want new Stats each iteration? or is overwriting fine?
	static ArrayList<Event> Events = new ArrayList<Event>();
	
	//nrOfStats and nrOfEvents are used to store the number of events and number of stats from the initial files
	static int nrOfStats;
	static int nrOfEvents;

	static String statFile;
	static int days;
	



	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException{
		String eventsDoc = args[0];
		String username = args[1];
		String statsDoc = args[2];
 		days = Integer.parseInt(args[3]);
		//the preprocessing parses the initial input from the user and stores in Event and Stat objects as well as checking for inconsistencies
		preprocessing(eventsDoc, username, statsDoc, true);
			System.out.println("finished processing, beginning analysis...");
		//initDays creates the files from which the analysis will be done later
		initDays();
	    //produceMeanStd takes the files created in initDays and calculates the mean and standard deviation of every event and stores it in the Stats ArrayList
	    ArrayList<Stat> BaseStats = produceMeanStd();
	    System.out.println(BaseStats);
	    //the actual alert system:
	    //System.out.print(BaseStats);
	    while(input()){
		preprocessing(eventsDoc, username, statFile, false);
	    	checkDays(BaseStats); //need to make this function, almost the same as initdays
	    }
	    System.out.println("finished");

	}

	//runs getStats, getEvents and checks the data for inconsistencies
	public static void preprocessing(String events, String usr, String stats, boolean firstTime) throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Running intrusion detection system on "+usr+"'s data");
		getStats(stats);
		if(firstTime) {
			getEvents(events);
			checkLists(Stats, Events);
		}

	}
	
	//getStats reads through the initial statistics file and stores in an ArrayList
	public static void getStats(String statFile) throws FileNotFoundException{
		// This should try/catch when called in the interactive phase
		Stats.clear();
		Scanner in = new Scanner(new FileReader(statFile));
		nrOfStats = Integer.parseInt(in.nextLine());
		while(in.hasNext()){
			String ny = in.nextLine();
			String[] nyy = ny.split(":");
			String name;
			double mean = 0;
			double std = 0;
			name = nyy[0];
			if(!nyy[1].isEmpty()){
				mean = Double.parseDouble(nyy[1]);
			}
			else{System.out.println("missing mean, mean set to 0");}
			if(!nyy[2].isEmpty()){
				std = Double.parseDouble(nyy[2]);
			}
			else{System.out.println("missing std, std set to 0");}
			
			Stats.add(new Stat(nyy[0], mean, std));
		}
	}
	
	//getEvets reads through the initial event file and stores in an ArrayList
	public static void getEvents(String eventFile) throws FileNotFoundException{
		Scanner in = new Scanner(new FileReader(eventFile));
		String name;
		String type;
		boolean hasMax = true;
		double max;
		boolean hasMin = true;
		double min;
		boolean hasUnit = true;
		String unit;
		boolean hasWeight = true;
		int weigth;
		
		nrOfEvents = Integer.parseInt(in.nextLine());
		while(in.hasNext()){
			String ny = in.nextLine();
			String[] nyy = ny.split(":");
			name = nyy[0];
			type = nyy[1];
			if(nyy[2].isEmpty()){
				hasMin = false;
				min = 0;
			}
			else{min = Double.parseDouble(nyy[2]);}
			if(nyy[3].isEmpty()){
				hasMax = false;
				max = 0;
			}
			else{max = Double.parseDouble(nyy[3]);}
			
			if(nyy[4].isEmpty()){
				hasUnit = false;
				unit = "";
			}
			else{unit = nyy[4];}
			
			if(nyy[5].isEmpty()){
				hasWeight = false;
				weigth = 0;
			}
			else{weigth = Integer.parseInt(nyy[5]);}
			
			
			
			Events.add(new Event(name, type, hasMax, max, hasMin, min, hasUnit, unit, hasWeight, weigth));
		}
	}

	//checks that the number of stats and number of events is the same, checks that they have the same event types in each file.
	//if the input is consistent it returns true, if one of the checks fails it returns false -> the input is inconsistent
	public static boolean checkLists(ArrayList<Stat> stats, ArrayList<Event> events){
		if(!(stats.size() == events.size())){
			System.out.println("input inconsistency: stats list is not same size as events list");
			return false;
		}
		for(int i = 0; i<stats.size();i++){
			if(!stats.get(i).name.trim().equals(events.get(i).name.trim())){
				System.out.println("input inconsistency: stats names are not the same as event names");
				System.out.println(stats.get(i).name +"    "+ events.get(i).name);
				return false;
			}
		}

		return true;
	}

	// Does everything, creates the aggregate data file and calls genDay repeatedly to generate the logs for each day
	// TODO: decide if Stats should be local and the generation be moved to another function.
	public static void initDays() throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter dayTotals = new PrintWriter("Days.txt", "UTF-8");
		// Initiates the day file with names of events
		for(int i = 0; i<nrOfEvents; i++) {
			dayTotals.write(Events.get(i).name+" : ");
		}
		dayTotals.write("\n");

		// Split this part off into a different function? <BEGIN>

		// This is where the day data is generated
		double[][] allTotals = new double[days][nrOfEvents];
		double[] totals;

		for(int i = 0; i<days; i++) {
			for(int j = 0; j<nrOfEvents; j++) {
				allTotals[i][j] = normal(j, Stats); // Generates the total for specific event for that day.
			}
			totals = allTotals[i]; // so ugly, if the loop gets moved to a separate function this could be tidier I think
			genDay(i, totals.clone()); // Generates the specifics of the day using the totals and logs to file dayi.txt
		}

		// <END> Note: allTotals.add and genDay can happen after that function call

		// Write to the file
		writeToLog(dayTotals, allTotals);

		dayTotals.close();
	}

	// Uses all the information supplied to generate the total frequency of event. It then writes that value to the totals file and returns it
	// note: There is room for improvement by adding anomaly detection
	// TODO: Maybe Stats shouldn't be global, this function would be reusable if it got passed the Stats ArrayList.
	public static double normal(int index, ArrayList<Stat> Statsi) {
		String name = Statsi.get(index).name;
		double stdDev = Statsi.get(index).std;
		double mean = Statsi.get(index).mean;
		double min = Events.get(index).min;
		double max = Events.get(index).max;
		boolean hasMin = Events.get(index).hasMin;
		boolean hasMax = Events.get(index).hasMax;
		int counter = 0;
		int threshold = 1000000; // Magic number, it tries to find a random value that fits a million times before returning an error

		Random rng = new Random();
		double val = 0;

		// Bad variable name and stupid if-else logic.
		boolean works = true;
		while(works) {
			val = rng.nextGaussian()*stdDev + mean;
			if(!hasMin && !hasMax) {
				works = false;
			}
			else if(hasMin && min < val && !hasMax) {
				works = false;
			}
			else if(!hasMin && hasMax && val < max) {
				works = false;
			}
			else if(hasMin && hasMax && min < val && val < max) {
				works = false;
			}
			else{
				// Exits program if no value in specified interval gets generated with stdDev and mean supplied by user.
				counter++;
				if(counter > threshold) {
					printError(index);
					System.exit(1);
				}
			}
		}
		return val;
	}

	// Overload on the writeToLog function for dealing with the aggregate logfile which only logs the totals for each day
	// Note: This change means we no longer conflate the generating and the logging. However it means we have to iterate over the data twice.
	public static void writeToLog(PrintWriter file, double[][] data) {
		// Outer for loop iterates over days in the ArrayList
		for(int i = 0; i<days; i++) {
			file.write("Day"+(i+1)+": ");
			// Inner for loop iterates over events in the day
			for(int j = 0; j<nrOfEvents; j++) {
				file.write(""+rounder("" +data[i][j])+" : ");
			}
			file.write("\n");
		}
	}

	// Generates the details of the day randomly using the totals given to it by initDays. Not very elegant => loses some information.
	// TODO: Deal with the totals infinite loop in a more elegant way
	public static void genDay(int dayNum, double[] totals) throws FileNotFoundException, UnsupportedEncodingException {
		// Initialize all the variables
		int[] time = {0,0,0};
		Random rng = new Random();
		int eventNum = pickEvent(totals);
		String type;
		double size;
		String name;
		String unit;
		LogItem printMe;
		// Creates the list with the items to print
		ArrayList<LogItem> log = new ArrayList<LogItem>();

		// Creates the data for the logfile
		while(eventNum != -1) {
			time[2] += 4000+rng.nextInt(6000)+rng.nextInt(6000)+rng.nextInt(6000);
			time = incTime(time[0],time[1],time[2]);
			// Gets all the data needed about the event
			name = Events.get(eventNum).name;
			unit = Events.get(eventNum).unit;
			type = Events.get(eventNum).type;
			size = eventMagnitude(type, totals[eventNum]);
			// Puts the data into the logItem
			printMe = new LogItem(time[0], time[1], time[2], name, size, unit, type);
			// Appends to log
			log.add(printMe);
			// Updates totals to avoid infinite loop, note that this means that the totals =/= totals from summing over
			totals[eventNum] -= size;
			if(totals[eventNum] < 1) {
				// Some information may be lost here. However this should not be significant due to the nature of the assignment
				totals[eventNum] = 0;
			}
			eventNum = pickEvent(totals); // Chooses which event is going to happen
		}
		// Names file
		String fileName = "Day"+dayNum+".txt";

		// Creates the file to write to
		PrintWriter cDay = new PrintWriter(fileName, "UTF-8");

		// Writes the log to the file
		writeToLog(cDay, log);

		// Saves the file
		cDay.close();
	}

	// Takes in a dayLog ArrayList and filename then prints the data in the ArrayList to the file.
	public static void writeToLog(PrintWriter file, ArrayList<LogItem> Log) {
		// Sort the log by time
		Collections.sort(Log);
		String item;
		for(int i = 0; i<Log.size(); i++) {
			item = "<";
			item += timeFormat(Log.get(i).hours)+":";
			item += timeFormat(Log.get(i).mins)+":";
			item += timeFormat(Log.get(i).secs)+"> ";
			item += Log.get(i).name+": ";
			item += sizeFormat(Log.get(i).size, Log.get(i).type)+" ";
			item += Log.get(i).unit+"\n";
			file.write(item);
		}
	}

	//produceMeanStd iterated through each event in days and stores the number in an arraylist, these numbers are then the basis for calculating std and mean
	//each event, std and mean is then stored as a Stat object in a new arraylist that is returned as output when the function is run.
	public static ArrayList<Stat> produceMeanStd() throws FileNotFoundException{
		ArrayList<Stat> base = new ArrayList<Stat>();
		Scanner in = new Scanner(new FileReader("Days.txt"));
		String ini = in.nextLine();
		String[] init = ini.split(": "); 
		
		in.close();

		for(int i=1; i<init.length+1; i++){
			Scanner inn = new Scanner(new FileReader("Days.txt"));
			inn.nextLine();
			ArrayList<Double> read = new ArrayList<Double>();
			while(inn.hasNext()){
				String[] day = inn.nextLine().split(": ");
				read.add(Double.parseDouble(day[i].trim()));
				
			}
			String name = init[i-1];
			double mean = calcAvg(read);
			double std = calcStd(read);
			
			Stat ny = new Stat(name, mean, std);
			base.add(ny);
			
			inn.close();
			
		}
		return base;
		
	}
	
	//returns the average/mean from numbers in an Arraylist
	public static double calcAvg(ArrayList<Double> data){
	double n = data.size();
	double tot = 0;
	for(int i=0; i<data.size(); i++){
		tot += data.get(i); 
	}
	
	double avg = tot/n;
	
	return avg;
	
	}
	
	//returns the standard deviation from numbers in an Arraylist
	public static double calcStd(ArrayList<Double> data){
	double avg = calcAvg(data);
	double n = data.size();
	double sum = 0;
	for(int i = 0; i<data.size(); i++){
		double x = data.get(i);
		sum += Math.pow((x-avg), 2);
	}
	
	double std = Math.sqrt(sum/n);
	
	return std;
}

	// Takes in number of days to generate, base statistics to compare to and then generates profiles for each day.
	// TODO: Are we supposed to log the days generated for the interactive part as well?
	// TODO: Anyway that shouldn't be difficult to add, for now it only checks if it's supposed to flag them.
	public static void checkDays(ArrayList<Stat> baseStats) {
		double[] totals = new double[nrOfEvents];
		for(int i=0; i<days; i++) {
			for(int j=0; j<nrOfEvents;j++) {
				totals[j] = normal(j, Stats);
			}
			boolean flag = alertEngine(totals, baseStats);
			if(flag) {
				System.out.println("flag!  day number: " + i);
			}
		}
	}

	// Asks for the totals for each event for some day and the base statistics calculated in preproccessing then flags suspicious days.
	public static boolean alertEngine(double[] freq, ArrayList<Stat> base) {
	    int threshold = 0;
	    double alert = 0;
	    for(int i = 0; i<nrOfEvents; i++) {
			threshold += 2*Events.get(i).weight;
			alert += Events.get(i).weight*(Math.abs(freq[i]-base.get(i).mean))/base.get(i).std;
	    }
	    if(alert > threshold) {
			return true;
	    }
	    return false;
	}

	//handles input from user in the runtime
	//prompts the user for input in the form [Stats.txt Days] or "quit", if the input is not in that format the user is prompted to try again
	public static boolean input(){
		String s;
		String[] sa;
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter new file and number of days [Stats.txt Days] or \"quit\" to exit" );
		while(true){
			s = in.nextLine().trim();
			sa = s.split(" ");
			if(sa.length==2){
				statFile = sa[0];
				days = Integer.parseInt(sa[1]);
				System.out.println("got: " + statFile+ " " + days);
				return true;
			}
			if(sa.length==1 && sa[0].contains("quit")){
				System.out.println("quitting");
				return false;
			}

			else{
				System.out.println("illegal input, try again");
			}
		}
	}



	// These functions are used by genDay to make things a little cleaner.

	// Picks an event to add to the log next, note that this function is basically obsolete
	// It should now take in eventNum and check if the total is depleted then return eventNum(+1)
	public static int pickEvent(double[] totals) {
	    for(int i = 0; i<totals.length; i++) {
	    	if(totals[i] > 0) {
	    		return i;
	    	}
	    }
	    return -1;
	}

	// Makes sure the time is correctly formatted, that is seconds < 60, minutes < 60, hours < 24.
	public static int[] incTime(int hrs, int min, int sec) {
		min += sec/60;
		sec = sec%60;
		hrs += min/60;
		min = min%60;
		hrs = hrs%24;
		int[] time = new int[] {hrs, min, sec};
		return time;
	}

	// Decides the magnitude of the eventInstance for the log file.
	public static double eventMagnitude(String type, double total) {
	    Random rng = new Random();
	    if(type.equals("D")) return 1;
	    // This is very ugly, could be improved with dynamic probability (1.42 would grow from 1)
	    double val = rng.nextDouble()*total*1.25; // it has a 80% chance of splitting the total
	    if(val > total) return total; // 20% chance of returning the total straight away
	    return val/1.25; // the first events will be inflated, but this helps a little
	}



	// These functions are used by the writeToLog function to make the logfile cleaner and easier to read.

	// Returns the eventSize correctly formatted for the log file based on the type of the event
	public static String sizeFormat(double size, String type) {
	    if(type.equals("E")) {
		return ""+((int) (Math.round(size)));
	    }
	    else if(type.equals("D")) {
		return "1";
	    }
	    return rounder(""+size);
	}

	// Cuts of trailing decimals after the first two (doesn't do rounding but it doesn't matter for random data anyway)
	public static String rounder(String dubs) {
	    String num = "";
	    int stop = dubs.indexOf('.')+3;
	    for(int i = 0; i<stop; i++) {
	    	num += dubs.charAt(i);
	    }
	    return num;
	}

	// Makes sure that the time string is of the same length e.g. 00:05:03 instead of 0:5:3)
	public static String timeFormat(int n) {
	    if((""+n).length() == 1) return "0"+n;
	    return ""+n;
	}

	// Prints error message for normal before exiting program to avoid infinite loops.
	public static void printError(int i) {
		if (Events.get(i).hasMin && Events.get(i).hasMax) {
			System.out.println("The program could not find a value with standard deviation: "+Stats.get(i).std+", mean: "+Stats.get(i).mean+
					" in the interval ["+Events.get(i).min+"; "+Events.get(i).max+"].");
		}
		else if(Events.get(i).hasMin) {
			System.out.println("The program could not find a value with standard deviation: "+Stats.get(i).std+", mean: "+Stats.get(i).mean+" larger than "+Events.get(i).min);
		}
		else if(Events.get(i).hasMax) {
			System.out.println("The program could not find a value with standard deviation: "+Stats.get(i).std+", mean: "+Stats.get(i).mean+" smaller than "+Events.get(i).max);
		}
		else {
			System.out.println("This is unexpected. The type of error that caused the program to quit should not be possible with your input.");
		}
		System.out.println("Either you were extraordinarily unlucky or something is wrong with your input for "+Events.get(i).name+" please try again.");
	}

}
