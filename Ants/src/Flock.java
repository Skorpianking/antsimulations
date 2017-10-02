

import java.io.PrintWriter;
import java.util.ArrayList;

public class Flock {
	int time = 0;
	double width = 1000;
	double height = 1000;
	PrintWriter outputStream = null;
		
	  ArrayList<basicBoid> boids; // An ArrayList for all the boids 
	  Flock(PrintWriter stream) {
	    boids = new ArrayList<basicBoid>(); // Initialize the ArrayList
	    outputStream = stream;
	  }

	  void run() {

	   // To avoid concurrency issues, we walk backwards through our list of agents
	   // when an agent runs out of energy it is removed from the scene, when all agents
	   // die (atm) the scene should end -- will modify to end the scene if all "attack" agents
	   // are dead, or they achieve hitting the goal
	    while(true) {
		    //System.out.println("time: " + time);
		    ++time;
		   for (int i = boids.size() - 1; i >= 0; i--) {
		     try{
		      basicBoid b = boids.get(i);
		      if(b.role.equals("attack") && b.checkWin()) { // check win condition here
		        System.out.println("invaders win!");
		        outputStream.write(time+",0\n");
		        outputStream.flush();
		        return;
		      }
		      else if (b.finished()) {
		        System.out.println(b.name + " out of energy \n");
		        boids.remove(b);
		      } // end if
		      else {
			    //System.out.println(i);
		        b.run(boids, width, height, time);
		      }
		     }
		     catch(Exception e) {
		       return; // just exit gracefully
		     }
	
		    } // end for
		    if(boids.size() <= 0) { // end the scene if no agents are left
		      outputStream.write(time+",1\n");
		      outputStream.flush();
		      return;
		    }
	    }
	 }

	  void addBoid(basicBoid b) {
	    boids.add(b);
	  }
	  
	  void removeBoid(basicBoid b) {
	    boids.remove(b);
	  }

} // class Flock
