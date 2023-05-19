package lintfordpickle.mailtrain.controllers.editor;

import lintfordpickle.mailtrain.controllers.editor.interfaces.IBrushModeCallback;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.partitioning.GridEntity;
import net.lintford.library.core.geometry.partitioning.SpatialHashGrid;

public class EditorBrushController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Editor Brush Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private IBrushModeCallback mIBrushModeCallback;
	private String mDoingWhatMessage;
	private EditorBrush mEditorBrush;

	private EditorHashGridController mHashGridController;

	private float mCursorWorldX;
	private float mCursorWorldY;

	private boolean mShowCursorPosition;
	private boolean mShowCursorGridUid;
	private boolean mShowHeight;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public SpatialHashGrid<GridEntity> hashGrid() {
		return mHashGridController.hashGrid();
	}

	public boolean showPosition() {
		return mShowCursorPosition;
	}

	public void showPosition(boolean newValue) {
		mShowCursorPosition = newValue;
	}

	public boolean showGridUid() {
		return mShowCursorGridUid;
	}

	public void showGridUid(boolean newValue) {
		mShowCursorGridUid = newValue;
	}

	public boolean showHeight() {
		return mShowHeight;
	}

	public void showHeight(boolean newValue) {
		mShowHeight = newValue;
	}

	public void setCursor(float worldX, float worldY) {
		mCursorWorldX = worldX;
		mCursorWorldY = worldY;
	}

	public float cursorWorldX() {
		return mCursorWorldX;
	}

	public float cursorWorldY() {
		return mCursorWorldY;
	}

	public EditorBrush brush() {
		return mEditorBrush;
	}

	public String doingWhatMessage() {
		return mDoingWhatMessage;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorBrushController(ControllerManager controllerManager, EditorBrush editorBrush, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mEditorBrush = editorBrush;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		mHashGridController = (EditorHashGridController) mControllerManager.getControllerByNameRequired(EditorHashGridController.CONTROLLER_NAME, mEntityGroupUid);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void clearAction(int ownerHash) {
		if (mEditorBrush.isOwner(ownerHash) == false)
			return; // don't own layer

		if (mEditorBrush.isActionSet() == false)
			return; // no action set

		if (mIBrushModeCallback != null)
			mIBrushModeCallback.onLayerDeselected();

		mDoingWhatMessage = null;

		mEditorBrush.clearAction();
	}

	public void clearLayerMode() {
		if (mIBrushModeCallback != null)
			mIBrushModeCallback.onLayerDeselected();

		mIBrushModeCallback = null;
		mDoingWhatMessage = null;

		mEditorBrush.clearLayer();
	}

	public void clearActiveLayer(int ownerHash) {
		mEditorBrush.clearLayer();

		if (mIBrushModeCallback != null)
			mIBrushModeCallback.onLayerDeselected();

		mIBrushModeCallback = null;
	}

	public void setActiveLayer(IBrushModeCallback callback, EditorLayer newLayer, int ownerHash) {
		clearActiveLayer(ownerHash);
		if (newLayer == EditorLayer.Nothing || newLayer == null) {
			return;
		}

		if (mEditorBrush.isBrushLayer(newLayer))
			return; // already set

		if (mEditorBrush.isBrushLayer(EditorLayer.Nothing) == false)
			return; // something else is set

		mEditorBrush.brushLayer(newLayer, ownerHash);

		mIBrushModeCallback = callback;

		if (mIBrushModeCallback != null)
			mIBrushModeCallback.onLayerSelected();
	}

	public boolean isLayerActive(EditorLayer brushMode) {
		return mEditorBrush.isBrushLayer(brushMode);
	}

	public boolean setAction(int actionUid, String actionString, int ownerHash) {
		if (mEditorBrush.isOwner(ownerHash) == false)
			return false; // don't own layer

		if (mEditorBrush.isActionSet() && mEditorBrush.brushActionUid() != actionUid)
			return false; // already doing something

		mDoingWhatMessage = actionString;

		mEditorBrush.brushActionUid(actionUid);

		return true;
	}

	public void finishAction(int ownerHash) {
		if (mEditorBrush.isOwner(ownerHash) == false)
			return; // don't own layer

		mDoingWhatMessage = null;

		mEditorBrush.brushActionUid(EditorBrush.NO_ACTION_UID);
	}
}
