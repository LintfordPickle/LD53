package lintfordpickle.mailtrain.data.scene.grid;

import lintfordpickle.mailtrain.data.scene.BaseInstanceManager;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;
import net.lintford.library.core.geometry.partitioning.GridEntity;
import net.lintford.library.core.geometry.partitioning.SpatialHashGrid;

public class HashGridManager extends BaseInstanceManager {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpatialHashGrid<GridEntity> mHashGrid;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public SpatialHashGrid<GridEntity> hashGrid() {
		return mHashGrid;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public HashGridManager() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	public void initializeManager() {

	}

	public void createNewHashGrid(int boundaryWidth, int boundaryHeight, int tilesWide, int tilesHigh) {
		mHashGrid = new SpatialHashGrid<GridEntity>(boundaryWidth, boundaryHeight, tilesWide, tilesHigh);
	}

	// ---------------------------------------------
	// Inherited-Methods
	// ---------------------------------------------

	@Override
	public void storeInSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		sceneDefinition.hashGridSaveManager().updateSettings(mHashGrid);
	}

	@Override
	public void loadFromSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		final var lGridSettings = sceneDefinition.hashGridSaveManager();
		mHashGrid = new SpatialHashGrid<GridEntity>(lGridSettings.hashGridWidth, lGridSettings.hashGridHeight, lGridSettings.hashGridTilesWide, lGridSettings.hashGridTilesHigh);
	}

	@Override
	public void finalizeAfterLoading(GameSceneInstance sceneInstance) {

	}

}
