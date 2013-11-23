#!/bin/bash

export MPLAYER="/Applications/Video/MPlayer?OSX.app/Contents/Resources/External_Binaries/mplayer.app/Contents/MacOS/mplayer"

echo $1
while [ 1 -eq 1 ]; do
	$MPLAYER -fs -shuffle -fixed-vo $1/*.mov
done
