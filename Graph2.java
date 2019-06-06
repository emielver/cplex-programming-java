
/*******************************************************************************
  * File: Graph.java
  * Author: Andre Berger  
  *
  * Class to represent graphs using adjacency matrix and adjacency lists
  ******************************************************************************/

import java.util.*;
import java.io.*;

public class Graph {

	class Node {
		private int id;
		private double weight;

		public Node(int u, double w) {
			this.id = u;
			this.weight = w;
		}

		public int getID() {
			return this.id;
		}

		public double getWeight() {
			return this.weight;
		}
	}

	class Edge {
		private int firstNode;
		private int secondNode;
		private double weight;

		public Edge(int u, int v, double w) {
			this.firstNode = u;
			this.secondNode = v;
			this.weight = w;
		}

		public int getFirstNodeOfEdge() {
			return this.firstNode;
		}

		public int getSecondNodeOfEdge() {
			return this.secondNode;
		}

		public double getWeight() {
			return this.weight;
		}
	}

	private int numberOfNodes;
	private int numberOfEdges;

	private int sourceIndex;
	private int destIndex;

	private ArrayList<Node> nodeList;
	private ArrayList<Edge> edgeList;

	private int[][] adjacent;
	private ArrayList<ArrayList<Edge>> adjacencyLists;

	// Constructor for a graph, information is read from file
	// adjacency matrix as well as adjacency list is filled

	public Graph(String filename) throws java.io.FileNotFoundException {
		File file = new File(filename);
		Scanner input = new Scanner(file);
		this.numberOfNodes = Integer.parseInt(input.nextLine().trim());
		this.numberOfEdges = Integer.parseInt(input.nextLine().trim());
		this.adjacent = new int[this.numberOfNodes][this.numberOfNodes];

		for (int i = 0; i < this.numberOfNodes; i++) {
			for (int j = 0; j < this.numberOfNodes; j++) {
				adjacent[i][j] = 0;
			}
		}
		this.nodeList = new ArrayList<Node>();
		this.edgeList = new ArrayList<Edge>();

		this.adjacencyLists = new ArrayList<ArrayList<Edge>>();
		for (int i = 0; i < this.numberOfNodes; i++) {
			this.adjacencyLists.add(new ArrayList<Edge>());
		}

		for (int i = 0; i < this.numberOfNodes; i++) {
			String line = input.nextLine().trim();
			String[] parts = line.split("	");
			int nodeIndex = Integer.parseInt(parts[0].trim());
			double w = Double.parseDouble(parts[1].trim());
			Node node = new Node(nodeIndex, w);
			this.nodeList.add(node);
		}
		for (int i = 0; i < this.numberOfEdges; i++) {
			String line = input.nextLine().trim();
			String[] parts = line.split("	");
			int firstNode = Integer.parseInt(parts[0].trim());
			int secondNode = Integer.parseInt(parts[1].trim());
			double weight = Double.parseDouble(parts[2].trim());
			
			this.adjacent[firstNode][secondNode] = 1;
			this.adjacent[secondNode][firstNode] = 1;

			Edge edge = new Edge(firstNode, secondNode, weight);

			this.edgeList.add(edge);

			this.adjacencyLists.get(firstNode).add(edge);
			this.adjacencyLists.get(secondNode).add(edge);
		}
		input.close();
	}

	public int getSource() {
		return this.sourceIndex;
	}

	public int getDest() {
		return this.destIndex;
	}

	// returns the number of nodes of this graph
	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	// returns the number of edges of this graph
	public int getNumberOfEdges() {
		return this.numberOfEdges;
	}

	// returns whether node i and node j are adjacent in this graph
	public boolean isAdjacent(int i, int j) {
		return (this.adjacent[i][j] == 1);
	}
	
	public ArrayList<Edge> getEdgelist() {
		return this.edgeList;
	}

	public ArrayList<Node> getNodeList() {
		return this.nodeList;
	}
	// returns weight of vertex which is ith in the list
	public double getNodeWeight(int i) {
		return this.nodeList.get(i).weight;
	}

	public Edge getEdge(int i) {
		return this.edgeList.get(i);
	}

	public int degree(int i) {
		return this.adjacencyLists.get(i).size();
	}

}