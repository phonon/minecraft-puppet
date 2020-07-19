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
{actor_commands}

## Engine commands
{engine_commands}