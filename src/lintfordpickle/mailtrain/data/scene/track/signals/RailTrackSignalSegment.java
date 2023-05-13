package lintfordpickle.mailtrain.data.scene.track.signals;

import lintfordpickle.mailtrain.data.scene.track.RailTrackSegment;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSignalSegmentSaveDefinition;
import net.lintford.library.core.entities.instances.ClosedPooledBaseData;

public class RailTrackSignalSegment extends ClosedPooledBaseData implements Comparable<RailTrackSignalSegment> {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public transient RailTrackSegment trackSegment; // Parent TrackSegment
	public transient RailTrackSignalBlock signalBlock; // Parent signal block

	private int mTrackSegmentUid;
	private int mSignalBlockUid;

	private boolean mIsSignalHead;

	private int mDestinationNodeUid;
	private float mStartDistance;
	private float mLength;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int trackSegmentUid() {
		return mTrackSegmentUid;
	}

	public int signalBlockUid() {
		return mSignalBlockUid;
	}

	/*
	 * The start location of this signal (in node space)
	 * */
	public float startDistance() {
		return mStartDistance;
	}

	/*
	 * Used for tracking the travel direction of the signal segment 
	 */
	public int destinationNodeUid() {
		return mDestinationNodeUid;
	}

	/*
	 * The length of the signal segment. Updated on each signal insertion into the node
	 * */
	public float length() {
		return mLength;
	}

	/*
	 * Is this an actual signal on the track, or then start/end of a TrackSegment
	 */
	public boolean isSignalHead() {
		return mIsSignalHead;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public RailTrackSignalSegment(int pPoolUid) {
		super(pPoolUid);

		mIsSignalHead = false;
	}

	public RailTrackSignalSegment(RailTrackSignalSegmentSaveDefinition saveDef) {
		super(saveDef.uid);

		mDestinationNodeUid = saveDef.destinationNodeUid;
		mLength = saveDef.length;
		mSignalBlockUid = saveDef.signalBlockUid;
		mStartDistance = saveDef.startDistance;
		mTrackSegmentUid = saveDef.trackSegmentUid;

		mIsSignalHead = saveDef.isSignalHead;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void init(RailTrackSegment parentTrackSegment, boolean isSignalHead, float startDistance, int destinationNodeUid) {
		trackSegment = parentTrackSegment;
		mTrackSegmentUid = parentTrackSegment.uid;

		mIsSignalHead = isSignalHead;

		mStartDistance = startDistance;
		mDestinationNodeUid = destinationNodeUid;
	}

	public void updateLength(float length) {
		mLength = length;
	}

	// ---------------------------------------------
	// IO Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackSignalSegmentSaveDefinition saveDef) {

	}

	public void saveIntoDef(RailTrackSignalSegmentSaveDefinition saveDef) {
		saveDef.uid = uid;

		saveDef.trackSegmentUid = mTrackSegmentUid;
		saveDef.signalBlockUid = mSignalBlockUid;

		saveDef.isSignalHead = mIsSignalHead;

		saveDef.destinationNodeUid = mDestinationNodeUid;
		saveDef.startDistance = mStartDistance;
		saveDef.length = mLength;
	}

	// ---------------------------------------------
	// Comparable
	// ---------------------------------------------

	@Override
	public int compareTo(RailTrackSignalSegment o) {
		return mStartDistance < o.mStartDistance ? -1 : 1;
	}
}