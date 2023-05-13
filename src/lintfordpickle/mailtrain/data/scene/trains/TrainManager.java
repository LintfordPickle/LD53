package lintfordpickle.mailtrain.data.scene.trains;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.BaseInstanceManager;
import lintfordpickle.mailtrain.data.scene.GameSceneInstance;
import lintfordpickle.mailtrain.data.scene.savedefinitions.GameSceneSaveDefinition;

public class TrainManager extends BaseInstanceManager {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected final List<Train> mInstances = new ArrayList<>();
	private transient List<Train> mPooledItems;
	private int mEnlargePoolStepAmount = 8;
	private int mTrainUidCounter = 0;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int getNewTrainUid() {
		return mTrainUidCounter++;
	}

	public List<Train> activeTrains() {
		return mInstances;
	}

	public int numInstances() {
		return mInstances.size();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainManager() {
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initializeManager() {
		// TODO Auto-generated method stub

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public Train getFreePooledItem() {
		Train lInst = null;

		if (mPooledItems == null) {
			mPooledItems = new ArrayList<>();
		}

		if (mPooledItems.size() > 0) {
			lInst = mPooledItems.remove(0);
			mInstances.add(lInst);

		} else {
			lInst = enlargenInstancePool(mEnlargePoolStepAmount);
			mInstances.add(lInst);
		}

		return lInst;
	}

	public void returnPooledItem(Train returnedItem) {
		if (returnedItem == null)
			return;

		if (mInstances.contains(returnedItem)) {
			mInstances.remove(returnedItem);
		}

		if (mPooledItems == null) {
			mPooledItems = new ArrayList<>();
		}

		if (!mPooledItems.contains(returnedItem)) {
			returnedItem.reset();
			mPooledItems.add(returnedItem);
		}
	}

	private Train enlargenInstancePool(int enlargeByAmount) {
		for (int i = 0; i < enlargeByAmount; i++) {
			mPooledItems.add(createPoolObjectInstance());
		}

		Train lInst = mPooledItems.remove(0);

		return lInst;
	}

	private Train createPoolObjectInstance() {
		return new Train(getNewTrainUid());
	}

	// ---------------------------------------------
	// Inherited-Methods
	// ---------------------------------------------

	@Override
	public void storeInSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFromSceneDefinition(GameSceneSaveDefinition sceneDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finalizeAfterLoading(GameSceneInstance sceneInstance) {
		// TODO Auto-generated method stub

	}
}
