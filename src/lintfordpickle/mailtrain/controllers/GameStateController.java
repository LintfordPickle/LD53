package lintfordpickle.mailtrain.controllers;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.GameState;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameStateController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Game State Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameWorldHeader mTrackHeader;
	private GameState mGameState;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameWorldHeader trackHeader() {
		return mTrackHeader;
	}

	public boolean getHasWon() {
		if (mGameState == null || ConstantsGame.PREVIEW_MODE)
			return false;

		return false;
	}

	public boolean getHasLost() {
		if (mGameState == null || ConstantsGame.PREVIEW_MODE)
			return false;

		return false;
	}

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameState gameState() {
		return mGameState;
	}

	@Override
	public boolean isInitialized() {
		return mGameState != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateController(ControllerManager pControllerManager, GameState pGameState, GameWorldHeader pTrackHeader, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mTrackHeader = pTrackHeader;
		mGameState = pGameState;
	}

	// ---------------------------------------------
	// Core-Methods
	// --------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		if (!isInitialized())
			return;
	}

}
