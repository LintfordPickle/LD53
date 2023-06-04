package lintfordpickle.mailtrain.data.scene.track;

import lintfordpickle.mailtrain.data.scene.track.RailTrackInstance.RailTrackSignalSegmentManager;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSegmentSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalSegment;

public class RailTrackSegment extends TrackSegment {

	// This is hopefully only temporary, but in order to limit the complexity of the signal blocks, this flag can
	// be set to true to limit each signal collect to a single signal (to be placed at 0.5 distance).
	public static final boolean DEBUG_SINGLE_SIGNAL_PER_SEGMENT = true;

	// |---------- Track Segment --------------|
	// |-SS-||-----Signal Segment -----||--SS--|

	public class SegmentSignalsCollection {

		// This class is just an array of RailTrackSignalSegment, with extra
		// variables to track the indices and assignment/unassignments

		// ---------------------------------------------
		// Variables
		// ---------------------------------------------

		// We can't keep a reference to TrackSignalBlock here, because
		// each SegmentSignals contains multiple TrackSignalSegments, some of which may
		// belong to difference signal blocks.

		private int mParentTrackSegmentUid;
		private transient RailTrackSegment mParentTrackSegment;

		private int mPrimarySignalSegmentUid;
		private transient RailTrackSignalSegment mPrimarySignalSegment;

		private int mAuxiliarySignalSegmentUid;
		private transient RailTrackSignalSegment mAuxiliarySignalSegment;

		private int mLogicalUpdateCounter;
		private int mDestinationUid;

		// ---------------------------------------------
		// Properties
		// ---------------------------------------------

		public RailTrackSignalSegment primarySignalSegment() {
			return mPrimarySignalSegment;
		}

		public RailTrackSignalSegment auxiliarySignalSegment() {
			return mAuxiliarySignalSegment;
		}

		public void logicalUpdateCounter(int newCounterState) {
			mLogicalUpdateCounter = newCounterState;
		}

		public int logicalUpdateCounter() {
			return mLogicalUpdateCounter;
		}

		public int destinationNodeUid() {
			return mDestinationUid;
		}

		public boolean isAuxiliarySignalSegmentActive() {
			return mPrimarySignalSegment.length() > 0.f && mPrimarySignalSegment.length() < 1.f;
		}

		// ---------------------------------------------
		// Constructor
		// ---------------------------------------------

		public SegmentSignalsCollection(RailTrackSegment parentTrackSegment) {
			mParentTrackSegmentUid = parentTrackSegment.uid;
			mParentTrackSegment = parentTrackSegment;
		}

		// ---------------------------------------------
		// Core-Methods
		// ---------------------------------------------

		

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		public void setSignalAtDistance(float distanceIntoSegment) {
			if (distanceIntoSegment < 0 || distanceIntoSegment > 1)
				return;

			mPrimarySignalSegment.updateSignalSegmentLength(distanceIntoSegment);
			updateSignalLengths();
		}

		public void createSignalSegments(RailTrackSignalSegmentManager sigSegmentManager) {
			mPrimarySignalSegment = sigSegmentManager.getFreePooledItem();
			mPrimarySignalSegmentUid = mPrimarySignalSegment.uid;
			mPrimarySignalSegment.init(mParentTrackSegment, false, 0, mDestinationUid);

			mAuxiliarySignalSegment = sigSegmentManager.getFreePooledItem();
			mAuxiliarySignalSegmentUid = mAuxiliarySignalSegment.uid;
			mAuxiliarySignalSegment.init(mParentTrackSegment, true, 1, mDestinationUid);

			updateSignalLengths();
		}

		public void updateSignalLengths() {
			mAuxiliarySignalSegment.updateStartDistance(mPrimarySignalSegment.length());
			mAuxiliarySignalSegment.updateSignalSegmentLength(1.f - mPrimarySignalSegment.length());
		}

		public void reset() {
			mDestinationUid = RailTrackNode.NO_NODE_UID;
			mPrimarySignalSegmentUid = -1;
			mAuxiliarySignalSegmentUid = -1;

			mPrimarySignalSegment = null;
			mAuxiliarySignalSegment = null;

			mLogicalUpdateCounter = 0;
		}

		public RailTrackSignalSegment getSignal(float distIntoSegment) {
			if (distIntoSegment < mPrimarySignalSegment.length())
				return mPrimarySignalSegment;

			return mAuxiliarySignalSegment;
		}

		public RailTrackSignalSegment getNextSignal(RailTrackSignalSegment currentSignalSegment) {
			if (currentSignalSegment == mPrimarySignalSegment)
				return mAuxiliarySignalSegment;

			return null;
		}
		
