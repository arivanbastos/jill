# Java Indoor Location Library (JILL)
Jill is a java library that allows experimenation and development of indoor location systems. 
It supports many types of systems, for example systems based on WiFi, images or magnetic signals.

# Installation and use
Jill have two main parts: a **Java library** and an **Android library**.

The **Java library** have general classes that can be used either to desktop as mobile applications. The Java packages are described bellow:

1. fitting: classes that perform curve fitting , predicting the behavior of the location system reference signals.
2. outliers: classes that perform outliers removing from a set of signals.
3. location: classes that perform the location using signals captured by sensors of the devices to identify their position.
4. models: classes that represent the map where the location takes place. In this package are stored classes that represent the signals and signals datasets.
5. utils: utility classes.

The **Android library** have Android specific classes. The Android packages are described bellow: 

# An example usage
To give an overview of library operation will present an application example. Let's imagine that Bob wants to develop a indoor tracking system based on intensity map of the magnetic field. Bob needs:

1. A way to capture and create a fingerprinting map of the magnetic field intensity in the indoor enviroment (offline phase).
2. A way to capture the current intensity of the magnetic field and find the position in the fingerprinting map with the greatest similarity with the newly captured measure (online phase).
3. (Optionally) Bob wants to display the estimated position in a 2D map.
 
All these three needs can be met using the jill library. How? Lets see now.

For 1: A magnetic intensity signal is a double value. Bob can use the DoubleSignalSample from models.signal package in the java library to represent this data. In each map position, Bob can store many magnetic measures using the GeoMagneticSignalDataSet class from models.signal.datasets package. If Bob wanted to store another type of signal it could also create your own classes to represent theses values.
 


# Where does Jill come from?

# What is the current Jill status?
