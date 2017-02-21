package com.betmansmall.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.betmansmall.game.GameScreenInteface.DeviceSettings;
import com.betmansmall.game.GameScreenInteface.GameInterface;
import com.betmansmall.game.gameLogic.GameField;
import com.betmansmall.game.gameLogic.UnderConstruction;

public class GameScreen implements Screen {
    private static final float MAX_ZOOM = 50f; //max size
    private static final float MIN_ZOOM = 0.2f; // 2x zoom
    private float MAX_DESTINATION_X = 0f;
    private float MAX_DESTINATION_Y = 0f;
    private BitmapFont bitmapFont = new BitmapFont();
    private DeviceSettings deviceSettings = new DeviceSettings();

    private float currentDuration;
    private float MAX_DURATION_FOR_DEFEAT_SCREEN = 5f;

    private Texture defeatScreen;

    public static OrthographicCamera camera;

    private GameInterface gameInterface;
    private GameField gameField;

    class CameraController implements GestureListener {
        float velX, velY;
        boolean flinging = false; // Что бы не пересикалось одно действие с другим действием (с) Андрей А
        float initialScale = 2f;
        boolean lastCircleTouched = false;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            Gdx.app.log("CameraController::touchDown()", " -- x:" + x + " y:" + y + " pointer:" + pointer + " button:" + button);
            flinging = false;
            initialScale = camera.zoom;
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Gdx.app.log("CameraController::tap()", " -- x:" + x + " y:" + y + " count:" + count + " button:" + button);
//          CHECK IF THE PAUSE BUTTON IS TOUCHED //CHECK IF THE TOWER BUTTON IS TOUCHED
            if (gameInterface.getCreepsRoulette().isButtonTouched(x, y) || gameInterface.getTowersRoulette().isButtonTouched(x, y)) {
                return false;
            }

            Vector3 touch = new Vector3(x, y, 0);
            camera.unproject(touch);
            GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
            GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);

