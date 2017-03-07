package com.betmansmall.game.GameScreenInteface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.betmansmall.game.gameLogic.GameField;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.touchable;

/**
 * Created by Transet on 07.02.2016.
 * This class provides elements which placed on game screen.
 * TODO implement more interface options
 */
public class GameInterface {

    public enum GameInterfaceElements {
        TOWERS_ROULETTE,
        CREEPS_ROULETTE
    }

    public TowersRoulette getTowersRoulette() {
        return towersRoulette;
    }

    public void setTowersRoulette(TowersRoulette towersRoulette) {
        this.towersRoulette = towersRoulette;
    }

    public CreepsRoulette getCreepsRoulette() {
        return creepsRoulette;
    }

    public Stage getInterfaceStage(){
        return stage;
    }

    public void setCreepsRoulette(CreepsRoulette creepsRoulette) {
        this.creepsRoulette = creepsRoulette;
    }
    private BitmapFont bitmapFont = new BitmapFont();
    private TowersRoulette towersRoulette;
    private CreepsRoulette creepsRoulette;
    private Stage stage;
    private GameField gameField;

    public GameInterface(GameField gameField) {
        this.gameField = gameField;
        init();
    }

    private void init() {
        stage = new Stage();
        towersRoulette = new TowersRoulette(gameField);
        creepsRoulette = new CreepsRoulette(gameField);

        for(Actor actor : creepsRoulette.getGroup()) {
            stage.addActor(actor);
        }
        try {
            for (Actor actor : towersRoulette.getGroup()) {
                stage.addActor(actor);
            }
        } catch(Error error){ Gdx.app.log("Error:", "no circle1 group");}

    }

    public InputMultiplexer setCommonInputHandler(InputProcessor inputProcessor) {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputProcessor);
        inputMultiplexer.addProcessor(stage);
        return inputMultiplexer;
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
//        bitmapFont.setColor(Color.WHITE);
//        bitmapFont.draw(getInterfaceStage().getBatch(),String.valueOf(" "),1,2);
    }
    public  void stageUpdate() {
        init();
    }

}
