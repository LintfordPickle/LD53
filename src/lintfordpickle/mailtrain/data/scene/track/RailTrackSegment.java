package lintfordpickle.mailtrain.data.scene.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

		private final List<Integer> mSignalSegmentUids = new ArrayList<>();
		private transient List<RailTrackSignalSegment> mSignals;

		private int mLogicalUpdateCounter;
		private int mDestinationUid;

		// ---------------------------------------------
		// Properties
		// ---------------------------------------------

		public int numSignalsInCollection() {
			return mSignals.size();
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

		// ---------------------------------------------
		// Constructor
		// ---------------------------------------------

		public SegmentSignalsCollection(RailTrackSegment parentTrackSegment) {
			mParentTrackSegmentUid = parentTrackSegment.uid;
			mParentTrackSegment = parentTrackSegment;
			mSignals = new ArrayList<>();
		}

		// ---------------------------------------------
		// Core-Methods
		// ---------------------------------------------

		public void initialize(RailTrackInstance railTrackInstance) {
			mSignals = new ArrayList<>();

			mParentTrackSegment = railTrackInstance.getSegmentByUid(mParentTrackSegmentUid);

			// Need to resolve the trackSignalSegment uid references
			final int lNumSignalSegmentReferences = mSignalSegmentUids.size();
			for (int i = 0; i < lNumSignalSegmentReferences; i++) {
				var lNextUid = mSignalSegmentUids.get(i);
				final var lSignalSegment = railTrackInstance.trackSignalSegments.getInstanceByUid(lNextUid);
				lSignalSegment.trackSegment = mParentTrackSegment;

				mSignals.add(lSignalSegment);
			}
		}

		// ---------------------------------------------
		// Methods
		// ---------------------------------------------

		public void addSignalSegment(RailTrackSignalSegment newSignalSegment, boolean isSignalHead, float distIntoSegment) {
			newSignalSegment.init(mParentTrackSegment, isSignalHead, distIntoSegment, mDestinationUid);

			mSignalSegmentUids.add(newSignalSegment.uid);
			mSignals.add(newSignalSegment);

			Collections.sort(mSignals);

			// update the segment lengths
			final int lNumSegments = mSignals.size();
			float lRemainingDist = 1.f;
			for (int i = lNumSegments - 1; i >= 0; i--) {
				final var lSignal = mSignals.get(i);

				float lDist = lRemainingDist - lSignal.startDistance();
				lSignal.updateLength(lDist);
				lRemainingDist = lSignal.startDistance();
			}
		}

		public void reset() {
			mDestinationUid = RailTrackNode.NO_NODE_UID;
			mSignals.clear();

			mLogicalUpdateCounter = 0;
		}

		// ---------------------------------------------

		public RailTrackSignalSegment getSignal(float pDistIntoSegment) {
			final int lNumSignals = mSignals.size();
			if (lNumSignals == 1)
				return mSignals.get(0);

			var lSignalSegment = mSignals.get(0);
			for (int i = 0; i < lNumSignals; i++) {
				final var lSignalToCheck = mSignals.get(i);
				if (lSignalToCheck.startDistance() <= pDistIntoSegment && pDistIntoSegment <= lSignalToCheck.startDistance() + lSignalToCheck.length())
					return lSignalToCheck; // return last valid find

				lSignalSegment = lSignalToCheck;
			}
			return lSignalSegment;
		}

		public RailTrackSignalSegment getNextSignal(RailTrackSignalSegment pSignalSegment) {
			final int lNumSignals = mSignals.size();
			if (lNumSignals == 1)
				return null;

			final float lDistAfter = pSignalSegment.startDistance() + pSignalSegment.length();
			if (lDistAfter > 1)
				return null;
			for (int i = 0; i < lNumSignals; i++) {
				final var lSignal = mSignals.get(i);
				if (lSignal.startDistance() >= lDistAfter) {
					return lSignal;

				}
			}
			return null;
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
		signalsA.addSignalSegment(trackInstance.trackSignalSegments.getFreePooledItem(), false, 0);

		signalsB.mDestinationUid = nodeBUid;
		signalsB.addSignalSegment(trackInstance.trackSignalSegments.getFreePooledItem(), false, 0);
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

		signalsA.mSignalSegmentUids.addAll(saveDef.signalsA.signalSegmentUids);
		signalsA.mDestinationUid = saveDef.signalsA.destinationUid;
		signalsB.mSignalSegmentUids.addAll(saveDef.signalsB.signalSegmentUids);
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

		saveDef.signalsA.signalSegmentUids.addAll(signalsA.mSignalSegmentUids);
		saveDef.signalsA.destinationUid = signalsA.destinationNodeUid();
		saveDef.signalsB.signalSegmentUids.addAll(signalsB.mSignalSegmentUids);
		saveDef.signalsB.destinationUid = signalsB.destinationNodeUid();
	}

	public void finalizeAfterLoading(RailTrackInstance trackInstance) {
		signalsA.mParentTrackSegment = this;
		signalsA.initialize(trackInstance);

		signalsB.mParentTrackSegment = this;
		signalsB.initialize(trackInstance);
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

	public void addTrackSignal(RailTrackInstance trackInstance, float dist, int destinationNodeUid) {
		final var lSegmentSignalCollection = getSignalsList(destinationNodeUid);
		if (lSegmentSignalCollection == null)
			return;

		if (DEBUG_SINGLE_SIGNAL_PER_SEGMENT && lSegmentSignalCollection.numSignalsInCollection() >= 2)
			return;

		lSegmentSignalCollection.addSignalSegment(trackInstance.trackSignalSegments.getFreePooledItem(), true, dist);
	}
}
