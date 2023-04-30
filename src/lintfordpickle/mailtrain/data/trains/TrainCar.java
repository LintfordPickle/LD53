package lintfordpickle.mailtrain.data.trains;

import lintfordpickle.mailtrain.data.trains.definitions.TrainCarDefinition;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.entity.instances.IndexedPooledBaseData;

public class TrainCar extends IndexedPooledBaseData {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -2553655033432113706L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrainHitch frontHitch = new TrainHitch(this);
	public final TrainHitch rearHitch = new TrainHitch(this);
	public final TrainAxle frontAxle = new TrainAxle(this);
	public final TrainAxle rearAxle = new TrainAxle(this);

	public Train train;
	private int mTrainCarNumber;
	private boolean mHasHadCollision;
	private float mTimeSinceCollision;
	private TrainCarDefinition mCarDefinition;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int trainCarNumber() {
		return mTrainCarNumber;
	}

	public boolean isAssigned() {
		return mCarDefinition != null;
	}

	public boolean isLocomotive() {
		return mCarDefinition.isLocomotive;
	}

	public float topSpeed() {
		return mCarDefinition.topSpeed;
	}

	public float maxAccel() {
		return mCarDefinition.maxAccel;
	}

	public float maxBrake() {
		return mCarDefinition.maxBrake;
	}

	public void setHasHadCollision() {
		if (!mHasHadCollision) { // don't keep resetting the timer on collisions, or trains never leave
			mHasHadCollision = true;
			mTimeSinceCollision = 0.f;

		}
	}

	public boolean hasHadCollision() {
		return mHasHadCollision;
	}

	public float timeSinceCollision() {
		return mTimeSinceCollision;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainCar(int pPoolUid) {
		super(pPoolUid);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void update(LintfordCore pCore) {
		if (hasHadCollision()) {
			mTimeSinceCollision += pCore.gameTime().elapsedTimeMilli();
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void init(TrainCarDefinition pDefinition) {
		mCarDefinition = pDefinition;
	}

	public void unassign() {
		mCarDefinition = null;

		mHasHadCollision = false;
		mTimeSinceCollision = 0.f;

		frontAxle.reset();
		rearAxle.reset();

		return;
	}

	public void reset() {
	}

	public TrainAxle getMatchingAxle(TrainHitch pHitch) {
		return pHitch == frontHitch ? frontAxle : rearAxle;
	}

	public TrainHitch getMatchingHitch(TrainAxle pAxle) {
		return pAxle == frontAxle ? frontHitch : rearHitch;
	}

	public TrainHitch getOtherHitch(TrainHitch pA) {
		if (frontHitch == pA) {
			return rearHitch;
		} else
			return frontHitch;
	}

	public TrainAxle getOtherAxle(TrainAxle pA) {
		if (frontAxle == pA) {
			return rearAxle;
		} else
			return frontAxle;
	}

	public TrainAxle getAxleOnFreeHitch() {
		if (mCarDefinition != null && mCarDefinition.isLocomotive)
			return rearAxle;

		if (frontHitch.connectedTo == null)
			return frontAxle;
		if (rearHitch.connectedTo == null)
			return rearAxle;
		return null;
	}

	public TrainHitch getFreeHitch() {
		if (frontHitch.connectedTo == null)
			return frontHitch;
		if (rearHitch.connectedTo == null)
			return rearHitch;
		return null;
	}

}
