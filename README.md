# COSC322_Project
The game of Amazons was invented by Walter Zamkauskas. It is a two-player abstract strategy board game played on a 10x10 square board with four amazons and several arrows. Each player places their four amazons on the board in their starting positions: two on one side of the board and two on the opposite side. The amazons can move like both a queen 

### Objective
The game's objective is to be the last player to make a legal move. On each turn, a player can move one of their Amazons to a new square, using either a queen move (any number of squares horizontally, vertically, or diagonally) or a knight move (two squares in one direction followed by one square perpendicular to that direction). After moving, the player must shoot an arrow from Amazon's new location to any empty square, blocking that square and creating an obstacle for both players. The arrow can be shot in any direction but cannot pass through any existing obstacles.

### Run the Game

Brefore we begin, please note that it take two players(in the same game room) for the AI to start playing. 

1. Install `maven`, I used `brew` to do so.
2. Clone the project repository on your machine
3. Open up the project in an IDE of your choice I used Eclipse IDE
4. Right click on the project -> Run As -> Run Configurations -> Java Application
5. We are now going to create a new run configuration for the project by clicking on Java Application.
6. To this new configuration, add arguments. This is going to be your `username` and `password`
7. You are all set. Run the Java configuration you just created and you should see something like this: 
![A screenshot of the game](https://github.com/shreyasichauhan19/COSC322_Project/blob/main/image.png)

Once you run the game and join a room, you should see something like this on your console: 
`Apr 15, 2023 12:35:06 A.M. ubc.cosc322.Bot onLogin
INFO: Login Successful.
Yellow Lake(0,0)
Yellow Lake
(Room-join-response) RoomName:Yellow Lake; NumUser: 1; PlayerID: 1
[sourceRoom, cmd, params]---[game-state, room.name]
Extension Request from dispatch(): cosc322.game-state.board---Yellow Lake
cmd:cosc322.game-state.board; From:Yellow Lake; CurrRoom: Yellow Lake`

### How to win
Players take turns moving and shooting arrows and occupying an empty square until one of the victory conditions is met. The game is won by the player who either captures all of the opponent's Amazons or successfully blocks their opponent's Amazons so they cannot make a move.

### Algorithm
We researched various graph and tree search algorithms for the game of Amazons and shortlisted three options: alpha-beta pruning, min-max, and Monte Carlo tree search. After evaluating the performance of each algorithm, we decided to use both the min-max and alpha-beta pruning algorithms in our implementation. 
In two-player zero-sum games with perfect information, the minimax algorithm can select optimal moves by a depth-first enumeration of the game tree. The alpha–beta search algorithm computes the same optimal move as minimax, but achieves much greater efficiency by eliminating subtrees that are provably irrelevant. By combining both algorithms, we can leverage their strengths while mitigating their weaknesses. The min-max algorithm explores all moves to find the best one, and the alpha-beta pruning algorithm reduces computational costs. Our research has shown that using both algorithms is the optimal approach for implementing Amazon, leading to top performance and reduced response times regardless of the number of moves.

### Implementation
Bot:
This program defines a game-playing bot named "Bot" that uses the SmartFoxServer 2X (SFS2X) platform to play the game of Amazons. The bot can play as either the white or black player and implements the GamePlayer interface. It includes methods for handling game messages, making moves, and connecting to the SFS2X server. Additionally, the program features a main() method that runs the bot from the command line and takes in the username and password as arguments.

COSC322Test:
This section begins by defining an enumeration class, Tile, which represents the possible states of a cell on the Amazons game board. These states include `EMPTY`, `WHITE`, `BLACK`, and `FIRE`.
Next, the program defines several constants, including NODE_LIMIT, which limits the total number of nodes the AI can evaluate; ROW_LENGTH, which represents the number of cells in a row; and INITIAL_BOARD_STATE, a 2D array that represents the initial state of the game board.
The bulk of the section is dedicated to the COSC322Test class, which implements the GamePlayer interface for the Amazon game. This class includes a setPlayer method that sets the player and opponent tiles, a logger object for logging messages during the game, and currentState and movesMap objects to keep track of the current game state and possible moves, respectively. There are also utility methods to convert between indices and array lists and to extract the current and next queen positions and arrow positions from the AmazonsGameMessage object.
While this class includes some utility methods and a skeleton for the GamePlayer interface, the implementation of the actual game-playing logic is done in other classes: Graph, Heuristics, PossibleMoves, and SearchTree.

Graph:
The Graph data structure is used to represent the Amazon game board. It contains methods for creating and updating the graph and for computing the shortest distance between two nodes using a heuristic algorithm.The class has a constructor that takes a 2D array of integers representing the initial state of the game board. It initializes the graph by creating a Node object for each tile on the board and connecting each node to its adjacent nodes.
The `updateGraph` method takes a move record and updates the graph accordingly. The method sets the current node to empty, enables edges for all connected nodes, sets the next node to the player's tile, disables edges for all connected nodes, and sets the arrow node to the FIRE tile. The method then refreshes the distances for all nodes.
The copy method creates a deep copy of a given graph. The method creates a new graph object with the same number of nodes as the source graph and then copies the properties of each node and its edges to the new graph.
Private helper methods called `initializeGraph`, `createNodes`, and `addEdge` are used to create and connect nodes and edges in the graph. Another private method called `toggleConnectedNodeEdges` is used to enable or disable the edges for all nodes connected to a given node. The class also includes a private instance variable called `heuristicValue`, which represents the current heuristic value of the graph and is computed by the `updateDistances` method.

Heuristics:
This section describes the implementation of the `Heuristics` class, which is responsible for calculating a heuristic score for a given game state.
The `allDistances` method sets the `kDist` and `qDist` values on each empty tile on the board for a given player. These values represent the minimum number of moves required for the player
