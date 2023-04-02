package ubc.cosc322;
import ubc.cosc322.COSC322Test;

import java.util.*;
public class Heuristics {

    public record DistanceNode(Graph.Node node, COSC322Test.Tile relatedPlayer){

        @Override
        public boolean equals(Object o) { 
        	return o instanceof DistanceNode d && d.node.equals(node); 
        	
        }

    }

    /**
     * Sets the kDist and qDist values on each empty tile
     * on the board for a given player
     * @param g
     * @param player
     */
    public static void allDistances(Graph g, COSC322Test.Tile player){

        List<DistanceNode> searchList = new LinkedList<>();
        Set<Graph.Node> unvisited = new HashSet<>();

        //Get all nodes with a player on them
        for(Graph.Node n : g.getNodes()){
            //Skip fire nodes
            COSC322Test.Tile v = n.getValue();
            if(v.isFire()) continue;

            //Add all non-player nodes to unvisited set
            if(!v.isPlayer())
                unvisited.add(n);

            //Add player nodes to search list
            if(n.getValue() == player) {
                n.playerZeroDistances(n.getValue());
                searchList.add(new DistanceNode(n, n.getValue()));
            }
        }

        int qDist = 1;

        //While there are still unvisited notes search the board iteratively
        while(!unvisited.isEmpty()){
            searchList = allDistancesHelper(searchList, qDist++, unvisited);

            //If the returned search list is empty the remaining unvisited nodes must be unreachable
            if (searchList.isEmpty()) break;
        }

    }

    /**
     * Sets the kDist and qDist values for each node exactly
     * one Chess Queen move away from the list of starting nodes.
     * Returns the list of nodes that were visited and had not been
     * visited before.
     * @param startingNodes List of starting nodes
     * @param qDist the Queens Distance value away from the player
     * @param unvisited a set of unvisited nodes
     * @return a List of newly visited nodes
     */
    private static List<DistanceNode> allDistancesHelper(List<DistanceNode> startingNodes, int qDist, Set<Graph.Node> unvisited){

        //A list of all newly visited startingNodes this iteration
        List<DistanceNode> returnList = new LinkedList<>();

        for(DistanceNode start : startingNodes) {

            Graph.Node startNode = start.node;
            unvisited.remove(startNode);

            //Move in each direction 1 tile at a time
            for (Graph.Edge.Direction direction : Graph.Edge.Direction.values()) {

                Graph.Edge current = startNode.getEdgeInDirection(direction);

                //kDist is set to the starting startingNodes k distance from the player
                int kDist = 1;
                if(start.relatedPlayer.isWhite())
                    kDist = startNode.getKdist1();
                else if(start.relatedPlayer.isBlack())
                    kDist = startNode.getKdist2();

                //Keep moving until we hit a wall, fire tile or another player
                while(current!=null){
                    Graph.Node currentNode = current.getNode();

                    //Update distances
                    if(start.relatedPlayer().isWhite()){
                        if(currentNode.getQdist1() > qDist) currentNode.setQdist1(qDist);
                        if(currentNode.getKdist1() > kDist) currentNode.setKdist1(++kDist);
                    }else if(start.relatedPlayer().isBlack()){
                        if(currentNode.getQdist2() > qDist) currentNode.setQdist2(qDist);
                        if(currentNode.getKdist2() > kDist) currentNode.setKdist2(++kDist);
                    }

                    //Add the node to the return list if it was visited for the first time this iteration
                    if(unvisited.remove(currentNode))
                        returnList.add(new DistanceNode(currentNode, start.relatedPlayer));

                    //Move to the next tile
                    current = currentNode.getEdgeInDirection(direction);
                }


            }
        }

        return returnList;
    }

//    private Heuristic(){
//
//    }

    public static float calculateT(Graph board, COSC322Test.Tile turn){
       
    	float w = calculateW(board);

        float f1 = function1(w,board);
        float f2 = function2(w,board);
        float f3 = function3(w,board);
        float f4 = function4(w,board);

        double magnitude = Math.sqrt(f1*f1 + f2*f2 + f3*f3 + f4*f4);

        float t1 = 0;
        float t2 = 0;

        for(Graph.Node n : board.getNodes()){
        	
                if(!n.getValue().isEmpty()) continue;
                t1 += calculateTi(turn, n.getQdist1(), n.getQdist2());
                t2 += calculateTi(turn, n.getKdist1(), n.getKdist2());
                
        }

        float c1 = calculateC1(board);
        float c2 = calculateC2(board);

        double p1 = (f1/magnitude) * t1;
        double p2 = (f2/magnitude) * c1;
        double p3 = (f3/magnitude) * c2;
        double p4 = (f4/magnitude) * t2;

        return (float) (p1 + p2 + p3 + p4);
    }

