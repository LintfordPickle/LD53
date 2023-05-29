package lintfordpickle.mailtrain.data.scene.track;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.scene.track.savedefinition.TrackSwitchSaveDefinition;
import net.lintford.library.core.maths.RandomNumbers;

/**
 * {@link TrackSwitch}s sit on top of {@link RailTrackNode}s. Each switch defines a main segment and between [0,3] auxiliary segments between which can be switched.
 * */
public class TrackSwitch {

	// ---------------------------------------------
	// Constants / Statics
	// ---------------------------------------------

	private static final List<RailTrackSegment> SEGMENT_UPDATE_LIST = new ArrayList<>();

	public static final int NO_SEGMENT = -1;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final RailTrackNode parentNode;
	public final int parentNodeUid;

	private int mMainSegmentLocalIndex;
	private int mActiveAuxiliarySegmentIndex;

	/**
	 * All edges connected to this {@link TrackSwitch} Just because an edge is connected to a switch, doesn't mean the edge can be traversed from that node.
	 */
	private transient List<RailTrackSegment> connectedSegments;
	private final List<Integer> connectedSegmentUids;

	// Clickable part
	public float signalBoxWorldX = 0.f;
	public float signalBoxWorldY = 0.f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public List<RailTrackSegment> connectedSegments() {
		return connectedSegments;
	}

	public List<Integer> connectedSegmentUids() {
		return connectedSegmentUids;
	}

	public RailTrackSegment getConnectedSegmentByIndex(int edgeListIndex) {
		return connectedSegments.get(edgeListIndex);
	}

	// TODO: Move the whitelist to the switch (as every node has a switch now anyway)
	public RailTrackSegment getRandomWhitelistedSegmentApartFrom(RailTrackSegment currentEdge, int destinationNode) {
		if (connectedSegments == null || connectedSegments.size() == 0)
			return null;

		// final var pEdgeUidWhiteList = currentEdge.allowedEdgeConections;

		SEGMENT_UPDATE_LIST.clear();

		final int lSegmentCount = connectedSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			if (connectedSegments.get(i) == null)
				continue;

			final int lUidToCheck = connectedSegments.get(i).uid;
			if (lUidToCheck != currentEdge.uid /* && pEdgeUidWhiteList.contains(lUidToCheck) */) {
				// Mark allowed edges, just not the one we just left
				SEGMENT_UPDATE_LIST.add(connectedSegments.get(i));
			}
		}

		// Now select a random, allowed segment
		if (SEGMENT_UPDATE_LIST.size() == 1)
			return SEGMENT_UPDATE_LIST.get(0);

		final int lUEdgeCount = SEGMENT_UPDATE_LIST.size();

		if (lUEdgeCount == 0) {
			return null;
		}

		final int lRandIndex = RandomNumbers.random(0, lUEdgeCount);

