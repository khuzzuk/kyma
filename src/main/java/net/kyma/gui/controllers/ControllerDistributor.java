package net.kyma.gui.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyma.gui.manager.ManagerPaneController;

@AllArgsConstructor
@Getter
public class ControllerDistributor {
    private MainController controller;
    private PlayerPaneController playerPaneController;
    private ManagerPaneController managerPaneController;
    private ContentView contentView;
}
