# MinigamesBase
Base minigame plugin for my Minecraft Network.

## How does it work?

So how does this work? These are the main concepts
* **GameGroup**: An instance of a minigame with players in it.
* **Game**: This is basically the main interface that handles managing GameGroups and general interoperation between GameGroups and Bukkit.
* **User**: A player in a GameGroup (not to be confused with a **Player** who is someone on any Bukkit server)
* **Team**: A team of users in a GameGroup
* **Map**: A collection of Minecraft worlds that a GameGroup can have loaded. It may only load 1 Map at once
* **GameState**: A state of a GameGroup, e.g. *lobby*, *game* (for game in progress), *aftermath* (for game finished)

And how do these enable us to play a MiniGame? 
* Some servers (e.g. the hub) have a default game type (*hub* in this case), so when a player joins 
and there is no GameGroup of that type, it creates a new one for them
* For main game servers, there is a more complex system that resolves around the **Controller** telling
us what the player wants to play before they arrive (MinigamesBase functionality **in bold**)
  1. **A User in a hub uses a sign to join a minigame**
  2. **We send a request to the controller to put their player into that minigame**
  3. The controller finds a server for the request, and alerts it on what minigame the player wants to join/create [we handle this request here](src/main/java/com/ithinkrok/minigames/api/protocol/ClientMinigamesProtocol.java)
  4. **The receiving server sends a request to the controller to send that player over to them**
  5. The controller forwards this request to the server the player is on (the hub server)
  6. The player's server requests that BungeeCord move the player to the receiving server
  7. **When the player connects to the receiving server it puts them as a User in a GameGroup**
  8. **The User may now enjoy the minigame** (also depends on specific minigame plugins)
  
## How do you specify a Minigame?
* You need to create a config for it, which specifies the gamestates and the materials used by the minigame (e.g. custom items, kits, maps and custom listeners)
* You will also probably want a config for your map, enabling you to set details such as spawnpoint, the map environment, and any custom listeners for the map
* You will also probably want to create some CustomListeners, which can respond to events such as a UserJoined event to implement the minigame
