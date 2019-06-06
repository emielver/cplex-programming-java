
import ilog.concert.*;
import ilog.cplex.*;

public class A0 {
	public static void main(String[] args) {
		try {
			// initializes a new cplex object
			IloCplex cplex = new IloCplex();

			int[] buildingCostArray = { 1325, 1100, 1500, 1200, 1400 };
			int[] capacityArray = { 40000, 30000, 50000, 20000, 40000 };
			int[] demand = { 40000, 25000, 35000 };
			int[][] manufacturingCosts = { { 35, 30, 45 }, { 45, 40, 50 }, { 70, 65, 60 }, { 20, 45, 25 },
					{ 65, 45, 45 } };

			int numberBuildings = buildingCostArray.length;
			// creates an array of binary variables of size numberBuildings
			IloNumVar[] plantsChosen = cplex.numVarArray(numberBuildings, 0, 1, IloNumVarType.Int);
			// keeps track of how much of each product is produced
			IloNumVar[] xProduced = cplex.numVarArray(numberBuildings, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			IloNumVar[] yProduced = cplex.numVarArray(numberBuildings, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			IloNumVar[] zProduced = cplex.numVarArray(numberBuildings, 0, Integer.MAX_VALUE, IloNumVarType.Int);
			
			// need to minimize costs
			IloNumVar costs = cplex.numVar(0, Integer.MAX_VALUE, IloNumVarType.Int);
			cplex.addMinimize(costs);
			// adding costs of constructing plants
			IloLinearNumExpr expression = cplex.linearNumExpr();
			// add costs of constructing the plants
			for (int i = 0; i < numberBuildings; i++) {
				expression.addTerm(plantsChosen[i], buildingCostArray[i] * 1000);
			}
			// add costs of producing the goods
			for (int i = 0; i < numberBuildings; i++) {
				expression.addTerm(xProduced[i], manufacturingCosts[i][0]);
				expression.addTerm(yProduced[i], manufacturingCosts[i][1]);
				expression.addTerm(zProduced[i], manufacturingCosts[i][2]);
			}
			cplex.addEq(expression, costs);
			// adding production constraints
			IloLinearNumExpr const1 = cplex.linearNumExpr();
			IloLinearNumExpr const2 = cplex.linearNumExpr();
			IloLinearNumExpr const3 = cplex.linearNumExpr();
			for (int i = 0; i < numberBuildings; i++) {
				const1.addTerm(xProduced[i], 1);
				const2.addTerm(yProduced[i], 1);
				const3.addTerm(zProduced[i], 1);
				IloLinearNumExpr const4 = cplex.linearNumExpr();
				const4.addTerm(xProduced[i], 1);
				const4.addTerm(yProduced[i], 1);
				const4.addTerm(zProduced[i], 1);
				const4.addTerm(plantsChosen[i],-1 * capacityArray[i]);
				cplex.addLe(const4, 0);
			}
			cplex.addLe(const1, demand[0]);
			cplex.addLe(const2, demand[1]);
			cplex.addLe(const3, demand[2]);
			cplex.addGe(const1, demand[0]);
			cplex.addGe(const2, demand[1]);
			cplex.addGe(const3, demand[2]);
			// solve ILP
			cplex.solve();
			// output the optimal solution value of the objective function
			System.out.println();
			System.out.println("Value" + cplex.getObjValue());
			System.out.println();
			// Print the production of each good per building and if it was used or not
			for (int i = 0; i < numberBuildings; i++) {
				System.out.println("Used building " + i  + " = " + cplex.getValue(plantsChosen[i]));
				System.out.print("Produced X in building" + (i + 1) + ": " + cplex.getValue(xProduced[i]));
				System.out.println(", Y: " + cplex.getValue(yProduced[i]) + ", Z: " + cplex.getValue(zProduced[i]));
			}
			// close cplex object
			cplex.end();
		}
		catch (IloException e) {}
	}
}
