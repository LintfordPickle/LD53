package lintfordpickle.mailtrain.controllers.editor;

import lintfordpickle.mailtrain.data.editor.EditorLayer;

public class EditorBrush {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int NO_OWNER_HASH = -1;
	public static final int NO_ACTION_UID = -1;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private EditorLayer mEditorLayer = EditorLayer.Nothing;
	private int mEditorActionUid = NO_ACTION_UID;

	private int mOwnerHash;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorBrush() {
		mOwnerHash = NO_OWNER_HASH;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void clearLayer() {
		mEditorLayer = EditorLayer.Nothing;
		mEditorActionUid = NO_ACTION_UID;

		mOwnerHash = NO_OWNER_HASH;
	}

	public void clearAction() {
		mEditorActionUid = NO_ACTION_UID;
	}

	// Owner ---------------------------------------

	public boolean isOwnerSet() {
		return mOwnerHash != NO_OWNER_HASH;
	}

	public boolean isOwner(int ownerhash) {
		return mOwnerHash == ownerhash;
	}

	public boolean isOwnerOrNoOwner(int ownerhash) {
		return mOwnerHash == NO_OWNER_HASH || mOwnerHash == ownerhash;
	}

	// Layers --------------------------------------

	public EditorLayer brushLayer() {
		return mEditorLayer;
	}

	public void brushLayer(EditorLayer layer, int ownerHash) {
		if (isOwnerOrNoOwner(ownerHash) == false) {
			return; // not owner
		}

		if (layer == EditorLayer.Nothing) {
			clearLayer();
			return;
		}

		mOwnerHash = ownerHash;

		mEditorLayer = layer;
	}

	public boolean isBrushLayer(EditorLayer brushMode) {
		return mEditorLayer == brushMode;
	}

	// Actons --------------------------------------

	public boolean isActionSet() {
		return mEditorActionUid != NO_ACTION_UID;
	}

	public boolean isActionModeSet(int actionUid) {
		return mEditorActionUid == actionUid;
	}

	public int brushActionUid() {
		return mEditorActionUid;
	}

	public void brushActionUid(int actionUid) {
		mEditorActionUid = actionUid;
	}
}
