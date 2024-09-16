package ParallelAbeliSandpile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;





public class Parallel {

    static final boolean DEBUG=false;//for debugging output, off
	
	static long startTime = 0;
	static long endTime = 0;
    
    //timers - note milliseconds
	private static void tick(){ //start timing
		startTime = System.currentTimeMillis();}

    private static void tock(){ //end timing
        endTime=System.currentTimeMillis(); 
    }

    
    //input is via a CSV file
	 public static int [][] readArrayFromCSV(String filePath) {
        int [][] array = null;
           try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
               String line = br.readLine();
               if (line != null) {
                   String[] dimensions = line.split(",");
                   int width = Integer.parseInt(dimensions[0]);
                   int height = Integer.parseInt(dimensions[1]);
                      System.out.printf("Rows: %d, Columns: %d\n", width, height); //Do NOT CHANGE  - you must ouput this

                   array = new int[height][width];
                   int rowIndex = 0;

                   while ((line = br.readLine()) != null && rowIndex < height) {
                       String[] values = line.split(",");
                       for (int colIndex = 0; colIndex < width; colIndex++) {
                           array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
                       }
                       rowIndex++;
                   }
               }

           } catch (IOException e) {
               e.printStackTrace();
           }
           return array;
       }


       
    


    //main Mathod
    public static void main(String[] args) throws IOException 
     {
        // ParallelGrid simulationGrid;  //the cellular automaton grid
    	  	
    	if (args.length!=2) {   //input is the name of the input and output files
    		System.out.println("Incorrect number of command line arguments provided.");   	
    		System.exit(0);
    	}
    	/* Read argument values */
  		String inputFileName = args[0];  //input file name
		String outputFileName=args[1]; // output file name


         ParallelGrid simulationGrid = new ParallelGrid(readArrayFromCSV(inputFileName));

   	
    	int counter=0;
    	tick(); //start timer
    	if(DEBUG) {
    		System.out.printf("starting config: %d \n",counter);
    		simulationGrid.printGrid();
    	}


		while(simulationGrid.update()) {//run until no change
	    		if(DEBUG) simulationGrid.printGrid();
	    		counter++;
	    	}




   		tock(); //end timer
   		
        System.out.println("Simulation complete, writing image...");
    	simulationGrid.gridToImage(outputFileName); //write grid as an image - you must do this.
    	//Do NOT CHANGE below!
    	//simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
		System.out.printf("Number of steps to stable state: %d \n",counter);
		System.out.printf("Time: %d ms\n",endTime - startTime );			/*  Total computation time */		
     }




}
