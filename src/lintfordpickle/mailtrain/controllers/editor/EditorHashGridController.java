package lintfordpickle.mailtrain.controllers.editor;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.controllers.editor.interfaces.IGridControllerCallback;
import lintfordpickle.mailtrain.controllers.scene.GameSceneController;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.partitioning.GridEntity;
import net.lintford.library.core.geometry.partitioning.SpatialHashGrid;

public class EditorHashGridController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Editor HashGrid Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private GameSceneController mGameSceneController;

	private SpatialHashGrid<GridEntity> mHashGrid;
	private final List<IGridControllerCallback> mHashContainerControllers = new ArrayList<>();

	// --------------------------------------
	// Properties
	// --------------------------------------

	public SpatialHashGrid<GridEntity> hashGrid() {
		return mHashGrid;
	}

	// --------------------------------------
	// Constructors
	// --------------------------------------

	public EditorHashGridController(ControllerManager controllerManager, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();
		mGameSceneController = (GameSceneController) lControllerManager.getControllerByNameRequired(GameSceneController.CONTROLLER_NAME, mEntityGroupUid);

		mHashGrid = mGameSceneController.gameScene().hashGridManager().hashGrid();
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addGridListener(IGridControllerCallback gridListener) {
		if (mHashContainerControllers.contains(gridListener) == false) {
			mHashContainerControllers.add(gridListener);
		}
	}

	public void removeGridListener(IGridControllerCallback gridListener) {
		mHashContainerControllers.remove(gridListener);
	}

	public void resizeGrid(int width, int height, int tilesWide, int tilesHigh) {
		// have the dimensions / grid changed?
		if (mHashGrid.boundaryWidth() == width && mHashGrid.boundaryHeight() == height && mHashGrid.numTilesWide() == tilesWide && mHashGrid.numTilesHigh() == tilesHigh)
			return;

		// clear all entities and their caches, from all cells
		final int lTotalNumCells = mHashGrid.numTilesWide() * mHashGrid.numTilesHigh();
		for (int i = 0; i < lTotalNumCells; i++) {
			final var lCell = mHashGrid.getCell(i);
			if (lCell == null || lCell.size() == 0)
				continue;

			final int lNumEntitiesInCell = lCell.size();
			for (int j = 0; j < lNumEntitiesInCell; j++) {
				lCell.get(j).clearGridCache();
			}
		}

		final int lNumListeners = mHashContainerControllers.size();
		for (int i = 0; i < lNumListeners; i++) {
			mHashContainerControllers.get(i).gridDeleted(mHashGrid);
		}

		mGameSceneController.gameScene().hashGridManager().createNewHashGrid(width, height, tilesWide, tilesHigh);
		mHashGrid = mGameSceneController.gameScene().hashGridManager().hashGrid();

		for (int i = 0; i < lNumListeners; i++) {
			mHashContainerControllers.get(i).gridCreated(mHashGrid);
		}
	}
}