    private static float calculateTi(COSC322Test.Tile player, int dist1, int dist2){
        float k = 1/5f;

        //n = m = infinity
        if(dist1 == Integer.MAX_VALUE && dist2 == Integer.MAX_VALUE) return 0;
        //n = m < infinity
        else if(dist1 == dist2) return player == COSC322Test.Tile.WHITE ? k : -k;
        //n < m (player 1 is closer)
        else if(dist1 < dist2) return 1;
        //n > m (player 2 is closer)
        else return -1;
    }

    private static float calculateC1(Graph board){
        float sum = 0;
        for (Graph.Node n : board.getNodes()) {
            sum += Math.pow(2, -n.getQdist1()) - Math.pow(2, -n.getQdist2());
        }

        return 2 * sum;
    }

    private static float calculateC2(Graph board){
        float sum = 0;
        for (Graph.Node n : board.getNodes()) {
            float difference = (n.getKdist2() - n.getKdist1()) / 6f;
            sum += Math.min(1, Math.max(-1, difference));
        }

        return sum;
    }

    private static float calculateW(Graph board){
        float sum = 0;
        for (Graph.Node n: board.getNodes()) {
            sum += Math.pow(2, -Math.abs(n.getQdist1()-n.getQdist2()));
        }
        return sum;
    }

    /*TODO Functions need implementation
        Should fluctuate in importance over the course of the game
    */

    //Increasingly important during the game
    //Gives good estimates of expected territory shortly before filling phase
    private static float function1(float w, Graph board){
 
        w = 100; 
	    for(Graph.Node n : board.getNodes()){
	       if(!n.getValue().isEmpty()) w--;
	    }
	        return w;
    }

    //Supports positional play in the opening
    //Smooths transition between the beginning and later phases of the game
    private static float function2(float w, Graph board){
    	
    	w = 0;
 	   
   	    for(Graph.Node n : board.getNodes()){
   	       if(!n.getValue().isEmpty()) w++;
   	    }
   	    
   	    w = ((w-30)/10);
   	    w *= w; 
   	    w *= (-1);
   	    w += 40; 
   	    return w;
	        
    	}
    	

        
  

    //Supports positional play in the opening
    //Smooths transition between the beginning and later phases of the game
    private static float function3(float w, Graph board){

    	w = 0;
    	   
   	    for(Graph.Node n : board.getNodes()){
   	       if(!n.getValue().isEmpty()) w++;
   	    }
   	    
   	    w = ((w-60)/10);
   	    w *= w; 
   	    w *= (-1);
   	    w += 40; 
   	    return w;
  
        
    }

    //Most important at the beginning of the game
    private static float function4 (float w, Graph board){
      	
        w = 0; 
        for(Graph.Node n : board.getNodes()){
           if(!n.getValue().isEmpty()) w++;
        }
            return w;

    }
    /**
     * Calculates the distance between the center of the board and the closest empty tile for a given player.
     * The closer the empty tile is to the center, the higher the score.
     * @param board the current game board
     * @return the score for this heuristic
     */
    public static int emptyTilesNearPlayer(Graph g) {
        int count = 0;
        for (Graph.Node node : g.getNodes()) {
            if (node.getValue().isPlayer()) {
                for (Graph.Node neighbor : node.getNeighbors(node)) {
                    if (neighbor.getValue().isEmpty()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }


    /**
     * Calculates the number of moves available to a player's pieces divided by the total number of pieces on the board.
     * A higher ratio indicates a more advantageous position for the player.
     * @param board the current game board
     * @return the score for this heuristic
     */
 /*  private static float function6(Graph board) {
        int numMoves = 0;
        int numPieces = 0;

        // Count number of moves and pieces for the player
        for (Graph.Node node : board.getNodes()) {
            COSC322Test.Tile tile = node.getValue();
            if (tile.isFire() || tile == COSC322Test.Tile.EMPTY) continue;

            if (tile.isPlayer()) {
                numPieces++;
                numMoves += node.getValidMoves().size();
            }
        }

        // Calculate ratio of moves to pieces
        if (numPieces == 0) return 0f; // avoid division by zero
        float ratio = (float) numMoves / (float) numPieces;

        return ratio;
    }*/ // this function is meant to calculate the Manhattan's distance 

}