		return SEGMENT_UPDATE_LIST.get(lRandIndex);
	}

	public RailTrackSegment getRandomSegmentApartFrom(RailTrackSegment currentSegment, int destinationNode) {
		if (connectedSegments == null || connectedSegments.size() == 0)
			return null;

		SEGMENT_UPDATE_LIST.clear();

		final int lEdgeCount = connectedSegments.size();
		for (int i = 0; i < lEdgeCount; i++) {
			if (connectedSegments.get(i) == null)
				continue;

			final int lUidToCheck = connectedSegments.get(i).uid;
			if (lUidToCheck != currentSegment.uid) {
				SEGMENT_UPDATE_LIST.add(connectedSegments.get(i));
			}
		}

		// Now select a random, allowed edge
		if (SEGMENT_UPDATE_LIST.size() == 1)
			return SEGMENT_UPDATE_LIST.get(0);

		final int lUEdgeCount = SEGMENT_UPDATE_LIST.size();

		if (lUEdgeCount == 0) {
			return null;
		}

		final int lRandIndex = RandomNumbers.random(0, lUEdgeCount);

		return SEGMENT_UPDATE_LIST.get(lRandIndex);
	}

	public RailTrackSegment getRandomSegment() {
		if (connectedSegments == null || connectedSegments.size() == 0)
			return null;

		final int lEdgeCount = connectedSegments.size();
		final int lRandIndex = RandomNumbers.random(0, lEdgeCount);

		return connectedSegments.get(lRandIndex);

	}

	public void removeSegmentByUid(int edgeUid) {
		SEGMENT_UPDATE_LIST.clear();
		final int lSegmentCount = connectedSegments.size();
		for (int i = 0; i < lSegmentCount; i++) {
			SEGMENT_UPDATE_LIST.add(connectedSegments.get(i));
		}

		for (int i = 0; i < lSegmentCount; i++) {
			final var lSegment = SEGMENT_UPDATE_LIST.get(i);
			if (lSegment != null && lSegment.uid == edgeUid) {
				connectedSegments.remove(lSegment);
			}
		}

		if (connectedSegmentUids.contains((Integer) edgeUid)) {
			connectedSegmentUids.remove((Integer) edgeUid);
		}
	}

	public boolean getIsEndNode() {
		return numberConnectedSegments() == 1;
	}

	public RailTrackSegment getConnectedSegmentByUid(int edgeUid) {
		final int lEdgeCount = connectedSegments.size();
		for (int i = 0; i < lEdgeCount; i++) {
			if (connectedSegments.get(i) == null)
				continue;

			if (connectedSegments.get(i).uid == edgeUid) {
				return connectedSegments.get(i);
			}
		}

		return null;
	}

	public void addSegmentToSwitch(RailTrackSegment edge) {
		if (!connectedSegments.contains(edge))
			connectedSegments.add(edge);

		if (!connectedSegmentUids.contains(edge.uid))
			connectedSegmentUids.add(edge.uid);

		if (connectedSegments.size() == 1) {
			mMainSegmentLocalIndex = 0;
		}

		setActiveAuxiliarySegment();
	}

	// Sets the active auxiliary segment index to the first available (non-mainline) segment in the connected segments list.
	private void setActiveAuxiliarySegment() {
		if (numberConnectedSegments() < 2) {
			mActiveAuxiliarySegmentIndex = NO_SEGMENT;
			return;
		}

		final int lNumEdges = connectedSegments.size();
		for (int i = 0; i < lNumEdges; i++) {
			if (i == mMainSegmentLocalIndex)
				continue;

			mActiveAuxiliarySegmentIndex = i;
			return;
		}
	}

	// ---

	public int connectedSegmentUidFromIndex(int index) {
		if (index < 0 || index >= connectedSegments.size())
			return NO_SEGMENT;

		return connectedSegmentUids.get(index);
	}

	public int mainSegmentUid() {
		if (mMainSegmentLocalIndex < 0 || mMainSegmentLocalIndex >= connectedSegmentUids.size())
			return NO_SEGMENT;

		return connectedSegmentUids.get(mMainSegmentLocalIndex);
	}

	public int activeAuxiliarySegmentUid() {
		if (mActiveAuxiliarySegmentIndex < 0 || mActiveAuxiliarySegmentIndex >= connectedSegmentUids.size())
			return NO_SEGMENT;

		return connectedSegmentUids.get(mActiveAuxiliarySegmentIndex);
	}

	public int numberConnectedSegments() {
		return connectedSegments.size();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackSwitch(RailTrackNode parentNode) {
		this.parentNode = parentNode;
		this.parentNodeUid = parentNode.uid;

		mActiveAuxiliarySegmentIndex = NO_SEGMENT;

		connectedSegments = new ArrayList<>();
		connectedSegmentUids = new ArrayList<>();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	/** Cycles the switch forwards through the allowed auxiliary segments.*/
	public void cycleSwitchMainSegmentForward() {
		if (connectedSegments.size() < 2) {
			mMainSegmentLocalIndex = 0;

			setActiveAuxiliarySegment();

			return;
		}

		mMainSegmentLocalIndex++;
		if (mMainSegmentLocalIndex > connectedSegments.size() - 1) {
			mMainSegmentLocalIndex = 0;
		}

		setActiveAuxiliarySegment();
	}

	public void cycleSwitchAuxSegmentsForward() {
		if (connectedSegments.size() <= 2)
			return;

		if (connectedSegments.size() == 2)
			return; // current active is the only auxiliary segment

		final int lNumSegments = connectedSegments.size();
		for (int i = 0; i < lNumSegments; i++) {
			mActiveAuxiliarySegmentIndex++;

			if (mActiveAuxiliarySegmentIndex >= lNumSegments)
				mActiveAuxiliarySegmentIndex = 0;

			if (mActiveAuxiliarySegmentIndex != mMainSegmentLocalIndex)
				return; // equals take it
		}
	}

	/** Cycles the switch backwards through the allowed auxiliary segments. */
	public void cycleSwitchAuxSegmentBackwards() {
		if (connectedSegments.size() <= 2)
			return;

		if (connectedSegments.size() == 2)
			return; // current active is the only auxiliary segment

		final int lNumSegments = connectedSegments.size();
		for (int i = 0; i < lNumSegments; i++) {
			mActiveAuxiliarySegmentIndex--;

			if (mActiveAuxiliarySegmentIndex < 0)
				mActiveAuxiliarySegmentIndex = connectedSegments.size() - 1;

			if (mActiveAuxiliarySegmentIndex != mMainSegmentLocalIndex)
				return; // equals take it
		}

	}

	public void init(int mainSegmentUid) {
		reset();

		if (mainSegmentUid == NO_SEGMENT)
			return;

		mMainSegmentLocalIndex = 0;
		mActiveAuxiliarySegmentIndex = 0;
	}

	public void reset() {
		connectedSegmentUids.clear();

		mMainSegmentLocalIndex = NO_SEGMENT;
		mActiveAuxiliarySegmentIndex = NO_SEGMENT;
	}

	public void loadFromDef(TrackSwitchSaveDefinition saveDef) {
		mMainSegmentLocalIndex = saveDef.mainSegmentLocalIndex;
		mActiveAuxiliarySegmentIndex = saveDef.activeAuxiliarySegmentLocalIndex;

		connectedSegmentUids.addAll(saveDef.connectedEdgeUids);

		signalBoxWorldX = saveDef.boxOffsetX;
		signalBoxWorldY = saveDef.boxOffsetY;
	}

	public void saveIntoDef(TrackSwitchSaveDefinition saveDef) {
		saveDef.mainSegmentLocalIndex = mMainSegmentLocalIndex;
		saveDef.activeAuxiliarySegmentLocalIndex = mActiveAuxiliarySegmentIndex;

		saveDef.connectedEdgeUids.addAll(connectedSegmentUids);

		saveDef.boxOffsetX = signalBoxWorldX;
		saveDef.boxOffsetY = signalBoxWorldY;
	}

	public int getOutSegmentUid(int segmentUid) {

		// TODO: Implement whitelisted segments from in incoming segmentUid 

		// If the segmentUid is the main segment, then take the active segment path
		if (segmentUid == mainSegmentUid())
			return connectedSegmentUidFromIndex(mActiveAuxiliarySegmentIndex);

		// otherwise, auxiliary segments feed back into the main line
		return mainSegmentUid();
	}

	public void finalizeAfterLoading(RailTrackInstance railTrackInstance) {
		if (connectedSegments == null)
			connectedSegments = new ArrayList<>();

		final var lEdgeUidCount = connectedSegmentUids.size();
		for (int i = 0; i < lEdgeUidCount; i++) {
			final var lEdge = railTrackInstance.getEdgeByUid(connectedSegmentUids.get(i));
			if (lEdge != null) {
				if (!connectedSegments.contains(lEdge))
					connectedSegments.add(lEdge);
			} else {
				throw new RuntimeException("Error resolving track edges from Node.ConnectedEdgeUids");
			}
		}
	}

	public int getOtherSegmentConnectionUid(int notThisEdgeUid) {
		final int allowedSegmentCount = connectedSegmentUids.size();
		for (int i = 0; i < allowedSegmentCount; i++) {
			if (connectedSegmentUids.get(i) != notThisEdgeUid)
				return connectedSegmentUids.get(i);
		}

		return NO_SEGMENT;
	}

	public int getOtherSegmentConnectionUids2(int notThisEdgeUid) {
		boolean lFoundOne = false;
		final int allowedEdgeCount = connectedSegmentUids.size();
		for (int i = 0; i < allowedEdgeCount; i++) {
			if (connectedSegmentUids.get(i) != notThisEdgeUid) {
				if (!lFoundOne)
					lFoundOne = true;
				else
					return connectedSegmentUids.get(i);
			}
		}

		return NO_SEGMENT;
	}

	/** Switches are only active if there are at least two auxiliary lines to switch between */
	public boolean isSwitchActive() {
		return connectedSegments.size() > 2;
	}
}
