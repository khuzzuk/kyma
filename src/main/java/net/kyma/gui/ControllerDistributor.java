package net.kyma.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.gui.content.ContentPaneController;
import net.kyma.gui.player.PlayerPaneController;
import net.kyma.gui.manager.ManagerPaneController;

@AllArgsConstructor
@Getter
public class ControllerDistributor {
    private MainController controller;
    private PlayerPaneController playerPaneController;
    private ManagerPaneController managerPaneController;
    private ContentPaneController contentPaneController;
}
