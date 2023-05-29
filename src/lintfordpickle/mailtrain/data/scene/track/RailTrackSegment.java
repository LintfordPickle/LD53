package lintfordpickle.mailtrain.data.scene.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackSegmentSaveDefinition;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalBlock.SignalState;
import lintfordpickle.mailtrain.data.scene.track.signals.RailTrackSignalSegment;

public class RailTrackSegment extends TrackEdge {

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

			mParentTrackSegment = railTrackInstance.getEdgeByUid(mParentTrackSegmentUid);

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

			// update the parentage ...
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

	public static final int EDGE_TYPE_NONE = -1;
	public static final int EDGE_TYPE_STRAIGHT = 0;
	public static final int EDGE_TYPE_CURVE = 1;

	public static final int EDGE_SPECIAL_TYPE_UNASSIGNED = 0; // nothing
	public static final int EDGE_SPECIAL_TYPE_MAP_SPAWN = 1; // player map spawn (edge needs name)
	public static final int EDGE_SPECIAL_TYPE_MAP_EXIT = 2; // player map exit point (edge needs special name)
	public static final int EDGE_SPECIAL_TYPE_MAP_EDGE = 4; // edge of map (enemy spawn / leave)
	public static final int EDGE_SPECIAL_TYPE_STATION = 8; // station / town for trading
	public static final int EDGE_SPECIAL_TYPE_ENEMY_SPAWN = 16; // station / town for trading

