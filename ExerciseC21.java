
/*******************************************************************************
  * File: ExerciseC21.java
  * Author: Emiel Verkade
  * Date: 31.1.2018
  * Class to solve the lift problem of C2, version 1 (test feasibility)
  ******************************************************************************/

import ilog.concert.*;
import ilog.cplex.*;

public class ExerciseC21 {
	//
	public static void LiftFeasibilitySolver(int numberLifts, int numberStops, int numberFloors, double beginTime) {
		try {
			// create a cplex object
			IloCplex cplex = new IloCplex();
			// maximise a constant, as we are testing feasibility
			IloNumVar constant = cplex.numVar(2043, 2043, IloNumVarType.Int);
			cplex.addMaximize(constant);
			// initializing the connection variables
			IloNumVar[][][] connections = new IloNumVar[numberFloors][numberFloors][];
			// for every floor
			for (int f1 = 0; f1 < numberFloors; f1++) {
				// for every floor
				for (int f2 = 0; f2 < numberFloors; f2++) {
					/*
					 * create an array of size numberLifts of binary variables, which can take on
					 * either 0 or 1. 0 representing that there is not a connection, between the two
					 * floors with that lift, 1 being there is a connection between the two floors
					 * with that lift
					 */
					connections[f1][f2] = cplex.numVarArray(numberLifts, 0, 1, IloNumVarType.Int);
				}
			}
			// initializing the stops variables
			IloNumVar[][] stops = new IloNumVar[numberFloors][];
			// for every floor
			for (int f = 0; f < numberFloors; f++) {
				/*
				 * create an array of size numberLifts of binary variables, which can take on
				 * either 0 or 1. 0 representing that there is not a stop at that floor for that
				 * lift, 1 being there is a stop at that floor with that lift
				 */
				stops[f] = cplex.numVarArray(numberLifts, 0, 1, IloNumVarType.Int);
			}
			// adding constraints for connections
			// for every floor
			for (int f1 = 0; f1 < numberFloors; f1++) {
				// for every floor
				for (int f2 = 0; f2 < numberFloors; f2++) {
					// create a new expression
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					// for every lift
					for (int l = 0; l < numberLifts; l++) {
						// add the binary connections variable for each lift between the two floors
						expr1.addTerm(connections[f1][f2][l], 1);
					}
					// add the constraint that this should be at least 1, ensuring each floor is
					// connected
					// with all others
					cplex.addGe(expr1, 1);
				}
			}
			// for every lift
			for (int l = 0; l < numberLifts; l++) {
				// create a new expression
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				// for every floor
				for (int f1 = 0; f1 < numberFloors; f1++) {
					// add the binary stops variable for each floor with that lift
					expr1.addTerm(stops[f1][l], 1);
				}
				// add the constraint that this should be at most the maximum number of stops,
				// to ensure
				// that no lift stops more than it is allowed to
				cplex.addLe(expr1, numberStops);
			}
			// for every floor
			for (int f1 = 0; f1 < numberFloors; f1++) {
				// for every floor
				for (int f2 = 0; f2 < numberFloors; f2++) {
					// for every lift
					for (int l = 0; l < numberLifts; l++) {
						// if a lift does not stop there, neither connection should not exist,
						// either between f1 and f2, or f2 and f1
						cplex.addLe(connections[f1][f2][l], stops[f1][l]);
						cplex.addLe(connections[f1][f2][l], stops[f2][l]);
					}
				}
			}
			// do not print any information regarding the progress of the solver (quicker
			// run times)
			cplex.setOut(null);
			// solve the ILP as it has been set up
			cplex.solve();
			// try the following:
			try {
				// if the objective value exists
				double d = cplex.getObjValue();
				// end the program
				cplex.end();
				// if the objective value does not exist, and CpxException will be thrown which
				// is caught here
			} catch (CpxException e) {
				// end the program
				cplex.end();
				// print out that the current set up of parameters is not feasible.
				System.out.println("Current set up not feasible");
				System.out.print("The maximum number of floors that adhere to the criterion with " + numberLifts
						+ " lifts, and " + numberStops + " stops per lift is not: ");
				System.out.println(numberFloors + " floors");
				System.out.println("Runtime = " + (System.currentTimeMillis() - beginTime) + "ms");
				// after telling the user, exit the program
				System.exit(0);
			}
		} catch (IloException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		// instruct user on the inputs expected and in what order
		if (args.length != 3) {
			System.out.println("Usage: java ExerciseC2 numberOfLifts numberOfStops numberOfFloors");
			System.out.println("Exiting now...");
			System.exit(0);
		}
		double beginTime = System.currentTimeMillis();
		/*******
		 * REDUNDANT CODE - LAZY WAY TO TEST MAXIMUM NUMBER OF FLOORS POSSIBLE 
		 * if (args.length == 2) { 
		 * 	int n = Integer.parseInt(args[0]); int s = Integer.parseInt(args[1]);
		 * 	int f1 = 0; System.out.println("With " + n + "elevators and " + s + " stops:");
		 * 	for (f1 = 1; f1 < Integer.MAX_VALUE; f1++) { 
		 * 		System.out.println("Currently checking feasibility of " + f1 + " floors");
		 * 		LiftFeasibilitySolver(n, s, f1, beginTime);
		 * 	}
		 * } else if (args.length == 3) {
		 ****************/
		// number of lifts is the first input
		int n = Integer.parseInt(args[0]);
		// number of stops is the second input
		int s = Integer.parseInt(args[1]);
		// number of floors to be tested is the third input
		int f = Integer.parseInt(args[2]);
		// run the solver
		LiftFeasibilitySolver(n, s, f, beginTime);
		// if we get here, that means the solver completed and a feasible solution to the ILP was found
		// therefore, we can tell the user the current set-up is feasible
		System.out.println("Congratulations! Current set up IS feasible");
		System.out.printf("For the parameters: numberOfLifts = %d, numberOfStops = %d, "
				+ "numberOfFloors = %d, the ILP was found to be solvable\n", n, s, f);
		System.out.println("Runtime = " + (System.currentTimeMillis() - beginTime) + "ms");
		// exit afterwards
		System.exit(0);
		/* } */
	}
}
