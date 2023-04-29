package lintfordpickle.mailtrain.renderers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lintfordpickle.mailtrain.data.track.Track;
import lintfordpickle.mailtrain.data.track.TrackSegment;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.debug.stats.DebugStats;
import net.lintford.library.core.graphics.batching.TextureSlotBatch;
import net.lintford.library.core.graphics.shaders.ShaderMVP_PCT;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.input.mouse.IInputProcessor;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Matrix4f;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.splines.SplinePoint;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackMeshRenderer extends BaseRenderer implements IInputProcessor {

	public class TrackVertex {
		float x, y, u, v;
	}

	private static class TrackVertexDefinition {

		public static final int elementBytes = 4;

		public static final int positionElementCount = 4;
		public static final int colorElementCount = 4;
		public static final int textureElementCount = 2;

		public static final int elementCount = positionElementCount + colorElementCount + textureElementCount;

		public static final int positionBytesCount = positionElementCount * elementBytes;
		public static final int colorBytesCount = colorElementCount * elementBytes;
		public static final int textureBytesCount = textureElementCount * elementBytes;

		public static final int positionByteOffset = 0;
		public static final int colorByteOffset = positionByteOffset + positionBytesCount;
		public static final int textureByteOffset = colorByteOffset + colorBytesCount;

		public static final int stride = positionBytesCount + colorBytesCount + textureBytesCount;
	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	protected static final int MAX_SPRITES = 10000;
	protected static final int NUM_VERTICES_PER_SPRITE = 4;
	protected static final int NUM_INDICES_PER_SPRITE = 6;

	protected static final int MAX_VERTEX_COUNT = MAX_SPRITES * NUM_VERTICES_PER_SPRITE;
	protected static final int MAX_INDEX_COUNT = MAX_SPRITES * NUM_INDICES_PER_SPRITE;

	protected static final String VERT_FILENAME = "/res/shaders/shader_basic_pct.vert";
	protected static final String FRAG_FILENAME = "res/shaders/shaderTrack.frag";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected final TextureSlotBatch mTextureSlots = new TextureSlotBatch();

	protected ShaderMVP_PCT mShader;

	private static IntBuffer mTrackIndexBuffer;
	private FloatBuffer mTrackVertexBuffer;

	protected final ArrayList<Integer> mTrackIndexArray = new ArrayList<>();
	protected final ArrayList<TrackVertex> mTrackVertexArray = new ArrayList<>();

	protected Matrix4f mModelMatrix;
	protected int mVaoId = -1;
	protected int mVboId = -1;
	protected int mVioId = -1;
	protected int mVertexCount = 0;

	private boolean mAreGlContainersInitialized;
	private boolean _countDebugStats;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public void _countDebugStats(boolean enableCountStats) {
		_countDebugStats = enableCountStats;
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public TrackMeshRenderer(RendererManager pRendererManager, String pRendererName, int pEntityGroupID) {
		super(pRendererManager, pRendererName, pEntityGroupID);

		mShader = new ShaderMVP_PCT("TrackShader", VERT_FILENAME, FRAG_FILENAME);
		mModelMatrix = new Matrix4f();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mShader.loadResources(resourceManager);

		if (mVboId == -1) {
			mVboId = GL15.glGenBuffers();
			Debug.debugManager().logger().v(getClass().getSimpleName(), "[OpenGl] glGenBuffers: vbo " + mVboId);
		}

		if (mVioId == -1) {
			mVioId = GL15.glGenBuffers();
			Debug.debugManager().logger().v(getClass().getSimpleName(), "[OpenGl] glGenBuffers: vio " + mVioId);
		}

		mResourcesLoaded = true;

		if (resourceManager.isMainOpenGlThread())
			initializeGlContainers();

		Debug.debugManager().stats().incTag(DebugStats.TAG_ID_BATCH_OBJECTS);
	}

	/**
	 * OpenGl container objects (Array objects/framebuffers/program pipeline/transform feedback) are not shared between OpenGl contexts and must be created on the main thread.
	 */
	private void initializeGlContainers() {
		if (!mResourcesLoaded) {
			Debug.debugManager().logger().i(getClass().getSimpleName(), "Cannot create Gl containers until resources have been loaded");
			return;
		}

		if (mAreGlContainersInitialized)
			return;

		if (mVaoId == -1) {
			mVaoId = GL30.glGenVertexArrays();
			Debug.debugManager().logger().v(getClass().getSimpleName(), "[OpenGl] glGenVertexArrays: " + mVaoId);
		}

		GL30.glBindVertexArray(mVaoId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mVboId);

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, TrackVertexDefinition.positionElementCount, GL11.GL_FLOAT, false, TrackVertexDefinition.stride, TrackVertexDefinition.positionByteOffset);

		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, TrackVertexDefinition.colorElementCount, GL11.GL_FLOAT, false, TrackVertexDefinition.stride, TrackVertexDefinition.colorByteOffset);

		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, TrackVertexDefinition.textureElementCount, GL11.GL_FLOAT, false, TrackVertexDefinition.stride, TrackVertexDefinition.textureByteOffset);

		GL30.glBindVertexArray(0);
		mAreGlContainersInitialized = true;
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mShader.unloadResources();
		if (mVaoId > -1)
			GL30.glDeleteVertexArrays(mVaoId);

		if (mVboId > -1)
			GL15.glDeleteBuffers(mVboId);
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_U, this)) {
			mShader.recompile();
		}

		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		// todo. not sure whats going on here - seems like the shader doesn't accept information about screen size ??
