
import java.io.PrintWriter;
import java.util.ArrayList;

public class basicBoid {
	Vector2D position;
	Vector2D velocity;
	Vector2D acceleration;
	double r;
	double angle = 0;
	double circleRadii = 0.01; // creates tight circles if pushed over 0.1
	double radius = 100; // how far away to fly
	double maxforce;    // Maximum steering force
	double maxspeed;    // Maximum speed
	ArrayList<Vector2D> goals;
	int numGoals;
	Boolean[] metGoals;
	//color col;
	String name;
	String role; // tracks current role
	int numNeighbors; // track the 'close' neighbors, anyone within 25 pixels. changes per time step
	float avgDist; // the average distance from those neighbors
	float energy; // way to track energy levels of attackers (basically establishes a timeframe for them to achieve a goal)
	int pursuitTime = 20; // pursue agents for 20 time steps
	int timer = 0;
	
	// write to file functionality
	PrintWriter outputStream;
	boolean roleChange = true; // allows circle and patrol behaviors
	boolean sig = false; // allows signaling to occur
	boolean noFlock = true; // turns off flocking behavior of defending birds -- however, we keep separation, still want birds to avoid colliding with one another
	
	// Nest center
	int nestX = 320;
	int nestY = 320;

	basicBoid(double x, double y, String str, float fuelLevel, PrintWriter stream) { // basicBoid constructor
		acceleration = new Vector2D(0, 0);
	    velocity = new Vector2D(1,1); // trying 1 atm
	    position = new Vector2D(x, y);
	    name = str;
	    avgDist = 0;
	    r = 2.0;
	    maxspeed = 1;
	    maxforce = 0.03;
	    goals = new ArrayList<Vector2D>();
	    numGoals = 5;
	    metGoals = new Boolean[] {false, false, false, false, false};
	    energy = fuelLevel; // energy level of the basicBoid, aka its lifespan (1000, atm, allows them to reach all the goals)
	    //col = color(255, 204, 0);
	    
	    outputStream = stream;
	    
	 } // end basicBoid constructor ctr

	 void run(ArrayList<basicBoid> basicBoids, double width, double height, int t) { // invoked to run the basicBoid
	   timer = t;
	   flock(basicBoids);
	   update();
	   checkGoals(basicBoids); // update goals if they are reached
	   checkCollisions(basicBoids);  // check collisions here
	   decEnergy();
	   borders(width, height); // check the borders
	   render(); // paint on cnvas
	 }
	  
	 // decrement energy level
	 void decEnergy() {
	    --energy;
	 }
	  
	 // check if the basicBoid is out of energy
	 boolean finished() {
	   if(this.energy == 0)
	     return true;
	   else
	     return false;
	 }
	  
	  // Calculate distance between basicBoids, if distance between opposing teams is less than 10 pixels, they both die
	  void checkCollisions(ArrayList<basicBoid> basicBoids) {
	    // To avoid concurrency issues, we must iterate through the list
	    // backwards and without 'enhanced' iteration (ie for(basicBoid curr : basicBoids))
	    float killDist = 4f; //10f;
	    
	    ArrayList<basicBoid> toRemove = new ArrayList<basicBoid>();
	    
	    for (int i = basicBoids.size() - 1; i >= 0; i--) {
	      basicBoid b = basicBoids.get(i); 
	      double d = position.sub(b.position).length(); // distance between this basicBoid and another
	      //System.out.println(this.role + " " + b.role + " " + d);
	      if(this.role.equals("attack") && (b.role.equals("circle") || b.role.equals("patrol")) && d < killDist) {
	        // reduce energy to 0 for both
	        System.out.println("patrollers won here");
	        outputStream.write(timer+",1\n");
	        outputStream.flush();
	        toRemove.add(this);
	        toRemove.add(b);
	      } // end if
	    } // end for
	    
	    // see if we can remove here without issues -- placed here to avoid concurrency access issues (delete while trying
	    // to modify)
	    for(int j = toRemove.size() - 1; j >= 0; j--) {
	      basicBoids.remove(toRemove.get(j));
	    }
	    
	  } // end check collision

	  void applyForce(Vector2D force) {
	    // We could add mass here if we want A = F / M
	    acceleration = acceleration.add(force);
	  }