	public static String getEdgeTypeName(int edgeType) {
		switch (edgeType) {
		default:
		case EDGE_TYPE_NONE:
			return "unknown";
		case EDGE_TYPE_STRAIGHT:
			return "straight";
		case EDGE_TYPE_CURVE:
			return "curve";
		}
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public float edgeLengthInMeters;

	public int specialEdgeType = EDGE_SPECIAL_TYPE_UNASSIGNED;
	public int edgeType = EDGE_TYPE_STRAIGHT;

	public float edgeAngle; // tolerence check

	public String segmentName;
	public String specialName;

	public final SegmentSignalsCollection signalsA = new SegmentSignalsCollection(this);
	public final SegmentSignalsCollection signalsB = new SegmentSignalsCollection(this);
	public int logicalUpdateCounter;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void setEdgeBitFlag(int pTypeToSet) {
		specialEdgeType = pTypeToSet;
	}

	public void setEdgeWithType(int pTypeToSet) {
		specialEdgeType |= pTypeToSet;
	}

	public boolean isEdgeOfType(int pTypeToCheck) {
		return (specialEdgeType & pTypeToCheck) == pTypeToCheck;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public RailTrackSegment(RailTrackInstance pTrack, final int pUid, int pNodeAUid, int pNodeBUid, float pEdgeAngle) {
		super(pUid, pNodeAUid, pNodeBUid);

		edgeAngle = pEdgeAngle;

		signalsA.mDestinationUid = nodeAUid;
		signalsA.addSignalSegment(pTrack.trackSignalSegments.getFreePooledItem(), false, 0);

		signalsB.mDestinationUid = nodeBUid;
		signalsB.addSignalSegment(pTrack.trackSignalSegments.getFreePooledItem(), false, 0);
	}

	public RailTrackSegment(RailTrackSegmentSaveDefinition saveDef) {
		super(saveDef.uid, saveDef.nodeAUid, saveDef.nodeBUid);

		nodeAAngle = saveDef.nodeAAngle;
		nodeBAngle = saveDef.nodeBAngle;

		control0X = saveDef.control0X;
		control0Y = saveDef.control0Y;

		control1X = saveDef.control1X;
		control1Y = saveDef.control1Y;

		edgeLengthInMeters = saveDef.edgeLengthInMeters;
		edgeType = saveDef.edgeType;

		signalsA.mSignalSegmentUids.addAll(saveDef.signalsA.signalSegmentUids);
		signalsA.mDestinationUid = saveDef.signalsA.destinationUid;
		signalsB.mSignalSegmentUids.addAll(saveDef.signalsB.signalSegmentUids);
		signalsB.mDestinationUid = saveDef.signalsB.destinationUid;
	}

	// ---------------------------------------------
	// IO-Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackSegmentSaveDefinition saveDef) {
		// TrackEdge variables
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
		edgeLengthInMeters = saveDef.edgeLengthInMeters;
		specialEdgeType = saveDef.specialEdgeType;
		edgeType = saveDef.edgeType;
		edgeAngle = saveDef.edgeAngle;
		segmentName = saveDef.segmentName;
		specialName = saveDef.specialName;
	}

	public void saveIntoDef(RailTrackSegmentSaveDefinition saveDef) {
		// TrackEdge variables
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
		saveDef.edgeLengthInMeters = edgeLengthInMeters;
		saveDef.specialEdgeType = specialEdgeType;
		saveDef.edgeType = edgeType;
		saveDef.edgeAngle = edgeAngle;
		saveDef.segmentName = segmentName;
		saveDef.specialName = specialName;

		saveDef.signalsA.signalSegmentUids.addAll(signalsA.mSignalSegmentUids);
		saveDef.signalsA.destinationUid = signalsA.destinationNodeUid();
		saveDef.signalsB.signalSegmentUids.addAll(signalsB.mSignalSegmentUids);
		saveDef.signalsB.destinationUid = signalsB.destinationNodeUid();
	}

	public void finalizeAfterLoading(RailTrackInstance pTrack) {
		signalsA.mParentTrackSegment = this;
		signalsA.initialize(pTrack);

		signalsB.mParentTrackSegment = this;
		signalsB.initialize(pTrack);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public int getOtherNodeUid(int pNodeUid) {
		if (nodeAUid == pNodeUid) {
			return nodeBUid;

		} else {
			return nodeAUid;

		}
	}

	public static int getCommonNodeUid(RailTrackSegment pEdgeA, RailTrackSegment pEdgeB) {
		if (pEdgeA.nodeAUid == pEdgeB.nodeAUid)
			return pEdgeB.nodeAUid;
		else if (pEdgeA.nodeBUid == pEdgeB.nodeBUid)
			return pEdgeB.nodeBUid;
		else if (pEdgeA.nodeAUid == pEdgeB.nodeBUid)
			return pEdgeB.nodeBUid;
		else if (pEdgeA.nodeBUid == pEdgeB.nodeAUid)
			return pEdgeB.nodeAUid;
		return -1;
	}

	// ---------------------------------------------

	public void setBothSignalStates(int pDestinationNodeUid, float pDistIntoSegment, SignalState pNewState) {
		final var lSignalSetA = signalsA.getSignal(pDestinationNodeUid == signalsA.mDestinationUid ? pDistIntoSegment : 1.f - pDistIntoSegment);
		if (lSignalSetA != null && lSignalSetA.signalBlock != null) {
			lSignalSetA.signalBlock.signalState(pNewState);

		}
		final var lSignalSetB = signalsB.getSignal(pDestinationNodeUid == signalsB.mDestinationUid ? pDistIntoSegment : 1.f - pDistIntoSegment);
		if (lSignalSetB != null && lSignalSetB.signalBlock != null) {
			lSignalSetB.signalBlock.signalState(pNewState);

		}
	}

	public SegmentSignalsCollection getSignalsList(int pDestNodeUid) {
		if (signalsA.mDestinationUid == pDestNodeUid)
			return signalsA;
		if (signalsB.mDestinationUid == pDestNodeUid)
			return signalsB;
		return null;
	}

	public void addTrackSignal(RailTrackInstance pTrack, float lDist, int pDestNodeUid) {
		final var lSegmentSignalCollection = getSignalsList(pDestNodeUid);
		if (lSegmentSignalCollection == null)
			return;

		lSegmentSignalCollection.addSignalSegment(pTrack.trackSignalSegments.getFreePooledItem(), true, lDist);
	}
}
