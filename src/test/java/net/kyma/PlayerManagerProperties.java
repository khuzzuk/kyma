package net.kyma;

import net.kyma.player.Player;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class PlayerManagerProperties {
   static Player player;

   @After("set(net.kyma.player.Player net.kyma.player.PlayerManager.currentPlayer) && args(player)")
   public void setCurrentPlayer(Player player) {
      PlayerManagerProperties.player = player;
   }
}
