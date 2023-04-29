package lintfordpickle.mailtrain.screens.game;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.mailtrain.ConstantsGame;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager screenManager) {
		super(screenManager);
		// TODO Auto-generated constructor stub
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE, this) || core.input().gamepads().isGamepadButtonDownTimed(GLFW.GLFW_GAMEPAD_BUTTON_START, this)) {
			if (ConstantsGame.ESCAPE_RESTART_MAIN_SCENE) {
				final var lLoadingScreen = new LoadingScreen(screenManager(), true, new GameScreen(screenManager()));
				screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, lLoadingScreen));
				return;
			}

			screenManager().addScreen(new PauseScreen(screenManager()));

			return;
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void createControllers(ControllerManager controllerManager) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initializeControllers(LintfordCore core) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createRenderers(LintfordCore core) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initializeRenderers(LintfordCore core) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadRendererResources(ResourceManager resourceManager) {
		// TODO Auto-generated method stub

	}

}
