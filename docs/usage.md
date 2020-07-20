Puppet Usage
========================

Blender Minecraft Exporter
------------------------
**Blender Minecraft .json exporter:** https://github.com/phonon/blender-minecraft-json

Be sure to read the installation and export guides in the addon readme.
Custom models used with this plugin follow all the same 
restrictions as vanilla .json models.


Creating and Loading Models into Puppet
------------------------------------------
1. Create model in Blender (follow all Minecraft .json restrictions).
2. Create an armature (skeleton) and assign all objects in the
model to the bone they should be exported with.
3. Export using the exporter with **Export bones** and
**Export animations** settings selected. This will export model into
several chunks named `model.bone0.json`, `model.bone1.json`, ...
alongside a `model.data.json` file containing bone structure and
animations.
4. Put the Puppet plugin `.jar` into your Minecraft server
`plugins/` folder.
5. Put each custom model and textures into a separate directory in the
`plugins/puppet/data/` folder. E.g. if you have a model named `monster`,
but all its `monster.bone0.json`, `monster.bone1.json`, ... and
`monster.data.json` into `plugins/puppet/data/monster/`. Put all textures
for the model in this folder as well.
6. When the plugin runs, it will automatically generate a resource pack
needed for clients to view the custom models. This will be built into
the folder `plugins/puppet/resourcepack/puppet_resourcepack.zip`.