	  // We accumulate a new acceleration each time based on three rules
	  void flock(ArrayList<basicBoid> basicBoids) {
	    
	    Vector2D sep = separate(basicBoids);   // Separation
	    Vector2D ali = align(basicBoids);      // Alignment
	    Vector2D coh = cohesion(basicBoids);   // Cohesion
	    Vector2D gol = targets(); // Move towards target
	    
	    // Enemy detected move away
	    if(detect(basicBoids)) {
	      sep = sep.scale(-3.5);
	      ali = ali.scale(-1.5);
	      coh = coh.scale(-1.5);
	      gol = gol.scale(1.5); // want target is still important
	    }
	    else if(detectObstacle()) { // these weights can be tuned
	      sep = sep.scale(-1.5);
	      ali = ali.scale(-2.5);
	      coh = coh.scale(-3.5);
	      gol = gol.scale(-3.0); // want targets to be less important
	    }
	    else {
	      // Arbitrarily weight these forces
	      sep = sep.scale(1.5); // change to 1.5 for pursuit / evasion
	      ali = ali.scale(.5); // 1.5 for P / E
	      coh = coh.scale(1.0); // 2.0 for P/ E
	      gol = gol.scale(2.0); // 2.0 for P / E
	      
	      if(noFlock && (this.role.equals("circle") || this.role.equals("patrol"))) { // zero out these factors
	    	  ali = ali.scale(0);
	    	  coh = coh.scale(0);
	      }
	    }
	    // Add the force vectors to acceleration    
	    applyForce(sep);
	    applyForce(ali);
	    applyForce(coh);
	    
	    if(this.role.equals("circle")) {
	      Vector2D circle = circleFlight();
	      circle = circle.scale(1.5); // nest defenders atm
	      applyForce(circle);
	    }
	    else if(this.name.equals("Patrol")) { // don't add goal
	    	
	    }
	    else {
	      applyForce(gol); // other birds
	    }
	    //acceleration,velocity,numNeighbors,avgDisttoNeighbor
	    //outputStream.write(name+","+position.dX+","+position.dY+","+acceleration.length()+","+velocity.length()+","+numNeighbors+","+avgDist+"\n");
	    numNeighbors = 0;
	    avgDist = 0; 
	  }

	  // Method to update position
	  void update() {
	    // Update velocity
	    velocity = velocity.add(acceleration);
	    // Limit speed
	    velocity = velocity.normalize();
	    velocity = velocity.scale(maxspeed);
	    position = position.add(velocity);
	    // Reset acceleration to 0 each cycle
	    acceleration = new Vector2D(0,0);
	  }

	  // A method that calculates and applies a steering force towards a target
	  // STEER = DESIRED MINUS VELOCITY
	  Vector2D seek(Vector2D target) {
	    Vector2D desired = target.sub(position); // A vector pointing from the position to the target
	    // Scale to maximum speed
	    desired.normalize();
	    desired = desired.scale(maxspeed);

	    // Steering = Desired minus Velocity
	    Vector2D steer = desired.sub(velocity);
	    steer.limit(steer, maxforce);  // Limit to maximum steering force
	    return steer;
	  }
	  
	  
	  // Increments angle
	  void incAngle() {
	    double pi = 3.14;
	    // values between .01 and .05 
	    angle = (float) ((angle + circleRadii) - 2*pi); // set rotation angle
	  }
	  
	  // This method should make a bird fly in a circle around a pre-determined point
	  // We are setting this circle
	  Vector2D circleFlight() {
	    
	    float X = nestX;
	    float Y = nestY;
	    Vector2D target = new Vector2D(X, Y);
	    double orbitRadius = this.radius;

	    // Need to work on this piece, want agent to return to an orbit position
	    // that is within a certain distance
	    double distance = Math.pow(( Math.pow((position.dX - target.dX),2) + Math.pow((position.dY - target.dY),2)),.5);   
	    if(distance > orbitRadius) {
	      // Euclidean distance -- too far away, the direct route is nice, but I think
	      // we need to ease them into the orbit
	      // System.out.println("Head home: " + distance);
	      Vector2D desired = target.sub(position);
	      desired.normalize();
	      desired = desired.scale(maxspeed);
	      return desired;
	    }
	    
	    incAngle(); // increment agent angle
	    double x = X + orbitRadius * Math.cos(angle);
	    double y = Y + orbitRadius * Math.sin(angle);
	    Vector2D heading = new Vector2D(x,y);
	    heading = heading.sub(target);
	    return heading;
	  }

	  /**
	   * May want to rewrite to enable console production?
	   */
	  void render() {

	  }

	 
	  /** 
	   * Keeps the agent inside a user defined bordered area
	   * @param width
	   * @param height
	   */
	  void borders(double width, double height) {    
	     if ((position.dX < 0) || (position.dX > width)) {
	      velocity.dX = velocity.dX * -1;
	    }
	    if ((position.dY < 0) || (position.dY > height)) {
	      velocity.dY = velocity.dY * -1;
	    }  
	  }

