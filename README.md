# COSC322_Project 

### Heuristics Description: 

We are implementing some of the heuristics used for the game of Amazons. The game is a two-player board game that is played on a 10x10 board. The game begins with each player having four pieces called amazons, which can be moved in a straight line (horizontally, vertically, or diagonally) to an empty square on the board. Once a player moves an amazon, that amazon shoots an arrow that blocks the movement of all pieces in a straight line (horizontally, vertically, or diagonally) originating from the square where the arrow lands.

The allDistances method sets the kDist and qDist values on each empty tile on the board for a given player. kDist and qDist represent the number of moves required for the player's amazons to reach the tile. The method works by performing a breadth-first search from each of the player's amazons.

The calculateT method calculates the value of the t heuristic, which is used to evaluate a board position. The method uses four different sub-heuristics (f1, f2, f3, and f4) that are combined to produce the final value of t. The t value is a weighted sum of the values of the sub-heuristics. The sub-heuristics are weighted using the values of t1 and t2, which are calculated based on the distances of each empty tile from the player's amazons (qDist and kDist). The method also uses two other sub-heuristics (c1 and c2) that are calculated based on the number of moves available to each player's amazons. The values of t1, t2, c1, and c2 are all scaled by constants (k, a1, a2, b1, and b2) to produce the final value of t.