		public void finalizeAfterLoading(RailTrackInstance railTrackInstance) {
			mParentTrackSegment = railTrackInstance.getSegmentByUid(mParentTrackSegmentUid);

			mPrimarySignalSegment = railTrackInstance.trackSignalSegments.getInstanceByUid(mPrimarySignalSegmentUid);
			if(mPrimarySignalSegment != null) 
				mPrimarySignalSegment.finalizeAfterLoading(railTrackInstance);
			
			mAuxiliarySignalSegment = railTrackInstance.trackSignalSegments.getInstanceByUid(mAuxiliarySignalSegmentUid);
			if(mAuxiliarySignalSegment != null) 
				mAuxiliarySignalSegment.finalizeAfterLoading(railTrackInstance);
			
			if(mPrimarySignalSegmentUid == mAuxiliarySignalSegmentUid)
				throw new RuntimeException("Corrupt save file");
			
		}
	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final int NO_SEGMENT = -1;

	public static final int SEGMENT_TYPE_NONE = -1;
	public static final int SEGMENT_TYPE_STRAIGHT = 0;
	public static final int SEGMENT_TYPE_CURVE = 1;

	public static final int SEGMENT_SPECIAL_TYPE_UNASSIGNED = 0; // nothing
	public static final int SEGMENT_SPECIAL_TYPE_MAP_SPAWN = 1; // player map spawn (Segment needs name)
	public static final int SEGMENT_SPECIAL_TYPE_MAP_EXIT = 2; // player map exit point (edge needs special name)
	public static final int SEGMENT_SPECIAL_TYPE_MAP_EDGE = 4; // edge of map (enemy spawn / leave)
	public static final int SEGMENT_SPECIAL_TYPE_STATION = 8; // station / town for trading
	public static final int SEGMENT_SPECIAL_TYPE_ENEMY_SPAWN = 16; // station / town for trading

