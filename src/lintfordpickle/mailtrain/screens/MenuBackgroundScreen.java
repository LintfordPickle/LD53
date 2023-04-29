package lintfordpickle.mailtrain.screens;

import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;

public class MenuBackgroundScreen extends Screen {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public MenuBackgroundScreen(ScreenManager screenManager) {
		super(screenManager);

		mScreenManager.core().createNewGameCamera();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

}
