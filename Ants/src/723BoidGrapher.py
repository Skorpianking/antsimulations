#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Aug 11 12:35:32 2017

@author: skorpianking
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
 


 
birddata = pd.read_csv("boid_testing.csv")
bird_names = pd.unique(birddata.bird_name) 
 
# storing the indices of the bird Eric
#ix = birddata.bird_name == "Eric"
#x,y = birddata.longitude[ix], birddata.latitude[ix]
#plt.figure(figsize = (7,7))
#plt.plot(x,y,"b.")
# 
#ix = birddata.bird_name == "Eunice"
#x,y = birddata.longitude[ix], birddata.latitude[ix]
#plt.figure(figsize = (7,7))
#plt.plot(x,y,"r.")

''' To look at all the birds trajectories,
    we plot each bird in the same plot '''
plt.figure(figsize = (7,7))
for bird_name in bird_names:
    # storing the indices of the bird Eric
    ix = birddata.bird_name == bird_name  
    x,y = birddata.longitude[ix], birddata.latitude[ix]
    plt.plot(x,y,".", label=bird_name)
    #plt.plot(x,y,"b-", label=bird_name)
plt.xlabel("Longitude")
plt.ylabel("Latitude")
plt.legend(loc="lower right")
plt.show()