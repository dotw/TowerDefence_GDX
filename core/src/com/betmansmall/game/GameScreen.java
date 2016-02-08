package com.betmansmall.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

	private static final float MAX_ZOOM = 1f; //normal size
	private static final float MIN_ZOOM = 0.5f; // 2x zoom

	private Vector2 dragOld, dragNew;

	private TiledMap _map;
	private IsometricTiledMapRenderer renderer;
	public OrthographicCamera cam;
	private ArrayList<Creep> creeps;
	private ArrayList<Tower> towers;

	private Map<String,TiledMapTile> waterTiles, towerTiles;
	private Map<Point, Integer> stepsForWaveAlgorithm;
	private TiledMapTileLayer _layer, _layerB;
	private Point exitPoint;
	private int currentFinishedCreeps, gameOverLimitCreeps;

	private TowerDefence towerDefence;
	private GameScreen gs;

	private int intervalForTimerCreeps = 1;
	private Timer.Task timerForCreeps;

	private final GameInterface gameInterface = new GameInterface();

	public GameScreen(final TowerDefence towerDefence) {
		this.gs = this;
		this.towerDefence = towerDefence;
		this.cam = new OrthographicCamera();
		this.cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		_map = new TmxMapLoader().load("img/arena.tmx");
		_layer = (TiledMapTileLayer) _map.getLayers().get("Foreground");
		_layerB = (TiledMapTileLayer) _map.getLayers().get("Background");

		InputMultiplexer im = new InputMultiplexer();
		GestureListener gestureListener = new GestureListener() {

			@Override
			public boolean touchDown(float x, float y, int pointer, int button) {
				Gdx.app.log("Call function", "touchDown(" + x + ", " + y + ", " + pointer+ ", " + button);
				dragNew = new Vector2(Gdx.input.getX(), Gdx.input.getY());

				dragOld = dragNew;
				return true; //workaround
			}

			@Override
			public boolean tap(float x, float y, int count, int button) {
				//stepAllCreeps();

				Vector3 touch = new Vector3(x, y, 0);
				Point clickedCell = new Point();
				cam.unproject(touch);
				for (int tileX = 0; tileX < _layer.getWidth(); tileX++){
					for(int tileY = 0; tileY < _layer.getHeight(); tileY++){
						float x_pos = (tileX * _layer.getTileWidth() / 2.0f ) + (tileY * _layer.getTileWidth() / 2.0f);
						float y_pos = - (tileX * _layer.getTileHeight() / 2.0f) + (tileY * _layer.getTileHeight() / 2.0f) + _layer.getTileHeight() / 2.0f;
						ArrayList<Vector2> tilePoints = new ArrayList<Vector2>();
						tilePoints.add(new Vector2(x_pos,y_pos));
						tilePoints.add(new Vector2(x_pos + _layer.getTileWidth() / 2.0f,
								y_pos + _layer.getTileHeight() / 2.0f));
						tilePoints.add(new Vector2(x_pos + _layer.getTileWidth(), y_pos));
						tilePoints.add(new Vector2(x_pos + _layer.getTileWidth() / 2.0f,
								y_pos - _layer.getTileHeight() / 2.0f));
						CollisionDetection cl = new CollisionDetection();
						if(cl.estimation(tilePoints, touch)) {
							Gdx.app.log("Click tile", "x=" + tileX + " y=" + tileY);
							clickedCell = new Point(tileX,tileY);
						}
					}
				}
				if(CollisionDetection.cellIsEmpty(clickedCell.x, clickedCell.y, _layer)) {
					towers.add(new Tower(_layer, towerTiles.get("2"), new Point(clickedCell.x, clickedCell.y)));
//					waveAlgorithm();
				}
				return false;
			}

			@Override
			public boolean longPress(float x, float y) {
				return false;
			}

			@Override
			public boolean fling(float velocityX, float velocityY, int button) {
				return false;
			}

			@Override
			public boolean pan(float x, float y, float deltaX, float deltaY) {
				moveCamera();
				return false;
			}

			@Override
			public boolean panStop(float x, float y, int pointer, int button) {

				return false;
			}

			@Override
			public boolean zoom(float initialDistance, float distance) {
				int amount = ((int)initialDistance - (int)distance) / (int)5f;
				Gdx.app.log("Zoom", "Amount: " + amount + ", distance: " + distance + ", inintD: " + initialDistance);
				if (amount > 0 && cam.zoom < MAX_ZOOM) cam.zoom += amount/10000f;
				if (amount < 0 && cam.zoom > MIN_ZOOM) cam.zoom += amount/10000f;
				cam.update();
				return false;
			}

			@Override
			public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
				return false;
			}
		};
		im.addProcessor(new GestureDetector(gestureListener));

		Gdx.input.setInputProcessor(im);
		creeps = new ArrayList<Creep>();
		towers = new ArrayList<Tower>();
	}

	public void waveAlgorithm() {
		waveAlgorithm(-1, -1);
	}

	public void waveAlgorithm(int x, int y) {
		Gdx.app.log("WaveAlgorim", "x=" + x + ",y=" + y);
		if(x == -1 && y == -1) {
			if (exitPoint != null) {
				waveAlgorithm(exitPoint.x, exitPoint.y);
				return;
			}
		}


		for(int tmpX = 0; tmpX < _layerB.getWidth(); tmpX++) {
			for(int tmpY = 0; tmpY < _layerB.getHeight(); tmpY++) {
				stepsForWaveAlgorithm.put(new Point(tmpX, tmpY), 0);
			}
		}

		stepsForWaveAlgorithm.put(new Point(x, y), 1);

		waveStep(x, y, 1);
	}

	void waveStep(int x, int y, int step) {
		//------------3*3----------------
		boolean mass[][] = new boolean[3][3];
		int nextStep = step + 1;

		for (int tmpY = -1; tmpY < 2; tmpY++)
			for (int tmpX = -1; tmpX < 2; tmpX++)
				mass[tmpX + 1][tmpY + 1] = setNumOfCell(x + tmpX, y + tmpY, nextStep);

		for (int tmpY = -1; tmpY < 2; tmpY++)
			for (int tmpX = -1; tmpX < 2; tmpX++)
				if (mass[tmpX + 1][tmpY + 1])
					waveStep(x + tmpX, y + tmpY, nextStep);

	}

	boolean setNumOfCell(int x, int y, int step) {
		if(x >= 0 && x < _layerB.getWidth()) {
			if(y >= 0 && y < _layerB.getHeight()) {
				if(CollisionDetection.cellIsEmpty(x, y, _layer)) {
					if(getStepCell(x, y) > step || getStepCell(x, y) == 0) {
						setStepCell(x, y, step);
						return true;
					}
				}
			}
		}
		return false;
	}

	int getStepCell(int x, int y) {
		return stepsForWaveAlgorithm.get(new Point(x, y));
	}

	void setStepCell(int x, int y, int step) {
		stepsForWaveAlgorithm.put(new Point(x, y), step);
	}

	int stepAllCreeps() {
		boolean allDead = true;
		for(int k = 0; k < creeps.size(); k++) {
			int result = stepOneCreep(k);
			if(result != -2)
				allDead = false;

			if(result == 1) {
				currentFinishedCreeps++;
				if(currentFinishedCreeps >= gameOverLimitCreeps)
					return 1;
			}
			else if(result == -1)
				return -1;
		}

		if(allDead)
			return 2;
		else
			return 0;
	}

	int stepOneCreep(int creepId) {
		Creep tmpCreep = creeps.get(creepId);
		if(tmpCreep.isAlive()) {
			int currX = tmpCreep.getPosition().x;
			int currY = tmpCreep.getPosition().y;

			int exitX = currX, exitY = currY;

			int min = getNumStep(currX,currY);

			if(min == 1)
				return 1;
			if(min == 0)
				return -1;

			int defaultStep = min;
			//--------------Looking specific cell-----------------------
			for(int tmpY = -1; tmpY < 2; tmpY++)
				for(int tmpX = -1; tmpX < 2; tmpX++)
					if(!(tmpX == 0 && tmpY == 0)) {
						int num = getNumStep(currX + tmpX, currY + tmpY);
//                            Log.d("TTW", "stepOneCreep() -- num: " + num);
						if(num <= min && num != 0) {
							if(num == min) {
								if( ((int) (Math.random()*2)) == 1) {
									exitX = currX + tmpX;
									exitY = currY + tmpY;
								}
							} else if(num == defaultStep-1) {
								exitX = currX + tmpX;
								exitY = currY + tmpY;
								min = num;
							}
						}
					}
			//-----------------------------------------------------------

			if(exitX != currX || exitY != currY)
			{
				Gdx.app.log("Creep", "move to: x=" + exitX + " y=" + exitY);
				creeps.get(creepId).moveTo(new Point(exitX, exitY));
			} else {
				return 0;
			}
//            }
		}
		return 0;
	}

	int getNumStep(int x, int y) {
		if(x >= 0 && x < _layerB.getWidth()) {
			if(y >= 0 && y < _layerB.getHeight()) {
				if(CollisionDetection.cellIsEmpty(x, y, _layer)) {
					return getStepCell(x, y);
				}
			}
		}
		return 0;
	}

	private void moveCamera() {
		dragNew = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		if (!dragNew.equals(dragOld) && dragOld != null)
		{
			cam.translate(dragOld.x - dragNew.x, dragNew.y - dragOld.y); //Translate by subtracting the vectors
			cam.update();
		}
		dragOld = dragNew; //Drag old becomes drag new.
	}

	@Override
	public void show() {
		renderer = new IsometricTiledMapRenderer(_map);
		showCreeps();
		stepsForWaveAlgorithm = new HashMap<Point, Integer>();
	}

	private void createTimerForCreeps(){
		if(timerForCreeps == null) {
			timerForCreeps = Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					Gdx.app.log("Timer", "for Creeps!");
					stepAllCreeps();
				}
			}, 0, intervalForTimerCreeps);
		}
	}

	private void stopTimerForCreeps() {
		timerForCreeps.cancel();
		timerForCreeps = null;
	}

	public void showCreeps() {
		//Create tile set
		TiledMapTileSet tileset =  _map.getTileSets().getTileSet("creep");
		waterTiles = new HashMap<String,TiledMapTile>();
		towerTiles = new HashMap<String,TiledMapTile>();

		//Search in tileset objects with property "creep" and put them in waterTiles
		for(TiledMapTile tile:tileset){
			Object property = tile.getProperties().get("creep");
			if(property != null) {
				waterTiles.put((String) property, tile);
			}
		}
		tileset =  _map.getTileSets().getTileSet("tower");
		for(TiledMapTile tile:tileset){
			Object property = tile.getProperties().get("tower");
			if(property != null) {
				towerTiles.put((String) property, tile);
			}
		}

		creeps.add(new Creep(_layer, waterTiles.get("1"), new Point(3,4)));

		for(int x = 0; x < _layer.getWidth();x++){
			for(int y = 0; y < _layer.getHeight();y++){
				TiledMapTileLayer.Cell cell = _layerB.getCell(x, y);
				if(cell.getTile().getProperties().get("spawn") != null && cell.getTile().getProperties().get("spawn").equals("1")) {
					creeps.add(new Creep(_layer, waterTiles.get("1"), new Point(x, y)));
				}
				if(cell.getTile().getProperties().get("exitPoint") != null && cell.getTile().getProperties().get("exitPoint").equals("1")) {
					exitPoint = new Point(x, y);
				}
			}
		}
	}

	private void inputHandler(float delta) {
		if(gameInterface.isTouched(GameInterface.GameInterfaceElements.START_WAVE_BUTTON)) {
			waveAlgorithm();
			createTimerForCreeps();
			gameInterface.setVisible(false,GameInterface.GameInterfaceElements.START_WAVE_BUTTON);
		}
		if(gameInterface.isTouched(GameInterface.GameInterfaceElements.RETURN_BUTTON)) {
			towerDefence.setMainMenu(null);
		}

		if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
			Gdx.app.log("inputHandler", "Pressed MINUS");

		} else if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
			Gdx.app.log("inputHandler", "Pressed PLUS");
		}
	}
	
	@Override
	public void render(float delta) {
		inputHandler(delta);

		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setView(cam);
		renderer.render();

		drawGrid();
//		drawStepsAndMouse();

		gameInterface.draw();
	}

	private void drawGrid() {
		ShapeRenderer sr = new ShapeRenderer();
		int tileWidth = _map.getProperties().get("tilewidth", Integer.class);
		int tileHeight = _map.getProperties().get("tileheight", Integer.class);
		int halfTileWidth = tileWidth/2;
		int halfTileHeight = tileHeight/2;

		int fieldX = _map.getProperties().get("width", Integer.class);
		int fieldY = _map.getProperties().get("height", Integer.class);
		int mapWidth = fieldX * tileWidth;
		int mapHeight = fieldY * tileHeight;

		sr.setProjectionMatrix(cam.combined);
		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.setColor(Color.BROWN); // (100, 60, 21, 1f);
		for(int x = 0; x <= fieldX; x++)
			sr.line(x*halfTileWidth, halfTileHeight - x*halfTileHeight, mapWidth/2 + x*halfTileWidth, halfTileHeight + mapHeight/2 - x*halfTileHeight);
		for(int y = 0; y <= fieldY; y++)
			sr.line(y*halfTileWidth, halfTileHeight + y*halfTileHeight, mapWidth/2 + y*halfTileWidth, halfTileHeight -(mapHeight/2) + y*halfTileHeight);
		sr.end();
	}

	private void drawStepsAndMouse() {
		if(!stepsForWaveAlgorithm.isEmpty()) {

//			Gdx.app.log("tag", stepsForWaveAlgorithm.toString());
//			return;
			SpriteBatch batch = new SpriteBatch();
			BitmapFont font = new BitmapFont();
			int tileWidth = _map.getProperties().get("tilewidth", Integer.class);
			int tileHeight = _map.getProperties().get("tileheight", Integer.class);
			int halfTileWidth = tileWidth / 2;
			int halfTileHeight = tileHeight / 2;

			int fieldX = _map.getProperties().get("width", Integer.class);
			int fieldY = _map.getProperties().get("height", Integer.class);
			int mapWidth = fieldX * tileWidth;
			int mapHeight = fieldY * tileHeight;

			int isometricCoorX = 0;
			int isometricCoorY = halfTileHeight;

			batch.begin();
			font.setColor(Color.BROWN); // (100, 60, 21, 1f);
			for (int y = 0; y <= fieldY; y++) {
				for (int x = 0; x <= fieldX; x++) {
					float x1 = isometricCoorX + x * (tileWidth / 2);
					float y1 = isometricCoorY + (tileHeight / 2) + x * (tileHeight / 2);
//				p.drawText(pxlsX + sizeCell/2-5, pxlsY + sizeCell/2+5, QString("%1").arg(field.getStepCell(x, y)));
					CharSequence str = (CharSequence) stepsForWaveAlgorithm.get(new Point(x, y)).toString();
					font.draw(batch, str, x1, y1);
//				sr.line(x * halfTileWidth, halfTileHeight - x * halfTileHeight, mapWidth / 2 + x * halfTileWidth, halfTileHeight + mapHeight / 2 - x * halfTileHeight);
//				sr.line(y*halfTileWidth, halfTileHeight + y*halfTileHeight, mapWidth/2 + y*halfTileWidth, halfTileHeight -(mapHeight/2) + y*halfTileHeight);
				}
				isometricCoorX = (tileWidth / 2) * (fieldY - (y + 1));
				isometricCoorY = (tileHeight / 4) * (y + 1);
			}
			batch.end();
		}
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportHeight = height;
		cam.viewportWidth = width;
//		cam.position.set(800f, 0f, 100f);
		cam.update();
		Gdx.app.log("Screen resize", "width "+ width + "height "+ height );
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		//	dispose();
	}

	@Override
	public void dispose() {
		_map.dispose();
		_map = null;
		renderer.dispose();
		renderer = null;
	}
}