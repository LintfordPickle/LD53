package lintfordpickle.mailtrain.data.scene;

import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;

public abstract class BaseInstanceManager {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseInstanceManager() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public abstract void initializeManager();

	public abstract void storeInSceneDefinition(GameSceneSaveDefinition sceneDefinition);

	public abstract void loadFromSceneDefinition(GameSceneSaveDefinition sceneDefinition);

	public abstract void finalizeAfterLoading(GameSceneInstance sceneInstance);

}
