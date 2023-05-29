package lintfordpickle.mailtrain.data.scene.trains;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalSegment;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.entities.instances.OpenPooledBaseData;
import net.lintford.library.core.maths.MathHelper;

public class Train extends OpenPooledBaseData {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final List<TrainCar> UpdateListTrainCars = new ArrayList<>();

	private static final boolean OUTPUT_TRACK_FOLLOW_SEGMENT_INFO = false;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private List<TrainCar> carsAttachedToTrain;

	public TrainCar locomotiveCar;
	public TrainCar leadCar;
	public TrainCar lastCar;

	// FIXME: Clean up tomorrow
	public RailTrackSignalSegment currentForwardTrackSegment;

	private float mAcceleration;
	private float mActualSpeedInMetersPerSecond;
	public float targetSpeedInMetersPerSecond;

	private boolean mIsDestroyed;
	private boolean mIsPlayerControlled;
	private int mTrainNumber;
	private boolean mDrivingForward;

	// A logical counter for assigning to nodes saved in the train's follow list.
	// This allows the axles to tick off the nodes as they are passed, so they are not re-visited.
	// This is needed for long trains which multiple carriages.
	private int trainPathNodeTracker;

	boolean isSignalBraking;
	boolean isBraking;
	float distanceToNextStop;
	final float lDistanceToStartBraking = 96.f;

	// allows axles to figure out the next node uid to follow
	public final transient List<TrainFollowSegment> trackSegmentFollowList = new ArrayList<>();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean hasLocomotive() {
		final int lNumTrainCars = getNumberOfCarsInTrain();
		for (int i = 0; i < lNumTrainCars; i++) {
			if (carsAttachedToTrain.get(i).isLocomotive())
				return true;
		}
		return false;
	}

	public boolean isDestroyed() {
		return mIsDestroyed;
	}

	public boolean drivingForward() {
		return mDrivingForward;
	}

	public void drivingForward(boolean pNewValue) {
		mDrivingForward = pNewValue;
	}

	public int getNumberOfCarsInTrain() {
		return carsAttachedToTrain.size();
	}

	public TrainCar getLeadCar() {
		return leadCar;
	}

	public TrainCar getLastCar() {
		return lastCar;
	}

	public TrainCar getCarByIndex(int pIndex) {
		if (carsAttachedToTrain.size() == 0 || pIndex < 0 || pIndex >= carsAttachedToTrain.size())
			return null;

		return carsAttachedToTrain.get(pIndex);
	}

	public TrainCar getCarByCarNumber(int pCarNumber) {
		final int lNumCarts = carsAttachedToTrain.size();
		for (int i = 0; i < lNumCarts; i++) {
			if (carsAttachedToTrain.get(i).trainCarNumber() == pCarNumber)
				return carsAttachedToTrain.get(i);

		}
		return null;
	}

	/* A logical counter assigned to each engine and carriage of the same train */
	public int getTrainNumber() {
		return mTrainNumber;
	}

	public float acceleration() {
		return mAcceleration;
	}

	public float getSpeed() {
		return mActualSpeedInMetersPerSecond;
	}

	public void setSpeed(float pNewSpeedValue) {
		mActualSpeedInMetersPerSecond = pNewSpeedValue;
	}

	public boolean isPlayerControlled() {
		return mIsPlayerControlled;
	}

