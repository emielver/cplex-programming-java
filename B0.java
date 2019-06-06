
import ilog.concert.*;
import ilog.cplex.*;

public class B0 {
	public static void main(String[] args) {
		try {
			// initializes a new cplex object
			IloCplex cplex = new IloCplex();

			int[] normalDuration = {3, 10, 6, 3, 15, 7, 3};
			int[] fastDuration = { 2, 5, 4, 2, 10, 4, 1};
			int[] costNormal = { 2, 2, 10, 2, 12, 3, 2};
			int[] costFast = {3, 4, 14, 3, 16, 5, 5};

			int numberTasks = normalDuration.length;
			// creates an array of binary variables of size numberBuildings
			IloNumVar[] chosenNormal = cplex.numVarArray(numberTasks, 0, 1, IloNumVarType.Int);
			IloNumVar[] chosenFast = cplex.numVarArray(numberTasks, 0, 1, IloNumVarType.Int);
			// need to minimize costs
			IloNumVar costs = cplex.numVar(0, Integer.MAX_VALUE, IloNumVarType.Int);
			cplex.addMinimize(costs);
			IloLinearNumExpr expression = cplex.linearNumExpr();
			// add costs of doing tasks
			for (int i = 0; i < numberTasks; i++) {
				expression.addTerm(chosenNormal[i], costNormal[i]);
				expression.addTerm(chosenFast[i], costFast[i]);
			}
			cplex.addEq(expression, costs);
			
			// finish within 23 days
			IloLinearNumExpr const1 = cplex.linearNumExpr();
			IloLinearNumExpr const2 = cplex.linearNumExpr();
			IloLinearNumExpr const3 = cplex.linearNumExpr();
			// path 1
			const1.addTerm(chosenNormal[0], normalDuration[0]);
			const1.addTerm(chosenNormal[2], normalDuration[2]);
			const1.addTerm(chosenNormal[3], normalDuration[3]);
			const1.addTerm(chosenNormal[4], normalDuration[4]);
			const1.addTerm(chosenNormal[6], normalDuration[6]);
			const1.addTerm(chosenFast[0], fastDuration[0]);
			const1.addTerm(chosenFast[2], fastDuration[2]);
			const1.addTerm(chosenFast[3], fastDuration[3]);
			const1.addTerm(chosenFast[4], fastDuration[4]);
			const1.addTerm(chosenFast[6], fastDuration[6]);
			// path 2
			const2.addTerm(chosenNormal[0], normalDuration[0]);
			const2.addTerm(chosenNormal[1], normalDuration[1]);
			const2.addTerm(chosenNormal[4], normalDuration[4]);
			const2.addTerm(chosenNormal[6], normalDuration[6]);
			const2.addTerm(chosenFast[0], fastDuration[0]);
			const2.addTerm(chosenFast[1], fastDuration[1]);
			const2.addTerm(chosenFast[4], fastDuration[4]);
			const2.addTerm(chosenFast[6], fastDuration[6]);
			// path 3
			const3.addTerm(chosenNormal[0], normalDuration[0]);
			const3.addTerm(chosenNormal[5], normalDuration[5]);
			const3.addTerm(chosenNormal[6], normalDuration[6]);
			const3.addTerm(chosenFast[0], fastDuration[0]);
			const3.addTerm(chosenFast[5], fastDuration[5]);
			const3.addTerm(chosenFast[6], fastDuration[6]);
			
			cplex.addLe(const1, 23);
			cplex.addLe(const2, 23);
			cplex.addLe(const3, 23);
			
			// choose each task once
			for (int i = 0; i < numberTasks; i++) {
				IloLinearNumExpr constraint = cplex.linearNumExpr();
				constraint.addTerm(chosenNormal[i], 1);
				constraint.addTerm(chosenFast[i], 1);
				cplex.addEq(constraint, 1);
			}
			// solve ILP
			cplex.solve();
			// output the optimal solution value of the objective function
			System.out.println("Value" + cplex.getObjValue() + "\n");
			// print which tasks were completed quickly and which were completed normally
			for (int i = 0; i < numberTasks; i++) {
				System.out.println("Task " + i  + " completed normally: " + cplex.getValue(chosenNormal[i]) 
				+ " or quickly " + cplex.getValue(chosenFast[i]));
			}
			// close cplex object
			cplex.end();
		}
		catch (IloException e) {}
	}
}
