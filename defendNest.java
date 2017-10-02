
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

public class defendNest {

	public static void main(String[] args) {
		int numBadBoids = 0; // must be adjusted when adding circle boids
		int numCircBoids = 10;
		int numBoids = 1;
		double maxSpeedDefender = .5;
		double maxSpeedInvader = 1.50;
		float defenderFuel = 1800;
		float invaderFuel = 1800;
		Flock flock;
		Ellipse ells[];
		PrintWriter outputStream = null;
		boolean circleBoids = false;
		  
	   try{
	           outputStream = new PrintWriter( new FileOutputStream("D:\\AntRuns\\noflocks\\flexnosig\\ant_close_flex_nosig_10vs1_150vs5.csv", true));
	           //outputStream.write("bird_name,longitude,latitude,acceleration,velocity,numNeighbors,avgDisttoNeighbor\n");
	           outputStream.write("time,winner\n");
	   }
	   catch (FileNotFoundException e){
	             System.out.println("Error opening the file boid_tracks.csv.\n" + e.toString());
	             System.exit(0);
	   }
	 
   
	   
	  // Simulation loop
	  for(int numSims = 0; numSims < 10000; numSims++) {
	 
      flock = new Flock(outputStream);
	 
	  // Add some bad boids -- nest defenders at the moment
	  Random rand = new Random();
	  
	  // previous value: 620, 620 -- obviously, tighter boundaries enables better defense for circle defenders.
	  int boundX = 320; // guarantees ants start inside the rectangle 
	  int boundY = 320;
	  
	  //50 is the maximum and the 1 is our minimum 
	 for (int i = 0; i < numBadBoids; i++) {
	     basicBoid t = new basicBoid((rand.nextInt(boundX-100)+100), (rand.nextInt(boundY-100)+100), "Patrol" + i, defenderFuel, outputStream);
	     t.role = "patrol";
	     //t.col = color(255,0,0);
	     t.maxspeed = maxSpeedDefender;
	     t.radius = 200;

	     flock.addBoid(t);
	 }
	 
	 
	 for (int i = 0; i < numCircBoids; i++) {
	     basicBoid t = new basicBoid((rand.nextInt(boundX-100)+100), (rand.nextInt(boundY-100)+100), "Circle" + i, defenderFuel, outputStream);
	     t.role = "circle";
	     //t.col = color(255,0,0);
	     t.maxspeed = maxSpeedDefender;
	     t.radius = 100;
	     
	     flock.addBoid(t);
	 }
	 
	 
	 
	 if(circleBoids) {
//		  // Add 4 circling defenders
//		  basicBoid c1 = new basicBoid(220,320, "CircleW",defenderFuel, outputStream);
//		  c1.role = "circle";
//		  //c1.col = color(255,0,0);
//		  c1.maxspeed = maxSpeedDefender;
//		  basicBoid c2 = new basicBoid(420,320, "CircleE",defenderFuel, outputStream);
//		  c2.role = "circle";
//		  //c2.col = color(255,0,0);
//		  c2.maxspeed = maxSpeedDefender;
//		  basicBoid c3 = new basicBoid(320,220, "CircleN",defenderFuel, outputStream);
//		  c3.role = "circle";
//		  //c3.col = color(255,0,0);
//		  c3.maxspeed = maxSpeedDefender;
//		  basicBoid c4 = new basicBoid(320,420, "CircleS",defenderFuel, outputStream);
//		  c4.role = "circle";
//		  //c4.col = color(255,0,0);
//		  c4.maxspeed = maxSpeedDefender;
//		  flock.addBoid(c1);
//		  flock.addBoid(c2);
//		  flock.addBoid(c3);
//		  flock.addBoid(c4);
//		 
//		  numBadBoids += 4; // don't forget to add in these boids above
		 
	 }
	  // Add an initial set of boids into the system
	  for (int i = 0; i < numBoids; i++) {
	    basicBoid b = new basicBoid(25.0, 25.0, "Attack" + i, invaderFuel, outputStream); // smaller box to appear
	    b.role = "attack";
	    b.maxspeed = maxSpeedInvader;
	    flock.addBoid(b);
	  }
	  
	  // Add goals to each boid
	  for( int i = 0; i < numBoids+numBadBoids+numCircBoids; i++) {
		  basicBoid t = flock.boids.get(i);
	    if(t.role.equals("attack")) {
	      t.goals.add(new Vector2D(320,320));//new Vector2D(100,100)); // <-- orig initial target
	      t.goals.add(new Vector2D(550,100));
	      t.goals.add(new Vector2D(550,550));
	      t.goals.add(new Vector2D(100,550));  
	      t.goals.add(new Vector2D(320,320)); // <--- target base (last goal)
	    }
	    else {
	    // no goals
	    }
	  }
	  
	  // Create some obstacles
	  ells = new Ellipse[1];
	  ells[0] = new Ellipse(320, 320, 50, 50); // place nest in center of the field of view
	  //ells[1] = new Ellipse(400, 250, 55, 55); // <--- obstacles to be added later
	  //ells[2] = new Ellipse(350, 450, 55, 55);
	  //ells[3] = new Ellipse(100, 350, 55, 55);
	 
	  flock.run(); // begin the simulation here.
	}
	} // end simulation outer loop
	
}
