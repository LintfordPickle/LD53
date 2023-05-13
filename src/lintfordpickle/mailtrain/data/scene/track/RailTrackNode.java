package lintfordpickle.mailtrain.data.scene.track;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.MapEntity;
import lintfordpickle.mailtrain.data.scene.track.savedefinition.RailTrackNodeSaveDefinition;
import net.lintford.library.core.maths.RandomNumbers;

public class RailTrackNode extends MapEntity {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final List<RailTrackSegment> EDGE_UPDATE_LIST = new ArrayList<>();

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	/**
	 * All edges connected to this node. Just because an edge is connected to a node, doesn't mean the edge can be traversed from that node.
	 */
	private transient List<RailTrackSegment> connectedEdges;
	private final List<Integer> connectedEdgeUids;

	public transient boolean isSelected;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public List<RailTrackSegment> connectedEdges() {
		return connectedEdges;
	}

	public List<Integer> connectedEdgeUids() {
		return connectedEdgeUids;
	}

	public int numberConnectedEdges() {
		return connectedEdges.size();
	}

	public void addEdgeToNode(RailTrackSegment pEdge) {
		if (!connectedEdges.contains(pEdge)) {
			connectedEdges.add(pEdge);
			connectedEdgeUids.add(pEdge.uid);
		}
	}

	public RailTrackSegment getEdgeByIndex(int pEdgeListIndex) {
		return connectedEdges.get(pEdgeListIndex);
	}

	public RailTrackSegment getEdgeByUid(int pEdgeUid) {
		final int lEdgeCount = connectedEdges.size();
		for (int i = 0; i < lEdgeCount; i++) {
			if (connectedEdges.get(i) == null)
				continue;

			if (connectedEdges.get(i).uid == pEdgeUid) {
				return connectedEdges.get(i);
			}
		}

		return null;
	}

	public void removeEdgeByUid(int pEdgeUid) {
		EDGE_UPDATE_LIST.clear();
		final int lEdgeCount = connectedEdges.size();
		for (int i = 0; i < lEdgeCount; i++) {
			EDGE_UPDATE_LIST.add(connectedEdges.get(i));
		}

		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = EDGE_UPDATE_LIST.get(i);
			if (lEdge != null && lEdge.uid == pEdgeUid) {
				connectedEdges.remove(lEdge);
			}
		}

