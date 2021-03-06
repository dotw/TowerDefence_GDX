<?xml version="1.0" encoding="UTF-8"?>
<tileset name="towerFire1" tilewidth="64" tileheight="64" tilecount="6" columns="2">
 <properties>
  <property name="factionName" value="Faction1"/>
  <property name="name" value="Fire Tower 1"/>
  <property name="radiusDetection" value="150"/>
  <property name="radiusFlyShell" value="300"/>
  <property name="damage" value="17"/>
  <property name="size" value="2"/>
  <property name="cost" value="25"/>
  <property name="ammoSize" value="10"/>
  <property name="ammoSpeed" value="15"/>
  <property name="reloadTime" value="1.5"/>
  <property name="towerAttackType" value="Range"/>
  <property name="shellAttackType" value="FirstTarget"/>
  <property name="shellEffectType" value="FireEffect"/>
  <property name="shellEffectType_time" value="5"/>
  <property name="shellEffectType_damage" value="0.8"/>
  <!-- <property name="shellEffectType_speed" value="2"/> -->
 </properties>
 <image source="../../textures/warcraft2/tilesets/winter/human/buildings/scout_tower_fire.png" trans="ff00ff" width="128" height="192"/>
 <terraintypes>
  <terrain name="idleTile" tile="0"/>
  <terrain name="ammo_UP" tile="1"/>
  <terrain name="ammo_UP_RIGHT" tile="2"/>
  <terrain name="ammo_RIGHT" tile="3"/>
  <terrain name="ammo_DOWN_RIGHT" tile="4"/>
  <terrain name="ammo_DOWN" tile="5"/>
 </terraintypes>
 <tile id="0">
  <properties>
   <property name="tileName" value="idleTile"/>
  </properties>
 </tile>
 <tile id="1">
  <properties>
   <property name="tileName" value="ammo_UP"/>
  </properties>
 </tile>
 <tile id="2">
  <properties>
   <property name="tileName" value="ammo_UP_RIGHT"/>
  </properties>
 </tile>
 <tile id="3">
  <properties>
   <property name="tileName" value="ammo_RIGHT"/>
  </properties>
 </tile>
 <tile id="4">
  <properties>
   <property name="tileName" value="ammo_DOWN_RIGHT"/>
  </properties>
 </tile>
 <tile id="5">
  <properties>
   <property name="tileName" value="ammo_DOWN"/>
  </properties>
 </tile>
</tileset>
