package lintfordpickle.mailtrain.data.scene.trains;

import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance;
import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;

public class TrainAxle {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrainCar parentTrainCar;
	public final TrainFollowEdge nextFollowEdge = new TrainFollowEdge();

	public RailTrackSegment currentEdge; // ref to edge
	public int destinationNodeUid = -1; // going to
	public float normalizedDistanceAlongEdge; // distance into
	public float overshootDistanceInMeters;

	// World position of this axle. This is derived from the current position on the track (node + edge + distance into edge).
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
		return normalizedDistanceAlongEdge < 0.0f || normalizedDistanceAlongEdge >= 1.f;
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
		if (currentEdge == null)
			return true;

		// Always progressing from 0 -> 1

		final float lThisSegmentLength = currentEdge.edgeLengthInMeters;
		final float lThisSegmentUnit = (1.f / lThisSegmentLength);

		// normalize the distance in meters into segment space
		final float lHowLongToDriveThisSegment = (pDriveDistInMeters + overshootDistanceInMeters) * lThisSegmentUnit;
		overshootDistanceInMeters = 0.f;

		final float lAmountRemaining = 1.0f - normalizedDistanceAlongEdge;
		final float lAmtDriveThisSegment = (float) Math.min(lAmountRemaining, lHowLongToDriveThisSegment);

		if (lThisSegmentUnit > 0.f) {
			increaseDistanceTravelled(lAmtDriveThisSegment / lThisSegmentUnit);
			mDistanceInMetersTravelledLastTick = lAmtDriveThisSegment / lThisSegmentUnit;
		}

		final var lDestNode = pTrack.getNodeByUid(destinationNodeUid);

		// Would the next update push us over the edge?
		if (lDestNode.getIsEndNode() && lAmountRemaining < lHowLongToDriveThisSegment) {
			normalizedDistanceAlongEdge = 1.f;
			return true;
		}

		normalizedDistanceAlongEdge += lAmtDriveThisSegment;
		if (normalizedDistanceAlongEdge > 1.f)
			return true;

		// If we overshoot this tick, then carry over the adjustment amount to the next tick/segment
		float restAmt = pDriveDistInMeters - (lAmtDriveThisSegment / lThisSegmentUnit);
		if (lAmountRemaining > 0.f && lAmountRemaining < lHowLongToDriveThisSegment)
			overshootDistanceInMeters = restAmt;

		return false;

	}

	public void reset() {
		nextFollowEdge.edge = null;
		nextFollowEdge.logicalCounter = -1;

		currentEdge = null;
		destinationNodeUid = -1;

		normalizedDistanceAlongEdge = 0.f;
	}
}
