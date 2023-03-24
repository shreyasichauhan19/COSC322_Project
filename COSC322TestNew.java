
package ubc.cosc322;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.Amazon.GameBoard;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */

public class COSC322Test extends GamePlayer{

    private GameClient gameClient = null; 
    private BaseGameGUI gamegui;
	
    private String userName = null;
    private String passwd = null;
    private ArrayList<ArrayList<Integer>> gameBoard = null;
    
    
 
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {				 
    	COSC322Test player = new COSC322Test(args[0], args[1]); 
    	
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	
    	this.passwd = passwd;
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	this.gamegui = new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
    	System.out.println("Congratualations!!! "
    			+ "I am called because the server indicated that the login is successfully");
    	System.out.println("The next step is to find a room and join it: "
    			+ "the gameClient instance created in my constructor knows how!"); 
    	userName = gameClient.getUserName(); //we pass this in another method param... tbd
    	if(gamegui != null) {
    		gamegui.setRoomInformation(gameClient.getRoomList());
    	}
    } 
    

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        // This method will be called by the GameClient when it receives a game-related message
        // from the server.
    	    // create a local game board. record the actions of the opponent.
    	  if (messageType.equals(GameMessage.GAME_STATE_BOARD)) {
    	        ArrayList<Integer> boardState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
    	        gameBoard = new ArrayList<>();

    	        // Convert the board state from a flat list to a 2D array list
    	        for (int i = 0; i < 10; i++) {
    	            ArrayList<Integer> row = new ArrayList<>();
    	            for (int j = 0; j < 10; j++) {
    	                row.add(boardState.get(i * 10 + j));
    	            }
    	            gameBoard.add(row);
    	        }

    	        gamegui.setGameState(boardState);
    	    } else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
    	    	  // Retrieve opponent's move details
    	        ArrayList<Integer> oppQueenPosCurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
    	        ArrayList<Integer> oppQueenPosNext = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
    	        ArrayList<Integer> oppArrowPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);

    	     // Update game board with the move

    	        gameBoard.get(oppQueenPosNext.get(0)).set(oppQueenPosNext.get(1), 1); // place moved queen
    	        gameBoard.get(oppArrowPos.get(0)).set(oppArrowPos.get(1), 2); // place arrow
    	        gameBoard.get(oppQueenPosCurr.get(0)).set(oppQueenPosCurr.get(1), 0); // remove old queen

    	        // Update the GUI with the new game state
    	        
    	        gamegui.updateGameState(oppQueenPosCurr, oppQueenPosNext, oppArrowPos);


    	    }
    	    return true;
    	
    	}
    private void handleGameMessage(String message) {
        // parse the message and update the game state
        gameState.parseGameStateMessage(message);

        // get the current player's queen and arrow positions
        ArrayList<Integer> myQueenPosCurr = gameState.getMyQueenPosition();
        ArrayList<Integer> myArrowPos = gameState.getMyArrowPosition();

        // get the available moves for the current player
        ArrayList<ArrayList<Integer>> myQueenMoves = getValidMovesForQueen(myQueenPosCurr);
        ArrayList<ArrayList<Integer>> myArrowMoves = getValidMovesForArrow(myArrowPos);

        // send the best move to the server
        calculateBestMove();
    }
    private void calculateBestMove() {
        // set the initial depth and alpha-beta values
    	   // set the initial depth and alpha-beta values
        int depth = 3;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

       
        // get the current player's queen and arrow positions
        ArrayList<Integer> myQueenPosCurr = gameState.getMyQueenPosition();
        ArrayList<Integer> myArrowPos = gameState.getMyArrowPosition();

        // initialize the best move with the current positions
        int qr = myQueenPosCurr.get(0);
        int qc = myQueenPosCurr.get(1);
        int ar = myArrowPos.get(0);
        int ac = myArrowPos.get(1);
        int qfr = qr;
        int qfc = qc;

        // get the available moves for the current player
        ArrayList<ArrayList<Integer>> myQueenMoves = gameState.getValidMovesForQueen(myQueenPosCurr);
        ArrayList<ArrayList<Integer>> myArrowMoves = gameState.getValidMovesForArrow(myArrowPos);

        // iterate through all possible moves and evaluate them using alpha-beta pruning
        for (ArrayList<Integer> queenMove : myQueenMoves) {
            int qtr = queenMove.get(0);
            int qtc = queenMove.get(1);

            for (ArrayList<Integer> arrowMove : myArrowMoves) {
                int atr = arrowMove.get(0);
                int atc = arrowMove.get(1);

                // make a copy of the current game board and update it with the current move
                Map<String, Object> gameStateUpdate = new HashMap<>();
                gameStateUpdate.put(AmazonsGameMessage.QUEEN_POS_CURR, myQueenPosCurr);
                gameStateUpdate.put(AmazonsGameMessage.QUEEN_POS_NEXT, queenMove);
                gameStateUpdate.put(AmazonsGameMessage.ARROW_POS, arrowMove);
                gamegui.updateGameState(gameStateUpdate);

                // evaluate the current move using alpha-beta pruning
                int eval = alphaBeta(gameState, depth, alpha, beta, false);

                // update the best move if the current move has a higher evaluation score
                if (eval > alpha) {
                    alpha = eval;
                    qr = qtr;
                    qc = qtc;
                    ar = atr;
                    ac = atc;
                    qfr = myQueenPosCurr.get(0);
                    qfc = myQueenPosCurr.get(1);
                }
            }
        }

        // update the game state with the best move and send it to the server
        Map<String, Object> gameStateUpdate = new HashMap<>();
        gameStateUpdate.put(AmazonsGameMessage.QUEEN_POS_CURR, myQueenPosCurr);
        gameStateUpdate.put(AmazonsGameMessage.QUEEN_POS_NEXT, Arrays.asList(qr, qc));
        gameStateUpdate.put(AmazonsGameMessage.ARROW_POS, Arrays.asList(ar, ac));
        gameState.updateGameState(gameStateUpdate);
        playerMove(qr, qc, ar, ac, qfr, qfc);
    }
    
    private int alphaBeta(ArrayList<ArrayList<Integer>> board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
            return evaluateBoard(board);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            ArrayList<ArrayList<Integer>> myQueenPosCurr = board.get(0);
            ArrayList<ArrayList<Integer>> myArrowPos = board.get(1);
            ArrayList<ArrayList<Integer>> myQueenMoves = getValidMovesForQueen(myQueenPosCurr);
            ArrayList<ArrayList<Integer>> myArrowMoves = getValidMovesForArrow(myArrowPos);

            for (ArrayList<Integer> queenMove : myQueenMoves) {
                for (ArrayList<Integer> arrowMove : myArrowMoves) {
                    ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
                    newBoard.add(new ArrayList<>(queenMove));
                    newBoard.add(new ArrayList<>(arrowMove));
                    updateGameState(newBoard, myQueenPosCurr, queenMove, myArrowPos, arrowMove);
                    int eval = alphaBeta(newBoard, depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            ArrayList<ArrayList<Integer>> oppQueenPosCurr = board.get(2);
            ArrayList<ArrayList<Integer>> oppArrowPos = board.get(3);
            ArrayList<ArrayList<Integer>> oppQueenMoves = getValidMovesForQueen(oppQueenPosCurr);
            ArrayList<ArrayList<Integer>> oppArrowMoves = getValidMovesForArrow(oppArrowPos);

            for (ArrayList<Integer> queenMove : oppQueenMoves) {
                for (ArrayList<Integer> arrowMove : oppArrowMoves) {
                    ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
                    newBoard.add(oppQueenPosCurr);
                    newBoard.add(oppArrowPos);
                    newBoard.add(new ArrayList<>(queenMove));
                    newBoard.add(new ArrayList<>(arrowMove));
                    int eval = alphaBeta(newBoard, depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }

            return minEval;
        }
    }

    private int evaluateBoard(ArrayList<ArrayList<Integer>> gameBoard) {
        int myQueenRow = gameBoard.get(0).get(0);
        int myQueenCol = gameBoard.get(0).get(1);
        int oppQueenRow = gameBoard.get(1).get(0);
        int oppQueenCol = gameBoard.get(1).get(1);
        int arrowRow = gameBoard.get(2).get(0);
        int arrowCol = gameBoard.get(2).get(1);

        // calculate the distance between my queen and the opponent queen
        int queenDistance = Math.max(Math.abs(myQueenRow - oppQueenRow), Math.abs(myQueenCol - oppQueenCol));

        // calculate the number of moves available to the opponent queen
        ArrayList<ArrayList<Integer>> oppQueenMoves = getValidQueenMoves(oppQueenRow, oppQueenCol);
        int oppQueenMovesCount = oppQueenMoves.size();

        // calculate the number of moves available to my queen
        ArrayList<ArrayList<Integer>> myQueenMoves = getValidQueenMoves(myQueenRow, myQueenCol);
        int myQueenMovesCount = myQueenMoves.size();

        // calculate the number of moves available to the arrow
        ArrayList<ArrayList<Integer>> arrowMoves = getValidArrowMoves(arrowRow, arrowCol);
        int arrowMovesCount = arrowMoves.size();

        // compute the heuristic value
        int heuristicValue = queenDistance + (5 * oppQueenMovesCount) - (3 * myQueenMovesCount) - (2 * arrowMovesCount);
        return heuristicValue;
    }

    
    public ArrayList<ArrayList<Integer>> getValidMovesForQueen(ArrayList<Integer> queenPos) {
        ArrayList<ArrayList<Integer>> validMoves = new ArrayList<ArrayList<Integer>>();

        int row = queenPos.get(0);
        int col = queenPos.get(1);

        // Check all the 8 directions from the current queen position
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];

            // Move the queen in this direction until we hit a wall, another piece, or run out of moves
            while (isInBounds(r, c) && isEmptyCell(r, c)) {
                ArrayList<Integer> move = new ArrayList<Integer>();
                move.add(r);
                move.add(c);
                validMoves.add(move);
                r += dir[0];
                c += dir[1];
            }

            // If the queen can attack a piece in this direction, add the attack move to the list
            if (isInBounds(r, c) && isOpponentPiece(r, c)) {
                ArrayList<Integer> move = new ArrayList<Integer>();
                move.add(r);
                move.add(c);
                validMoves.add(move);
            }
        }

        return validMoves;
    }
    public ArrayList<ArrayList<Integer>> getValidArrowMoves(int[] queenPos, ArrayList<Integer> gameState) {
        ArrayList<ArrayList<Integer>> validArrowMoves = new ArrayList<>();

        int queenRow = queenPos[0];
        int queenCol = queenPos[1];

        // Check for valid arrow moves in all 8 directions
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                // Don't check the same position as the queen
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }

                int currRow = queenRow + rowOffset;
                int currCol = queenCol + colOffset;

                // Check if the current position is within the board
                while (currRow >= 0 && currRow < BOARD_SIZE && currCol >= 0 && currCol < BOARD_SIZE) {
                    int position = currRow * BOARD_SIZE + currCol;

                    // If the current position is not occupied, add it as a valid arrow move
                    if (gameState.get(position) == EMPTY) {
                        ArrayList<Integer> arrowMove = new ArrayList<>();
                        arrowMove.add(currRow);
                        arrowMove.add(currCol);
                        validArrowMoves.add(arrowMove);
                    }

                    // If the current position is occupied by a queen, stop searching in this direction
                    if (gameState.get(position) == QUEEN) {
                        break;
                    }

                    currRow += rowOffset;
                    currCol += colOffset;
                }
            }
        }

        return validArrowMoves;
    }



    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < gameBoard.BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean isEmptyCell(int row, int col) {
        return board[row][col] == EMPTY;
    }

    public boolean isOpponentPiece(int row, int col) {
        int piece = board[row][col];
        return piece != EMPTY && piece != currentPlayer;
    }
	/* To test your implementation of the game start two different instances of the human PLayer and have them start playing moves and 
    have them talk to the server. */
    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class
