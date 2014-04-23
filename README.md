[WebSocketSpaceGame](http://bjr24.github.com/WebSocketSpaceGame/)
==================

<script>
  alert("hello world");
</script>

The client code is [here](https://github.com/bjr24/WebSocketSpaceGame/blob/master/public/javascripts/spacegame.js)

The server code for controlling the server side websocket is [here](https://github.com/bjr24/WebSocketSpaceGame/blob/master/app/controllers/GameWebSocket.java)

The server code for handling a game's event stream is [here](https://github.com/bjr24/WebSocketSpaceGame/blob/master/app/models/Game.java)

Installation
------------------
1. Install Java JDK 5.0 or later. ([download](http://www.oracle.com/technetwork/java/javase/downloads/index.html))
2. Download Play! `curl -o play.zip http://download.playframework.org/releases/play-1.2.5.zip`
3. Extract Play! `unzip play.zip`
4. Download WebSocketSpaceGame project. `curl -L -o SpaceGame.zip https://github.com/bjr24/WebSocketSpaceGame/archive/master.zip`
5. Extract project. `unzip SpaceGame.zip`
6. `cd WebSocketSpaceGame-master`
7. Run the application. `../play-1.2.5/play run`
8. Open a broswer and goto [http://localhost:9000/](http://localhost:9000/)

To run a game more than one player has to join the same game. This can be done by using different browsers, 
or the same browser in separate windows.

Controls
------------------
**z :** Rotate left

**x :** Rotate right

**, :** Move forward

**. :** Fire
