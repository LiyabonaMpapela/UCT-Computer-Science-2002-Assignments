//Copyright M.M.Kuttel 2024 CSC2002S, UCT
//Made parallel by Liyabona Mpapela
package ParallelAbeliSandpile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import javax.imageio.ImageIO;






//This class is for the grid for the Abelian Sandpile cellular automaton
public class ParallelGrid {
	private int rows, columns;
	public int [][] grid; //grid
	public int [][] updateGrid;//grid for next time step
	//private static final int THRESHOLD = 100000;
	ForkJoinPool pool=new ForkJoinPool();




    public ParallelGrid(int w, int h) {
		rows = w+2; //for the "sink" border
		columns = h+2; //for the "sink" border
		grid = new int[this.rows][this.columns];
		updateGrid=new int[this.rows][this.columns];
		/* grid  initialization */
		for(int i=0; i<this.rows; i++ ) {
			for( int j=0; j<this.columns; j++ ) {
				grid[i][j]=0;
				updateGrid[i][j]=0;
			}
		}
	}



	public ParallelGrid(int[][] newGrid ) {
		this(newGrid.length,newGrid[0].length); //call constructor above
		//don't copy over sink border
		for(int i=1; i<rows-1; i++ ) {
			for( int j=1; j<columns-1; j++ ) {
				this.grid[i][j]=newGrid[i-1][j-1];
			}
		}

	}
	public ParallelGrid(ParallelGrid copyGrid) {
		this(copyGrid.rows,copyGrid.columns); //call constructor above
		/* grid  initialization */
		for(int i=0; i<rows; i++ ) {
			for( int j=0; j<columns; j++ ) {
				this.grid[i][j]=copyGrid.get(i,j);
			}
		}
	}
	
	public int getRows() {
		return rows-2; //less the sink
	}

	public int getColumns() {
		return columns-2;//less the sink
	}


	int get(int i, int j) {
		return this.grid[i][j];
	}

	void setAll(int value) {
		//borders are always 0
		for( int i = 1; i<rows-1; i++ ) {
			for( int j = 1; j<columns-1; j++ ) 			
				grid[i][j]=value;
			}
	}
	

	//for the next timestep - copy updateGrid into grid
	public void nextTimeStep() {
		for(int i=1; i<rows-1; i++ ) {
			for( int j=1; j<columns-1; j++ ) {
				this.grid[i][j]=updateGrid[i][j];
			}
		}
	}
	

    //Update the grid and if changed return true else false
    boolean update() {

        // Create and invoke the GridUpdateTask
       UpdateGrid pg = new UpdateGrid(1, rows - 1);
		boolean change = pool.invoke(pg);
		if(change)
		{
			nextTimeStep();
		}

		return change;

    }
	
	
	
	//display the grid in text format
	void printGrid( ) {
		int i,j;
		//not border is not printed
		System.out.printf("Grid:\n");
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n");
		for( i=1; i<rows-1; i++ ) {
			System.out.printf("|");
			for( j=1; j<columns-1; j++ ) {
				if ( grid[i][j] > 0)
					System.out.printf("%4d", grid[i][j] );
				else
					System.out.printf("    ");
			}
			System.out.printf("|\n");
		}
		System.out.printf("+");
		for( j=1; j<columns-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n\n");
	}

	//write grid out as an image
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        //integer values from 0 to 255.
        int a=0;
        int g=0;//green
        int b=0;//blue
        int r=0;//red

		for( int i=0; i<rows; i++ ) {
			for( int j=0; j<columns; j++ ) {
			     g=0;//green
			     b=0;//blue
			     r=0;//red

				switch (grid[i][j]) {
					case 0:
		                break;
		            case 1:
		            	g=255;
		                break;
		            case 2:
		                b=255;
		                break;
		            case 3:
		                r = 255;
		                break;
		            default:
		                break;

				}
		                // Set destination pixel to mean
		                // Re-assemble destination pixel.
		              int dpixel = (0xff000000)
		                		| (a << 24)
		                        | (r << 16)
		                        | (g<< 8)
		                        | b;
		              dstImage.setRGB(i, j, dpixel); //write it out


			}}

        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
	}




	class UpdateGrid extends RecursiveTask<Boolean>
	{
		//Threshold for spliting tasks
		private static final int THRESHOLD = 1000;
		//the start and end of rows
		private int start, end;

		//constructor to initialise start and end
		UpdateGrid(int start, int end)
		{
			this.start=start;
			this.end=end;

		}

		//Compute method for RecursiveTask that updates the grid
  @Override
		protected Boolean compute()
		{
			//if the number of rows are less than the Threshold
			if((end-start <THRESHOLD))
			{
				//key method to calculate the next update grid
				boolean change=false;

				//update grid
				for( int i =start; i<end; i++ )
				{
					for( int j = 1; j<columns-1; j++ )
					{
						//calculates new value of each cell
						updateGrid[i][j] = (grid[i][j] % 4) +
								(grid[i-1][j] / 4) +
								(grid[i+1][j] / 4) +
								(grid[i][j-1] / 4) +
								(grid[i][j+1] / 4);

						//Checks of there were any changes to the grid
						if (grid[i][j]!=updateGrid[i][j]) {
							change=true;
						}
					}
				} //end nested for

				//returns whether the were changes or not
				return change;
			}
			else
			{
				//calculates the half of the rows
				int split =(end-start)/2;

				//Assigns each half of the grid to a new UpdateGrid class
				UpdateGrid left =new UpdateGrid(start,split);//first half
				UpdateGrid right =new UpdateGrid(split, end);//second half
				left.fork();//Executes the left half asynchronously
				boolean rightAns=right.compute();
				boolean leftAns =left.join();

				return  rightAns || leftAns ;//Compute the right half and join the left half
			}
		}



	}


}