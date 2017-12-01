#!bin/bash


echo debugalias | jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass debugkeystore -keystore debugkeystore bin/FBReaderJ-release-unsigned.apk debugfbreader

