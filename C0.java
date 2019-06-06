
import java.io.*;
import java.util.*;
import ilog.concert.*;
import ilog.cplex.*;
import java.lang.System;

public class C0 {
	public static void main(String[] args) throws FileNotFoundException {
		try {
			double start = System.currentTimeMillis();
			// initializes a new cplex object
			IloCplex cplex = new IloCplex();
			// Create the graph and get all properties
			Graph2 graph = new Graph2("instance1.txt");
			int numberNodes = graph.getNumberOfNodes();
			int source = graph.getSource();
			int dest = graph.getDest();
			double[][] weightMatrix = new double[numberNodes][numberNodes];
			ArrayList<Graph2.Edge> edgeList = graph.getEdgelist();
			// Start as unreachable
			for (int i = 0; i < numberNodes; i++) {
				for (int j = 0; j < numberNodes; j++) {
					weightMatrix[i][j] = 0;
				}
			}
			// populate weights where possible
			for (Graph2.Edge edge : edgeList) {
				weightMatrix[edge.getFirstNodeOfEdge()][edge.getSecondNodeOfEdge()] = edge.getWeight();
			}

			IloNumVar[] nodes = cplex.numVarArray(numberNodes, 0, Float.MAX_VALUE, IloNumVarType.Float);
			cplex.addMaximize(nodes[dest]);
			cplex.addEq(nodes[source], 0);
			for (int i = 0; i < numberNodes; i++) {
				for (int j = 0; j < numberNodes; j++) {
					if (i != j) {
						if (weightMatrix[i][j] != 0) {
							IloLinearNumExpr constraint = cplex.linearNumExpr();
							constraint.addTerm(nodes[j], 1);
							constraint.addTerm(nodes[i], -1);
							cplex.addLe(constraint, weightMatrix[i][j]);
						}
					}
				}

			}
			// solve ILP
			cplex.solve();
			// output the optimal solution value of the objective function
			System.out.println("~~~~~~~Optimal Value~~~~~~~\n" + cplex.getObjValue() + "\n");
			// print values
			for (int i = 0; i < numberNodes; i++) {
				System.out.println("Node " + i + ": " + cplex.getValue(nodes[i]));
			}
//			System.out.println();
			double end = System.currentTimeMillis();
			System.out.println("Running time = " + (end-start) + "ms");
			// close cplex object
			cplex.end();
		}
		// if an exception is thrown, catch it
		catch (IloException e) {
			// output the stack trace for debugging
			e.printStackTrace();
		}
	}
}
