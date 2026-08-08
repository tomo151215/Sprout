package sample;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import engine.core.GameApplication;
import engine.core.GameSettings;
import engine.graphics.CameraBounds;
import engine.graphics.CameraFollow;
import engine.graphics.RendererConfig;
import engine.input.InputContext;

public class MyGame extends GameApplication {
        private static final int WORLD_WIDTH = 3000;
        private static final int WORLD_HEIGHT = 1000;

        private InputContext<Action> input;

        @Override
        protected GameSettings createSettings() {
                return GameSettings.builder()
                                .size(800, 600)
                                .title("Sprout Runner")
                                .build();
        }

        @Override
        protected RendererConfig createRendererConfig() {
                return RendererConfig.builder()
                                .backgroundColor(new Color(210, 235, 255))
                                .isAntiAliasing(false)
                                .isDebugRender(false)
                                .isInterpolation(true)
                                .build();
        }

        @Override
        protected void onInit() {
                input = new InputContext<>(
                                engine().getKeyboard(),
                                engine().getMouse(),
                                Action.class);

                input.addMapping(Action.LEFT, KeyEvent.VK_LEFT);
                input.addMapping(Action.LEFT, KeyEvent.VK_A);

                input.addMapping(Action.RIGHT, KeyEvent.VK_RIGHT);
                input.addMapping(Action.RIGHT, KeyEvent.VK_D);

                input.addMapping(Action.UP, KeyEvent.VK_UP);
                input.addMapping(Action.UP, KeyEvent.VK_W);

                input.addMapping(Action.DOWN, KeyEvent.VK_DOWN);
                input.addMapping(Action.DOWN, KeyEvent.VK_S);

                List<Block> blocks = new ArrayList<>();
                List<Coin> coins = new ArrayList<>();

                createStage(blocks, coins);

                Goal goal = new Goal(2850, 820, 80, 80);

                Player player = new Player(
                                100,
                                100,
                                input,
                                blocks,
                                coins,
                                goal);

                Chaser chaser = new Chaser(
                                700,
                                300,
                                player,
                                blocks);

                for (Block block : blocks) {
                        engine().addObject(block);
                }

                for (Coin coin : coins) {
                        engine().addObject(coin);
                }

                engine().addObject(goal);
                engine().addObject(player);
                engine().addObject(chaser);

                CameraFollow cameraFollow = new CameraFollow(
                                engine().getCamera(),
                                player,
                                engine().getSettings().getWidth(),
                                engine().getSettings().getHeight());

                cameraFollow.setSmooth(true);
                cameraFollow.setFollowSpeed(0.12);

                CameraBounds cameraBounds = new CameraBounds(
                                0,
                                0,
                                WORLD_WIDTH,
                                WORLD_HEIGHT);

                engine().addObject(new CameraFollowUpdater(
                                engine().getCamera(),
                                cameraFollow,
                                cameraBounds,
                                engine().getSettings().getWidth(),
                                engine().getSettings().getHeight()));

                engine().addObject(new StatusText(
                                engine().getCamera(),
                                player,
                                coins.size()));
        }

        private void createStage(List<Block> blocks, List<Coin> coins) {
                blocks.add(new Block(0, 950, WORLD_WIDTH, 50));

                blocks.add(new Block(300, 760, 300, 40));
                blocks.add(new Block(800, 650, 300, 40));
                blocks.add(new Block(1300, 780, 300, 40));
                blocks.add(new Block(1800, 600, 400, 40));
                blocks.add(new Block(2400, 760, 350, 40));

                blocks.add(new Block(500, 850, 80, 100));
                blocks.add(new Block(1150, 800, 80, 150));
                blocks.add(new Block(1650, 720, 80, 230));
                blocks.add(new Block(2250, 820, 80, 130));

                coins.add(new Coin(360, 700));
                coins.add(new Coin(900, 590));
                coins.add(new Coin(1400, 720));
                coins.add(new Coin(1950, 540));
                coins.add(new Coin(2520, 700));
        }
}