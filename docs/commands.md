Puppet Chat Commands
==============================
The Puppet plugin adds animatable objects called **actors** to
the world. The `/actor` command is used to create and manipulate
these objects. You can use this to move actors around, change
their pose, and play/pause animations.

The general format of most `/actor` commands is as follows:

    /actor subcommand ... [actor0] [actor1] [actor2] ...

The `[actor0] [actor1] [actor2] ...` are an optional list of actor
names to target the command on. If these are not used, the command
will instead run on the first actor you are looking at:

- `/actor rotate 0 20 0`: will rotate the actor you are looking at
- `/actor rotate 0 20 0 actor0 actor1`: will rotate actors named "actor0" and "actor1"

This command format makes it easy to run commands directly
on the actor in front of you, while flexible enough to
manipulate many actors at once.

## Actor commands
### `/actor help`
Print command list and descriptions

### `/actor info [actor0] [actor1] ...`
Print info about actors in input list or from what
player is looking at.

### `/actor create [type] [x] [y] [z]`
Create an actor with given type at player location,
or (x, y, z) if those are entered.

### `/actor mesh [type] [x] [y] [z]`
Create a single model "mesh" actor with given model
type at player location or (x, y, z) if those are entered.

### `/actor kill [actor0] [actor1] ...`
Remove actor. Will remove actors from list of names,
or the actor the player is looking at.

### `/actor killall`
Remove all actors currently in game.

### `/actor list`
Print list of actors currently in game and their location

### `/actor models`
print list of custom model data

### `/actor reset [actor0] [actor1] ...`
Reset actor rotation and all bone poses.

### `/actor move [x] [y] [z] [actor0] [actor1] ...`
Move actors by (x, y, z) relative to its current location.

### `/actor teleport [x] [y] [z] [actor0] [actor1] ...`
Move actor to (x, y, z) location in its world.

### `/actor rotate [x] [y] [z] [actor0] [actor1] ...`
Rotate actor by (x, y, z) relative to its current rotation.
Note: Minecraft rotation order is ZYX.

### `/actor pose [actor0] [actor1] ...`
Move and rotate actor using player movement. The actor
will move with player and face same direction as player.

### `/actor armorstands [show/hide] [actor0] [actor1] ...`
Show or hide ArmorStand entities used for animating
actor models. Used for debug.

### `/actor animlist`
Print list of all available animations

### `/actor animinfo [animation]`
Print information about an animation

### `/actor play [animation] [actor0] [actor1] ...`
Make actors play animation from name `[animation]`.

### `/actor stop [animation] [actor0] [actor1] ...`
Stop actor from playing animation from name `[animation]`.
Unlike pause, this removes a specific animation from the actor.

### `/actor stopall [actor0] [actor1] ...`
Stop all actor animations. Unlike pause, this removes
all animations from the actor.

### `/actor pause [actor0] [actor1] ...`
Pause actor from playing all animations. This will not
remove any animations, it will only stop animations
from updating.

### `/actor start [actor0] [actor1] ...`
Start (unpause) actor animations. Use this after `/actor pause`
to start animations again.

### `/actor step [actor0] [actor1] ...`
Run single animation update step for actors.
This works on paused actors, so you can view
animations frame-by-frame.

### `/actor restart [actor0] [actor1] ...`
Restart all animations from their initial frame.
Use this to sync animations across multiple actors.

### `/actor bone info [actor] [bone]`
Prints info (position, rotation, ...) about a bone in an
actor skeleton.

### `/actor bone rotate [actor] [bone] [x] [y] [z]`
Set rotation for actor named `[actor]` bone named `[bone]`
to euler angle input (x, y, z) in degrees. Euler rotation
order is ZYX.

### `/actor bone position [actor] [bone] [x] [y] [z]`
Set position for actor named `[actor]` bone named `[bone]`
to input (x, y, z).



## Engine commands
### `/puppet reload`
Reloads plugin and re-creates resource pack.
Will add any new resources to engine.

### `/puppet engine`
Start/stop/step animation render loop engine.

### `/puppet killall`
Kills all actors, same as `/actor killall`, but only requires
`/puppet` command permission node instead of operator.

