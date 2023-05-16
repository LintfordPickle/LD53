package lintfordpickle.mailtrain.renderers.editor.panels;

import lintfordpickle.mailtrain.controllers.editor.EditorHashGridController;
import lintfordpickle.mailtrain.renderers.editor.EditorHashGridRenderer;
import lintfordpickle.mailtrain.renderers.editor.UiPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.InputManager;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.renderers.windows.components.UiButton;
import net.lintford.library.renderers.windows.components.UiIntSlider;

public class GridPanel extends UiPanel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int BUTTON_APPLY = 10;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private UiIntSlider mGridWidth;
	private UiIntSlider mGridHeight;
	private UiIntSlider mGridTilesWide;
	private UiIntSlider mGridTilesHigh;

	private UiButton mApplyButton;

	private EditorHashGridController mHashGridController;
	private EditorHashGridRenderer mEditorHashGridRenderer;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public int layerOwnerHashCode() {
		return mEditorHashGridRenderer.hashCode();
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GridPanel(UiWindow parentWindow, int entityGroupUid) {
		super(parentWindow, "Hash Grid", entityGroupUid);

		mPanelTitle = "Hash Grid";
		mRenderPanelTitle = true;
		mShowActiveLayerButton = false;

		mGridWidth = new UiIntSlider(parentWindow);
		mGridWidth.sliderLabel("Grid Width");
		mGridWidth.setMinMax(500, 10000);

		mGridHeight = new UiIntSlider(parentWindow);
		mGridHeight.sliderLabel("Grid Height");
		mGridHeight.setMinMax(500, 10000);

		mGridTilesWide = new UiIntSlider(parentWindow);
		mGridTilesWide.sliderLabel("Tiles Wide");
		mGridTilesWide.setMinMax(5, 100);

		mGridTilesHigh = new UiIntSlider(parentWindow);
		mGridTilesHigh.sliderLabel("Tiles High");
		mGridTilesHigh.setMinMax(5, 100);

		mApplyButton = new UiButton(parentWindow);
		mApplyButton.setClickListener(this, BUTTON_APPLY);
		mApplyButton.buttonLabel("Apply");

		addWidget(mGridWidth);
		addWidget(mGridHeight);
		addWidget(mGridTilesWide);
		addWidget(mGridTilesHigh);
		addWidget(mApplyButton);
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		final var lControllMnanager = core.controllerManager();
		mHashGridController = (EditorHashGridController) lControllMnanager.getControllerByNameRequired(EditorHashGridController.CONTROLLER_NAME, mEntityGroupUid);

		{
			final var lHashGrid = mHashGridController.hashGrid();

			mGridWidth.currentValue(lHashGrid.boundaryWidth());
			mGridHeight.currentValue(lHashGrid.boundaryHeight());

			mGridTilesWide.currentValue(lHashGrid.numTilesWide());
			mGridTilesHigh.currentValue(lHashGrid.numTilesHigh());
		}

		mEditorHashGridRenderer = (EditorHashGridRenderer) mParentWindow.rendererManager().getRenderer(EditorHashGridRenderer.RENDERER_NAME);
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);
	}

	@Override
	public void widgetOnClick(InputManager inputManager, int entryUid) {
		switch (entryUid) {
		case BUTTON_APPLY:
			updateHashGridSizes();
			break;

		case BUTTON_SHOW_LAYER:
			if (mEditorHashGridRenderer != null) {
				final var lCurentVisibility = mEditorHashGridRenderer.renderHashGrid();
				mEditorHashGridRenderer.renderHashGrid(!lCurentVisibility);
			}
			break;
		}
	}

	// --------------------------------------

	@Override
	protected void arrangeWidgets(LintfordCore core) {
		float lCurPositionX = mPanelArea.x() + mPaddingLeft;
		float lCurPositionY = mPanelArea.y() + mPaddingTop;

		float lWidgetHeight = 25.f;
		float lVSpacing = mVerticalSpacing;

		if (mRenderPanelTitle || mIsExpandable)
			lCurPositionY += getTitleBarHeight() + lVSpacing;

		mGridWidth.setPosition(lCurPositionX, lCurPositionY);
		mGridWidth.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mGridWidth.height(lWidgetHeight);
		mGridWidth.marginTop(10);

		lCurPositionY = increaseYPosition(lCurPositionY, mGridWidth, mGridHeight) + lVSpacing;

		mGridHeight.setPosition(lCurPositionX, lCurPositionY);
		mGridHeight.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mGridHeight.height(lWidgetHeight);
		mGridHeight.marginTop(10);

		lCurPositionY = increaseYPosition(lCurPositionY, mGridHeight, mGridTilesWide) + lVSpacing;

		mGridTilesWide.setPosition(lCurPositionX, lCurPositionY);
		mGridTilesWide.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mGridTilesWide.height(lWidgetHeight);
		mGridTilesWide.marginTop(10);

		lCurPositionY = increaseYPosition(lCurPositionY, mGridTilesWide, mGridTilesHigh) + lVSpacing;

		mGridTilesHigh.setPosition(lCurPositionX, lCurPositionY);
		mGridTilesHigh.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mGridTilesHigh.height(lWidgetHeight);
		mGridTilesHigh.marginTop(10);

		lCurPositionY = increaseYPosition(lCurPositionY, mGridTilesHigh, mApplyButton) + lVSpacing;

		mApplyButton.setPosition(lCurPositionX, lCurPositionY);
		mApplyButton.width(mPanelArea.width() - mPaddingLeft - mPaddingRight);
		mApplyButton.height(lWidgetHeight);

		lCurPositionY = increaseYPosition(lCurPositionY, mApplyButton, null) + lVSpacing;
	}

	private void updateHashGridSizes() {
		final var lNewWidth = mGridWidth.currentValue();
		final var lNewHeight = mGridHeight.currentValue();
		final var lNewNumTilesWide = mGridTilesWide.currentValue();
		final var lNewNumTilesHigh = mGridTilesHigh.currentValue();

		mHashGridController.resizeGrid(lNewWidth, lNewHeight, lNewNumTilesWide, lNewNumTilesHigh);
	}

	@Override
	public void widgetOnDataChanged(InputManager inputManager, int entryUid) {

	}

	// --------------------------------------
	
	@Override
	public boolean allowKeyboardInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		// TODO Auto-generated method stub
		return false;
	}
}