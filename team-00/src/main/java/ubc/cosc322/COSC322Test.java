package ubc.cosc322;

import ubc.cosc322.queen_movement.*;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

import java.util.*;
import java.util.logging.Logger;


public class COSC322Test{

	//IMPORTANT
	//Moves are presented as 2 element Integer ArrayLists
	//The first int representing the Y value from bottom to top
	//The second int representing the X value from right to left
	//[10, 7] corresponds to [10, G] on the game board GUI.
	//[5, 3] corresponds to [5, C] etc...

	public enum Tile {
		EMPTY(0),
		WHITE(1),
		BLACK(2),
		FIRE(3);

		public final int id;
		private static final Map<Integer, Tile> map = new HashMap<>();

		Tile(int id) {
			this.id = id;
		}

		static {
			for (Tile tile : Tile.values()) {
				map.put(tile.id, tile);
			}
		}

		public static Tile valueOf(int id){
			return map.get(id);
		}

		public boolean isEmpty(){
			return this == EMPTY;
		}

		public boolean isPlayer(){
			return isWhite() || isBlack();
		}

		public boolean isWhite(){
			return this == WHITE;
		}

		public boolean isBlack(){
			return this == BLACK;
		}

		public boolean isFire(){
			return this == FIRE;
		}

	}

	private static final int NODE_LIMIT = 250000;
	private static final int ROW_LENGTH = 10;
	private static final int[][] INITIAL_BOARD_STATE = {

			{0, 0, 0, 2, 0, 0, 2, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 0, 0, 0, 0, 0, 0, 2},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
	};

	public static int[][] getInitialBoardState(){
		return INITIAL_BOARD_STATE;
	}

	private Tile player;
	private Tile opponent;

	public void setPlayer(Tile p){
		if(p.isPlayer()){
			player = p;
			opponent = p.isWhite() ? Tile.BLACK : Tile.WHITE;
		}
	}

	private final Logger logger;

	private Graph currentState;
	private Map<PossibleMoves.Move, Graph> movesMap;

	public COSC322Test(){
		logger = Logger.getLogger(COSC322Test.class.toString());

		currentState = new Graph(INITIAL_BOARD_STATE);
		movesMap = new HashMap<>();
	}

	// Takes the message Details that are stored in the array and retrieves the position of:
	// Current X and Y of Queen
	public static int getQueenCurrentIndex (Map<String, Object> msgDetails) {
		ArrayList<Integer> current = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		return (ROW_LENGTH - current.get(0)) * ROW_LENGTH + (current.get(1)-1);
	}

	public static int getQueenNextIndex (Map<String, Object> msgDetails) {
		ArrayList<Integer> next = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
		return (ROW_LENGTH - next.get(0)) * ROW_LENGTH + (next.get(1)-1);
	}

	public static int getArrowIndex (Map<String, Object> msgDetails) {
		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
		return (ROW_LENGTH - arrow.get(0)) * ROW_LENGTH + (arrow.get(1)-1);
	}

	public static List<Integer> indexToArrayList(int index){
		ArrayList<Integer> list = new ArrayList<>(2);
		//Y Position
		list.add((int) Math.ceil(ROW_LENGTH - ((float) index/ROW_LENGTH)));
		//X Position
		list.add((index % ROW_LENGTH) + 1);
		return list;
	}


	/**
	 * Takes information from the opponents move and updates the board state graph accordingly
	 * @param opponentMove Opponent move information
	 */
	public void opponentMove(Map<String, Object> opponentMove){
		//Translate the x y coordinates to the graph node index
		int currentIndex = getQueenCurrentIndex(opponentMove);
		int nextIndex = getQueenNextIndex(opponentMove);
		int arrowIndex = getArrowIndex(opponentMove);

		PossibleMoves.Move move = new PossibleMoves.Move(currentIndex, nextIndex, arrowIndex);

		//Check validity
		movesMap = PossibleMoves.allMoves(currentState, opponent);

		if(!movesMap.containsKey(move)){
			String msg = (opponent.isWhite() ? "White " : "Black ") +
					"has made an illegal move.";
			logger.severe(msg);
		}

		logger.info("Opponent has made their move. Calculating next move...");

		//Update the graph
		if(player.isWhite())
			currentState.updateGraph(move, Tile.BLACK);
		else
			currentState.updateGraph(move, Tile.WHITE);

	}

	/**
	 * Calculates the best possible move from the current state of the board.
	 * @return An Map containing movement information
	 */
	public Map<String, Object> makeMove() {

		movesMap = PossibleMoves.allMoves(currentState, player);

		int depth = 1;
		for(int i = 5; i > 0; i--){
			if(Math.pow(movesMap.size(), i) < NODE_LIMIT){
				depth = i;
				break;
			}
		}

		PossibleMoves.Move bestMove = SearchTree.performAlphaBeta(Graph.copy(currentState), player, depth);

		//Double check there aren't any possible moves whatsoever
		if(bestMove == null) {
			bestMove = SearchTree.performAlphaBeta(Graph.copy(currentState), player, 1);
		}

		if(bestMove == null){
			return Collections.emptyMap();
		}

		//Put the move information into a message details object to send back to the game server
		Map<String, Object> playerMove = new HashMap<>();
		playerMove.put(AmazonsGameMessage.QUEEN_POS_CURR, indexToArrayList(bestMove.currentIndex()));
		playerMove.put(AmazonsGameMessage.QUEEN_POS_NEXT, indexToArrayList(bestMove.nextIndex()));
		playerMove.put(AmazonsGameMessage.ARROW_POS, indexToArrayList(bestMove.arrowIndex()));

		//Don't forget to update the current state of the game!
		currentState = movesMap.get(bestMove);

		movesMap = PossibleMoves.allMoves(currentState, opponent);
		if(movesMap.isEmpty()){
			logger.info("Opponent has no valid moves. We've won!");
		}

		return playerMove;
	}


}