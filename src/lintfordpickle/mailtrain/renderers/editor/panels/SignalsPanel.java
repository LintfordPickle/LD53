package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.TrackEditorController;
import lintfordpickle.mailtrain.data.editor.EditorLayer;
import lintfordpickle.mailtrain.renderers.EditorTrackRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiEnumSelection;
import net.lintford.library.renderers.windows.components.UiHorizontalEntryGroup;
import net.lintford.library.renderers.windows.components.UiIndexedEnum;
import net.lintford.library.renderers.windows.components.UiIntSlider;
import net.lintford.library.renderers.windows.components.UiLabel;
import net.lintford.library.renderers.windows.components.UiLabelledInt;
import net.lintford.library.renderers.windows.components.UiLabelledString;
import net.lintford.library.renderers.windows.components.UifSlider;

public class SignalsPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final String TITLE = "Signals";

	private static final int BUTTON_CREATE_SIGNAL = 10;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TrackEditorController mTrackEditorController;

	private UiLabelledInt mSelectedSegmentLabel;
	private UiHorizontalEntryGroup mHorizonalSegment;
	private UiLabelledInt mNodeStartUid;
	private UiLabelledInt mNodeEndUid;

	private UiButton mToggleDirection;
	private UiEnumSelection mSelectedSignal;
	private UifSlider mDistance;

	private UiButton mCreateSignal;
	private UiButton mDeleteSignal;

	// Renderers
	private EditorTrackRenderer mEditorTrackRenderer;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SignalsPanel(UiWindow parentWindow, int entityGroupdUid) {
		super(parentWindow, TITLE, entityGroupdUid);

		mShowActiveLayerButton = true;
		mShowShowLayerButton = true;

		mRenderPanelTitle = true;
		mPanelTitle = TITLE;
		mEditorLayer = EditorLayer.Track;

		mSelectedSegmentLabel = new UiLabelledInt(parentWindow);
		mSelectedSegmentLabel.labelText("Segment: ");
		mSelectedSegmentLabel.value(-1);

		mHorizonalSegment = new UiHorizontalEntryGroup(parentWindow);

		mNodeStartUid = new UiLabelledInt(parentWindow);
		mNodeStartUid.labelText("s: ");
		mNodeStartUid.value(2);

		mNodeEndUid = new UiLabelledInt(parentWindow);
		mNodeEndUid.labelText("e: ");
		mNodeEndUid.value(3);

		mHorizonalSegment.widgets().add(mNodeStartUid);
		mHorizonalSegment.widgets().add(mNodeEndUid);

		mSelectedSignal = new UiEnumSelection(parentWindow);
		mSelectedSignal.addItem(new UiIndexedEnum(0, "0.33"));
		mSelectedSignal.addItem(new UiIndexedEnum(1, "0.66"));

		mToggleDirection = new UiButton(parentWindow, "Toggle Dir");
		mToggleDirection.setClickListener(this, BUTTON_CREATE_SIGNAL);

		mDistance = new UifSlider(parentWindow);
		mDistance.sliderLabel("dist");
		mDistance.setMinMax(0.f, 1.f);

		mCreateSignal = new UiButton(parentWindow, "Create");
		mDeleteSignal = new UiButton(parentWindow, "Delete");

		addWidget(mSelectedSegmentLabel);
		addWidget(mHorizonalSegment);
		addWidget(mDistance);
		addWidget(mToggleDirection);
		addWidget(mSelectedSignal);
		addWidget(mCreateSignal);
		addWidget(mDeleteSignal);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllerManager = core.controllerManager();
		mTrackEditorController = (TrackEditorController) lControllerManager.getControllerByNameRequired(TrackEditorController.CONTROLLER_NAME, mEntityGroupUid);

		final var lRendererManager = mParentWindow.rendererManager();
		mEditorTrackRenderer = (EditorTrackRenderer) lRendererManager.getRenderer(EditorTrackRenderer.RENDERER_NAME);
		mEditorTrackRenderer.drawEditorSignals(isLayerVisible());

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);
		
		
		
	}
	
	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	public void widgetOnDataChanged(InputManager inputManager, int entryUid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetOnClick(InputManager inputManager, int entryUid) {
		switch (entryUid) {

		}
	}

	@Override
	public int layerOwnerHashCode() {
		return mEditorTrackRenderer.hashCode();
	}

	// --------------------------------------
	// Inherited Methods (IInputProcessor)
	// --------------------------------------

	@Override
	public boolean allowKeyboardInput() {
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		return false;
	}
}
