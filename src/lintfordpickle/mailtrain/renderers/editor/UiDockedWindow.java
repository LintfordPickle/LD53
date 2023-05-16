package lintfordpickle.mailtrain.renderers.editor;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.mailtrain.renderers.editor.panels.FileInfoPanel;
import lintfordpickle.mailtrain.renderers.editor.panels.GridPanel;
import lintfordpickle.mailtrain.renderers.editor.panels.JunctionsPanel;
import lintfordpickle.mailtrain.renderers.editor.panels.NodePanel;
import lintfordpickle.mailtrain.renderers.editor.panels.SegmentPanel;
import lintfordpickle.mailtrain.renderers.editor.panels.SignalsPanel;
import lintfordpickle.mailtrain.renderers.editor.panels.TrackInfoPanel;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UiWindow;

public class UiDockedWindow extends UiWindow {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int DOCKED_WINDOW_WIDTH = 320;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private final List<UiPanel> mEditorPanels = new ArrayList<>();

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<UiPanel> editorPanels() {
		return mEditorPanels;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UiDockedWindow(RendererManager rendererManager, String rendererName, int entityGroupUid) {
		super(rendererManager, rendererName, entityGroupUid);

		mIsWindowMoveable = false;
		mScrollBar.autoHide(false);

		isOpen(true);

		createGuiPanels();
	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		initializeGuiPanels(core);

		super.initialize(core);
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		loadPanelResources(resourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		unloadPanelResources();
	}

	@Override
	public boolean handleInput(LintfordCore core) {
		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.handleInput(core);
		}

		final var lMouseX = core.HUD().getMouseWorldSpaceX();
		final var lMouseY = core.HUD().getMouseWorldSpaceY();

		boolean editorResult = super.handleInput(core);

		if (mWindowArea.intersectsAA(lMouseX, lMouseY)) {
			if (core.input().mouse().tryAcquireMouseOverThisComponent(hashCode())) {
				// prevent futher renderers from using clicks over the editor window
			}
		}

		return editorResult;
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		arrangePanels(core);

		mContentDisplayArea.set(mWindowArea);

		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.update(core);
		}
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.draw(core);
		}
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void createGuiPanels() {
		mEditorPanels.add(new FileInfoPanel(this, mEntityGroupUid));
		mEditorPanels.add(new TrackInfoPanel(this, mEntityGroupUid));
		mEditorPanels.add(new GridPanel(this, mEntityGroupUid));
		mEditorPanels.add(new NodePanel(this, mEntityGroupUid));
		mEditorPanels.add(new SegmentPanel(this, mEntityGroupUid));
		mEditorPanels.add(new SignalsPanel(this, mEntityGroupUid));
		mEditorPanels.add(new JunctionsPanel(this, mEntityGroupUid));
	}

	private void arrangePanels(LintfordCore core) {
		float currentPositionX = mWindowArea.x() + 5.f;
		float lTitlebarHeight = mRenderWindowTitle ? mTitleBarHeight : 0.f;
		float currentPositionY = mScrollBar.currentYPos() + mWindowArea.y() + lTitlebarHeight + 5.f;

		float panelWidth = mWindowArea.width() - 5.f * 2.f - mScrollBar.width();
		float lTotalContentHeight = lTitlebarHeight;

		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.mPanelArea.setPosition(currentPositionX, currentPositionY);
			lPanel.mPanelArea.width(panelWidth);

			final float lPanelHeight = lPanel.getPanelFullHeight();
			lPanel.mPanelArea.height(lPanelHeight);
			lTotalContentHeight += lPanelHeight + 15.f;

			currentPositionY += lPanel.getPanelFullHeight() + 15.f;
		}

		mFullContentRectangle.height(lTotalContentHeight);
	}

	@Override
	public void updateWindowPosition(LintfordCore core) {
		super.updateWindowPosition(core);

		final var lHudBounds = core.HUD().boundingRectangle();

		final float lUiScaleW = 1.f;// mUiStructureController.uiCanv

		final float GuiWidth = DOCKED_WINDOW_WIDTH * lUiScaleW;

		mWindowArea.x(lHudBounds.right() - GuiWidth);
		mWindowArea.y(lHudBounds.top());

		mWindowArea.width(GuiWidth);
		mWindowArea.height(lHudBounds.height());
	}

	private void initializeGuiPanels(LintfordCore core) {
		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.initialize(core);
		}
	}

	private void loadPanelResources(ResourceManager resourceManager) {
		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.loadResources(resourceManager);
		}
	}

	private void unloadPanelResources() {
		final int lNumPanels = mEditorPanels.size();
		for (int i = 0; i < lNumPanels; i++) {
			final var lPanel = mEditorPanels.get(i);

			lPanel.unloadResources();
		}
	}

}
