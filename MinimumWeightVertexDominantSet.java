
/*******************************************************************************
  * File: MinimumWeightVertexDominantSet.java
  * Author: Emiel Verkade
  * Date: 31.1.2018
  * Class to solve the Minimum Weight Vertex Dominant Set problem for a given 
  * graph, with the graph information given in the format specified on Student Portal
  * If no arguments are given, B1.txt through B5.txt are read and computed. Else,
  * the program will take all arguments as filenames and attempt to solve the 
  * ILPs and LPs of the Minimum Weight Vertex Dominant Set problem associated
  * with the graphs in those files. It will then output the results in a file named
  * Output.csv, with the Dutch delimiter and the Dutch number format.
  ******************************************************************************/

import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;

public class MinimumWeightVertexDominantSet {
	private Graph graph;

	// constructor
	public MinimumWeightVertexDominantSet(String filename) {
		try {
			this.graph = new Graph(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double solveLP(int type) {
		try {
			// create the cplex object
			IloCplex cplex = new IloCplex();
			// number of nodes
			int n = graph.getNumberOfNodes();
			// binary matrix of nodes
			IloNumVar[] nodeChosen;
			// if type = 1, ILP
			if (type == 1) {
				nodeChosen = cplex.numVarArray(n, 0, 1, IloNumVarType.Int);
			} // otherwise LP
			else {
				nodeChosen = cplex.numVarArray(n, 0, 1, IloNumVarType.Float);
			}
			// add objective function
			IloLinearNumExpr expr1 = cplex.linearNumExpr();
			// for every node
			for (int i = 0; i < n; i++) {
				// multiply nodeChosen by node weight
				expr1.addTerm(graph.getNodeWeight(i), nodeChosen[i]);
			}
			// minimize this sum
			cplex.addMinimize(expr1);
			// add constraints
			// for every node
			for (int i = 0; i < n; i++) {
				// create a new constraint expression
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				// for every node
				for (int j = 0; j < n; j++) {
					// multiply if the edge exists by if the node was chosen or not
					expr2.addTerm(graph.edgeExists(i, j), nodeChosen[j]);
				}
				// make sure this sum is greater than or equal to 1
				cplex.addGe(expr2, 1);
			}
			// solve this ILP/LP
			cplex.solve();
			// return the objective value
			return cplex.getObjValue();

		} catch (IloException e) {
			// catch any IloException thrown and print the stack trace
			e.printStackTrace();
			return 0;
		}
	}

	public static void main(String[] args) {
		// if no arguments are given, read the prespecified files
		if (args.length == 0) {
			// matrix to record all data
			double[][] infoMatrix = new double[5][5];
			String name;
			String extension = ".txt";
			// for every instance
			for (int i = 1; i < 6; i++) {
				// start with B
				name = "B";
				// add the numbers 1 to 5 when needed
				name += (String.valueOf(i));
				// add the extension
				name += (extension);
				// create a new MinimumWeightVertexDominantSet object with the file name
				// associated to it
				MinimumWeightVertexDominantSet set = new MinimumWeightVertexDominantSet(name);
				// record the current starting time for the ILP solver
				double startTime = System.currentTimeMillis();
				// solve the ILP and save the results in the information matrix
				infoMatrix[i - 1][0] = set.solveLP(1);
				// record the running time of the ILP solver
				double endTime = System.currentTimeMillis();
				infoMatrix[i - 1][1] = (endTime - startTime);
				// record the current starting time for the LP solver
				startTime = System.currentTimeMillis();
				// solve the LP and save the results in the information matrix
				infoMatrix[i - 1][2] = set.solveLP(0);
				// record the running time of the LP solver
				endTime = System.currentTimeMillis();
				infoMatrix[i - 1][3] = (endTime - startTime);
				// compute the ratio of ILP value to LP value and save the value
				infoMatrix[i - 1][4] = (infoMatrix[i - 1][0] / infoMatrix[i - 1][2]);
			}
			// print the information matrix to a .csv file for easy data processing
			printToFile(infoMatrix);
		// otherwise, read the files as they are given in the command line
		} else {
			double[][] infoMatrix = new double[args.length][5];
			for (int i = 0; i < args.length; i++) {
				
				String name = args[i];
				MinimumWeightVertexDominantSet set = new MinimumWeightVertexDominantSet(name);
				// record the current starting time for the ILP solver
				double startTime = System.currentTimeMillis();
				// solve the ILP and save the results in the information matrix
				infoMatrix[i - 1][0] = set.solveLP(1);
				// record the running time of the ILP solver
				double endTime = System.currentTimeMillis();
				infoMatrix[i - 1][1] = (endTime - startTime);
				// record the current starting time for the LP solver
				startTime = System.currentTimeMillis();
				// solve the LP and save the results in the information matrix
				infoMatrix[i - 1][2] = set.solveLP(0);
				// record the running time of the LP solver
				endTime = System.currentTimeMillis();
				infoMatrix[i - 1][3] = (endTime - startTime);
				// compute the ratio of ILP value to LP value and save the value
				infoMatrix[i - 1][4] = (infoMatrix[i - 1][0] / infoMatrix[i - 1][2]);
			}
			// print the information matrix to a .csv file for easy data processing
			printToFile(infoMatrix);
		}
	}

	public static void printToFile(double[][] infoMatrix) {
		try {
			// define the delimiter (works for dutch computers)
			String delimiter = ";";
			// define the newline symbol
			String newLine = "\n";
			// create a new PrintWriter object to print to a file
			PrintWriter pw = new PrintWriter(new File("Output.csv"));
			// write the header of the document with all the appropriate headers
			pw.append("Instance;Optimal Value (ILP);Run time ILP (ms);Optimal value (LP);"
					+ "Run time LP (ms); ratio ILP/LP\n");
			// for as many times as there are rows in the information matrix
			for (int i = 0; i < infoMatrix.length; i++) {
				// write the instance number
				pw.append(String.valueOf(i + 1));
				// move on to next column
				pw.append(delimiter);
				// for the number of columns in the current row
				for (int j = 0; j < infoMatrix[i].length; j++) {
					// append the information in the information matrix, replacing decimals with
					// commas
					pw.append(String.valueOf(infoMatrix[i][j]).replace(".", ","));
					// move on to next column
					pw.append(delimiter);
				}
				// finally add a next line
				pw.append(newLine);
			}
			// flush the PrintWriter object
			pw.flush();
			// close it
			pw.close();
			// notify the console that the task has been completed
			System.out.println("Done");
		} catch (IOException e) {
			// print a stack trace in case an exception is thrown
			e.printStackTrace();
		}
	}
}
