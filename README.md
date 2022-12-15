## pitneybowes

Two Apps that switch to each other. Both are set to capture sessions into my test org `PSYET`.

## branches

- main
  - basic app switching with FS integrated
  
- rc/keystore
  - same as above, but now each app stores and retrieves values from a keystore

- rc/sharedPrefs
  - same as above, but now each app stores and retrieves values from shared preferences
  
## running

1. sync gradle in each project
2. build each project to a device/emulator
3. open one of the apps and click through the buttons

![screen capture](/screencap.gif "screencap")
