package lintfordpickle.mailtrain.data.scene.trains;

import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;

public class TrainAxle {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrainCar parentTrainCar;
	public final TrainFollowSegment nextFollowSegment = new TrainFollowSegment();

	public RailTrackSegment currentSegment;
	public int destinationNodeUid = -1; // going to
	public float normalizedDistanceAlongSegment; // distance into
	public float overshootDistanceInMeters;

	// World position of this axle. This is derived from the current position on the track (node + segment + distance into segment).
	public float worldPositionX;
	public float worldPositionY;

	private float mTotalDistanceInMetersTravelled;
	private float mDistanceInMetersTravelledLastTick;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void increaseDistanceTravelled(float pAmt) {
		mTotalDistanceInMetersTravelled += Math.abs(pAmt);

	}

	public float distanceTravelledInMetersLastTick() {
		return mDistanceInMetersTravelledLastTick;
	}

	public float totalDistanceTravelledInMeters() {
		return mTotalDistanceInMetersTravelled;
	}

	public boolean hasArrived() {
		return normalizedDistanceAlongSegment < 0.0f || normalizedDistanceAlongSegment >= 1.f;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainAxle(TrainCar pParentTrainCar) {
		parentTrainCar = pParentTrainCar;

		reset();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	/** Returns true if end of track reached (without applying any movement) */
	public boolean driveAxleForward(RailTrackInstance pTrack, float pDriveDistInMeters) {
		if (currentSegment == null)
			return true;

		// Always progressing from 0 -> 1

		final float lThisSegmentLength = currentSegment.segmentLengthInMeters;
		final float lThisSegmentUnit = (1.f / lThisSegmentLength);

		// normalize the distance in meters into segment space
		final float lHowLongToDriveThisSegment = (pDriveDistInMeters + overshootDistanceInMeters) * lThisSegmentUnit;
		overshootDistanceInMeters = 0.f;

		final float lAmountRemaining = 1.0f - normalizedDistanceAlongSegment;
		final float lAmtDriveThisSegment = (float) Math.min(lAmountRemaining, lHowLongToDriveThisSegment);

		if (lThisSegmentUnit > 0.f) {
			increaseDistanceTravelled(lAmtDriveThisSegment / lThisSegmentUnit);
			mDistanceInMetersTravelledLastTick = lAmtDriveThisSegment / lThisSegmentUnit;
		}

		final var lDestNode = pTrack.getNodeByUid(destinationNodeUid);

		// Would the next update push us over the segment?
		if (lDestNode.getIsEndNode() && lAmountRemaining < lHowLongToDriveThisSegment) {
			normalizedDistanceAlongSegment = 1.f;
			return true;
		}

		normalizedDistanceAlongSegment += lAmtDriveThisSegment;
		if (normalizedDistanceAlongSegment > 1.f)
			return true;

		// If we overshoot this tick, then carry over the adjustment amount to the next tick/segment
		float restAmt = pDriveDistInMeters - (lAmtDriveThisSegment / lThisSegmentUnit);
		if (lAmountRemaining > 0.f && lAmountRemaining < lHowLongToDriveThisSegment)
			overshootDistanceInMeters = restAmt;

		return false;

	}

	public void reset() {
		nextFollowSegment.Segment = null;
		nextFollowSegment.logicalCounter = -1;

		currentSegment = null;
		destinationNodeUid = -1;

		normalizedDistanceAlongSegment = 0.f;
	}
}