            if (cellCoordinate != null && gameField.getUnderConstruction() == null) {
                if (button == 0) {
                    gameField.towerActions(cellCoordinate.x, cellCoordinate.y);
                } else if (button == 1) {
                    gameField.removeTower(cellCoordinate.x, cellCoordinate.y);
//                  gameField.prepareBuildTower(cellCoordinate.x, cellCoordinate.y);
//              } else if(button == 2) {
//                  gameField.createCreep(cellCoordinate.x, cellCoordinate.y);
                } else if (button == 3) {
                    gameField.setSpawnPoint(cellCoordinate.x, cellCoordinate.y);
                } else if (button == 4) {
                    gameField.setExitPoint(cellCoordinate.x, cellCoordinate.y);
                }
            }
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            Gdx.app.log("CameraController::longPress()", " -- x:" + x + " y:" + y);
//          gameField.createSpawnTimerForCreeps();
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
//            Gdx.app.log("CameraController::fling()", " -- velocityX:" + velocityX + " velocityY:" + velocityY + " button:" + button);
            if (!lastCircleTouched) {
                flinging = true;
                velX = camera.zoom * velocityX * 0.5f;
                velY = camera.zoom * velocityY * 0.5f;
            }
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            Vector3 touch = new Vector3(x, y, 0);
            camera.unproject(touch);
//            Gdx.app.log("CameraController::pan()", " -- x:" + x + " y:" + y + " deltaX:" + deltaX + " deltaY:" + deltaY);
//            Gdx.app.log("CameraController::pan(1)", " -- x:" + camera.position.x + " y:" + camera.position.y);
//            Gdx.app.log("CameraController::pan(2)", " -- x:" + touch.x + " y:" + touch.y);
            if (gameInterface.getTowersRoulette().makeRotation(x, y, deltaX, deltaY) && deviceSettings.getDevice() == "android") {
                lastCircleTouched = true;
                return true;
            }
            lastCircleTouched = false;
            if (gameField.getUnderConstruction() == null || Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
//                if (camera.position.x + -deltaX * camera.zoom < MAX_DESTINATION_X && camera.position.x + -deltaX * camera.zoom > 0)
                    camera.position.add(-deltaX * camera.zoom, 0, 0);
//                if (Math.abs(camera.position.y + deltaY * camera.zoom) < MAX_DESTINATION_Y)
                    camera.position.add(0, deltaY * camera.zoom, 0);
            } else {
                int space = 50;
                if(x < space) {
                    camera.position.add(-5, 0, 0);
                }
                if(x > Gdx.graphics.getWidth()-space) {
                    camera.position.add(5, 0, 0);
                }
                if(y < space) {
                    camera.position.add(0, 5, 0);
                }
                if(y > Gdx.graphics.getHeight()-space) {
                    camera.position.add(0, -5, 0);
                }
//                if (camera.position.x + -deltaX * camera.zoom < MAX_DESTINATION_X && camera.position.x + -deltaX * camera.zoom > 0)
//                    camera.position.set(touch.x, touch.y, 0);
//                if (Math.abs(camera.position.y + deltaY * camera.zoom) < MAX_DESTINATION_Y)
//                    camera.position.add(0, deltaY * camera.zoom, 0);
            }
            GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
            GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);
            if (cellCoordinate != null) {
                if (gameField.getUnderConstruction() != null) {
                    gameField.getUnderConstruction().setEndCoors(cellCoordinate.x, cellCoordinate.y);
                }
            }
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
//            Gdx.app.log("CameraController::panStop()", " -- x:" + x + " y:" + y + " pointer:" + pointer + " button:" + button);
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            Gdx.app.log("CameraController::zoom()", " -- initialDistance:" + initialDistance + " distance:" + distance);
            float ratio = initialDistance / distance;
            float newZoom = initialScale * ratio;
            if (newZoom < MAX_ZOOM && newZoom > MIN_ZOOM) {
                camera.zoom = newZoom;
            }
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            Gdx.app.log("CameraController::pinch()", " -- initialPointer1:" + initialPointer1 + " initialPointer2:" + initialPointer2 + " pointer1:" + pointer1 + " pointer2:" + pointer2);
            return false;
        }

        public void update() {
            try {
                if (gameField.getUnderConstruction() == null) {
                    if (flinging) {
                        velX *= 0.98f;
                        velY *= 0.98f;
                        if (camera.position.x + -velX * Gdx.graphics.getDeltaTime() > 0 && camera.position.x + -velX * Gdx.graphics.getDeltaTime() < MAX_DESTINATION_X)
                            camera.position.add(-velX * Gdx.graphics.getDeltaTime(), 0, 0);
                        if (Math.abs(camera.position.y + velY * Gdx.graphics.getDeltaTime()) < MAX_DESTINATION_Y)
                            camera.position.add(0, velY * Gdx.graphics.getDeltaTime(), 0);
                        if (Math.abs(velX) < 0.01f) velX = 0;
                        if (Math.abs(velY) < 0.01f) velY = 0;
                    }
                }
            } catch (Exception exp) {
                int a;
            }
        }
    }

    class MyGestureDetector extends GestureDetector {
        public MyGestureDetector(GestureListener listener) {
            super(listener);
        }

        @Override
        public boolean keyDown(int keycode) {
//            Gdx.app.log("MyGestureDetector::keyDown()", " -- keycode:" + keycode);
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
//            Gdx.app.log("MyGestureDetector::keyUp()", " -- keycode:" + keycode);
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
//            Gdx.app.log("MyGestureDetector::keyTyped()", " -- character:" + character);
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Gdx.app.log("MyGestureDetector::touchDown()", " -- screenX:" + screenX + " screenY:" + screenY + " pointer:" + pointer + " button:" + button);
            Vector3 touch = new Vector3(screenX, screenY, 0);
            camera.unproject(touch);
            GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
            GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);
            if (cellCoordinate != null) {
                UnderConstruction underConstruction = gameField.getUnderConstruction();
                if (underConstruction != null) {
                    if(button == 0) {
                        underConstruction.setStartCoors(cellCoordinate.x, cellCoordinate.y);
                    } else if(button == 1){
                        gameField.removeTower(cellCoordinate.x, cellCoordinate.y);
                    }
                }
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            Gdx.app.log("MyGestureDetector::touchUp()", " -- screenX:" + screenX + " screenY:" + screenY + " pointer:" + pointer + " button:" + button);
            if(gameField != null && button == 0) {
                Vector3 touch = new Vector3(screenX, screenY, 0);
                camera.unproject(touch);
                GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
                GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);
                if (cellCoordinate != null) {
                    gameField.buildTowersWithUnderConstruction(cellCoordinate.x, cellCoordinate.y);
                }
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
//            Gdx.app.log("MyGestureDetector::touchDragged()", " -- screenX:" + screenX + " screenY:" + screenY + " pointer:" + pointer);
//            Vector3 touch = new Vector3(screenX, screenY, 0);
//            camera.unproject(touch);
//            GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
//            GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);
//            if (cellCoordinate != null) {
//                if (gameField.getUnderConstruction() != null) {
//                    gameField.getUnderConstruction().setEndCoors(cellCoordinate.x, cellCoordinate.y);
//                }
//            }
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
//          Gdx.app.log("MyGestureDetector::mouseMoved()", " -- screenX:" + screenX + " screenY:" + screenY);
            Vector3 touch = new Vector3(screenX, screenY, 0);
            camera.unproject(touch);
            GridPoint2 grafCoordinate = new GridPoint2((int) touch.x, (int) touch.y);
            GridPoint2 cellCoordinate = gameField.whichCell(grafCoordinate);
            if (cellCoordinate != null) {
                if (gameField.getUnderConstruction() != null) {
                    gameField.getUnderConstruction().setEndCoors(cellCoordinate.x, cellCoordinate.y);
                }
            }
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            Gdx.app.log("MyGestureDetector::scrolled()", " -- amount:" + amount);
            if (amount == 1) {
                if (camera.zoom <= MAX_ZOOM)
                    camera.zoom += 0.1f;
            } else if (amount == -1) {
                if (camera.zoom >= MIN_ZOOM)
                    camera.zoom -= 0.1f;
            }
            camera.update();
            return false;
        }
    }

    private CameraController cameraController = new CameraController();
    private MyGestureDetector myGestureDetector = new MyGestureDetector(cameraController);

    public GameScreen(String mapName) {
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        gameField = new GameField(mapName);       //"maps/arena3.tmx"
        gameInterface = new GameInterface(gameField);

        InputMultiplexer inputMultiplexer = gameInterface.setCommonInputHandler(new GestureDetector(cameraController));
        inputMultiplexer.addProcessor(myGestureDetector);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.app.log("tag", "cel " + gameField.getSizeCellX() + " field" + gameField.getSizeFieldX());
        Gdx.app.log("tag", "cel " + gameField.getSizeCellY() + " field" + gameField.getSizeFieldY());
        MAX_DESTINATION_X = gameField.getSizeCellX() * gameField.getSizeFieldX();
        MAX_DESTINATION_Y = gameField.getSizeCellY() * gameField.getSizeFieldY() / 2f;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen::show()", " Called!");
        //Start position of camera
//        camera.position.add((gameField.getSizeFieldX() * gameField.getSizeCellX()) / 2, 0, 0);
        camera.position.set(0f, 0f, 0f);
    }

    private void inputHandler(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            if (camera.zoom <= MAX_ZOOM)
                camera.zoom += 0.1f;
            camera.update();
            Gdx.app.log("GameScreen::inputHandler()", "-- Pressed MINUS");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            if (camera.zoom >= MIN_ZOOM)
                camera.zoom -= 0.1f;
            camera.update();
            Gdx.app.log("GameScreen::inputHandler()", "-- Pressed PLUS");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0)) {
//          gameField.setGamePause(!gameField.getGamePaused());
            gameInterface.getCreepsRoulette().buttonClick();
            Gdx.app.log("GameScreen::inputHandler()", "-- Pressed NUM_0");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            gameField.isDrawableGrid++;
            if(gameField.isDrawableGrid > 5) {
                gameField.isDrawableGrid = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.isDrawableGrid:" + gameField.isDrawableGrid);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            GameField.isDrawableCreeps++;
            if(GameField.isDrawableCreeps > 5) {
                GameField.isDrawableCreeps = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- GameField.isDrawableCreeps:" + GameField.isDrawableCreeps);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            gameField.isDrawableTowers++;
            if(gameField.isDrawableTowers > 5) {
                gameField.isDrawableTowers = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.isDrawableTowers:" + gameField.isDrawableTowers);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            gameField.isDrawableGridNav++;
            if(gameField.isDrawableGridNav > 5) {
                gameField.isDrawableGridNav = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.isDrawableGridNav:" + gameField.isDrawableGridNav);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            gameField.isDrawableBackground++;
            if(gameField.isDrawableBackground > 5) {
                gameField.isDrawableBackground = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.isDrawableBackground:" + gameField.isDrawableBackground);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            gameField.isDrawableForeground++;
            if(gameField.isDrawableForeground > 5) {
                gameField.isDrawableForeground = 0;
            }
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.isDrawableForeground:" + gameField.isDrawableForeground);
//        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- isKeyJustPressed(Input.Keys.BACK || Input.Keys.BACKSPACE);");
            TowerDefence.getInstance().removeTopScreen();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_8)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- isKeyJustPressed(Input.Keys.NUM_8 || Input.Keys.NUMPAD_8); -- gameField.gameSpeed:" + gameField.gameSpeed);
            gameField.gameSpeed -= 0.1f;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_9)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- isKeyJustPressed(Input.Keys.NUM_9 || Input.Keys.NUMPAD_9); -- gameField.gameSpeed:" + gameField.gameSpeed);
            gameField.gameSpeed += 0.1f;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.turnLeft();");
            gameField.turnLeft();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.turnRight();");
            gameField.turnRight();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.flipX();");
            gameField.flipX();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            Gdx.app.log("GameScreen::inputHandler()", "-- gameField.flipY();");
            gameField.flipY();
        }
    }

    @Override
    public void render(float delta) {
//      Gdx.app.log("GameScreen::render()", "-- delta:" + delta);
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        String gameState = gameField.getGameState();
        if (gameState.equals("In progress")) {
            inputHandler(delta);
            cameraController.update();
            camera.update();
            gameField.render(delta, camera);
            gameInterface.act(delta);
            gameInterface.draw();
            gameInterface.getInterfaceStage().getBatch().begin();
            bitmapFont.getData().setScale(4);
            bitmapFont.setColor(Color.YELLOW);
            bitmapFont.draw(gameInterface.getInterfaceStage().getBatch(), String.valueOf("Gold amount: "
                                + gameField.getGamerGold()),
                                Gdx.graphics.getWidth() / 2 - 150,
                                Gdx.graphics.getHeight() - 10);
            gameInterface.getInterfaceStage().getBatch().end();
        } else if (gameState.equals("Lose")) {
            currentDuration += delta;
            if (currentDuration > MAX_DURATION_FOR_DEFEAT_SCREEN) {
                //this.dispose();
                TowerDefence.getInstance().nextGameLevel();
                return;
            }
            if (defeatScreen == null)
                defeatScreen = new Texture(Gdx.files.internal("img/defeat.jpg"));
            gameInterface.getInterfaceStage().getBatch().begin();
            gameInterface.getInterfaceStage().getBatch().draw(defeatScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gameInterface.getInterfaceStage().getBatch().end();
        } else if (gameState.equals("Win")) {
            currentDuration += delta;
            if (currentDuration > MAX_DURATION_FOR_DEFEAT_SCREEN) {
                //this.dispose();
                TowerDefence.getInstance().nextGameLevel();
                return;
            }
            if (defeatScreen == null)
                defeatScreen = new Texture(Gdx.files.internal("img/victory.jpg"));
            gameInterface.getInterfaceStage().getBatch().begin();
            gameInterface.getInterfaceStage().getBatch().draw(defeatScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gameInterface.getInterfaceStage().getBatch().end();
        } else {
            Gdx.app.log("Something goes wrong", "123");
        }
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GameScreen::resize()", "-- New width:" + width + " height:" + height);
        camera.viewportHeight = height;
        camera.viewportWidth = width;
        camera.update();
        //gameInterface.getInterfaceStage().getViewport().update(width, height);
        //gameInterface.getInterfaceStage().getCamera().viewportHeight = height;
        //gameInterface.getInterfaceStage().getCamera().viewportWidth = width;
        //gameInterface.getInterfaceStage().getCamera().update();
    }

    @Override
    public void pause() {
        Gdx.app.log("GameScreen::pause()", " Called!");
    }

    @Override
    public void resume() {
        Gdx.app.log("GameScreen::resume()", " Called!");
    }

    @Override
    public void hide() {
        Gdx.app.log("GameScreen::hide()", " Called!");
    }

    @Override
    public void dispose() { // TODO Нужно очищать всю память!
        Gdx.app.log("GameScreen::dispose()", " Called!");
        gameField = null;
        gameInterface = null;
    }
}
