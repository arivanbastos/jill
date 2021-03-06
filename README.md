# Java Indoor Location Library (JILL)
Jill is a java library that allows experimentation and development of indoor location systems. 
It supports many types of systems, for example systems based on WiFi, images or magnetic signals.

# Installation and use
Jill have two main parts: a **Java library** and an **Android library**.

The **Java library** is a Eclipse project (java folder) and have general classes that can be used either to desktop as mobile applications. You can reference it through Eclispe or build a jar file to use in other IDEs or Android projects. The Java packages are described bellow:

1. **fitting**: classes that perform curve fitting , predicting the behavior of the location system reference signals.
2. **outliers**: classes that perform outliers removing from a set of signals.
3. **location**: classes that perform the location using signals captured by sensors of the devices to identify their position.
4. **models**: classes that represent the map (where the location takes place), signals and signals datasets.
5. **utils**: utility classes.

The **Android library** have Android specific classes. You can use it to build Android applications. The Android folder contains also the JILL mobile app, which allows you:

1. Display your enviroment **2D map**.
2. Create **reference objects** and save them to map.
3. Select a **position**, **capture different types of signal** measures and **save** those measures in the map associated with the respective position.
3. See **charts** that display the captured signal data.
4. Run **locations algorithms** and see the result in the map.

The app currently can handle WiFi, BLE and magnetic field signals. But you can implement your own classes to handle other signals, like sound or image. A screenshot of the app can be viewed bellow:

![alt text](https://raw.githubusercontent.com/arivanbastos/jill/master/www/images/app-screenshot.jpg "Android App Screenshot")

The Android packages are described bellow:

1. **location**: classes that make the glue between device sensors and location algorithms present inside java library location package.
2. **sensors**: classes that access device sensors and store the data as models of java library models.signal package.

# An example usage
To give an overview of library operation we will present an application example. Let's imagine that Bob wants to develop a indoor tracking system based on intensity map of the magnetic field. Bob needs:

1. A way to capture and create a fingerprinting map of the magnetic field intensity in the indoor enviroment (offline phase).
2. A way to capture the current intensity of the magnetic field and find the position in the fingerprinting map with the greatest similarity with the newly captured measure (online phase).
3. (Optionally) Bob wants to display the estimated position in a 2D map.
 
All these three needs can be met using the jill library. How? Lets see now!

**Need 1**: A magnetic intensity signal is a double value. Bob can use the DoubleSignalSample from models.signal package in the java library to represent this data. In each map position, Bob can store many magnetic measures using the GeoMagneticSignalDataSet class from models.signal.datasets package. If Bob wanted to store another type of signal it could also create your own classes to represent theses values. The map class provides a way to store set of samples associated to points. For example, Bob can store 100 magnetic intensity measured in the position x:1, y:1 of the map. This is saved to a file and this data can be accessed easily, using something like map.getPoint(1,1).

So, how the enviroment signals will be gathered and stored in the map class? In this point takes action the Android library and Android App. Using it Bob can use an smartphone sensor or tablet sensor to capture and store magnetic intensity signal measures.

**Need 2**: Using the same principle for gather signals to build a fingerprinting map, Bob can capture signals in the online phase. The difference is that this time these signals do not need to be store; instead they will be sent to an location algorithm. The location algorithm will analize the data and estimate a position. To do it Bob can extend BaseLocationAlgorithm from the package location in the java library. This class has the run () method, which takes as its argument an array of sets of signals and returns an estimated position. Bob should extend BaseLocationMethod class in the Android app or use some already implemented location algorithms in the android library location package. This algorithms are available when running the app: you can choose and see them in action!
 
**Need 3**: The environment map can be represented by a simple JSON text and can be loaded using the models.serialize.MapSerializer class of the java library. A sample JSON map representation is presented bellow:

```javascript
{
 "color":"#F7EBB2",
 "length":15,
 "width":25,
 "scale":100.0",
 "rooms":[
  {
   "type":"rectangular",
   "id":"bedroom",
   "label":"Bedroom 1",
   "color":"#ff99ff",
   "bounds":{"bottom":4,"left":5,"right":9,"top":2}
  },
  ...
  ],
  "objects":[
   {"id":"00_07_80_20_01_B7","position":{"x":3,"y":3}},
   ...
  ],
  "points":[
   {
    "label":"10x5",
    "x":10,
    "y":5,
    "dataSets":[
    {
     "type":"geomagnetic",
     "signalSourceId":"earth-magnetic-field",
     "date":1435846025493,
     "samples":[
      22.7395250287941,...,23.0016242212
     ]
    },
    {
     "type":"wifi",
     "signalSourceId":"00_07_80_20_01_B7",
     "date":1435846025493,
     "samples":[
      -62.38756348,...,-60.276125111
     ]
    }
    ]
  }
 ]
}
```

# Where does Jill come from?
It is part of a master's thesis in computer science at the Federal University of Bahia. The original work title is "A Indoor Location System for Smartphones Based on WiFI and Bluetooth Low Energy", in portuguese "Um sistema de localização indoor para smartphones baseado em WiFi e Bluetooth Low Energy".

# What is the current Jill status?
The development is currently stopped, but you are free to download , change and improve the library if desired.
