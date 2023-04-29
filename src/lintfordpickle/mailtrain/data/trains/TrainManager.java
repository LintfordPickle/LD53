package lintfordpickle.mailtrain.data.trains;

import java.util.List;

import net.lintford.library.core.entity.instances.IndexedPoolInstanceManager;

public class TrainManager extends IndexedPoolInstanceManager<Train> {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 125458359688874600L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

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

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainManager() {
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected Train createPoolObjectInstance() {
		return new Train(getNewTrainUid());
	}
}