	  // Separation
	  // Method checks for nearby basicBoids and steers away
	  Vector2D separate (ArrayList<basicBoid> basicBoids) {
	    float desiredseparation = 25.0f;
	    Vector2D steer = new Vector2D(0, 0);
	    int count = 0;
	    float neighborDistances = 0;
	    // For every basicBoid in the system, check if it's too close
	    for (basicBoid other : basicBoids) {
	      double d = Vector2D.dist(position, other.position);
	      // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
	      if ((d > 0) && (d < desiredseparation)) {
	        // Calculate vector pointing away from neighbor
	        Vector2D diff = position.sub(other.position);
	        diff.normalize();
	        diff = diff.scale(1/d);        // Weight by distance (this is division...)
	        steer = steer.add(diff);
	        count++;            // Keep track of how many
	   
	        // Experimenting with counts -- 10 is our defined 'neighborhood' distance
	        if (d > 0 && d < 10) {
	          numNeighbors++;
	          neighborDistances += d;
	        }
	      }
	      else if(d < 1){
	        //System.out.println("too close!");
	      }
	      
	    } // end for
	    
	    // Average if their are any neighbors
	    if(numNeighbors > 0)
	      avgDist = neighborDistances / numNeighbors;
	    
	    // Average -- divide by how many
	    if (count > 0) {
	      steer = steer.scale((float)1/count);
	    }

	    // As long as the vector is greater than 0
	    if (steer.length() > 0) {

	      // Implement Reynolds: Steering = Desired - Velocity
	      //steer.normalize();
	      //steer.scale(maxspeed);
	      steer.sub(velocity);
	      steer.limit(steer,maxforce);
	    }
	    return steer;
	  }

	  // Alignment
	  // For every nearby basicBoid in the system, calculate the average velocity
	  Vector2D align (ArrayList<basicBoid> basicBoids) {
	    float neighbordist = 50;
	    Vector2D sum = new Vector2D(0, 0);
	    int count = 0;
	    for (basicBoid other : basicBoids) {
	      double d = Vector2D.dist(position, other.position);
	      if ((d > 0) && (d < neighbordist)) {
	        sum = sum.add(other.velocity);
	        count++;
	      }
	    }
	    if (count > 0) {
	      sum = sum.scale(1/(float)count);
	      // First two lines of code below could be condensed with new Vector2D setMag() method
	      // Not using this method until Processing.js catches up
	      // sum.setMag(maxspeed);

	      // Implement Reynolds: Steering = Desired - Velocity
	      sum.normalize();
	      //sum.setMag(maxspeed);
	      sum = sum.scale(maxspeed);
	      Vector2D steer = sum.sub(velocity);
	      steer.limit(steer, maxforce);
	      return steer;
	    } 
	    else {
	      return new Vector2D(0, 0);
	    }
	  }

	  // Cohesion
	  // For the average position (i.e. center) of all nearby basicBoids, calculate steering vector towards that position
	  Vector2D cohesion (ArrayList<basicBoid> basicBoids) {
	    float neighbordist = 50;
	    Vector2D sum = new Vector2D(0, 0);   // Start with empty vector to accumulate all positions
	    int count = 0;
	    for (basicBoid other : basicBoids) {
	      double d = Vector2D.dist(position, other.position);
	      if ((d > 0) && (d < neighbordist)) {
	        sum = sum.add(other.position); // Add position
	        count++;
	      }
	    }
	    if (count > 0) {
	      sum = sum.scale(1/count);
	      return seek(sum);  // Steer towards the position
	    } 
	    else {
	      return new Vector2D(0, 0);
	    }
	  }
	  
	  // Goals -- get basicBoids to seek a target
	  Vector2D targets() {
	    Vector2D heading = new Vector2D(0,0);
	    for(int i = 0; i < numGoals; i++) {
	        if(metGoals != null && !metGoals[i]) {
	          if(goals.size() != 0)
	            heading = seek(goals.get(i));
	            //print("Heading to goal: " + i);
	          return heading; // short circuit
	        }
	    }
	    // otherwise, just float around
	    return heading;
	  }
	  
