<?xml version="1.0" encoding="UTF-8"?>
<tileset name="tower1_scout" tilewidth="64" tileheight="64">
 <properties>
  <property name="attackSpeed" value="0.2"/>
  <property name="damage" value="10"/>
  <property name="factionName" value="Faction1"/>
  <property name="name" value="Scout"/>
  <property name="radius" value="1"/>
  <property name="size" value="1"/>
  <property name="type" value="tower"/>
 </properties>
 <image source="../textures/warcraft2/tilesets/winter/human/buildings/scout_tower.png" trans="ff00ff" width="128" height="192"/>
 <terraintypes>
  <terrain name="idleTile" tile="0"/>
  <terrain name="arrow_up" tile="1"/>
  <terrain name="arrow_up_right" tile="2"/>
  <terrain name="arrow_right" tile="3"/>
  <terrain name="arrow_down_right" tile="4"/>
  <terrain name="arrow_down" tile="5"/>
 </terraintypes>
 <tile id="0">
  <properties>
   <property name="tileName" value="idleTile"/>
  </properties>
 </tile>
 <tile id="1">
  <properties>
   <property name="tileName" value="arrow_up"/>
  </properties>
 </tile>
 <tile id="2">
  <properties>
   <property name="tileName" value="arrow_up_right"/>
  </properties>
 </tile>
 <tile id="3">
  <properties>
   <property name="tileName" value="arrow_right"/>
  </properties>
 </tile>
 <tile id="4">
  <properties>
   <property name="tileName" value="arrow_down_right"/>
  </properties>
 </tile>
 <tile id="5">
  <properties>
   <property name="tileName" value="arrow_down"/>
  </properties>
 </tile>
</tileset>
