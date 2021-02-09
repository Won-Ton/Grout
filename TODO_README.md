# Grout
Bringing structure mods & datapacks together!

## About
This is an experimental utility mod that adds some compatibility improvements to Minecraft's datapack loading system.

### World-gen Mods:
Mods can add content that gets used in world-generation. There are two ways they can tell the game about their content
and how to use it:
1. Code - Content can be added dynamically through Minecraft/Forge's APIs, performing logic and inter-mod compatibility
   steps to add all their stuff without the end-user having to worry about setting it up.
2. DataPacks - Mod jars _are_ DataPacks, so can contain configurations of their content for Minecraft to load and
   incorporate into world-gen. (To be clear, this still requires code for the content, it just doesn't use code to
   tell Minecraft _about_ the content or perform compatibility measures etc).

### So What's The Problem?
DataPacks are static configurations. Barring a few exceptions (Biomes being one) then whatever is defined in a DataPack
is _all_ that's used in the world. When you have two mods that, for example, both try to tell Minecraft to use their
set of custom structures, only one 'wins' and gets used - that is whichever DataPack is ordered on top in the
world-creation DataPacks menu.

> _Why would mods add their content via DataPacks if it's inherently incompatible with other mods?_

Because it's easier than trying to understand Minecraft's code, mostly...

### So What Can We Do?
As the end-user, you can _DataPack_ your way out! You just need to know every bit of content that vanilla and each mod
you want to use adds, how it should be configured, where it should be placed in your datapack, what it should be called,
what values are valid/safe to use...

> ... _"just?"_... -_-

Oh, yeah...

**Introducing Grout**. This mod provides an alternative solution to the above by dynamically merging DataPack content
(from _all_ zip-file/folder/mod-jar DataPacks) with _all_ the content added by mods that use the more compatible 'code'
approach described above.
