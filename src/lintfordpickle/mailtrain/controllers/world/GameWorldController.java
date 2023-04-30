package lintfordpickle.mailtrain.controllers.world;

import lintfordpickle.mailtrain.controllers.TrainController;
import lintfordpickle.mailtrain.data.world.scenes.GameScene;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameWorldController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Game World Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrainController mTrainController;

	private boolean mIsGameLoaded;
	private GameScene mGameWorld;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isGameLoaded() {
		return mIsGameLoaded;
	}

	@Override
	public boolean isInitialized() {
		return mGameWorld != null;
	}

	public GameScene gameWorld() {
		return mGameWorld;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWorldController(ControllerManager pControllerManager, GameScene pGameWorld, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mIsGameLoaded = false;
		mGameWorld = pGameWorld;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mTrainController = (TrainController) pCore.controllerManager().getControllerByNameRequired(TrainController.CONTROLLER_NAME, entityGroupUid());
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void startNewGame() {
		final var lPlayerTrain = mTrainController.addNewMainTrain();
		lPlayerTrain.isPlayerControlled(true);

		mIsGameLoaded = true;
	}
}
