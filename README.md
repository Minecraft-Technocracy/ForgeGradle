ForgeGradle
===========

[![Join the chat at https://gitter.im/MinecraftForge/ForgeGradle](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/MinecraftForge/ForgeGradle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Minecraft mod development framework used by Forge and FML for the gradle build system

This branch has been hotfixed to address
[an issue regarding OpenJDK](https://github.com/MinecraftForge/ForgeGradle/issues/652)
to allow usage of ForgeGradle on Linux from MC 1.12 without migrating to ForgeGradle 3.0 (which would include backporting FG 3.0 to MC 1.12 first)

Furthermore, publishing and license-enforcement within the buildscript have been disabled. This branch is therefore technically incompatible to the original repository and should not be pull-requested. (Moreover, FG-2.3 has reached end-of-life and thus isn't supported anyway)
