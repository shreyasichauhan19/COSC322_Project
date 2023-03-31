package ubc.cosc322.queen_movement;


import java.util.Map;

import ubc.cosc322.COSC322Test.Tile;

public class SearchTree {

    private SearchTree() {

    }

    public record MinimaxMove(PossibleMoves.Move move, float heuristic) { }


    private static MinimaxMove AlphaBeta(Graph node, int depth, float alpha, float beta, Tile player, PossibleMoves.Move lastMove) {
        if (depth == 0)
            return new MinimaxMove(lastMove, Heuristics.calculateT(node, player));

        float bestHeuristic;
        PossibleMoves.Move bestMove = null;
        Map<PossibleMoves.Move, Graph> movesMap = PossibleMoves.allMoves(node, player);

        if (player.isWhite()) {

            bestHeuristic = Integer.MIN_VALUE;

            for (Map.Entry<PossibleMoves.Move, Graph> entry : movesMap.entrySet()) {
                PossibleMoves.Move currentMove = entry.getKey();
                float chosenHeuristic = AlphaBeta(entry.getValue(), depth - 1, alpha, beta, Tile.BLACK, currentMove).heuristic;
                if (bestHeuristic < chosenHeuristic) {
                    bestHeuristic = chosenHeuristic;
                    bestMove = currentMove;
                }

                alpha = Math.max(alpha, bestHeuristic);
                if (beta <= alpha) {
                    break;
                }
            }

        } else {

            bestHeuristic = Integer.MAX_VALUE;

            for (Map.Entry<PossibleMoves.Move, Graph> entry : movesMap.entrySet()) {
                float chosenHeuristic = AlphaBeta(entry.getValue(), depth - 1, alpha, beta, Tile.WHITE, entry.getKey()).heuristic;
                if (bestHeuristic > chosenHeuristic) {
                    bestHeuristic = chosenHeuristic;
                    bestMove = entry.getKey();
                }

                beta = Math.max(alpha, bestHeuristic);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return new MinimaxMove(bestMove, bestHeuristic);
    }

    public static PossibleMoves.Move performAlphaBeta(Graph node, Tile player, int depth) {
        MinimaxMove chosenOne = AlphaBeta(node, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, player, null);
        return chosenOne.move;
    }
}