//		final var lDesktopWidth = pCore.config().display().desktopWidth();
//		final var lDesktopHeight = pCore.config().display().desktopHeight();
//		mShader.screenResolutionWidth(lDesktopWidth);
//		mShader.screenResolutionHeight(lDesktopHeight);
//
//		final var lCamera = pCore.gameCamera();
//		mShader.cameraResolutionWidth(lCamera.getWidth());
//		mShader.cameraResolutionHeight(lCamera.getHeight());
//
//		mShader.pixelSize(1f);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	protected void drawMesh(LintfordCore pCore, Texture pTexture) {
		if (!mResourcesLoaded)
			return;

		if (mTrackIndexArray.size() == 0)
			return;

		if (!mAreGlContainersInitialized)
			initializeGlContainers();

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, pTexture.getTextureID());

		GL30.glBindVertexArray(mVaoId);

		mShader.projectionMatrix(pCore.gameCamera().projection());
		mShader.viewMatrix(pCore.gameCamera().view());
		mModelMatrix.setIdentity();
		mModelMatrix.translate(0, 0f, -.01f);
		mShader.modelMatrix(mModelMatrix);

		mShader.bind();

		if (_countDebugStats) {
			Debug.debugManager().stats().incTag(DebugStats.TAG_ID_DRAWCALLS);

			final int lNumQuads = mTrackIndexArray.size() / NUM_INDICES_PER_SPRITE;
			Debug.debugManager().stats().incTag(DebugStats.TAG_ID_VERTS, lNumQuads * 4);
			Debug.debugManager().stats().incTag(DebugStats.TAG_ID_TRIS, lNumQuads * 2);
		}

		GL11.glDrawElements(GL11.GL_TRIANGLES, mTrackIndexArray.size(), GL11.GL_UNSIGNED_INT, 0);

		mShader.unbind();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindVertexArray(0);

		mTextureSlots.clear();
	}

	public void loadTrackMesh(Track pTrack) {
		if (pTrack == null)
			return;

		mTrackVertexArray.clear();
		mTrackIndexArray.clear();

		final var lTrack = pTrack;
		final var lEdgeList = lTrack.edges();

		var lTempVector = new Vector2f();
		var tempDriveDirection = new SplinePoint();
		var tempSideDirection = new SplinePoint();

		final float lScaledSegWidth = 32.0f;

		int lCurrentIndex = 0;

		final var lEdgeCount = lEdgeList.size();
		for (int i = 0; i < lEdgeCount; i++) {
			final var lEdge = lEdgeList.get(i);

			float v = 0.f;

			var lNodeA = lTrack.getNodeByUid(lEdge.nodeAUid);
			var lNodeB = lTrack.getNodeByUid(lEdge.nodeBUid);
			if (lNodeA.x > lNodeB.x) {
				var temp = lNodeB;
				lNodeB = lNodeA;
				lNodeA = temp;
			}
			if (lNodeA.y > lNodeB.y) {
				var temp = lNodeB;
				lNodeB = lNodeA;
				lNodeA = temp;
			}
			final float lDist = Vector2f.dst(lNodeA.x, lNodeA.y, lNodeB.x, lNodeB.y);

			float lOldPointX = lNodeA.x;
			float lOldPointY = lNodeA.y;

			// Straight segment or curved
			if (lEdge.edgeType == TrackSegment.EDGE_TYPE_STRAIGHT) {
				tempDriveDirection.x = lNodeB.x - lOldPointX;
				tempDriveDirection.y = lNodeB.y - lOldPointY;

				tempSideDirection.x = tempDriveDirection.y;
				tempSideDirection.y = -tempDriveDirection.x;

				lTempVector.set(tempSideDirection.x, tempSideDirection.y);
				lTempVector.nor();

				final var lOuterPoint = new TrackVertex();
				final var lInnerPoint = new TrackVertex();

				lOuterPoint.x = (lNodeA.x + lTempVector.x * lScaledSegWidth / 2);
				lOuterPoint.y = (lNodeA.y + lTempVector.y * lScaledSegWidth / 2);
				lOuterPoint.u = 1.f;
				lOuterPoint.v = 0;

				lInnerPoint.x = (lNodeA.x - lTempVector.x * lScaledSegWidth / 2);
				lInnerPoint.y = (lNodeA.y - lTempVector.y * lScaledSegWidth / 2);
				lInnerPoint.u = 0.f;
				lInnerPoint.v = 0;

				mTrackVertexArray.add(lOuterPoint); // 0
				mTrackVertexArray.add(lInnerPoint); // 1

				final var lOuterPointE = new TrackVertex();
				final var lInnerPointE = new TrackVertex();

				lOuterPointE.x = (lNodeB.x + lTempVector.x * lScaledSegWidth / 2);
				lOuterPointE.y = (lNodeB.y + lTempVector.y * lScaledSegWidth / 2);
				lOuterPointE.u = 1.f;
				lOuterPointE.v = lDist / 32.0f;

				lInnerPointE.x = (lNodeB.x - lTempVector.x * lScaledSegWidth / 2);
				lInnerPointE.y = (lNodeB.y - lTempVector.y * lScaledSegWidth / 2);
				lInnerPointE.u = 0.f;
				lInnerPointE.v = lDist / 32.0f;

				mTrackVertexArray.add(lOuterPointE); // 2
				mTrackVertexArray.add(lInnerPointE); // 3

				mTrackIndexArray.add(lCurrentIndex + 0);
				mTrackIndexArray.add(lCurrentIndex + 1);
				mTrackIndexArray.add(lCurrentIndex + 2);

				mTrackIndexArray.add(lCurrentIndex + 1);
				mTrackIndexArray.add(lCurrentIndex + 3);
				mTrackIndexArray.add(lCurrentIndex + 2);

				lCurrentIndex += 4;

			} else {
				// S-Curve

				final float lStepSize = 0.01f;

				final float textureIncV = (lDist * 1.4f) * lStepSize * (1.f / 32.f);
				{ // add the first edge
					lOldPointX = lNodeA.x;
					lOldPointY = lNodeA.y;

					final float lNewPointX = MathHelper.bezier4CurveTo(lStepSize, lNodeA.x, lEdge.lControl0X, lEdge.lControl1X, lNodeB.x);
					final float lNewPointY = MathHelper.bezier4CurveTo(lStepSize, lNodeA.y, lEdge.lControl0Y, lEdge.lControl1Y, lNodeB.y);

					tempDriveDirection.x = lNewPointX - lOldPointX;
					tempDriveDirection.y = lNewPointY - lOldPointY;

					tempSideDirection.x = tempDriveDirection.y;
					tempSideDirection.y = -tempDriveDirection.x;

					lTempVector.set(tempSideDirection.x, tempSideDirection.y);
					lTempVector.nor();
					{
						final var lOuterPoint = new TrackVertex();
						final var lInnerPoint = new TrackVertex();

						lOuterPoint.x = (lOldPointX + lTempVector.x * lScaledSegWidth / 2);
						lOuterPoint.y = (lOldPointY + lTempVector.y * lScaledSegWidth / 2);
						lOuterPoint.u = 1.f;
						lOuterPoint.v = v;

						lInnerPoint.x = (lOldPointX - lTempVector.x * lScaledSegWidth / 2);
						lInnerPoint.y = (lOldPointY - lTempVector.y * lScaledSegWidth / 2);
						lInnerPoint.u = 0.f;
						lInnerPoint.v = v;

						mTrackVertexArray.add(lOuterPoint);
						mTrackVertexArray.add(lInnerPoint);

						v += textureIncV;

					}
					lOldPointX = lNewPointX;
					lOldPointY = lNewPointY;

				}
				for (float t = lStepSize * 2f; t <= 1f + lStepSize; t += lStepSize) {
					final float lNewPointX = MathHelper.bezier4CurveTo(t, lNodeA.x, lEdge.lControl0X, lEdge.lControl1X, lNodeB.x);
					final float lNewPointY = MathHelper.bezier4CurveTo(t, lNodeA.y, lEdge.lControl0Y, lEdge.lControl1Y, lNodeB.y);

					tempDriveDirection.x = lNewPointX - lOldPointX;
					tempDriveDirection.y = lNewPointY - lOldPointY;

					tempSideDirection.x = tempDriveDirection.y;
					tempSideDirection.y = -tempDriveDirection.x;

					lTempVector.set(tempSideDirection.x, tempSideDirection.y);
					lTempVector.nor();
					{
						final var lOuterPoint = new TrackVertex();
						final var lInnerPoint = new TrackVertex();

						lOuterPoint.x = (lNewPointX + lTempVector.x * lScaledSegWidth / 2);
						lOuterPoint.y = (lNewPointY + lTempVector.y * lScaledSegWidth / 2);
						lOuterPoint.u = 1.f;
						lOuterPoint.v = v;

						lInnerPoint.x = (lNewPointX - lTempVector.x * lScaledSegWidth / 2);
						lInnerPoint.y = (lNewPointY - lTempVector.y * lScaledSegWidth / 2);
						lInnerPoint.u = 0.f;
						lInnerPoint.v = v;

						mTrackVertexArray.add(lOuterPoint);
						mTrackVertexArray.add(lInnerPoint);

						mTrackIndexArray.add(lCurrentIndex + 0);
						mTrackIndexArray.add(lCurrentIndex + 1);
						mTrackIndexArray.add(lCurrentIndex + 2);

						mTrackIndexArray.add(lCurrentIndex + 1);
						mTrackIndexArray.add(lCurrentIndex + 3);
						mTrackIndexArray.add(lCurrentIndex + 2);

						lCurrentIndex += 2;

						float ldd = Vector2f.dst(lOldPointX, lOldPointY, lNewPointX, lNewPointY);

						v += ldd * (1.f / 32.f);

						lOldPointX = lNewPointX;
						lOldPointY = lNewPointY;

					}
					// get ready for next iteration
					lOldPointX = lNewPointX;
					lOldPointY = lNewPointY;
				}
				lCurrentIndex += 2;

			}
		}

		if (mTrackVertexArray.size() == 0)
			return;

		{
			final int lNumVertices = mTrackVertexArray.size();
			mTrackVertexBuffer = BufferUtils.createFloatBuffer(MAX_SPRITES * NUM_VERTICES_PER_SPRITE * TrackVertexDefinition.elementCount);

			for (int i = 0; i < lNumVertices; i++) {
				final var lTrackVertex = mTrackVertexArray.get(i);
				addVertToBuffer(lTrackVertex.x, lTrackVertex.y, -1, lTrackVertex.u, lTrackVertex.v);

			}

			mTrackVertexBuffer.flip();
		}

		{
			if (mTrackIndexArray.size() == 0)
				return;

			final int lNumIndices = mTrackIndexArray.size();
			mTrackIndexBuffer = BufferUtils.createIntBuffer(lNumIndices * 4);
			for (int i = 0; i < lNumIndices; i++) {
				final var lIndex = mTrackIndexArray.get(i);
				mTrackIndexBuffer.put(lIndex);

			}
			mTrackIndexBuffer.flip();
		}

		GL30.glBindVertexArray(mVaoId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mTrackVertexBuffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mVioId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mTrackIndexBuffer, GL15.GL_STATIC_DRAW);

		GL30.glBindVertexArray(0);

	}

	private void addVertToBuffer(float x, float y, float z, float u, float v) {
		mTrackVertexBuffer.put(x);
		mTrackVertexBuffer.put(y);
		mTrackVertexBuffer.put(z);
		mTrackVertexBuffer.put(1f);

		mTrackVertexBuffer.put(1f);
		mTrackVertexBuffer.put(1f);
		mTrackVertexBuffer.put(1f);
		mTrackVertexBuffer.put(1f);

		mTrackVertexBuffer.put(u);
		mTrackVertexBuffer.put(v);

		mVertexCount++;
	}

	// IInputProcessor

	@Override
	public boolean isCoolDownElapsed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetCoolDownTimer() {
		// TODO Auto-generated method stub

	}

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

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(LintfordCore core) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(LintfordCore core) {
		// TODO Auto-generated method stub

	}

}