	public static String getSegmentTypeName(int segmentType) {
		switch (segmentType) {
		default:
		case SEGMENT_TYPE_NONE:
			return "unknown";
		case SEGMENT_TYPE_STRAIGHT:
			return "straight";
		case SEGMENT_TYPE_CURVE:
			return "curve";
		}
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public float segmentLengthInMeters;

	public int specialSegmentType = SEGMENT_SPECIAL_TYPE_UNASSIGNED;
	public int segmentType = SEGMENT_TYPE_STRAIGHT;

	public float segmentAngle; // tolerence check

	public String segmentName;
	public String specialName;

	public final SegmentSignalsCollection signalsA = new SegmentSignalsCollection(this);
	public final SegmentSignalsCollection signalsB = new SegmentSignalsCollection(this);
	public int logicalUpdateCounter;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void setSegmentBitFlag(int typeToSet) {
		specialSegmentType = typeToSet;
	}

	public void setSegmentWithType(int typeToSet) {
		specialSegmentType |= typeToSet;
	}

	public boolean isSegmentOfType(int typeToCheck) {
		return (specialSegmentType & typeToCheck) == typeToCheck;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public RailTrackSegment(RailTrackInstance trackInstance, final int segmentUid, int nodeAUid, int nodeBUid, float segmentAngle) {
		super(segmentUid, nodeAUid, nodeBUid);

		this.segmentAngle = segmentAngle;

		signalsA.mDestinationUid = nodeAUid;
		signalsA.createSignalSegments(trackInstance.trackSignalSegments);

		signalsB.mDestinationUid = nodeBUid;
		signalsB.createSignalSegments(trackInstance.trackSignalSegments);
	}

	public RailTrackSegment(RailTrackSegmentSaveDefinition saveDef) {
		super(saveDef.uid, saveDef.nodeAUid, saveDef.nodeBUid);

		nodeAAngle = saveDef.nodeAAngle;
		nodeBAngle = saveDef.nodeBAngle;

		control0X = saveDef.control0X;
		control0Y = saveDef.control0Y;

		control1X = saveDef.control1X;
		control1Y = saveDef.control1Y;

		segmentLengthInMeters = saveDef.segmentLengthInMeters;
		segmentType = saveDef.segmentType;

		signalsA.mPrimarySignalSegmentUid = saveDef.signalsA.primarySignalSegmentUid;
		signalsA.mAuxiliarySignalSegmentUid = saveDef.signalsA.auxiliarySignalSegmentUid;
		signalsA.mDestinationUid = saveDef.signalsA.destinationUid;

		signalsB.mPrimarySignalSegmentUid = saveDef.signalsB.primarySignalSegmentUid;
		signalsB.mAuxiliarySignalSegmentUid = saveDef.signalsB.auxiliarySignalSegmentUid;
		signalsB.mDestinationUid = saveDef.signalsB.destinationUid;
	}

	// ---------------------------------------------
	// IO-Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackSegmentSaveDefinition saveDef) {
		// TrackSegmentvariables
		uid = saveDef.uid;

		nodeAUid = saveDef.nodeAUid;
		nodeAAngle = saveDef.nodeAAngle;

		nodeBUid = saveDef.nodeBUid;
		nodeBAngle = saveDef.nodeBAngle;

		control0X = saveDef.control0X;
		control0Y = saveDef.control0Y;

		control1X = saveDef.control1X;
		control1Y = saveDef.control1Y;

		// RailTrackSegment variables
		segmentLengthInMeters = saveDef.segmentLengthInMeters;
		specialSegmentType = saveDef.specialSegmentType;
		segmentType = saveDef.segmentType;
		segmentAngle = saveDef.segmentAngle;
		segmentName = saveDef.segmentName;
		specialName = saveDef.specialName;
	}

	public void saveIntoDef(RailTrackSegmentSaveDefinition saveDef) {
		// TrackSegment variables
		saveDef.uid = uid;

		saveDef.nodeAUid = nodeAUid;
		saveDef.nodeAAngle = nodeAAngle;

		saveDef.nodeBUid = nodeBUid;
		saveDef.nodeBAngle = nodeBAngle;

		saveDef.control0X = control0X;
		saveDef.control0Y = control0Y;

		saveDef.control1X = control1X;
		saveDef.control1Y = control1Y;

		// RailTrackSegment variables
		saveDef.segmentLengthInMeters = segmentLengthInMeters;
		saveDef.specialSegmentType = specialSegmentType;
		saveDef.segmentType = segmentType;
		saveDef.segmentAngle = segmentAngle;
		saveDef.segmentName = segmentName;
		saveDef.specialName = specialName;

		saveDef.signalsA.primarySignalSegmentUid = signalsA.mPrimarySignalSegmentUid;
		saveDef.signalsA.auxiliarySignalSegmentUid = signalsA.mAuxiliarySignalSegmentUid;
		saveDef.signalsA.destinationUid = signalsA.mDestinationUid;

		saveDef.signalsB.primarySignalSegmentUid = signalsB.mPrimarySignalSegmentUid;
		saveDef.signalsB.auxiliarySignalSegmentUid = signalsB.mAuxiliarySignalSegmentUid;
		saveDef.signalsB.destinationUid = signalsB.mDestinationUid;

	}

	public void finalizeAfterLoading(RailTrackInstance trackInstance) {
		signalsA.mParentTrackSegment = this;
		signalsA.finalizeAfterLoading(trackInstance);

		signalsB.mParentTrackSegment = this;
		signalsB.finalizeAfterLoading(trackInstance);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void reset() {
		signalsA.reset();
		signalsB.reset();

		segmentLengthInMeters = 0;
		segmentAngle = (float) Math.PI * 2.f;
		segmentName = null;
		specialName = null;
	}

	public int getOtherNodeUid(int nodeUid) {
		if (nodeAUid == nodeUid) {
			return nodeBUid;

		} else {
			return nodeAUid;

		}
	}

	public static int getCommonNodeUid(RailTrackSegment segmentdgeA, RailTrackSegment segmentB) {
		if (segmentdgeA.nodeAUid == segmentB.nodeAUid)
			return segmentB.nodeAUid;
		else if (segmentdgeA.nodeBUid == segmentB.nodeBUid)
			return segmentB.nodeBUid;
		else if (segmentdgeA.nodeAUid == segmentB.nodeBUid)
			return segmentB.nodeBUid;
		else if (segmentdgeA.nodeBUid == segmentB.nodeAUid)
			return segmentB.nodeAUid;
		return -1;
	}

	// ---------------------------------------------

	public void setBothSignalStates(int destinationNodeUid, float distIntoSegment, SignalState newState) {
		final var lSignalSetA = signalsA.getSignal(destinationNodeUid == signalsA.mDestinationUid ? distIntoSegment : 1.f - distIntoSegment);
		if (lSignalSetA != null && lSignalSetA.signalBlock != null) {
			lSignalSetA.signalBlock.signalState(newState);

		}
		final var lSignalSetB = signalsB.getSignal(destinationNodeUid == signalsB.mDestinationUid ? distIntoSegment : 1.f - distIntoSegment);
		if (lSignalSetB != null && lSignalSetB.signalBlock != null) {
			lSignalSetB.signalBlock.signalState(newState);

		}
	}

	public SegmentSignalsCollection getSignalsList(int destinationNodeUid) {
		if (signalsA.mDestinationUid == destinationNodeUid)
			return signalsA;
		if (signalsB.mDestinationUid == destinationNodeUid)
			return signalsB;
		return null;
	}

	public void setSegmentSignalAtDistance(float dist, int destinationNodeUid) {
		final var lSegmentSignalCollection = getSignalsList(destinationNodeUid);
		if (lSegmentSignalCollection == null)
			return;

		lSegmentSignalCollection.setSignalAtDistance(dist);
	}
}
