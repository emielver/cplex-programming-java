/*******************************************************************************
  * File: InvestmentPlan.java
  * Author: Emiel Verkade
  * Date: 31.1.2018
  * Class to solve the Investment problem as specified on Student Portal
  ******************************************************************************/

import java.io.*;
import java.util.*;
import ilog.concert.*;
import ilog.cplex.*;


public class InvestmentPlan {
	/* This function handles everything to do with the Investment Problem
	 * It reads information from files, aggregates it, sets up the ILP and
	 * solves it.
	 */
	public static void InvestmentSolving() {
		try {
			// initializes a new cplex object
			IloCplex cplex = new IloCplex();
			// calls the readBudgetArray method to return the budget array
			int[] budgetArray = readBudgetArray("budget.txt");
			// calls the readPayoutMatrix method to return the payout matrix
			int[][] payoutMatrix = readPayoutMatrix("payoutMatrix.txt");
			// uses the matrix to find the number of projects there are
			int numberProjects = payoutMatrix.length;
			// uses the array to find the number of years we are interested in
			int numberYears = budgetArray.length;
			// creates an array binary of variables of size numberProjects
			IloNumVar[] projectsChosen = cplex.numVarArray(numberProjects, 0, 1, IloNumVarType.Int);
			// creates an array of floating point variables between 0 and 1 of size numberProjects
			IloNumVar[] percentageInvested = cplex.numVarArray(numberProjects, 0, 1, IloNumVarType.Float);
			// creates an array of floating point variables between 0 and infinity of size numberYears + 1
			IloNumVar[] moneyInBank = cplex.numVarArray(numberYears + 1, 0, Float.MAX_VALUE, IloNumVarType.Float);
			// objective function (maximise the amount of money in the bank in year 6)
			cplex.addMaximize(moneyInBank[numberYears-1]);
			// for every year
			 for (int year = 0; year < numberYears + 1; year++) {
				 // create a new expression
				 IloLinearNumExpr expr1 = cplex.linearNumExpr();
				 // if we are in the last year
				 if (year == numberYears) {
					 // money in the last year is equal to 1.1* money of the year before
					expr1.addTerm(moneyInBank[numberYears-1], 1.1);
					cplex.addEq(expr1, moneyInBank[numberYears]);
				 }
				 // if this is the first year
				 else if (year == 0) {
					 // for every project
					 for (int project = 0; project < numberProjects; project++) {
						// add the cash flows of each project multiplied by how much is invested in them
						 expr1.addTerm(payoutMatrix[project][year], percentageInvested[project]);
					 }
					 // subtract the money in the bank in this year
					 expr1.addTerm(moneyInBank[year], -1);
					 // set this equal to the negative of the budget in this year
					 cplex.addEq(expr1, -1 * budgetArray[year]);
				 }
				 // otherwise
				 else {
					 // for every project
					 for (int project = 0; project < numberProjects; project++) {
						// add the cash flows of each project multiplied by how much is invested in them
						 expr1.addTerm(payoutMatrix[project][year], percentageInvested[project]);
					 }
					 // add 1.1 * the money left over from last year
					 expr1.addTerm(moneyInBank[year-1], 1.1);
					 // subtract the money left in this year
					 expr1.addTerm(moneyInBank[year], -1);
					 // set this equal to the negative of the budget in this year
					 cplex.addEq(expr1, -1 * budgetArray[year]);
				 }
			 }
			 // add upper bound for budget constraints
			 // for every year
			 for(int year = 0; year < numberYears; year++) {
				 // create a new expression
				 IloLinearNumExpr expr2 = cplex.linearNumExpr();
				 // for every project
				 for (int project = 0; project < numberProjects; project++) {
					 // add the percentageInvested multiplied by the payout of that project in that year
					 expr2.addTerm(payoutMatrix[project][year], percentageInvested[project]);
				 }
				 // this needs to be greater or equal to the negative of the budget array
				 cplex.addGe(expr2, -1 * budgetArray[year]);
			 }
			 // for every project
			 for (int project = 0; project < numberProjects; project++) {
				 // if a project is not to be invested in, invest 0%
				 cplex.addLe(percentageInvested[project], projectsChosen[project]);
			 }		 
			 // for every project
			 for (int project = 0; project < numberProjects; project++) {
				 // create a new expression
				 IloLinearNumExpr expr2 = cplex.linearNumExpr();
				 // if a project is chosen, at least 10% needs to be invested
				 expr2.addTerm(projectsChosen[project], 0.1);
				 cplex.addGe(percentageInvested[project], expr2);
			 }
			 // add lower & upper bound for projects chosen
			 IloLinearNumExpr expr2 = cplex.linearNumExpr();
			 for (int project = 0; project < numberProjects; project++) {
				 expr2.addTerm(projectsChosen[project], 1);
			 } 
			 // at most 3 projects 
			 cplex.addLe(expr2, 3);
			 // at least 2 projects
			 cplex.addGe(expr2, 2);
			 
			 // add mutual exclusivity of projects 1 & 2
			 expr2 = cplex.linearNumExpr();
			 expr2.addTerm(projectsChosen[0], 1);
			 expr2.addTerm(projectsChosen[1], 1);
			 cplex.addLe(expr2, 1);
			 
			 // add that either project 3 or 4 must be completed
			 expr2 = cplex.linearNumExpr();
			 expr2.addTerm(projectsChosen[2], 1);
			 expr2.addTerm(projectsChosen[3], 1);
			 cplex.addGe(expr2, 1);
			 
			 // add that the percentage invested in project 4 is the upper bound
			 // for percentage invested in project 5
			 cplex.addLe(percentageInvested[4], percentageInvested[3]);
			 
		      // solve ILP
		      cplex.solve();
		      // output the optimal solution value of the objective function
		      System.out.println("~~~~~~~Optimal Value~~~~~~~\n" + cplex.getObjValue() + "\n"); 
		      // print out the amount of money in the bank at the end of each year
		      for (int year = 0; year < numberYears + 1; year++) {
		    	  System.out.println("Capital at the end of year " + (year+1) + ": " + cplex.getValue(moneyInBank[year]));
		      }
		      System.out.println();
		      // print out how much was invested in each project
		      for (int i = 0; i < numberProjects; i++) {
		        System.out.println("Percentage invested in project " + (i+1) + ": " + cplex.getValue(percentageInvested[i]) * 100 + "%");
		      }
		      // close cplex object      
		      cplex.end(); 
		}
		// if an exception is thrown, catch it
		catch (IloException e) {
			// output the stack trace for debugging
			e.printStackTrace();
		}
	}
	/* This function reads information from the budget array, stores it in an integer array and
	 * returns it
	 * 
	 * @param filename - specifies the file in which the budget information can be found
	 * 
	 * @returns an integer array with all of the information about the budgets contained in the file
	 */
	public static int[] readBudgetArray(String filename) {
		try {
			// create a scanner object to read the specified file
			Scanner in = new Scanner(new File(filename));
			// the first number represents the number of years
			int years = in.nextInt();
			// create a new array with an element for each year
			int[] newArray = new int[years];
			// for every year
			for (int i = 0; i < years; i++) {
				// add the information in the text file
				newArray[i] = in.nextInt();
			}
			// close the scanner object
			in.close();
			// return the integer array
			return newArray;
		}
		// catch an IOException
		catch (IOException e) {
			// print out the stack trace for debugging
			e.printStackTrace();
			// exit the program
			System.exit(0);
			// will never be reached but needs to be here for compiling
			return null;
		}
	}
	/* This function reads information from the project payout matrix, stores it in an integer matrix and
	 * returns it
	 * 
	 * @param filename - specifies the file in which the project payout information can be found
	 * 
	 * @returns an integer matrix with all of the information about the project payouts contained in the file
	 */
	public static int[][] readPayoutMatrix(String filename) {
		try {
			// create a scanner object to read the specified file
			Scanner in = new Scanner(new File(filename));
			// the first number represents the number of rows in the payout matrix
			int rows = Integer.parseInt(in.nextLine());
			// the second the number of columns in the payout matrix
			int cols = Integer.parseInt(in.nextLine());
			// create a payout matrix according to the specified dimensions
			int[][] payoutMatrix = new int[rows][cols];
			// for every row in the matrix
			for (int i = 0; i < rows; i++) {
				// split the line into parts
				String parts[] = in.nextLine().split(" ");
				// for every column in the row
				for (int j = 0; j < cols; j++) {
					// read the integer and enter it into the payout matrix
					payoutMatrix[i][j] = Integer.parseInt(parts[j]);
				}
			}
			// close the scanner object
			in.close();
			// return the matrix
			return payoutMatrix;
		}
		// catch an IOException
		catch (IOException e) {
			// print the stack trace
			e.printStackTrace();
			// exit the program
			System.exit(0);
			// will never happen but needs to be here for compiling
			return null;
		}
	}
	/* This is the main function of the .java file which simply calls InvestmentSolving and lets
	 * that method handle everything
	 */
	public static void main(String[] args) {
		InvestmentSolving();
	}
}
