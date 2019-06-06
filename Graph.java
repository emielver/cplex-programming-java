/*******************************************************************************
  * File: Graph.java
  * Author: Andre Berger 
  * Modified by Emiel Verkade 
  * Date: 31.1.2018
  * Class to represent graphs of nodes with edges
  ******************************************************************************/

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Graph {

	private int numberOfNodes;
	private int numberOfEdges;

	private int[] nodeWeights;
	private int[][] adjacentNodes;

	// Constructor for a graph, information is read from file
	public Graph(String filename) throws FileNotFoundException {
		// create a scanner object to read a new file with the matching filename
		Scanner input = new Scanner(new File(filename));
		// identify the number of nodes and number of edges as the first two integers
		// contained in the first two lines
		this.numberOfNodes = Integer.parseInt(input.nextLine().trim());
		this.numberOfEdges = Integer.parseInt(input.nextLine().trim());
		// initialize vector of node weights
		this.nodeWeights = new int[numberOfNodes];
		// for all nodes in the graph
		for (int i = 0; i < this.numberOfNodes; i++) {
			// read the next line of data
			String line = input.nextLine();
			// split the line into parts, delimited by a tab character
			String[] parts = line.split("\t");
			// the second part of the line is the weight of the current node
			nodeWeights[i] = Integer.parseInt(parts[1]);
		}
		// initialize a matrix of edgeExistences
		this.adjacentNodes = new int[numberOfNodes][numberOfNodes];
		// for all nodes in the graph
		for (int i = 0; i < numberOfNodes; i++) {
			// for all nodes in the graph
			for (int j = 0; j < numberOfNodes; j++) {
				// the edge does not exist (the node i is not adjacent to the node j)
				this.adjacentNodes[i][j] = 0;
				// if this edge is directed to itself (i.e. is a node)
				if (i == j) {
					// the edge exists (the node is adjacent to itself)
					this.adjacentNodes[i][j] = 1;
				}
			}
		}
		// for all edges in the file
		for (int i = 0; i < numberOfEdges; i++) {
			// interpret the data in the line
			String line = input.nextLine();
			// the data is split up by tabs
			String[] parts = line.split("\t");
			// the first part indicates a node
			int row = Integer.parseInt(parts[0]);
			// as does the second part
			int col = Integer.parseInt(parts[1]);
			// the first node connects to the second via this edge
			adjacentNodes[row][col] = 1;
			// the second also connects to the first via this edge
			adjacentNodes[col][row] = 1;
		}
		// close the input when it reading is finished
		input.close();
	}

	// returns the number of nodes of this graph
	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	// returns 1 if edge exists connecting node i and j, 0 otherwise
	public int edgeExists(int i, int j) {
		return this.adjacentNodes[i][j];
	}

	// returns the weight of the node i
	public int getNodeWeight(int i) {
		return this.nodeWeights[i];
	}
}