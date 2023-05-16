package lintfordpickle.mailtrain.controllers.editor.gui;

import lintfordpickle.mailtrain.controllers.scene.GameSceneController;
import lintfordpickle.mailtrain.data.scene.GameSceneHeader;
import lintfordpickle.mailtrain.data.world.GameWorldHeader;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class EditorFileController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Editor File Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameWorldHeader mGameWorldHeader;
	private GameSceneHeader mGameSceneHeader;
	private GameSceneController mGameSceneController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameWorldHeader worldHeader() {
		return mGameWorldHeader;
	}

	public GameSceneHeader gameSceneHeader() {
		return mGameSceneHeader;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorFileController(ControllerManager controllerManager, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();

		mGameSceneController = (GameSceneController) lControllerManager.getControllerByNameRequired(GameSceneController.CONTROLLER_NAME, mEntityGroupUid);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public boolean saveScene() {
		if (mGameWorldHeader.isValid() == false) {
			// TODO: Output some kind of notification that the filepath is not valid
			return false;
		}

		// Get the savefile and path from the mGameFileHeader (if its valid)

		// TODO: Save in the correct filepath (this isn't it)
		mGameSceneController.saveSceneToFile(mGameWorldHeader.worldDirectory());
		return true;
	}

}
