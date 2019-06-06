
import java.io.*;
import java.util.*;
import ilog.concert.*;
import ilog.cplex.*;
import java.lang.System;

public class D0 {
	public static void main(String[] args) throws FileNotFoundException {
		try {
			double start = System.currentTimeMillis();
			// initializes a new cplex object
			IloCplex cplex = new IloCplex();
			// Create the graph and get all properties
			Graph2 graph = new Graph2("graph1.txt");
			int numberNodes = graph.getNumberOfNodes();
			// get the weights of all nodes
			double[] nodeWeights = new double[numberNodes];
			ArrayList<Graph2.Node> nodeList = graph.getNodeList();
			for (Graph2.Node node: nodeList) {
				nodeWeights[node.getID()] = node.getWeight();
			}
			// get the weights of all edges
			double[][] weightMatrix = new double[numberNodes][numberNodes];
			ArrayList<Graph2.Edge> edgeList = graph.getEdgelist();
			// Start as unreachable
			for (int i = 0; i < numberNodes; i++) {
				for (int j = 0; j < numberNodes; j++) {
					weightMatrix[i][j] = 0;
				}
			}
			// populate weights where possible
			// we do not actually need this
			// we just need which nodes are connected to which other nodes
			for (Graph2.Edge edge : edgeList) {
				weightMatrix[edge.getFirstNodeOfEdge()][edge.getSecondNodeOfEdge()] = edge.getWeight();
			}

			IloNumVar[] nodes = cplex.numVarArray(numberNodes, 0, 1, IloNumVarType.Int);
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < numberNodes; i++) {
				obj.addTerm(nodes[i], nodeWeights[i]);
			}
			cplex.addMinimize(obj);
			// add constraints
			for (int i = 0; i < numberNodes; i++) {
				for (int j = 0; j < numberNodes; j++) {
					if (i != j) {
						if (weightMatrix[i][j] != 0) {
							IloLinearNumExpr constraint = cplex.linearNumExpr();
							constraint.addTerm(nodes[j], 1);
							constraint.addTerm(nodes[i], 1);
							cplex.addGe(constraint, 1);
						}
					}
				}

			}
			// solve ILP
			cplex.solve();
			// output the optimal solution value of the objective function
			System.out.println();
			System.out.println("Value " + cplex.getObjValue());
			System.out.println();
			// print values
			for (int i = 0; i < numberNodes; i++) {
				System.out.println("Node " + i + ": " + cplex.getValue(nodes[i]));
			}
			System.out.println();
			double end = System.currentTimeMillis();
			System.out.println("Running time = " + (end-start) + "ms");
			// close cplex object
			cplex.end();
		}
		catch (IloException e) {}
	}
}
