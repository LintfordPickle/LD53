package lintfordpickle.mailtrain.data.track;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.data.MapEntity;
import net.lintford.library.core.maths.RandomNumbers;

public class TrackNode extends MapEntity {
	// ---------------------------------------------
		// Constants
		// ---------------------------------------------

		private static final long serialVersionUID = -376552211463747406L;

		private static final List<TrackSegment> EDGE_UPDATE_LIST = new ArrayList<>();

		// ---------------------------------------------
		// Variables
		// ---------------------------------------------

		/**
		 * All edges connected to this node. Just because an edge is connected to a node, doesn't mean the edge can be traversed from that node.
		 */
		private transient List<TrackSegment> connectedEdges;
		private List<Integer> connectedEdgeUids;
		
		public boolean isSelected;

		// ---------------------------------------------
		// Properties
		// ---------------------------------------------

		public List<TrackSegment> connectedEdges() {
			return connectedEdges;
		}

		public int numberConnectedEdges() {
			return connectedEdges.size();
		}

		public void addEdgeToNode(TrackSegment pEdge) {
			if (!connectedEdges.contains(pEdge)) {
				connectedEdges.add(pEdge);
				connectedEdgeUids.add(pEdge.uid);

			}

		}

		public TrackSegment getEdgeByIndex(int pEdgeListIndex) {
			return connectedEdges.get(pEdgeListIndex);
		}

		public TrackSegment getEdgeByUid(int pEdgeUid) {
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

		public TrackSegment getRandomEdgeApartFrom(TrackSegment pCurrentEdge, int pDestinationNode) {
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
					// This edge is theoretically allowed ...
					EDGE_UPDATE_LIST.add(connectedEdges.get(i));

					// ... as long as its not the edge we just left

				}

			}

			if (EDGE_UPDATE_LIST.size() == 1)
				return EDGE_UPDATE_LIST.get(0);

			final int lUEdgeCount = EDGE_UPDATE_LIST.size();

			if (lUEdgeCount == 0) {
				return null;
			}

			final int lRandIndex = RandomNumbers.random(0, lUEdgeCount);

			return EDGE_UPDATE_LIST.get(lRandIndex);

		}

		public TrackSegment getRandomEdge() {
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

		public TrackNode(final int pUid) {
			super(pUid);

			connectedEdges = new ArrayList<>();
			connectedEdgeUids = new ArrayList<>();

		}

		// ---------------------------------------------
		// Constructor
		// ---------------------------------------------

		public void resolveEdges(Track pTrack) {
			if (connectedEdges == null)
				connectedEdges = new ArrayList<>();

			final int lEdgeUidCount = connectedEdgeUids.size();
			for (int i = 0; i < lEdgeUidCount; i++) {
				final var lEdge = pTrack.getEdgeByUid(connectedEdgeUids.get(i));
				if (lEdge != null && !connectedEdges.contains(lEdge)) {
					connectedEdges.add(lEdge);

				} else {
					throw new RuntimeException("Error loading track");
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