		if (connectedEdgeUids.contains((Integer) pEdgeUid)) {
			connectedEdgeUids.remove((Integer) pEdgeUid);
		}

	}

	public RailTrackSegment getRandomWhitelistedEdgeApartFrom(RailTrackSegment pCurrentEdge, int pDestinationNode) {
		if (connectedEdges == null || connectedEdges.size() == 0)
			return null;

		final var pEdgeUidWhiteList = pCurrentEdge.allowedEdgeConections;

		EDGE_UPDATE_LIST.clear();

		final int lEdgeCount = connectedEdges.size();
		for (int i = 0; i < lEdgeCount; i++) {
			if (connectedEdges.get(i) == null)
				continue;

			final int lUidToCheck = connectedEdges.get(i).uid;
			if (lUidToCheck != pCurrentEdge.uid && pEdgeUidWhiteList.contains(lUidToCheck)) {
				// Mark allowed edges, just not the one we just left
				EDGE_UPDATE_LIST.add(connectedEdges.get(i));
			}
		}

		// Now select a random, allowed edge
		if (EDGE_UPDATE_LIST.size() == 1)
			return EDGE_UPDATE_LIST.get(0);

		final int lUEdgeCount = EDGE_UPDATE_LIST.size();

		if (lUEdgeCount == 0) {
			return null;
		}

		final int lRandIndex = RandomNumbers.random(0, lUEdgeCount);

		return EDGE_UPDATE_LIST.get(lRandIndex);
	}
	
	public RailTrackSegment getRandomEdgeApartFrom(RailTrackSegment pCurrentEdge, int pDestinationNode) {
		if (connectedEdges == null || connectedEdges.size() == 0)
			return null;

		EDGE_UPDATE_LIST.clear();

		final int lEdgeCount = connectedEdges.size();
		for (int i = 0; i < lEdgeCount; i++) {
			if (connectedEdges.get(i) == null)
				continue;

			final int lUidToCheck = connectedEdges.get(i).uid;
			if (lUidToCheck != pCurrentEdge.uid) {
				EDGE_UPDATE_LIST.add(connectedEdges.get(i));
			}
		}

		// Now select a random, allowed edge
		if (EDGE_UPDATE_LIST.size() == 1)
			return EDGE_UPDATE_LIST.get(0);

		final int lUEdgeCount = EDGE_UPDATE_LIST.size();

		if (lUEdgeCount == 0) {
			return null;
		}

		final int lRandIndex = RandomNumbers.random(0, lUEdgeCount);

		return EDGE_UPDATE_LIST.get(lRandIndex);
	}

	public RailTrackSegment getRandomEdge() {
		if (connectedEdges == null || connectedEdges.size() == 0)
			return null;

		final int lEdgeCount = connectedEdges.size();
		final int lRandIndex = RandomNumbers.random(0, lEdgeCount);

		return connectedEdges.get(lRandIndex);

	}

	public boolean getIsEndNode() {
		return connectedEdges.size() == 1;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public RailTrackNode(final int pUid) {
		super(pUid);

		connectedEdges = new ArrayList<>();
		connectedEdgeUids = new ArrayList<>();
	}

	public RailTrackNode(final RailTrackNodeSaveDefinition saveDef) {
		this(saveDef.uid);

		this.x = saveDef.x;
		this.y = saveDef.y;

		this.connectedEdgeUids.addAll(saveDef.connectedEdgeUids);
	}

	// ---------------------------------------------
	// IO Methods
	// ---------------------------------------------

	public void loadFromDef(RailTrackNodeSaveDefinition saveDef) {
		uid = saveDef.uid;
		x = saveDef.x;
		y = saveDef.y;
		connectedEdgeUids.addAll(saveDef.connectedEdgeUids);
	}

	public void saveIntoDef(RailTrackNodeSaveDefinition saveDef) {
		saveDef.uid = uid;
		saveDef.x = x;
		saveDef.y = y;
		saveDef.connectedEdgeUids.addAll(connectedEdgeUids());
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void finalizeAfterLoading(RailTrackInstance railTrackInstance) {
		if (connectedEdges == null)
			connectedEdges = new ArrayList<>();

		final var lEdgeUidCount = connectedEdgeUids.size();
		for (int i = 0; i < lEdgeUidCount; i++) {
			final var lEdge = railTrackInstance.getEdgeByUid(connectedEdgeUids.get(i));
			if (lEdge != null) {
				if (!connectedEdges.contains(lEdge))
					connectedEdges.add(lEdge);
			} else {
				throw new RuntimeException("Error resolving track edges from Node.ConnectedEdgeUids");
			}
		}
	}

	public int getOtherEdgeConnectionUids(int pNotThisEdgeUid) {
		final int allowedEdgeCount = connectedEdgeUids.size();
		for (int i = 0; i < allowedEdgeCount; i++) {
			if (connectedEdgeUids.get(i) != pNotThisEdgeUid)
				return connectedEdgeUids.get(i);
		}

		return -1;

	}

	public int getOtherEdgeConnectionUids2(int pOurEdgeUid) {
		boolean lFoundOne = false;
		final int allowedEdgeCount = connectedEdgeUids.size();
		for (int i = 0; i < allowedEdgeCount; i++) {
			if (connectedEdgeUids.get(i) != pOurEdgeUid) {
				if (!lFoundOne)
					lFoundOne = true;
				else
					return connectedEdgeUids.get(i);
			}
		}

		return -1;

	}
}