	public void isPlayerControlled(boolean pNewValue) {
		mIsPlayerControlled = pNewValue;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Train(int poolUid) {
		super(poolUid);
		carsAttachedToTrain = new ArrayList<>();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void init(int pTrainNumber) {
		mIsDestroyed = false;
		mTrainNumber = pTrainNumber;
	}

	public void destroy() {
		carsAttachedToTrain.clear();
		mIsDestroyed = true;
		mTrainNumber = -1;
	}

	public void update(LintfordCore pCore, RailTrackInstance pTrack) {

		updateTrainMotion(pCore, pTrack);
		updateTrainCarriages(pCore, pTrack);

	}

	private void updateTrainCarriages(LintfordCore pCore, RailTrackInstance pTrack) {
		final int lNumAttachedCars = getNumberOfCarsInTrain();
		for (int j = 0; j < lNumAttachedCars; j++) {
			final var lTrainCar = getCarByIndex(j);

			final var lCarriageDefinition = lTrainCar.definition();
			lCarriageDefinition.updateCarriageInstance(pCore, lTrainCar);

			if (lTrainCar.hasHadCollision()) {
				if (ConstantsGame.SOUNDS_ENABLED) {
					// mTrainSoundManager.play("SOUND_CRASH", lTrain.worldPositionX(), lTrain.worldPositionY(), 0.f, 0.f);

				}

				// Cleanup the carts after time has passed
				final float TIME_TILL_REMOVE = 2500.f;
				if (lTrainCar.timeSinceCollision() > TIME_TILL_REMOVE) {
					// removeTrainCar(lTrain);
					continue;
				}
			} else {
				// Debug update train world position
				updateAxleWorldPosition(pTrack, lTrainCar.frontAxle);
				updateAxleWorldPosition(pTrack, lTrainCar.rearAxle);

				lTrainCar.update(pCore);
			}
		}
	}

	private void updateTrainMotion(LintfordCore pCore, RailTrackInstance pTrack) {
		if (getSpeed() == 0) {
			if (targetSpeedInMetersPerSecond > 0.f && drivingForward() == false) {
				handleChangeDrivingDirection(pTrack, true);
			} else if (targetSpeedInMetersPerSecond < 0.f && drivingForward() == true) {
				handleChangeDrivingDirection(pTrack, false);
			}

		} else {
			if (drivingForward() && getSpeed() < 0.f) {
				handleChangeDrivingDirection(pTrack);
			} else if (!drivingForward() && getSpeed() > 0.f) {
				handleChangeDrivingDirection(pTrack);
			}

		}

		// Update the speed of the train
		updateTrainSpeed(pCore, pTrack);

		// Drive the train based on speed
		if (drivingForward()) {
			// locomotive always leads the front of a train driving forwards
			handleDriveAxles(pCore, pTrack, leadCar.frontAxle);
			handleUpdateAxles(pTrack, leadCar.frontAxle);
			if (getNumberOfCarsInTrain() == 1) {
				// so take the locomotive's rear axle
				removeFollowSegments(leadCar.rearAxle.nextFollowSegment);
			} else if (lastCar != null) {
				// otherwise take the rear car's 'rearaxle' - which ever that maybe
				removeFollowSegments(lastCar.getAxleOnFreeHitch().nextFollowSegment);
			}
		} else {
			// going backwards with one car means we're using the locomotive
			if (getNumberOfCarsInTrain() == 1) {
				handleDriveAxles(pCore, pTrack, leadCar.rearAxle);
				handleUpdateAxles(pTrack, leadCar.rearAxle);
				removeFollowSegments(leadCar.frontAxle.nextFollowSegment);
			} else {
				final var lRearmostAxle = lastCar.getAxleOnFreeHitch();
				handleDriveAxles(pCore, pTrack, lRearmostAxle);
				handleUpdateAxles(pTrack, lRearmostAxle);
				removeFollowSegments(leadCar.frontAxle.nextFollowSegment);

			}
		}
	}

	private void updateTrainSpeed(LintfordCore core, RailTrackInstance trackInstance) {
		final var lDelta = (float) core.gameTime().elapsedTimeMilli() * 0.001f;
		final var lLeadAxle = drivingForward() ? leadCar.frontAxle : lastCar.getAxleOnFreeHitch();

		distanceToNextStop -= lLeadAxle.distanceTravelledInMetersLastTick();

		// Check if an explicit stop position has been defined and brake when we get close enough
		if (isBraking) {
			final float lDistanceFromStopTarget = MathHelper.max(0.f, distanceToNextStop);

			final float lBrakingEffectWithinMeters = 128f; // FIXME: Calculate baseed weight and speed of train
			final float lConsiderAsArrivedWhenWithinMeters = 2.f;
			if (lDistanceFromStopTarget > lConsiderAsArrivedWhenWithinMeters) {
				if (lDistanceFromStopTarget < lBrakingEffectWithinMeters && lDistanceFromStopTarget > 0.f) {
					// Calculate the amount of force we need to apply to bring the train to a stop at the end of this segment
					final float pAmountOfForceToApply = 0.5f * (float) Math.pow(mActualSpeedInMetersPerSecond, 2) / lDistanceFromStopTarget;

					mAcceleration = drivingForward() ? -pAmountOfForceToApply : pAmountOfForceToApply;

				}
				// Stop when the calculated speed is very small
				if (Math.abs(mActualSpeedInMetersPerSecond) < 0.01f) {
					killSpeed();

				}
			} else {
				killSpeed();

			}
		} else {
			mAcceleration = targetSpeedInMetersPerSecond * lDelta;

		}
		if (isBraking) {
			System.out.println("Stopping in " + distanceToNextStop + "m");

		}

		mActualSpeedInMetersPerSecond += mAcceleration * lDelta;

		// Limit the top speed
		final var lTopSpeed = leadCar.topSpeed(); // only considers lead(locomotive)
		mActualSpeedInMetersPerSecond = MathHelper.clamp(mActualSpeedInMetersPerSecond, -lTopSpeed, lTopSpeed);

		// nothing to do
		if (Math.abs(mActualSpeedInMetersPerSecond) < 0.1f) {
			mActualSpeedInMetersPerSecond *= 0.97f;
			if (Math.abs(targetSpeedInMetersPerSecond) < 0.1f)
				mActualSpeedInMetersPerSecond = 0.f;

			// signalOveride = false;

		}
	}

	public void updateAxleWorldPosition(RailTrackInstance pTrack, TrainAxle axle) {
		// The position is based on the rail track
		final var lCurrentSegment = axle.currentSegment;
		final var lOriginNodeUid = lCurrentSegment.getOtherNodeUid(axle.destinationNodeUid);
		final var lDistIntoTrack = axle.normalizedDistanceAlongSegment;

		axle.worldPositionX = pTrack.getPositionAlongSegmentX(lCurrentSegment, lOriginNodeUid, lDistIntoTrack);
		axle.worldPositionY = pTrack.getPositionAlongSegmentY(lCurrentSegment, lOriginNodeUid, lDistIntoTrack);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void handleDriveAxles(LintfordCore pCore, RailTrackInstance pTrack, TrainAxle pTrainAxle) {
		final var lTrainCar = pTrainAxle.parentTrainCar;

		final float lDelta = (float) pCore.gameTime().elapsedTimeMilli() * 0.001f;

		// Drive the first (given) axle some destance
		if (pTrainAxle.driveAxleForward(pTrack, Math.abs(getSpeed()) * lDelta)) {
			killSpeed();
			handleUpdateAxles(pTrack, pTrainAxle);
			clearFollowPath();

			return;
		}
		// drive the other axle on this car forward
		final var lOtherAxle = lTrainCar.getOtherAxle(pTrainAxle);

		lOtherAxle.driveAxleForward(pTrack, Math.abs(getSpeed()) * lDelta);

		// Gte the matching axle
		final var lMatchingRearHitch = lTrainCar.getMatchingHitch(lOtherAxle);
		if (lMatchingRearHitch.connectedTo != null) {
			final var lOtherHitch = lMatchingRearHitch.connectedTo;
			final var lOtherAxleOnNextCar = lOtherHitch.parentCar.getMatchingAxle(lOtherHitch);

			handleDriveAxles(pCore, pTrack, lOtherAxleOnNextCar);

		}
	}

	public void handleUpdateAxles(RailTrackInstance pTrack, TrainAxle pTrainAxle) {
		final var lTrainCar = pTrainAxle.parentTrainCar;
		final var lMatchingHitch = lTrainCar.getMatchingHitch(pTrainAxle);

		// Update the axles on this car
		handleUpdateAxle(pTrack, pTrainAxle, lMatchingHitch.connectedTo == null);

		final var lOtherAxle = lTrainCar.getOtherAxle(pTrainAxle);
		handleUpdateAxle(pTrack, lOtherAxle, false);

		final var lMatchingRearHitch = lTrainCar.getMatchingHitch(lOtherAxle);
		if (lMatchingRearHitch.connectedTo != null) {
			final var lOtherHitch = lMatchingRearHitch.connectedTo;
			final var lOtherAxleOnNextCar = lOtherHitch.parentCar.getMatchingAxle(lOtherHitch);

			handleUpdateAxles(pTrack, lOtherAxleOnNextCar);

		}
	}

	public void handleUpdateAxle(RailTrackInstance pTrack, TrainAxle pTrainAxle, boolean pIsLeadAxle) {

		// FIXME: this isn't called if trains whacks into end node

		if (pTrainAxle.hasArrived()) {
			// We are the lead axle
			final var lCurrentNodeUid = pTrainAxle.destinationNodeUid;

			// Update the next segment (via the ticker)
			if (pIsLeadAxle) {
				// this is the lead axle, so get the next segment along which we should drive
				final var lNextSegment = pTrack.getNextSegment(pTrainAxle.currentSegment, pTrainAxle.destinationNodeUid);
				if (lNextSegment == null) { // end of track
					// This only returns the fact that we have no further node to goto in this 'direction'.
					// It speaks nothing to the current position on the currently still active segment.

					// TODO: Stop the train if under some tolerence
					// TODO: If the track has a stopper - then stop tolerence is heightened
					// TODO: If the train speed is too high to stop, then its a crash ...

					// TODO: Handle the case of derailment - no track to traverse
					pTrainAxle.parentTrainCar.train.killSpeed();

					return;
				}

				// Firstly, update this axle
				pTrainAxle.currentSegment = lNextSegment;
				pTrainAxle.destinationNodeUid = lNextSegment.getOtherNodeUid(lCurrentNodeUid);
				pTrainAxle.normalizedDistanceAlongSegment -= 1.f;

				// As this axle is not following another, track whichever path is taken and record it
				addFollowSegment(lNextSegment, pTrainAxle.destinationNodeUid);
				pTrainAxle.nextFollowSegment.Segment = lNextSegment;
				pTrainAxle.nextFollowSegment.logicalCounter = pTrainAxle.destinationNodeUid;

			} else {
				final var lNextTicker = getNextFollowSegment(pTrainAxle.nextFollowSegment);
				if (lNextTicker == null) {
					System.out.println("This is where I would have broken ");
					printTrainFollowSegmentDebugInfo();
					return;
				}
				// change the segment this axle is traversing to the next in its tracker list
				pTrainAxle.currentSegment = lNextTicker.Segment;
				pTrainAxle.destinationNodeUid = lNextTicker.targetNodeUid;
				pTrainAxle.normalizedDistanceAlongSegment -= 1.f;

				// As this axle is following another axle, get the next segment to follow from the train history.
				final var lNewTicker = getNextFollowSegment(pTrainAxle.nextFollowSegment);
				if (lNewTicker != null) {
					pTrainAxle.nextFollowSegment.Segment = lNewTicker.Segment;
					pTrainAxle.nextFollowSegment.logicalCounter = lNewTicker.logicalCounter;

				}
			}
		}
	}

	// TrainCars -------------------------------------

	public void mergeTrains(RailTrackInstance pTrack, Train pOtherTrain, TrainHitch pTrainHitch) {
		addTrainCarsToBackOfTrain(pTrainHitch);
		pOtherTrain.destroy();

		// handleChangeDrivingDirection(pTrack);
	}

	public void addTrainCarsToBackOfTrain(TrainHitch pTrainHitch) {
		if (pTrainHitch == null || pTrainHitch.parentCar == null)
			return;

		// pTrainHitch = pTrainHitch.parentCar.getFreeHitch();
		final var lTrainCar = pTrainHitch.parentCar;

		final var lCarInFront = lastCar;
		carsAttachedToTrain.add(lTrainCar);
		lTrainCar.train = this;

		if (leadCar == null)
			leadCar = lTrainCar;
		if (lCarInFront != null) {
			final var lFreeHitch = getNumberOfCarsInTrain() == 1 || lCarInFront == leadCar ? lCarInFront.rearHitch : lCarInFront.getFreeHitch();
			pTrainHitch.connectedTo = lFreeHitch;
			lFreeHitch.connectedTo = pTrainHitch;

		}
		lastCar = lTrainCar;

		TrainHitch lNextHitch = lTrainCar.getOtherHitch(pTrainHitch);
		while (lNextHitch != null && lNextHitch.connectedTo != null) {
			final var lNextTrainCar = lNextHitch.connectedTo.parentCar;

			carsAttachedToTrain.add(lNextTrainCar);
			lNextTrainCar.train = this;
			lNextHitch = lNextTrainCar.getOtherHitch(lNextHitch.connectedTo);

			lastCar = lNextTrainCar;

		}
		// Make sure we take the rest of the cars into our collecetion (their hitch refs should be correct
	}

	public TrainCar detachTrainCar(int pTrainCarNumber) {
		return detachTrainCar(getCarByIndex(pTrainCarNumber));
	}

	private TrainCar detachTrainCar(TrainCar pTrainCar) {
		if (pTrainCar == null)
			return null;

		TrainCar pReturnCar = null;

		UpdateListTrainCars.clear();
		final int lNumberTrainCars = carsAttachedToTrain.size();
		for (int i = 0; i < lNumberTrainCars; i++) {
			UpdateListTrainCars.add(carsAttachedToTrain.get(i));

		}
		TrainCar lPrevTrainCar = null;
		boolean lFoundTrainCarToUnhook = false;
		for (int i = 0; i < lNumberTrainCars; i++) {
			final var lTrainCar = UpdateListTrainCars.get(i);
			if (lFoundTrainCarToUnhook) {
				// Remove all following cars from our train
				carsAttachedToTrain.remove(lTrainCar);
				lTrainCar.train = null;
			}

			else if (lTrainCar.uid == pTrainCar.uid) {
				lFoundTrainCarToUnhook = true;
				// Update the axles
				// TODO:

				// Find the hitch which connects this car to the lPrevTrainCar
				TrainHitch lFoundTrainHitch = lTrainCar.frontHitch;
				if (lFoundTrainHitch.connectedTo == null) {
					// hitch not connected to anything, then it must be the other hitch on this car
					lFoundTrainHitch = lTrainCar.rearHitch;
				} else {
					if (lTrainCar.rearHitch.connectedTo == null) {
					} else {
						// could be either hitch
						if (lFoundTrainHitch.connectedTo.parentCar == lPrevTrainCar) {
						} else
							lFoundTrainHitch = lTrainCar.rearHitch;
					}
				}
				final var lOtherHitch = lFoundTrainHitch.connectedTo;
				lFoundTrainHitch.isOpen = true;
				lFoundTrainHitch.connectedTo = null;

				lOtherHitch.isOpen = true;
				lOtherHitch.connectedTo = null;

				carsAttachedToTrain.remove(lTrainCar);
				lTrainCar.train = null;

				// Return the 'lead' car of the new train
				pReturnCar = lTrainCar;
				lastCar = lPrevTrainCar;

			}
			lPrevTrainCar = lTrainCar;

		}
		// TODO: Update 'TrainCar' numbers

		return pReturnCar;
	}

	public void cleanUp() {
		clearFollowPath();

		leadCar = null;
		mTrainNumber = -1;
		trainPathNodeTracker = -1;

		final int lNumCarts = carsAttachedToTrain.size();
		for (int i = 0; i < lNumCarts; i++) {
			carsAttachedToTrain.get(i).unassign();

		}
		carsAttachedToTrain.clear();
	}

	// Pathing -------------------------------------

	// Changes the driving direction of all cars attached to the train
	public void handleChangeDrivingDirection(RailTrackInstance pTrack) {
		drivingForward(getSpeed() > 0.f);

		// 'Rebuild' the follow map using the last car
		clearFollowPath();

		// Just need to update the destination nodes of the axles, as their 'current positions' wont have changed
		rebuildFollowSegmentList(pTrack, !drivingForward() ? leadCar.frontAxle : lastCar.getAxleOnFreeHitch(), true);
	}

	public void handleChangeDrivingDirection(RailTrackInstance pTrack, boolean forwards) {
		mDrivingForward = forwards;

		// 'Rebuild' the follow map using the last car
		clearFollowPath();

		// Just need to update the destination nodes of the axles, as their 'current positions' wont have changed
		rebuildFollowSegmentList(pTrack, !drivingForward() ? leadCar.frontAxle : lastCar.getAxleOnFreeHitch(), true);
	}

	public void reorientateTrainCarsToLocomotive(RailTrackInstance pTrack) {
		// 'Rebuild' the follow map using the last car
		clearFollowPath();

		int lCurSegmentUid = -1;
		int lCurDestNodeUid = -1;

		// reorientate the train based on the lead car (constant)
		TrainHitch lCurrentHitch = leadCar.frontHitch;
		while (lCurrentHitch != null && lCurrentHitch.parentCar != null) {
			final var lParentCar = lCurrentHitch.parentCar;
			final var lMatchingAxle = lCurrentHitch == lParentCar.frontHitch ? lParentCar.frontAxle : lParentCar.rearAxle;

			if (lCurSegmentUid == -1)
				lCurSegmentUid = lMatchingAxle.currentSegment.uid;
			if (lCurDestNodeUid == -1)
				lCurDestNodeUid = lMatchingAxle.destinationNodeUid;
			if (lMatchingAxle.currentSegment.uid == lCurSegmentUid) {
				if (lCurDestNodeUid != lMatchingAxle.destinationNodeUid) {
					// Flip node
					lMatchingAxle.destinationNodeUid = lMatchingAxle.currentSegment.getOtherNodeUid(lMatchingAxle.destinationNodeUid);
					lMatchingAxle.normalizedDistanceAlongSegment = 1.0f - lMatchingAxle.normalizedDistanceAlongSegment;
				}
			} else {
				if (lCurDestNodeUid == lMatchingAxle.destinationNodeUid) {
					// Flip node
					lMatchingAxle.destinationNodeUid = lMatchingAxle.currentSegment.getOtherNodeUid(lMatchingAxle.destinationNodeUid);
					lMatchingAxle.normalizedDistanceAlongSegment = 1.0f - lMatchingAxle.normalizedDistanceAlongSegment;
				}
			}
			// ----

			final var lOtherAxle = lParentCar.getOtherAxle(lMatchingAxle);
			if (lOtherAxle.currentSegment.uid == lCurSegmentUid) {
				if (lCurDestNodeUid != lOtherAxle.destinationNodeUid) {
					// Flip node
					lOtherAxle.destinationNodeUid = lOtherAxle.currentSegment.getOtherNodeUid(lOtherAxle.destinationNodeUid);
					lOtherAxle.normalizedDistanceAlongSegment = 1.0f - lOtherAxle.normalizedDistanceAlongSegment;
				}
			} else {
				if (lCurDestNodeUid == lOtherAxle.destinationNodeUid) {
					// Flip node
					lOtherAxle.destinationNodeUid = lOtherAxle.currentSegment.getOtherNodeUid(lOtherAxle.destinationNodeUid);
					lOtherAxle.normalizedDistanceAlongSegment = 1.0f - lOtherAxle.normalizedDistanceAlongSegment;
				}
			}
			lCurSegmentUid = lOtherAxle.currentSegment.uid;
			lCurDestNodeUid = lOtherAxle.destinationNodeUid;

			// Update for next iteration ---
			lCurrentHitch = lParentCar.getOtherHitch(lCurrentHitch).connectedTo;

		}
		// 2. Second pass - rebuilds follow segment list from back to front
		rebuildFollowSegmentList(pTrack, !drivingForward() ? leadCar.frontAxle : lastCar.getAxleOnFreeHitch(), false);
	}

	private void rebuildFollowSegmentList(RailTrackInstance pTrack, TrainAxle pLeadingAxle, boolean pFLipDirection) {
		RailTrackSegment lLastSegmentAddedToFollowList = null;

		var lCurrentAxle = pLeadingAxle;

		var lCurrentTrainCar = pLeadingAxle.parentTrainCar;
		while (lCurrentAxle != null) {
			if (pFLipDirection) {
				final var lFlippedDestinationNodeUid = lCurrentAxle.currentSegment.getOtherNodeUid(lCurrentAxle.destinationNodeUid);
				lCurrentAxle.destinationNodeUid = lFlippedDestinationNodeUid;
				lCurrentAxle.normalizedDistanceAlongSegment = 1.0f - lCurrentAxle.normalizedDistanceAlongSegment;
				lCurrentAxle.overshootDistanceInMeters = 0.f;

				lCurrentAxle.nextFollowSegment.targetNodeUid = lFlippedDestinationNodeUid;
				lCurrentAxle.nextFollowSegment.logicalCounter = 0;

			}
			if (lLastSegmentAddedToFollowList == null || lCurrentAxle.currentSegment.uid != lLastSegmentAddedToFollowList.uid) {
				// Need to get a list of all nodes between the last registered node and the current one
				if (lLastSegmentAddedToFollowList != null) {
					final var lCommonNodeUid = RailTrackSegment.getCommonNodeUid(lLastSegmentAddedToFollowList, lCurrentAxle.currentSegment);
					if (lCommonNodeUid == -1) {
						final var lLastFollowSegment = getLastFollowSegment();
						final var lSourceNodeUid = lCurrentAxle.currentSegment.getOtherNodeUid(lCurrentAxle.destinationNodeUid);

						final var lMissingSegment = pTrack.getSegmentBetweenNodes(lSourceNodeUid, lLastFollowSegment.targetNodeUid);
						if (lMissingSegment != null) {
							addFollowSegment(lMissingSegment, lSourceNodeUid);
						}
					}
				}
				addFollowSegment(lCurrentAxle.currentSegment, lCurrentAxle.destinationNodeUid);
				lLastSegmentAddedToFollowList = lCurrentAxle.currentSegment;

			}
			final var lOtherAxle = lCurrentTrainCar.getOtherAxle(lCurrentAxle);
			if (pFLipDirection) {
				final var lFlippedDestinationNodeUid = lOtherAxle.currentSegment.getOtherNodeUid(lOtherAxle.destinationNodeUid);
				lOtherAxle.destinationNodeUid = lFlippedDestinationNodeUid;
				lOtherAxle.normalizedDistanceAlongSegment = 1.0f - lOtherAxle.normalizedDistanceAlongSegment;
				lOtherAxle.overshootDistanceInMeters = 0.f;

				lOtherAxle.nextFollowSegment.targetNodeUid = lFlippedDestinationNodeUid;
				lOtherAxle.nextFollowSegment.logicalCounter = 0;
			}
			if (lLastSegmentAddedToFollowList == null || lOtherAxle.currentSegment.uid != lLastSegmentAddedToFollowList.uid) {
				// Need to get a list of all nodes between the last registered node and the current one
				if (lLastSegmentAddedToFollowList != null) {
					final var lCommonNodeUid = RailTrackSegment.getCommonNodeUid(lLastSegmentAddedToFollowList, lOtherAxle.currentSegment);
					if (lCommonNodeUid == -1) {
						final var lLastFollowSegment = getLastFollowSegment();
						final var lSourceNodeUid = lOtherAxle.currentSegment.getOtherNodeUid(lOtherAxle.destinationNodeUid);

						final var lMissingSegment = pTrack.getSegmentBetweenNodes(lSourceNodeUid, lLastFollowSegment.targetNodeUid);
						if (lMissingSegment != null) {
							addFollowSegment(lMissingSegment, lSourceNodeUid);

						}
					}
				}
				addFollowSegment(lOtherAxle.currentSegment, lOtherAxle.destinationNodeUid);
				lLastSegmentAddedToFollowList = lOtherAxle.currentSegment;

			}
			TrainHitch lCurrentHitch = lCurrentTrainCar.getMatchingHitch(lOtherAxle);
			TrainHitch lNextCarHitch = lCurrentHitch.connectedTo;
			if (lNextCarHitch == null)
				break;

			lCurrentTrainCar = lNextCarHitch.parentCar;
			lCurrentAxle = lCurrentTrainCar.getMatchingAxle(lNextCarHitch);

		}
		if (OUTPUT_TRACK_FOLLOW_SEGMENT_INFO)
			printTrainFollowSegmentDebugInfo();
	}

	private void resetAxleTrackerLogicalCounters() {
		final int lNumCars = getNumberOfCarsInTrain();
		for (int i = 0; i < lNumCars; i++) {
			final var lTrainCar = getCarByIndex(i);

			lTrainCar.frontAxle.nextFollowSegment.logicalCounter = -1;
			lTrainCar.rearAxle.nextFollowSegment.logicalCounter = -1;

		}
	}

	// Reorientates all the cars so they are consistent with the main locomotive
	public void reorientateTrainCarsToLocomotiveOld(RailTrackInstance pTrack) {
		// 'Rebuild' the follow map using the last car
		clearFollowPath();
		RailTrackSegment lLastSegmentAddedToFollowList = null;

		final int lNumberCarsInTrain = getNumberOfCarsInTrain();

		int lCurSegmentUid = -1;
		int lCurDestNodeUid = -1;

		// 1. First pass - aligns all car directions to match the main locomotive
		for (int i = 0; i < lNumberCarsInTrain; i++) {
			final int lTrainCarUid = i;
			final var lTrainCar = getCarByIndex(lTrainCarUid);

			final var lFrontAxle = lTrainCar.frontAxle;

			if (lCurSegmentUid == -1)
				lCurSegmentUid = lFrontAxle.currentSegment.uid;
			if (lCurDestNodeUid == -1)
				lCurDestNodeUid = lFrontAxle.destinationNodeUid;
			if (lFrontAxle.currentSegment.uid == lCurSegmentUid) {
				if (lCurDestNodeUid != lFrontAxle.destinationNodeUid) {
					// Flip node
					lFrontAxle.destinationNodeUid = lFrontAxle.currentSegment.getOtherNodeUid(lFrontAxle.destinationNodeUid);
					lFrontAxle.normalizedDistanceAlongSegment = 1.0f - lFrontAxle.normalizedDistanceAlongSegment;
				}
			} else {
				if (lCurDestNodeUid == lFrontAxle.destinationNodeUid) {
					// Flip node
					lFrontAxle.destinationNodeUid = lFrontAxle.currentSegment.getOtherNodeUid(lFrontAxle.destinationNodeUid);
					lFrontAxle.normalizedDistanceAlongSegment = 1.0f - lFrontAxle.normalizedDistanceAlongSegment;
				}
			}
			lCurSegmentUid = lFrontAxle.currentSegment.uid;
			lCurDestNodeUid = lFrontAxle.destinationNodeUid;

			// ----

			final var lRearAxle = lTrainCar.rearAxle;
			if (lRearAxle.currentSegment.uid == lCurSegmentUid) {
				if (lCurDestNodeUid != lRearAxle.destinationNodeUid) {
					// Flip node
					lRearAxle.destinationNodeUid = lRearAxle.currentSegment.getOtherNodeUid(lRearAxle.destinationNodeUid);
					lRearAxle.normalizedDistanceAlongSegment = 1.0f - lRearAxle.normalizedDistanceAlongSegment;
				}
			} else {
				if (lCurDestNodeUid == lRearAxle.destinationNodeUid) {
					// Flip node
					lRearAxle.destinationNodeUid = lRearAxle.currentSegment.getOtherNodeUid(lRearAxle.destinationNodeUid);
					lRearAxle.normalizedDistanceAlongSegment = 1.0f - lRearAxle.normalizedDistanceAlongSegment;
				}
			}
			lCurSegmentUid = lRearAxle.currentSegment.uid;
			lCurDestNodeUid = lRearAxle.destinationNodeUid;

		}
		// 2. Second pass - rebuilds follow segment list from back to front

		// Just need to update the destination nodes of the axles, as their 'current positions' wont have changed
		for (int i = 0; i < lNumberCarsInTrain; i++) {
			// Iterating the train cars and NOT the train :(((((((
			final int lTrainCarUid = drivingForward() ? lNumberCarsInTrain - i - 1 : i;
			final var lTrainCar = getCarByIndex(lTrainCarUid);
			{
				// The segment the axle is on doesn't change just between we change direction ...
				final var lCurrentAxle = !drivingForward() ? lTrainCar.frontAxle : lTrainCar.rearAxle;
				if (lLastSegmentAddedToFollowList == null || lCurrentAxle.currentSegment.uid != lLastSegmentAddedToFollowList.uid) {
					// Need to get a list of all nodes between the last registered node and the current one
					if (lLastSegmentAddedToFollowList != null) {
						final var lCommonNodeUid = RailTrackSegment.getCommonNodeUid(lLastSegmentAddedToFollowList, lCurrentAxle.currentSegment);
						if (lCommonNodeUid == -1) {
							final var lLastFollowSegment = getLastFollowSegment();
							final var lSourceNodeUid = lCurrentAxle.currentSegment.getOtherNodeUid(lCurrentAxle.destinationNodeUid);

							final var lMissingSegment = pTrack.getSegmentBetweenNodes(lSourceNodeUid, lLastFollowSegment.targetNodeUid);
							if (lMissingSegment != null) {
								addFollowSegment(lMissingSegment, lSourceNodeUid);

							}
						}
					}
					addFollowSegment(lCurrentAxle.currentSegment, lCurrentAxle.destinationNodeUid);
					lLastSegmentAddedToFollowList = lCurrentAxle.currentSegment;

				}
			}
			{
				// The segment the axle is on doesn't change just between we change direction ...
				final var lCurrentAxle = !drivingForward() ? lTrainCar.rearAxle : lTrainCar.frontAxle;
				if (lLastSegmentAddedToFollowList == null || lCurrentAxle.currentSegment.uid != lLastSegmentAddedToFollowList.uid) {
					// Need to get a list of all nodes between the last registered node and the current one
					if (lLastSegmentAddedToFollowList != null) {
						final var lCommonNodeUid = RailTrackSegment.getCommonNodeUid(lLastSegmentAddedToFollowList, lCurrentAxle.currentSegment);
						if (lCommonNodeUid == -1) {
							final var lLastFollowSegment = getLastFollowSegment();
							final var lSourceNodeUid = lCurrentAxle.currentSegment.getOtherNodeUid(lCurrentAxle.destinationNodeUid);

							final var lMissingSegment = pTrack.getSegmentBetweenNodes(lSourceNodeUid, lLastFollowSegment.targetNodeUid);
							if (lMissingSegment != null) {
								addFollowSegment(lMissingSegment, lSourceNodeUid);

							}
						}
					}
					addFollowSegment(lCurrentAxle.currentSegment, lCurrentAxle.destinationNodeUid);
					lLastSegmentAddedToFollowList = lCurrentAxle.currentSegment;

				}
			}
		}
	}

	public void clearFollowPath() {
		resetAxleTrackerLogicalCounters();

		trackSegmentFollowList.clear();
		trainPathNodeTracker = 0;
		if (OUTPUT_TRACK_FOLLOW_SEGMENT_INFO) {
			System.out.println("Train Follow Segment List Cleared");
			System.out.println("====================================");

		}
	}

	public void removeFollowSegments(final TrainFollowSegment pAxleTicker) {
		if (trackSegmentFollowList.size() == 0)
			return;

		if (OUTPUT_TRACK_FOLLOW_SEGMENT_INFO) {
			System.out.println("Train FollowSegment List Removed older than " + pAxleTicker.logicalCounter);
		}

		// Remove all the tickers which are older than the last processed here
		while (trackSegmentFollowList.size() > 0 && trackSegmentFollowList.get(0).logicalCounter < pAxleTicker.logicalCounter) {
			trackSegmentFollowList.remove(0);
		}
	}

	public TrainFollowSegment addFollowSegment(RailTrackSegment segment, int destNodeUid) {
		// Sets the next segment and destination node that this train will follow
		final var lNewAxleTicker = new TrainFollowSegment(segment, destNodeUid, trainPathNodeTracker++);

		if (OUTPUT_TRACK_FOLLOW_SEGMENT_INFO)
			System.out.println("Added new follow segment e:" + +segment.uid + "  n:" + destNodeUid);

		trackSegmentFollowList.add(lNewAxleTicker);
		return lNewAxleTicker;
	}

	public TrainFollowSegment getNextFollowSegment(TrainFollowSegment currentSegment) {
		final int lNodeListSize = trackSegmentFollowList.size();
		for (int i = 0; i < lNodeListSize; i++) {
			if (trackSegmentFollowList.get(i).Segment.uid == currentSegment.Segment.uid && trackSegmentFollowList.get(i).logicalCounter >= currentSegment.logicalCounter) {
				if (i < lNodeListSize - 1)
					return trackSegmentFollowList.get(i + 1);

				return null;

			}
		}
		return null;
	}

	public TrainFollowSegment getLastFollowSegment() {
		if (trackSegmentFollowList.size() > 0)
			return trackSegmentFollowList.get(trackSegmentFollowList.size() - 1);

		return null;
	}

	// Speed Controls ------------------------------

	public void fullBrake() {
		// TODO: implement
	}

	public void brakeAtPosition(float pDistanceToBrakeInInMeters) {
		isBraking = true;
		distanceToNextStop = pDistanceToBrakeInInMeters;
	}

	public void setSignalStop(float pDistUntilNextStop) {
		isSignalBraking = true;
		distanceToNextStop = pDistUntilNextStop;
		isBraking = distanceToNextStop < lDistanceToStartBraking;
	}

	public void clearBrakes() {
	}

	public void setBrakes(float pBrakeAmount) {
		if (pBrakeAmount < 0.f) {
			isBraking = false;
			return;

		}
	}

	public void killSpeed() {
		isBraking = false;
		isSignalBraking = false;
		targetSpeedInMetersPerSecond = 0.f;
		mActualSpeedInMetersPerSecond = 0.f;
		mAcceleration = 0.f;
	}

	// Debug ---------------------------------------

	public void printTrainFollowSegmentDebugInfo() {
		System.out.println("Train Follow Segment List Debug");
		System.out.println("====================================");
		final int lNodeListSize = trackSegmentFollowList.size();
		for (int i = 0; i < lNodeListSize; i++) {
			final var lItem = trackSegmentFollowList.get(i);
			if (lItem == null) {
				System.out.println("Null item in train follow segment list");

			} else {
				System.out.println(i + ":   e:" + lItem.Segment.uid + "  destNode: " + lItem.targetNodeUid);

			}
		}
	}

}