	  // Check and update goals if met
	  void checkGoals(ArrayList<basicBoid> basicBoids) {
	    // could use a for loop if you don't care about changes
	    if(goals.size() == 0)
	      return;
	      
	    int proximity = 4;
	    
	    for(int i = 0; i < numGoals; i++) {
		    	if( ((position.dX > goals.get(i).dX - proximity) && position.dX < goals.get(i).dX + proximity) &&
			            (position.dY > goals.get(i).dY - proximity && position.dY < goals.get(i).dY + proximity) && !metGoals[i]) {
			        metGoals[i] = true;
			        //System.out.println("Met goal: " + i);
			        //colorMode(HSB, 100);  // Use HSB with scale of 0-100
			        //col = color(245,138,24);  // orange
			       //signal(basicBoids, 0, col);
		    	} // end if
	    } // end for
	  } // end checkGoals
	      
	  // Win condition for the invaders
	  boolean checkWin() {
		 int winProx = 10;
		 int nestLoc = 0;
	    // Adjust "win" distance as real bases are more than just a single point in space
	    if(((position.dX > goals.get(nestLoc).dX - winProx) && position.dX < goals.get(nestLoc).dX + winProx) &&
	            (position.dY > goals.get(nestLoc).dY - winProx && position.dY < goals.get(nestLoc).dY + winProx))
	            return true;
	    return metGoals[nestLoc]; // currently, last target is the nest
	  }
	  
	  
	  // Notify neighbors that we have reached a destination
	  void signal(ArrayList<basicBoid> basicBoids) { //, color col) {
	    int neighbordist = 200; // definition of "neighbor" distance (communication distance)
	    for (basicBoid other : basicBoids) {
	      if(other.role != "attack") {
	        double d = Vector2D.dist(position, other.position);      
	        if ((d >= 0) && (d < neighbordist)) {
	            //other.col = col;
	            other.role = "patrol"; // change the role of my friends 
	        } // if inside neighborly distance
	      } // end if
	    } // end for
	  } // end signal
	  
	  // Check to see if predator is nearby
//	  Boolean detect() {
//	    Boolean ret = false;
//	    if( ((position.x > currMouseX - 50) && position.x < currMouseX + 50) &&
//	            (position.y > currMouseY - 50 && position.y < currMouseY + 50)) {
//	        ret = true;
//	    }
//	    return ret;
//	  }
	  
	// see if we detect an enemy within our range (at the moment it's 360 degrees)
	  Boolean detect(ArrayList<basicBoid> basicBoids) {
	    float desiredseparation = 50f; // <-- adjust attacker sensor range (lower numbers = higher risk but prob more success)
	    float sensorRange = 150; // <--adjust allied sensor range
	    // For every basicBoid in the system, check if it's an enemy!
	    boolean changeToCirc = true; // default role is circling the nest
	    for (basicBoid other : basicBoids) {
	      if(this.role.equals("attack")) { // only invaders want to evade    
	         double d = Vector2D.dist(position, other.position);
	         // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
	         if ( (other.role.equals("patrol") || other.role.equals("circle")) && (d > 0) && (d < desiredseparation)) { // commentback in for avoidance
	          return true;
	         } // end if desired sep
	      } // end if attacker
	      else { // now if we are nest patroller (i.e. role = circle), we want to change our role to pursuit
	        if(this.role.equals("circle")) {
	          double d = Vector2D.dist(position, other.position);
	          if(d <= sensorRange && other.role.equals("attack")) {
	            this.role = "patrol"; // change our role
	            //this.col = color(48,139,206); // visually change our role (help us humans see things)
	            //  add signal fellow agents here -- this will lead to other role changes
	            if(sig)
	            	signal(basicBoids); //,this.col);
	          } // end if within sensor range
	        }  // end if circle
	        else if(this.role.equals("patrol")) {
	          double d = Vector2D.dist(position, other.position);
	          if(d <= sensorRange && other.role.equals("attack")) {
	            changeToCirc = false; // we will continue pursuit role (patrol)
	            applyForce((seek(other.position)).scale(2.5)); // move towards enemy (maybe)
	            if(sig)
	            	signal(basicBoids); //,this.col);
	          }
	        } // else if patroller
	      } // end else
	    } // end for
	    
	    if(roleChange) {
		    if(changeToCirc && this.role.equals("patrol")) {
		      this.role = "circle"; // head back to base and patrol there
		      //this.col = color(2550, 0, 0); // visually change role
		      pursuitTime = 20;
		    } // change role
	    }
	    return false;
	  }
	  
	  // Check for obstacles: at the moment these are ellipses (perfect circles)
	  Boolean detectObstacle() {
	    Boolean ret = false;
	    //for(Ellipse e : ells) {
	    //  if( dist(position.x, position.y, e.x, e.y) <= (e.width/2+25) ) {
	    //    ret = true;
	    //  }
	    //}
	    return ret;
	  }	
} // end basicBoid class
