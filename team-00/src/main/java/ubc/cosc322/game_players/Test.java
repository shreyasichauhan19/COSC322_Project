package ubc.cosc322.game_players;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;

public class Test {

    public static void main(String[] args) {
        GamePlayer human = new HumanPlayer();

        if(human.getGameGUI() == null) {
            human.Go();
        }
        else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(human::Go);
        }
    }


}
