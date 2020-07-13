"""
Tool for packing models into Minecraft item variants
using custom_model_value metadata

Outputs in /build:
- tmp/ directory stores built resource pack before zip 
- zipped resource pack: puppet_resource_pack.zip
- server/ directory stores server-side files needed by plugin
"""

import os
import shutil
import json

# resource pack format
PACK_FORMAT = 5

# get paths to template structure and source file directory
path_src = os.path.dirname(os.path.abspath(__file__))

# input files
path_template = os.path.join(path_src, 'template')
path_mcmeta = os.path.join(path_template, 'pack.mcmeta')
path_icon = os.path.join(path_template, 'pack.png')
path_models = os.path.join(path_src, 'models')

# output paths
path_out_temp = os.path.join(path_src, 'build', 'tmp')
path_out_mcmeta = os.path.join(path_out_temp, 'pack.mcmeta')
path_out_icon = os.path.join(path_out_temp, 'pack.png')
path_out_item_override = os.path.join(path_out_temp, 'assets', 'minecraft', 'models', 'item', 'bone.json')
path_out_models = os.path.join(path_out_temp, 'assets', 'minecraft', 'models', 'item')
path_out_textures = os.path.join(path_out_temp, 'assets', 'minecraft', 'textures', 'item')
path_out_server = os.path.join(path_src, 'build', 'server')
path_out_server_library = os.path.join(path_src, 'build', 'server', 'models.json')
path_out_server_data_dir = os.path.join(path_src, 'build', 'server', 'data')
path_out_zip = os.path.join(path_src, 'build', 'puppet_resource_pack')

# override item (default using a Bone object)
item_override = {
    "parent": "item/handheld",
    "textures": {
        "layer0": "item/bone"
    },
    "display": {},
    "overrides": []
}

# clean temp output directory
if os.path.exists(path_out_temp):
    shutil.rmtree(path_out_temp)
if os.path.exists(path_out_server):
    shutil.rmtree(path_out_server)
os.makedirs(path_out_temp, exist_ok=True)
os.makedirs(path_out_server_data_dir, exist_ok=True)

# ===================================
# write resourcepack metadata files
# ===================================
with open(path_mcmeta, 'r') as file_in, open(path_out_mcmeta, 'w+') as file_out:
    for line in file_in:
        if "{PACK_FORMAT}" in line:
            line = line.replace("{PACK_FORMAT}", str(PACK_FORMAT))
        file_out.write(line)

# copy icon
shutil.copyfile(path_icon, path_out_icon)

# ===================================
# write models
# ===================================

# list of all model part json file names (without json extension)
# names in list will be used to modify the target diamond_hoe.json
all_model_parts = []

for model_name in os.listdir(path_models):
    path_model = os.path.join(path_models, model_name)

    # skip files
    if not os.path.isdir(path_model):
        continue
    
    # search for .data.json file
    for fname in os.listdir(path_model):
        fname_parts = fname.split('.')
        if len(fname_parts) > 2 and fname_parts[-1] == 'json' and fname_parts[-2] == 'data':
            
            # copy data file to server directory
            path_model_data = os.path.join(path_model, fname)
            path_out = os.path.join(path_out_server_data_dir, fname)
            shutil.copyfile(path_model_data, path_out)

    # 2nd pass, copy files to resource pack
    for fname in os.listdir(path_model):
        root, ext = os.path.splitext(fname)
        if ext == '.json' and '.data' not in root:
            path_model_part = os.path.join(path_model, fname)
            path_out = os.path.join(path_out_models, fname)
            os.makedirs(os.path.dirname(path_out), exist_ok=True)
            shutil.copyfile(path_model_part, path_out)

            all_model_parts.append(root)
        elif ext == '.png' or ext == '.jpeg' or ext == '.jpg':
            path_texture = os.path.join(path_model, fname)
            path_out = os.path.join(path_out_textures, fname)
            os.makedirs(os.path.dirname(path_out), exist_ok=True)
            shutil.copyfile(path_texture, path_out)

print(all_model_parts)

# map model part to entry in item override .json
for i, model_part in enumerate(all_model_parts):
    item_override["overrides"].append({
        "predicate": {
            "custom_model_data": (i+1)
        },
        "model": f"item/{model_part}"
    })

with open(path_out_item_override, 'w+') as file_out:
    json.dump(item_override, file_out)

# =========================================
# create resource pack zip
# =========================================
shutil.make_archive(path_out_zip, 'zip', path_out_temp)
print("Built resourcepack:", path_out_zip + ".zip")

# =========================================
# server-side data
# =========================================
# write server model library json file with the array of all items
server_model_library = {
    "models": all_model_parts
}

with open(path_out_server_library, 'w+') as file_out:
    json.dump(server_model_library, file_out)
